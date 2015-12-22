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

package in.mtap.iincube.mongoser.codec.io;

import com.google.gson.JsonElement;
import com.mongodb.gridfs.GridFSDBFile;

import java.io.IOException;

public interface Response {
  void send(Meta meta, OutWriter out) throws IOException;

  void send(int code, Meta meta, String out) throws IOException;

  /** uses meta as {@link Meta#NOCACHE_JSON */
  void send(int code, JsonElement out) throws IOException;

  void send(int code, Meta meta, JsonElement out) throws IOException;

  void send(int code, Meta meta, GridFSDBFile out) throws IOException;
}
