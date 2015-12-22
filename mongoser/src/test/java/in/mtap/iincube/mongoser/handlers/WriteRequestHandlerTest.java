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

import java.io.IOException;
import java.util.List;

import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.MISSING_DB_COL_PARAMS;
import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.PARSE_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class WriteRequestHandlerTest {
  private Response response;
  private RequestReader reader;

  @Before public void setup() {
    response = mock(Response.class);
    reader = mock(RequestReader.class);
  }

  @Test public void errorInsertIfNoDbName() throws IOException {
    when(reader.getCollectionName()).thenReturn("colname");
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doInsert(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorUpdateIfNoDbName() throws Exception {
    when(reader.getCollectionName()).thenReturn("colname");
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doUpdate(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorInsertIfNoCollectionName() throws IOException {
    when(reader.getDbName()).thenReturn("dbname");
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doInsert(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorUpdateIfNoCollectionName() throws IOException {
    when(reader.getDbName()).thenReturn("dbname");
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doUpdate(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorInsertIfAnyDbNameAndColname() throws IOException {
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doInsert(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorUpdateIfAnyDbNameAndColname() throws IOException {
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doUpdate(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(MISSING_DB_COL_PARAMS).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorInsertIfInvalidData() throws Exception {
    when(reader.getDbName()).thenReturn("dbname");
    when(reader.getCollectionName()).thenReturn("colname");
    when(reader.readResultDbObject()).thenReturn(new Result<List<DBObject>>(null, false));
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doInsert(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(PARSE_ERROR).toJsonTree());
    verifyNoMoreInteractions(response);
  }

  @Test public void errorUpdateIfInvalidData() throws Exception {
    when(reader.getDbName()).thenReturn("dbname");
    when(reader.getCollectionName()).thenReturn("colname");
    when(reader.readResultDbObject()).thenReturn(new Result<List<DBObject>>(null, false));
    WriteRequestHandler writeRequestHandler = new WriteRequestHandler(null);
    writeRequestHandler.doUpdate(reader, response);
    verify(response).send(SC_BAD_REQUEST, Status.get(PARSE_ERROR).toJsonTree());
    verifyNoMoreInteractions(response);
  }
}
