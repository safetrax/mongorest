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

package in.mtap.iincube.mongoser.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class Status {
  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
  public static final Status FAIL = new Status("fail");
  public static final Status OK = new Status("ok");
  private String status;

  private Status(String status) {
    this.status = status;
  }

  public static Status get(String status) {
    // to implement cache later
    return new Status(status);
  }

  public String toJson() {
    return GSON.toJson(this);
  }

  public JsonElement toJsonTree() {
    return GSON.toJsonTree(this);
  }
}
