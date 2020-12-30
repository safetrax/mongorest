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

import in.mtap.iincube.mongoser.model.Status;
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
    Throwable throwable = (Throwable) req.getAttribute(Dispatcher.ERROR_EXCEPTION);

    if (exceptionClass == null) {
      LOG.warning("Unknown state for request " + req.toString());
      writeMongoError("Unknown error", baseRequest, res);
      return;
    }

    LOG.info("handling error type: " + exceptionClass.getSimpleName());
    if (MongoserException.class.equals(exceptionClass)) {
      MongoserException exception = (MongoserException) req.getAttribute(Dispatcher.ERROR_EXCEPTION);

      res.setContentType("application/json");
      res.setStatus(exception.getCode());

      Status st = exception.getStatus();
      if (st == null)
        st = Status.FAIL;

      PrintWriter w = res.getWriter();
      w.println(st.toJson());
      w.flush();
      baseRequest.setHandled(true);
      return;
    } else {
      writeMongoError(throwable.getMessage(), baseRequest, res);
      return;
    }
  }

  private void writeMongoError(String msg, Request baseRequest, HttpServletResponse res)
      throws IOException {
    res.setContentType("application/json");
    res.setStatus(SC_BAD_REQUEST);

    Status st = Status.get(msg);

    PrintWriter w = res.getWriter();
    w.println(st.toJson());
    w.flush();
    baseRequest.setHandled(true);
  }
}
