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

import javax.servlet.http.HttpServletResponse;

public interface Meta {
  void set(HttpServletResponse response);

  Meta NONE = new Meta() {
    @Override public void set(HttpServletResponse response) {

    }
  };

  Meta NOCACHE = new Meta() {
    @Override public void set(HttpServletResponse response) {
      response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
    }
  };

  Meta NOCACHE_JSON = new Meta() {
    @Override public void set(HttpServletResponse response) {
      NOCACHE.set(response);
      JSON.set(response);
    }
  };

  Meta JSON = new Meta() {
    @Override public void set(HttpServletResponse response) {
      response.setContentType("application/json");
    }
  };
}
