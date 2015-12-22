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
import com.mongodb.DBCursor;
import in.mtap.iincube.mongoapi.DBObjectEncoder;

import static in.mtap.iincube.mongoser.codec.utils.Mongo2Gson.getAsJsonObject;

public class JsonEncoder implements DBObjectEncoder<JsonElement> {

  @Override public JsonElement encode(DBCursor dbCursor) {
    JsonArray jsonArray = new JsonArray();
    while (dbCursor.hasNext()) {
      jsonArray.add(getAsJsonObject(dbCursor.next()));
    }
    return jsonArray;
  }

}
