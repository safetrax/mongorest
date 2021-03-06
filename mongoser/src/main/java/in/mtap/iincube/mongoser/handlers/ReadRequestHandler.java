/*
 * Copyright 2015 mtap technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package in.mtap.iincube.mongoser.handlers;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import in.mtap.iincube.mongoapi.DocumentClient;
import in.mtap.iincube.mongoapi.MongoReader;
import in.mtap.iincube.mongoser.MongoserException;
import in.mtap.iincube.mongoser.codec.JsonEncoder;
import in.mtap.iincube.mongoser.codec.Result;
import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.model.Status;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.MISSING_DB_COL_PARAMS;
import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.PARSE_ERROR;
import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.hasValidParams;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class ReadRequestHandler {
  static final String INVALID_LIMIT_SKIP = "skip or limit param is invalid";
  static final String[] invalidKeys = new String[]{"$where", "mapReduce", "$accumulator", "$function"};
  static final List<String> validKeys = Arrays.stream(QueryOperators.class.getDeclaredFields()).map(e -> {
    try {
      return ((String)e.get(null));
    } catch (IllegalAccessException illegalAccessException) {
      illegalAccessException.printStackTrace();
    }
    return "$or";
  }).collect(Collectors.toList());

  private final DocumentClient documentClient;
  private final RequestInterceptor interceptor;
  private final JsonEncoder jsonEncoder = new JsonEncoder();

  public ReadRequestHandler(DocumentClient documentClient) {
    this(documentClient, RequestInterceptor.ALLOW_ALL);
  }

  public ReadRequestHandler(DocumentClient documentClient, RequestInterceptor interceptor) {
    this.documentClient = documentClient;
    this.interceptor = interceptor;
  }

  /**
   * Doesn't maintains any state, hence ensures thread safety
   */
  public void process(RequestReader requestReader, Response responseWriter)
      throws IOException {
    if (!hasValidParams(requestReader)) {
      responseWriter.send(SC_BAD_REQUEST,
          Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
      return;
    }

    if (!interceptor.isReadAllowed(requestReader.getDbName(), requestReader.getDbName())) {
      responseWriter.send(SC_BAD_REQUEST,
          Status.get("Not allowed to read this namespace").toJsonTree());
      return;
    }
    if (requestReader.hasParam("distinct")) {
      processDistinctRequest(requestReader, responseWriter);
    } else {
      processFindRequest(requestReader, responseWriter);
    }
  }

  private boolean isInValidQuery(DBObject query) {
    for (String key : query.keySet()) {
      if (Arrays.stream(invalidKeys).anyMatch(key::equalsIgnoreCase))
        return true;
      if (key.length() == 0)
        return true;
      if (key.charAt(0) == '$')
        if (validKeys.parallelStream().noneMatch(key::equalsIgnoreCase))
          return true;
      if (query.get(key) instanceof DBObject && isInValidQuery((DBObject) query.get(key)))
        return true;
    }
    return false;
  }

  private void processFindRequest(RequestReader requestReader, Response responseWriter)
      throws IOException {
    int skip = -1;
    int limit = -1;
    try {
      skip = requestReader.getParameterAsInt("skip");
      limit = requestReader.getParameterAsInt("limit");
    } catch (NumberFormatException e) {
      responseWriter.send(SC_BAD_REQUEST, Status.get(INVALID_LIMIT_SKIP).toJsonTree());
      return;
    }

    Result<List<DBObject>> resultData = requestReader.readResultDbObject();
    if (!resultData.isValid()) {
      responseWriter.send(SC_BAD_REQUEST, Status.get(PARSE_ERROR).toJsonTree());
      return;
    }

    MongoReader mongoReader = documentClient.read(requestReader.getDbName(),
        requestReader.getCollectionName());
    if (skip > 0)
      mongoReader.skip(skip);
    if (limit > 0) {
      mongoReader.limit(Math.min(limit, 10000));
    } else {
      mongoReader.limit(10000);
    }

    if (requestReader.hasParam("fields")) {
      String[] fields = requestReader.getUrlParameter("fields").split("[,]");
      mongoReader.select(fields);
    }

    List<DBObject> queryData = resultData.getData();
    if (isInValidQuery(queryData.get(0)))
      responseWriter.send(SC_BAD_REQUEST,
              Status.get("Invalid Query").toJsonTree());
    mongoReader.find(queryData.get(0));

    // set sort if available
    if (queryData.size() > 1)
      mongoReader.sort(queryData.get(1));
    try {
      responseWriter.send(SC_OK, mongoReader.execute(jsonEncoder));
    } catch (IllegalArgumentException e) {
      throw new MongoserException(400,
          Status.get(" Error message: " + e.getMessage()
              + " Invalid request " + resultData.getPlainBody()));
    }
  }

  private void processDistinctRequest(RequestReader requestReader, Response responseWriter)
      throws  IOException {
    String distinctKey = requestReader.getUrlParameter("distinct");

    Result<List<DBObject>> resultData = requestReader.readResultDbObject();
    if (!resultData.isValid()) {
      responseWriter.send(SC_BAD_REQUEST, Status.get(PARSE_ERROR).toJsonTree());
      return;
    }

    MongoReader mongoReader = documentClient.read(requestReader.getDbName(),
        requestReader.getCollectionName());

    List<DBObject> queryData = resultData.getData();
    if (queryData.size() > 0) {
      if (isInValidQuery(queryData.get(0)))
        responseWriter.send(SC_BAD_REQUEST,
                Status.get("Invalid Query").toJsonTree());
    }
    mongoReader.find((queryData.size() > 0) ? queryData.get(0) : new BasicDBObject());

    try {
      responseWriter.send(SC_OK, new Gson().toJsonTree(mongoReader.distinct(distinctKey)));
    } catch (IllegalArgumentException e) {
      throw new MongoserException(400,
          Status.get(" Error message: " + e.getMessage()
              + " Invalid request " + resultData.getPlainBody()));
    }
  }

  public interface ReadProxy {
    boolean isNamespaceAllowed(String dbname, String colname);

    ReadProxy NONE = new ReadProxy() {
      @Override public boolean isNamespaceAllowed(String dbname, String colname) {
        return true;
      }
    };
  }

}
