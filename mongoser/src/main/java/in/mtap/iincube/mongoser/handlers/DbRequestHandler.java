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

package in.mtap.iincube.mongoser.handlers;

import in.mtap.iincube.mongoser.codec.io.RequestReader;

class DbRequestHandler {
  static final String MISSING_DB_COL_PARAMS = "dbname == null or colname == null";
  static final String PARSE_ERROR = "Could not parse data";

  /** @return true if {@link RequestReader} has dbname and colname */
  static boolean hasValidParams(RequestReader requestReader) {
    return requestReader.getDbName() != null && requestReader.getCollectionName() != null;
  }
}
