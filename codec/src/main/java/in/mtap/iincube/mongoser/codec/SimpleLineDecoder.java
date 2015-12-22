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

package in.mtap.iincube.mongoser.codec;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import in.mtap.iincube.mongoser.codec.io.RequestReader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleLineDecoder implements RequestReader.Decoder {
  private final LinkedList<String> lineEntries = new LinkedList<String>();
  private List<DBObject> dbObjects = new LinkedList<DBObject>();

  /**
   * treats each call value as separate line
   */
  @Override public void addData(String data) {
    lineEntries.add(data);
    addIfParseable(data);
  }

  private void addIfParseable(String data) {
    try {
      dbObjects.add((DBObject) JSON.parse(data));
    } catch (JSONParseException e) {
      e.printStackTrace();
    }
  }

  @Override public boolean isValid() {
    return dbObjects.size() > 0 && dbObjects.size() == lineEntries.size();
  }

  @Override public List<DBObject> getAsDBObject() {
    if (!isValid())
      throw new IllegalArgumentException("Can't decode invalid data \n" + lineEntries.toString());
    return new ArrayList<DBObject>(dbObjects);
  }

  @Override public void close() {
    dbObjects.clear();
    lineEntries.clear();
  }
}
