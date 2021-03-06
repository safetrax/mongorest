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
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import in.mtap.iincube.mongoapi.DocumentClient;
import in.mtap.iincube.mongoapi.MongoUpdater;
import in.mtap.iincube.mongoapi.MongoWriter;
import in.mtap.iincube.mongoser.codec.Result;
import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.model.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.MISSING_DB_COL_PARAMS;
import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.PARSE_ERROR;
import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.hasValidParams;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class WriteRequestHandler {
  private final DocumentClient documentClient;
  private final RequestInterceptor interceptor;

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

  public WriteRequestHandler(DocumentClient documentClient) {
    this(documentClient, RequestInterceptor.ALLOW_ALL);
  }

  public WriteRequestHandler(DocumentClient documentClient, RequestInterceptor interceptor) {
    this.documentClient = documentClient;
    this.interceptor = interceptor;
  }

  private String[] getStringIds(List<DBObject> dbObjects) {
    List<String> result = new ArrayList<>(dbObjects.size());
    for (DBObject dbObject : dbObjects) {
      result.add(dbObject.get("_id").toString());
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Doesn't maintains any state, hence ensures thread safety
   */
  public void doInsert(RequestReader requestReader, Response response)
      throws IOException {
    if (!hasValidParams(requestReader)) {
      response.send(SC_BAD_REQUEST,
          Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
      return;
    }

    Result<List<DBObject>> resultData = requestReader.readResultDbObject();
    if (!resultData.isValid()) {
      response.send(SC_BAD_REQUEST, Status.get(PARSE_ERROR).toJsonTree());
      return;
    }

    MongoWriter mongoWriter = documentClient.write(requestReader.getDbName(),
        requestReader.getCollectionName());
    mongoWriter.insert(resultData.getData());
    mongoWriter.execute();
    response.send(SC_OK, new Gson().toJsonTree(getStringIds(resultData.getData())));
  }

  /**
   * Doesn't maintains any state, hence ensures thread safe
   */
  public void doUpdate(RequestReader requestReader, Response responseWriter)
      throws IOException {
    if (!hasValidParams(requestReader)) {
      responseWriter.send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
      return;
    }
    boolean upsert = requestReader.getParameterAsBoolean("upsert");
    boolean multi = requestReader.getParameterAsBoolean("multi");
    Result<List<DBObject>> dataResult = requestReader.readResultDbObject();
    if (!dataResult.isValid()) {
      responseWriter.send(SC_BAD_REQUEST, Status.get(PARSE_ERROR).toJsonTree());
      return;
    }

    List<DBObject> dbObjects = dataResult.getData();
    if (dbObjects.size() < 2) {
      responseWriter.send(SC_BAD_REQUEST,
          Status.get("findObject=null or updateObject=null").toJsonTree());
      return;
    }
    if (dbObjects.size() > 2) {
      responseWriter.send(SC_BAD_REQUEST,
          Status.get("invalid request can't take more than 2 body arguments").toJsonTree());
      return;
    }

    MongoUpdater updater = documentClient.update(requestReader.getDbName(),
        requestReader.getCollectionName());
    if (isInValidQuery(dbObjects.get(1)))
      responseWriter.send(SC_BAD_REQUEST,
              Status.get("Invalid Query").toJsonTree());
    updater.find(dbObjects.get(0)).update(dbObjects.get(1)).multi(multi).upsert(upsert);
    updater.execute();
    responseWriter.send(SC_OK, Status.OK.toJsonTree());
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

}
