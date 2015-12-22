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

import in.mtap.iincube.mongoser.codec.io.Meta;
import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.codec.io.ResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

public abstract class MongoserServlet extends HttpServlet {

  private void notImplemented(Response response) throws IOException {
    response.send(SC_METHOD_NOT_ALLOWED, Meta.NOCACHE, "Not allowed");
  }

  /** To reuse existing RequestReader constructed at Filters. */
  private RequestReader getRequestReader(HttpServletRequest request) {
    return RequestReader.from(request);
  }

  @Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(getRequestReader(req), ResponseWriter.from(resp));
  }

  /** Calling super method will send not implemented error response */
  protected void doGet(RequestReader reader, Response writer) throws IOException {
    notImplemented(writer);
  }

  @Override protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPut(getRequestReader(req), ResponseWriter.from(resp));
  }

  /** Calling super method will send not implemented error response */
  protected void doPut(RequestReader reader, Response writer) throws IOException {
    notImplemented(writer);
  }

  @Override protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(getRequestReader(req), ResponseWriter.from(resp));
  }

  /** Calling super method will send not implemented error response */
  protected void doPost(RequestReader reader, Response writer) throws IOException {
    notImplemented(writer);
  }

  @Override protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doDelete(getRequestReader(req), ResponseWriter.from(resp));
  }

  /** Calling super method will send not implemented error response */
  protected void doDelete(RequestReader reader, Response response) throws IOException {
    notImplemented(response);
  }

  @Override protected final void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doOptions(getRequestReader(req), ResponseWriter.from(resp));
  }

  /** Calling super method will send not implemented error response */
  protected void doOptions(RequestReader reader, Response writer) throws IOException {
    notImplemented(writer);
  }
}
