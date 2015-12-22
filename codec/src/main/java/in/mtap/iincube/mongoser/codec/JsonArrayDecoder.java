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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import in.mtap.iincube.mongoser.codec.io.RequestReader;

import java.util.LinkedList;
import java.util.List;

public class JsonArrayDecoder implements RequestReader.Decoder {
  private StringBuilder dataBuilder = new StringBuilder();
  private JsonParser parser = new JsonParser();
  private JsonElement jsonElement;

  @Override public void addData(String data) {
    dataBuilder.append(data);
  }

  private JsonElement extractData(String jsonString) throws JsonSyntaxException {
    JsonElement element = parser.parse(jsonString);
    if (!element.isJsonArray() && !element.isJsonObject())
      throw new JsonSyntaxException("Not a valid json");
    return element;
  }

  @Override public boolean isValid() {
    if (jsonElement != null)
      return true;
    try {
      jsonElement = extractData(dataBuilder.toString());
      return true;
    } catch (JsonSyntaxException e) {
      jsonElement = null;
      return false;
    }
  }

  @Override public List<DBObject> getAsDBObject() {
    List<DBObject> dbObjects = new LinkedList<DBObject>();
    if (!isValid())
      throw new IllegalArgumentException("Parse error invalid json: \n" + dataBuilder.toString());

    if (jsonElement.isJsonArray()) {
      JsonArray jsonArray = jsonElement.getAsJsonArray();
      for (JsonElement element : jsonArray) {
        dbObjects.add((DBObject) JSON.parse(element.toString()));
      }
    } else {
      dbObjects.add((DBObject) JSON.parse(jsonElement.toString()));
    }
    return dbObjects;
  }

  @Override public void close() {
    dataBuilder.setLength(0);
    jsonElement = null;
  }
}
