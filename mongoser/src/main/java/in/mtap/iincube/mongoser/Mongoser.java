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

import in.mtap.iincube.mongoapi.MongoClient;
import in.mtap.iincube.mongoser.auth.AuthFactory;
import in.mtap.iincube.mongoser.config.MongoConfig;
import in.mtap.iincube.mongoser.config.ServerConfig;
import in.mtap.iincube.mongoser.handlers.DataBaseAccessChecker;
import in.mtap.iincube.mongoser.handlers.GridFsRequestHandler;
import in.mtap.iincube.mongoser.handlers.ReadRequestHandler;
import in.mtap.iincube.mongoser.handlers.WriteRequestHandler;
import in.mtap.iincube.mongoser.servlet.ErrorServlet;
import in.mtap.iincube.mongoser.servlet.GridFsServlet;
import in.mtap.iincube.mongoser.servlet.QueryServlet;
import in.mtap.iincube.mongoser.servlet.WriteServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class Mongoser {
  private final MongoClient mongoClient;
  private final ServerConfig serverConfig;
  private final Server server;
  private final Map<String, HttpServlet> pathHttpServlet;
  private FilterHolder authFilter;

  Mongoser(MongoConfig mongoConfig, ServerConfig serverConfig,
           Map<String, HttpServlet> pathHttpServlet, AuthFactory authFactory) {
    this(mongoConfig.getMongoClient(), serverConfig, new Server(), pathHttpServlet, authFactory);
  }

  private Mongoser(MongoClient mongoClient, ServerConfig serverConfig,
           Server server, Map<String, HttpServlet> pathHttpServlet, AuthFactory authFactory) {
    this.mongoClient = mongoClient;
    this.serverConfig = serverConfig;
    this.pathHttpServlet = pathHttpServlet;
    this.server = server;
    this.authFilter = new FilterHolder(authFactory.getAuthFilter());
  }

  public void start() throws Exception {
    server.setConnectors(serverConfig.getConnectors(server));
    ErrorPageErrorHandler errorHandler = new MongoserErrorHandler();
    errorHandler.addErrorPage(400, 599, "/error");
    errorHandler.setShowStacks(false);

    ServletContextHandler contextHandler = new ServletContextHandler(NO_SESSIONS);
    contextHandler.setContextPath("/");
    contextHandler.setErrorHandler(errorHandler);
    server.setHandler(contextHandler);

    for (String path : pathHttpServlet.keySet()) {
      contextHandler.addServlet(new ServletHolder(pathHttpServlet.get(path)), path);
    }

    if (authFilter != null)
      contextHandler.addFilter(authFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
    server.start();
    server.join();
  }

  public void shutdown() throws Exception {
    server.stop();
  }

  public static Builder using(MongoConfig mongoConfig, ServerConfig serverConfig,
                              DataBaseAccessChecker dataBaseAccessChecker) {
    return new Builder(mongoConfig, serverConfig, dataBaseAccessChecker);
  }

  public static class Builder {
    private final MongoConfig mongoConfig;
    private final ServerConfig serverConfig;
    private final Map<String, HttpServlet> pathHttpServlet = new HashMap<String, HttpServlet>();
    private AuthFactory authFactory;
    private DataBaseAccessChecker dataBaseAccessChecker;

    public Builder(MongoConfig mongoConfig, ServerConfig serverConfig,
                   DataBaseAccessChecker dataBaseAccessChecker) {
      this.mongoConfig = mongoConfig;
      this.serverConfig = serverConfig;
      this.dataBaseAccessChecker = dataBaseAccessChecker;
    }

    public Builder enableDefaultServlets() {
      MongoClient mongoClient = mongoConfig.getMongoClient();
      addServlet("/query", new QueryServlet(new ReadRequestHandler(mongoClient, dataBaseAccessChecker)));
      addServlet("/write", new WriteServlet(new WriteRequestHandler(mongoClient, dataBaseAccessChecker)));
      addServlet("/gridfs", new GridFsServlet(new GridFsRequestHandler(mongoClient, dataBaseAccessChecker)));
      addServlet("/error", new ErrorServlet());
      return this;
    }

    public Builder enableDefaultAuth() {
      enableAuth(AuthFactory.NONE);
      return this;
    }

    public Builder enableAuth(AuthFactory authFactory) {
      this.authFactory = authFactory;
      addServlet(AuthFactory.AUTH_PATH, authFactory.getAuthServlet());
      return this;
    }

    public Builder addServlet(String path, HttpServlet servlet) {
      if (pathHttpServlet.containsKey(path))
        throw new IllegalArgumentException("Path name: " + path
            + " is already configured for: " + pathHttpServlet.get(path).getClass().getSimpleName()
            + " can't register the same with" + servlet.getClass().getSimpleName());
      pathHttpServlet.put(path, servlet);
      return this;
    }

    public Mongoser build() {
      return new Mongoser(mongoConfig, serverConfig, pathHttpServlet, authFactory);
    }
  }
}
