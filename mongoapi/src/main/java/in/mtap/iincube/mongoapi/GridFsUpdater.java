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
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

class GridFsUpdater extends GridFsRequestBuilder<Boolean> {

  GridFsUpdater(Mongo mongo, String dbname, String bucketname) {
    super(mongo, dbname, bucketname);
  }

  @Override public Boolean execute() {
    GridFS gridFS = getGridFs();
    gridFS.remove(filename);
    GridFSInputFile file = gridFS.createFile(stream);
    file.setContentType(contentType);
    file.setFilename(filename);
    file.save();
    return true;
  }
}
