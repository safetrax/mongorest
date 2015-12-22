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
import in.mtap.iincube.mongoser.handlers.ReadRequestHandler;

import java.io.IOException;

public class QueryServlet extends MongoserServlet {
  private final ReadRequestHandler readRequestHandler;

  public QueryServlet(ReadRequestHandler readRequestHandler) {
    this.readRequestHandler = readRequestHandler;
  }

  @Override protected void doPost(RequestReader reader, Response writer)
      throws IOException {
    readRequestHandler.process(reader, writer);
  }
}
