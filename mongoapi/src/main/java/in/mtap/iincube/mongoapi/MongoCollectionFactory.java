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

import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;

import static in.mtap.iincube.mongoapi.internal.Utility.assertNotNull;

class MongoCollectionFactory implements MongoObjectFactory<DBCollection> {
  private final Mongo mongo;
  private final String dbname;
  private final String colname;

  public MongoCollectionFactory(Mongo mongo, String dbname, String colname) {
    verifyParams(mongo, dbname, colname);
    this.mongo = mongo;
    this.dbname = dbname;
    this.colname = colname;
  }

  private void verifyParams(Mongo mongo, String dbname, String colname) {
    assertNotNull(mongo, "mongo=null");
    assertNotNull(dbname, "dbname=null");
    assertNotNull(colname, "colname=null");
  }

  @Override public DBCollection get() {
    return mongo.getDB(dbname).getCollection(colname);
  }

  public GridFS getGridFs() {
    return new GridFS(mongo.getDB(dbname), colname);
  }
}
