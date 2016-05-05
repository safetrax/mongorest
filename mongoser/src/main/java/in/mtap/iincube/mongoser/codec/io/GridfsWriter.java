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

import com.mongodb.gridfs.GridFSDBFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

final class GridfsWriter extends OutWriter {
  private final GridFSDBFile file;

  public GridfsWriter(int code, GridFSDBFile file) {
    super(code);
    this.file = file;
  }

  @Override protected void writeTo(HttpServletResponse response) throws IOException {
    super.writeTo(response);
    OutputStream outStream = response.getOutputStream();
    response.setHeader("Content-Disposition", "attachment;filename=" + file.getFilename());
    response.setContentType(file.getContentType());
    response.setContentLength((int) file.getLength());
    file.writeTo(outStream);
    outStream.flush();
    outStream.close();
  }
}
