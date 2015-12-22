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
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
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

  public Connector[] getConnectors() {
    if (ssl) {
      SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
      sslConnector.setStatsOn(false);
      sslConnector.setHost(serverAddr);
      sslConnector.setPort(port);
      sslConnector.setThreadPool(threadPool);
      sslConnector.setName("Simple Mongoser SSL Connector");
      SslContextFactory sslContextFactory = sslConnector.getSslContextFactory();
      sslContextFactory.setKeyStorePath(sslKeystore);
      sslContextFactory.setKeyManagerPassword(sslPassword);
      return new Connector[]{sslConnector};
    } else {
      SelectChannelConnector connector = new SelectChannelConnector();
      connector.setStatsOn(false);
      connector.setHost(serverAddr);
      connector.setPort(port);
      connector.setThreadPool(threadPool);
      connector.setName("SimpleMongodb connector");
      return new Connector[]{connector};
    }
  }

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
}
