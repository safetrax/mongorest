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
import com.mongodb.WriteResult;

import java.util.LinkedList;
import java.util.List;

import static in.mtap.iincube.mongoapi.internal.Utility.assertNotNull;

public class MongoWriter {
  private final MongoObjectFactory<DBCollection> collectionFactory;
  private List<DBObject> dbObjects = new LinkedList<DBObject>();

  MongoWriter(MongoObjectFactory<DBCollection> collectionFactory) {
    this.collectionFactory = collectionFactory;
  }

  public MongoWriter insert(List<DBObject> dbObjects) {
    this.dbObjects.addAll(dbObjects);
    return this;
  }

  public MongoWriter insert(DBObject dbObject) {
    dbObjects.add(dbObject);
    return this;
  }

  private void assertArguments() {
    assertNotNull(dbObjects, "dbObject == null");
  }

  /** Uses {@link WriteConcern#JOURNALED} */
  public void execute() {
    execute(WriteConcern.JOURNALED);
  }

  public WriteResult execute(WriteConcern writeConcern) {
    assertArguments();
    assertNotNull(writeConcern, "WriteConcern == null");
    DBCollection collection = collectionFactory.get();
    return collection.insert(dbObjects, writeConcern);
  }
}
