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

package in.mtap.iincube.mongoser.servlet;

import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.handlers.GridFsRequestHandler;

import java.io.IOException;

public class GridFsServlet extends MongoserServlet {
  private final GridFsRequestHandler requestHandler;

  public GridFsServlet(GridFsRequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }

  @Override protected void doGet(RequestReader reader, Response writer) throws IOException {
    requestHandler.doReadGrid(reader, writer);
  }

  @Override protected void doPut(RequestReader reader, Response writer) throws IOException {
    requestHandler.doWriteGrid(reader, writer);
  }

  @Override protected void doPost(RequestReader reader, Response writer) throws IOException {
    requestHandler.doUpdateGrid(reader, writer);
  }
}
