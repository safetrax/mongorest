/*
 * Copyright 2015 mtap technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package in.mtap.iincube.restrunner;

import in.mtap.iincube.mongoser.Mongoser;
import in.mtap.iincube.mongoser.config.MongoConfig;
import in.mtap.iincube.mongoser.config.ServerConfig;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;

import java.util.logging.Logger;

@Command(name = Main.NAME, description = "Mongoser REST server")
public class Main extends HelpOption implements Runnable {
  private static final Logger LOG = Logger.getLogger(Main.class.getSimpleName());
  private static final String FANCY_THAT = "-----------";

  static final String NAME = "Mongoser";

  @Option(name = {"-p", "--port"}, description = "port number to listen")
  public int portNo = 8081;

  @Option(name = {"-m", "--mongo"}, description = "mongo servers in format [host:port]")
  public String mongoServer = "127.0.0.1:27017";

  static Main fromArgs(String... args) {
    return SingleCommand.singleCommand(Main.class).parse(args);
  }

  public static void main(String[] args) {
    fromArgs(args).run();
  }

  public MongoConfig getMongoConfig() {
    if (mongoServer == null || mongoServer.trim().length() == 0) {
      mongoServer = "127.0.0.1";
    }
    return new MongoConfig.Builder(mongoServer).build();
  }

  public ServerConfig getServerConfig() {
    if (portNo <= 0) {
      portNo = 8081;
    }
    return new ServerConfig.Builder(portNo).build();
  }

  @Override public void run() {
    if (showHelpIfRequested()) {
      return;
    }

    LOG.info(FANCY_THAT + "Starting the server @ [" + portNo + "]" + FANCY_THAT);
    LOG.info(FANCY_THAT + "Using mongo servers [" + mongoServer + "]" + FANCY_THAT);

    final Mongoser mongoser = Mongoser.using(getMongoConfig(), getServerConfig())
        .enableDefaultAuth()
        .enableDefaultServlets()
        .build();
    try {
      mongoser.start();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override public void run() {
        try {
          mongoser.shutdown();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }));
  }
}
