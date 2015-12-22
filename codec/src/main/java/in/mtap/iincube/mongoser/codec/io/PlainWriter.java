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
import java.io.PrintWriter;

/**
 * Writes the value of {@link Object#toString()} from
 * the parameter {@code message} to the response
 */
final class PlainWriter extends OutWriter {
  private final Object message;

  public PlainWriter(int code, Object message) {
    super(code);
    this.message = message;
  }

  @Override protected void writeTo(HttpServletResponse response) throws IOException {
    PrintWriter writer = response.getWriter();
    writer.write(message.toString());
    writer.flush();
  }
}
