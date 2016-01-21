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

package in.mtap.iincube.mongoser;

import in.mtap.iincube.mongoser.model.Status;

public class MongoserException extends RuntimeException {
  private final int code;
  private final Status status;

  public MongoserException(int code, Status status) {
    super("Error code [" + code + "] " + status.getMessage());
    this.code = code;
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  public int getCode() {
    return code;
  }
}
