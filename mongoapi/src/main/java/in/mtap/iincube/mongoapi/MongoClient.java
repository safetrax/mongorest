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

package in.mtap.iincube.mongoapi;

import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * A wrapper class for Mongo client java driver.
 * <p>
 * For MongoDB read request the api uses {@link DBObjectEncoder} to encode each cursor result.
 * For example getting the result in json
 */
public final class MongoClient implements DocumentClient, FsClient, SuperDocumentClient {

  private final Mongo mongo;

  /**
   * Will be removed in next major release
   *
   * Instead use {@link #MongoClient(com.mongodb.MongoClient)}
   */
  @Deprecated public MongoClient(Mongo mongo) {
    this.mongo = mongo;
  }

  /**
   * Accepts {@link Mongo} object that is already connected to the mongo server.
   * <p>
   * Note: Does not performs connect
   *
   * @see #MongoClient(com.mongodb.MongoClient)
   */
  public MongoClient(com.mongodb.MongoClient mongoClient) {
    this.mongo = mongoClient;
  }

  @Override public MongoReader read(String dbname, String colname) {
    return new MongoReader(new MongoCollectionFactory(mongo, dbname, colname));
  }

  @Override public MongoUpdater update(String dbname, String colname) {
    return new MongoUpdater(new MongoCollectionFactory(mongo, dbname, colname));
  }

  @Override public MongoWriter write(String dbname, String colname) {
    return new MongoWriter(new MongoCollectionFactory(mongo, dbname, colname));
  }

  @Override public MongoDeleter remover(String dbname, String colname) {
    return new MongoDeleter(new MongoCollectionFactory(mongo, dbname, colname));
  }

  @Override public GridFsRequestBuilder<Boolean> gridFsUpdate(String dbname, String bucketname) {
    return new GridFsUpdater(mongo, dbname, bucketname);
  }

  @Override public GridFsRequestBuilder<Boolean> gridFsWrite(String dbname, String bucketname) {
    return new GridFsWriter(mongo, dbname, bucketname);
  }

  @Override //
  public GridFsRequestBuilder<GridFSDBFile> gridFsRead(String dbname, String bucketname) {
    return new GridFsReader(mongo, dbname, bucketname);
  }
}
