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
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

import static in.mtap.iincube.mongoapi.internal.Utility.assertNotNull;

public class MongoUpdater {
  private final MongoObjectFactory<DBCollection> collectionFactory;
  private DBObject findObject;
  private DBObject updateObject;
  private boolean multi;
  private boolean upsert;

  MongoUpdater(MongoObjectFactory<DBCollection> collectionFactory) {
    this.collectionFactory = collectionFactory;
  }

  public MongoUpdater find(DBObject findObject) {
    this.findObject = findObject;
    return this;
  }

  public MongoUpdater update(DBObject updateObject) {
    this.updateObject = updateObject;
    return this;
  }

  public MongoUpdater multi(boolean multi) {
    this.multi = multi;
    return this;
  }

  public MongoUpdater upsert(boolean upsert) {
    this.upsert = upsert;
    return this;
  }

  public void execute() {
    execute(null);
  }

  private void assertArguments() {
    assertNotNull(findObject, "findObject == null");
    assertNotNull(updateObject, "updateObject == null");
  }

  public void execute(WriteConcern writeConcern) {
    assertArguments();
    DBCollection collection = collectionFactory.get();
    if (writeConcern != null) {
      collection.update(findObject, updateObject, multi, upsert, writeConcern);
    } else {
      collection.update(findObject, updateObject, multi, upsert);
    }
  }
}
