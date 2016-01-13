/*
 * Copyright 2015 mtap technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package in.mtap.iincube.mongoser.codec.io;

import com.google.gson.stream.JsonWriter;
import com.mongodb.DBObject;
import in.mtap.iincube.mongoser.codec.Result;
import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestReaderTest {

  @Test public void decodeUrlRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("dbname")).thenReturn("someDb");
    when(request.getParameter("colname")).thenReturn("someCollection");
    when(request.getParameter("somerandom")).thenReturn("custom");
    RequestReader reader = new RequestReader(request);
    assertThat(reader.getDbName()).isEqualTo("someDb");
    assertThat(reader.getCollectionName()).isEqualTo("someCollection");
    assertThat(reader.getUrlParameter("somerandom")).isEqualTo("custom");
    assertThat(reader.getUrlParameter("notfound")).isNull();
  }

  @Test public void decodeNewLineRequest() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject().name("key").value("value").endObject();
    when(request.getInputStream()).thenReturn(getAsInputStream(stringWriter.toString()
        + "\n" + stringWriter.toString()));

    RequestReader reader = new RequestReader(request);
    Result<List<DBObject>> resultData = reader.readResultDbObject();
    assertThat(resultData.isValid()).isTrue();
    assertThat(resultData.getData()).hasSize(2);
  }

  @Test public void decodeJsonArrayRequest() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter.beginArray().beginObject().name("key").value("value").endObject()
        .beginObject().name("key2").value("value2").endObject().endArray();

    when(request.getInputStream()).thenReturn(getAsInputStream(writer.toString()));

    RequestReader reader = new RequestReader(request);
    Result<List<DBObject>> resultData = reader.readResultDbObject();
    assertThat(resultData.isValid()).isTrue();
    assertThat(resultData.getData()).hasSize(2);
  }

  @Test public void decodePlainJsonRequest() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter.beginObject().name("key").value("value").endObject();
    when(request.getInputStream()).thenReturn(getAsInputStream(writer.toString()));

    RequestReader reader = new RequestReader(request);
    Result<List<DBObject>> resultData = reader.readResultDbObject();
    assertThat(resultData.isValid()).isTrue();
    assertThat(resultData.getData()).hasSize(1);
    assertThat(resultData.getData().get(0).get("key")).isEqualTo("value");
  }

  @Test public void decodeEmptyJsonRequest() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter.beginObject().endObject();
    when(request.getInputStream()).thenReturn(getAsInputStream(writer.toString()));

    RequestReader reader = new RequestReader(request);
    Result<List<DBObject>> resultData = reader.readResultDbObject();
    assertThat(resultData.isValid()).isTrue();
    assertThat(resultData.getData()).hasSize(1);
    assertThat(resultData.getData()).hasSize(1);
    assertThat(resultData.getData().get(0).keySet()).hasSize(0);
  }

  @Test public void invalidJsonRequest() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getInputStream()).thenReturn(getAsInputStream("{"));

    RequestReader reader = new RequestReader(request);
    Result<List<DBObject>> resultData = reader.readResultDbObject();
    assertThat(resultData.isValid()).isFalse();
    assertThat(resultData.getData()).isNull();
  }

  @Test public void readPlainText() throws Exception {
    String plainText = "Hello there";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getInputStream()).thenReturn(getAsInputStream(plainText));

    RequestReader reader = new RequestReader(request);
    assertThat(reader.readAsString()).isEqualTo(plainText);
  }

  @Test public void getTypeSafeTagObject() throws Exception {
    Long tag = 1000L;
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute("o")).thenReturn(tag);

    RequestReader reader = new RequestReader(request);
    assertThat(reader.getTaggedObject("o", Long.class)).isEqualTo(tag);
  }

  private ServletInputStream getAsInputStream(String value) {
    return new MockServletInputStream(new ByteArrayInputStream(value.getBytes(UTF_8)));
  }

  private class MockServletInputStream extends ServletInputStream {
    private final InputStream source;

    public MockServletInputStream(InputStream inputStream) {
      this.source = inputStream;
    }

    @Override public int read() throws IOException {
      return source.read();
    }

    @Override public boolean isFinished() {
      return false;
    }

    @Override public boolean isReady() {
      return false;
    }

    @Override public void setReadListener(ReadListener readListener) {

    }
  }
}
