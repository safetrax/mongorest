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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.LinkedList;
import java.util.List;

import static in.mtap.iincube.mongoapi.internal.Utility.assertNotNull;

public class MongoReader {
  private final MongoObjectFactory<DBCollection> collectionFactory;
  private int skip;
  private int limit;
  private String[] fields;
  private DBObject queryObject;
  private DBObject sortObject;
  private String indexName;

  MongoReader(MongoObjectFactory<DBCollection> collectionFactory) {
    this.collectionFactory = collectionFactory;
  }

  public MongoReader skip(int skip) {
    this.skip = skip;
    return this;
  }

  public MongoReader limit(int limit) {
    this.limit = limit;
    return this;
  }

  public MongoReader select(String[] fields) {
    this.fields = fields;
    return this;
  }

  public MongoReader find(DBObject queryObject) {
    this.queryObject = queryObject;
    return this;
  }

  public MongoReader sort(DBObject sortObject) {
    this.sortObject = sortObject;
    return this;
  }

  public MongoReader index(String indexName) {
    this.indexName = indexName;
    return this;
  }

  private DBCursor getCursor() {
    DBCollection collection = collectionFactory.get();
    assertNotNull(queryObject, "findQuery == null");
    DBCursor cursor;
    if (fields != null) {
      BasicDBObject selectFields = new BasicDBObject();
      for (String field : fields) {
        selectFields.append(field, 1);
      }
      cursor = collection.find(queryObject, selectFields);
    } else {
      cursor = collection.find(queryObject);
    }
    if (indexName != null)
      cursor.hint(indexName);
    if (skip > 0)
      cursor.skip(skip);
    if (limit > 0)
      cursor.limit(limit);
    if (sortObject != null)
      cursor.sort(sortObject);
    return cursor;
  }

  @Deprecated public List<DBObject> query() {
    return execute();
  }

  public List distinct(String key) {
    if (queryObject == null)
      return collectionFactory.get().distinct(key);
    return collectionFactory.get().distinct(key, queryObject);
  }

  /** use {@link #execute(DBObjectEncoder)}*/
  @Deprecated public <T> T query(DBObjectEncoder<T> encoder) {
    return execute(encoder);
  }

  public List<DBObject> execute() {
    DBCursor cursor = getCursor();
    List<DBObject> result = new LinkedList<DBObject>();
    while (cursor.hasNext()) {
      result.add(cursor.next());
    }
    cursor.close();
    return result;
  }

  public <T> T execute(DBObjectEncoder<T> encoder) {
    DBCursor cursor = getCursor();
    try {
      assertNotNull(encoder, "encoder == null");
      return encoder.encode(cursor);
    } finally {
      cursor.close();
    }
  }
}
