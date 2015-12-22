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

package in.mtap.iincube.mongoser;

import com.mongodb.MongoException;
import in.mtap.iincube.mongoser.model.Status;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class MongoserErrorHandler extends ErrorPageErrorHandler {
  private static final Logger LOG = Logger.getLogger(MongoserErrorHandler.class.getName());

  public MongoserErrorHandler() {
    super();
  }

  @Override public void handle(String target, Request baseRequest,
                     HttpServletRequest req, HttpServletResponse res) throws IOException {
    Class<?> exceptionClass = (Class<?>) req.getAttribute(Dispatcher.ERROR_EXCEPTION_TYPE);
    LOG.fine("handling error type: " + exceptionClass.getSimpleName());
    if (MongoserException.class.equals(exceptionClass)) {
      MongoserException exception = (MongoserException) req.getAttribute(Dispatcher.ERROR_EXCEPTION);

      res.setContentType("application/json;charset=UTF-8");
      res.setStatus(exception.getCode());

      Status st = exception.getStatus();
      if (st == null)
        st = Status.FAIL;

      PrintWriter w = res.getWriter();
      w.println(st.toJson());
      w.flush();
      AbstractHttpConnection.getCurrentConnection().getRequest().setHandled(true);
      return;
    } else if (MongoException.Network.class.equals(exceptionClass)) {
      writeMongoError("Mongodb server down or can not be reached", res);
      return;
    } else if (MongoException.CursorNotFound.class.equals(exceptionClass)) {
      writeMongoError("Mongodb: cursor not found", res);
      return;
    } else if (MongoException.DuplicateKey.class.equals(exceptionClass)) {
      writeMongoError("Mongodb: duplicate key", res);
      return;
    } else if (exceptionClass != null
        && "com.mongodb.CommandResult$CommandFailure".equals(exceptionClass.getName())) {
      Exception exc = (Exception) req.getAttribute(Dispatcher.ERROR_EXCEPTION);
      writeMongoError("Mongodb: " + exc.getMessage(), res);
      return;
    } else {
      writeMongoError("Unhandled exception", res);
      return;
    }
    // super.handle(target, baseRequest, req, res);
  }

  private void writeMongoError(String msg, HttpServletResponse res)
      throws IOException {
    res.setContentType("application/json;charset=UTF-8");
    res.setStatus(SC_BAD_REQUEST);

    Status st = Status.get(msg);

    PrintWriter w = res.getWriter();
    w.println(st.toJson());
    w.flush();
    AbstractHttpConnection.getCurrentConnection().getRequest().setHandled(true);
  }

}
