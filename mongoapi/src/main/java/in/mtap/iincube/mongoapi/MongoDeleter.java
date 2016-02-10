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

package in.mtap.iincube.mongoapi;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class MongoDeleter {
  private final MongoObjectFactory<DBCollection> collectionFactory;
  private DBObject dbObject;

  MongoDeleter(MongoObjectFactory<DBCollection> collectionFactory) {
    this.collectionFactory = collectionFactory;
  }

  MongoDeleter find(DBObject dbObject) {
    this.dbObject = dbObject;
    return this;
  }

  WriteResult execute() {
    return collectionFactory.get().remove(dbObject);
  }

  /** @return the dbobject the of the document that is deleted */
  DBObject findAndRemove(DBObject dbObject) {
    return collectionFactory.get().findAndRemove(dbObject);
  }
}
