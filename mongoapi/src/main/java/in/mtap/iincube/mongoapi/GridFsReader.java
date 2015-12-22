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

import java.io.InputStream;

class GridFsReader extends GridFsRequestBuilder<GridFSDBFile> {

  GridFsReader(Mongo mongo, String dbname, String bucketname) {
    super(mongo, dbname, bucketname);
  }

  @Override public GridFsRequestBuilder<GridFSDBFile> filestream(InputStream stream) {
    throw new IllegalArgumentException("not supported for gridfs reader");
  }

  @Override public GridFSDBFile execute() throws IllegalArgumentException {
    return null;
  }
}
