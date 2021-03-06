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
import java.io.IOException;

/**
 * Gives access to the raw response writer to define output for special messages
 * which is not defined under {@link ResponseWriter} inbuilt contracts
 */
public abstract class OutWriter {
  private final int code;

  protected OutWriter(int code) {
    this.code = code;
  }

  /** Override this method to define the write for special outputs */
  protected void writeTo(HttpServletResponse response) throws IOException {
    response.setStatus(code);
  }
}
