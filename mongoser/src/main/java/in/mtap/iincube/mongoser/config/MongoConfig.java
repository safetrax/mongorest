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

import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import in.mtap.iincube.mongoapi.MongoClient;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static in.mtap.iincube.mongoser.utils.Utility.toInt;

public class MongoConfig {
  private final String servers;
  private final boolean safeOperations;
  private final boolean removeIdField;
  private final boolean gridFsEnable;
  private final int threadNo;
  private com.mongodb.MongoClient mongo;
  private MongoClient mongoClient;

  public MongoConfig(String servers, boolean safeOperations, boolean removeIdField,
                     boolean gridFsEnable, int threadNo) {
    this.servers = servers;
    this.safeOperations = safeOperations;
    this.removeIdField = removeIdField;
    this.gridFsEnable = gridFsEnable;
    this.threadNo = threadNo;
  }

  private List<ServerAddress> toAddress(String servers) throws UnknownHostException {
    List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
    servers = servers.trim();
    String[] arrServer = servers.split("[,]");
    for (String server : arrServer) {
      String[] hostPort = server.split("[:]");
      if (hostPort.length != 2) {
        throw new IllegalArgumentException("Mongo servers should be in `server:port` format");
      }
      try {
        int port = Integer.parseInt(hostPort[1]);
        serverAddresses.add(new ServerAddress(hostPort[0], port));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Cannot parse port: " + hostPort[1]);
      }
    }
    return serverAddresses;
  }

  private com.mongodb.MongoClient getMongo() {
    if (mongo != null)
      return mongo;
    try {
      List<ServerAddress> serverAddresses = toAddress(servers);
      MongoClientOptions.Builder opts = new MongoClientOptions.Builder();

      if (threadNo < 100) {
        opts.connectionsPerHost(threadNo);
      } else {
        opts.connectionsPerHost(100);
      }
      opts.threadsAllowedToBlockForConnectionMultiplier(10);
      opts.maxWaitTime(10000);
      mongo = new com.mongodb.MongoClient(serverAddresses, opts.build());
      return mongo;
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Unknown host : " + servers);
    }
  }

  public MongoClient getMongoClient() {
    if (mongoClient == null)
      synchronized (this) {
        if (mongoClient == null)
          mongoClient = new MongoClient(getMongo());
      }
    return mongoClient;
  }

  /** Use {@link MongoConfig.Builder} instead */
  @Deprecated public static MongoConfig extractFrom(Properties properties) {
    String servers = properties.getProperty("mongo.servers", "127.0.0.1:27017");
    boolean safeOperations = Boolean.parseBoolean(
        properties.getProperty("mongo.safeoperations", "false"));
    boolean removeIdFields = Boolean.parseBoolean(
        properties.getProperty("mongo.remove.idfield", "false"));
    boolean gridfs = Boolean.parseBoolean(properties.getProperty("gridfs", "false"));
    int serverThreadNo = toInt(properties.getProperty("server.threadsno"), 50);
    return new MongoConfig(servers, safeOperations, removeIdFields, gridfs, serverThreadNo);
  }

  public static class Builder {
    private final String servers;
    private boolean safeOperation;
    private boolean removeIdField;
    private boolean enableGridFs;
    private int serverThreadNo = 50;

    public Builder(String servers) {
      this.servers = servers;
    }

    public Builder withSafeOperation(boolean safeOperation) {
      this.safeOperation = safeOperation;
      return this;
    }

    public Builder removeIdField(boolean removeIdField) {
      this.removeIdField = removeIdField;
      return this;
    }

    public Builder enableGridFs(boolean enableGridFs) {
      this.enableGridFs = enableGridFs;
      return this;
    }

    public Builder serverThreadNo(int serverThreadNo) {
      this.serverThreadNo = serverThreadNo;
      return this;
    }

    public MongoConfig build() {
      return new MongoConfig(servers, safeOperation, removeIdField, enableGridFs, serverThreadNo);
    }
  }
}
