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

package in.mtap.iincube.restrunner;

import in.mtap.iincube.mongoser.Mongoser;
import in.mtap.iincube.mongoser.config.MongoConfig;
import in.mtap.iincube.mongoser.config.ServerConfig;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class RestServer {
  private static final Logger LOG = Logger.getLogger(RestServer.class.getSimpleName());
  private static final String FANCY_THAT = "-----------";

  public static void main(String[] args) throws Exception {
    LOG.info(FANCY_THAT + "Starting the server" + FANCY_THAT);
    InputStream propertyStream = RestServer.class.getResourceAsStream("/mongoser.properties");
    Properties properties = new Properties();
    properties.load(propertyStream);

    MongoConfig mongoConfig = MongoConfig.extractFrom(properties);
    ServerConfig serverConfig = ServerConfig.extractFrom(properties);

    final Mongoser mongoser = Mongoser.using(mongoConfig, serverConfig)
        .enableDefaultAuth()
        .enableDefaultServlets()
        .addServlet("/errorsome", new ErrorServlet())
        .build();

    mongoser.start();

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