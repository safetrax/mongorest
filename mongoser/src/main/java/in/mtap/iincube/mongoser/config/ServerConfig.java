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

package in.mtap.iincube.mongoser.config;

import in.mtap.iincube.mongoser.utils.Utility;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.Properties;

public class ServerConfig {
  private final String serverAddr;
  private final int port;
  private final boolean ssl;
  private final String sslKeystore;
  private final String sslPassword;
  private final boolean serverAuthEnable;
  private final QueuedThreadPool threadPool;

  public ServerConfig(String serverAddr, int port, int noOfThreads, boolean ssl,
                      String sslKeystore, String sslPassword, boolean serverAuthEnable) {
    this.serverAddr = serverAddr;
    this.port = port;
    this.ssl = ssl;
    this.sslKeystore = sslKeystore;
    this.sslPassword = sslPassword;
    this.serverAuthEnable = serverAuthEnable;
    threadPool = new QueuedThreadPool(noOfThreads);
  }

  private ConnectionFactory getConnectionFactory() {
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    httpConfig.setSecurePort(port);
    httpConfig.setSendDateHeader(true);
    ConnectionFactory result;
    if (ssl) {
      SslContextFactory contextFactory = new SslContextFactory();
      contextFactory.setKeyStorePath(sslKeystore);
      contextFactory.setKeyManagerPassword(sslPassword);
      result = new SslConnectionFactory(contextFactory, HttpVersion.HTTP_2.asString());
    } else {
      result = new HttpConnectionFactory(httpConfig);
    }
    return result;
  }

  public Connector[] getConnectors(Server server) {
    ServerConnector connector = new ServerConnector(server, getConnectionFactory());
    connector.setHost(serverAddr);
    connector.setPort(port);
    return new Connector[] {connector};
  }

  /** use {@link ServerConfig.Builder } instead */
  @Deprecated
  public static ServerConfig extractFrom(Properties properties) {
    String serverAddr = properties.getProperty("server.adr", null);
    int port = Utility.toInt(properties.getProperty("server.port"), 8080);
    int noOfThreads = Utility.toInt(properties.getProperty("server.threadsno"), 50);
    boolean ssl = Boolean.parseBoolean(properties.getProperty("ssl", "false"));
    String sslKeystore = null;
    String sslPassword = null;
    if (ssl) {
      sslKeystore = properties.getProperty("ssl.keystore", "./config/keystore");
      sslPassword = properties.getProperty("ssl.password", "");
    }
    boolean serverAuthEnable = Boolean.parseBoolean(properties.getProperty("auth", "false"));
    return new ServerConfig(serverAddr, port, noOfThreads, ssl, sslKeystore,
        sslPassword, serverAuthEnable);
  }

  public static class Builder {
    private final int port;
    private final String serverAddr;
    private int threadNo = 50;
    private boolean enableSsl;
    private String keystore;
    private String password;
    private boolean enableServerAuth;

    public Builder(int port) {
      this(null, port);
    }

    public Builder(String serverAddr, int port) {
      this.serverAddr = serverAddr;
      this.port = port;
    }

    public Builder serverThreadNo(int threadNo) {
      this.threadNo = threadNo;
      return this;
    }

    public Builder enableSsl(boolean enableSsl) {
      this.enableSsl = enableSsl;
      return this;
    }

    public Builder sslKeystoreAndPassword(String keystore, String password) {
      this.keystore = keystore;
      this.password = password;
      return this;
    }

    public Builder enableServerAuth(boolean enableServerAuth) {
      this.enableServerAuth = enableServerAuth;
      return this;
    }

    public ServerConfig build() {
      return new ServerConfig(serverAddr, port, threadNo, enableSsl,
          keystore, password, enableServerAuth);
    }
  }
}
