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

import com.mongodb.DBObject;
import in.mtap.iincube.mongoser.codec.Result;
import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.model.Status;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.MISSING_DB_COL_PARAMS;
import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.PARSE_ERROR;
import static in.mtap.iincube.mongoser.handlers.ReadRequestHandler.INVALID_LIMIT_SKIP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReadRequestHandlerTest {
  private Response response;
  private RequestReader reader;

  @Before public void setup() {
    response = mock(Response.class);
    reader = mock(RequestReader.class);
  }

  @Test public void errorIfNoCollectionNameParam() throws Exception {
    when(reader.getDbName()).thenReturn("db");

    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null);
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorIfNoDbNameParam() throws Exception {
    when(reader.getCollectionName()).thenReturn("colname");

    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null);
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorIfNoParams() throws Exception {
    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null);
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorOnInvalidLimitParam() throws Exception {
    when(reader.getCollectionName()).thenReturn("colname");
    when(reader.getDbName()).thenReturn("dbname");
    when(reader.getParameterAsInt("limit")).thenThrow(new NumberFormatException());
    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null);
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get(INVALID_LIMIT_SKIP).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorOnInvalidSkipParam() throws Exception {
    when(reader.getCollectionName()).thenReturn("colname");
    when(reader.getDbName()).thenReturn("dbname");
    when(reader.getParameterAsInt("skip")).thenThrow(new NumberFormatException());
    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null);
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get(INVALID_LIMIT_SKIP).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorIfReadProxyDidNotAllow() throws Exception {
    when(reader.getCollectionName()).thenReturn("colname");
    when(reader.getDbName()).thenReturn("dbname");
    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null,
        new RequestInterceptor() {
          @Override public boolean isReadRestricted(String dbName, String colname) {
            return true;
          }

          @Override public boolean isWriteRestricted(String dbName, String colname) {
            throw new AssertionError("Inappropriate call on write handler");
          }
        });
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get("Not allowed to read this namespace").toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void parseErrorOnInputStream() throws Exception {
    when(reader.getCollectionName()).thenReturn("colname");
    when(reader.getDbName()).thenReturn("dbname");
    when(reader.readResultDbObject()).thenReturn(new Result<List<DBObject>>(null, false));
    ReadRequestHandler readRequestHandler = new ReadRequestHandler(null);
    readRequestHandler.process(reader, response);

    verify(response).send(HttpServletResponse.SC_BAD_REQUEST,
        Status.get(PARSE_ERROR).toJsonTree());
    verifyNoMoreInteractions(response);
  }
}
