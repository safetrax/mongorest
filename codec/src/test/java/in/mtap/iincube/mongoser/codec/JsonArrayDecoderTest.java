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

package in.mtap.iincube.mongoser.codec;

import com.google.gson.stream.JsonWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;

import static com.google.common.truth.Truth.assertThat;

public class JsonArrayDecoderTest {
  private JsonArrayDecoder jsonArrayDecoder = new JsonArrayDecoder();

  @After public void tearDown() {
    jsonArrayDecoder.close();
  }

  @Test public void handleJsonEntries() throws Exception {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);
    jsonWriter.beginObject().name("key").value("value").endObject();

    jsonArrayDecoder.addData(out.toString());
    assertThat(jsonArrayDecoder.isValid()).isTrue();
  }

  @Test public void handleJsonArrayEntries() throws Exception {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);
    jsonWriter.beginArray().beginObject().name("key1").value("value1").endObject()
        .beginObject().name("key2").value("value2").endObject().endArray();
    jsonArrayDecoder.addData(out.toString());
    assertThat(jsonArrayDecoder.isValid()).isTrue();
    assertThat(jsonArrayDecoder.getAsDBObject()).hasSize(2);
  }

  @Test public void supportStreamInputAndPostProcess() throws Exception {
    jsonArrayDecoder.addData("{\"key\"");
    jsonArrayDecoder.addData(":\"value\"");
    jsonArrayDecoder.addData("}");
    assertThat(jsonArrayDecoder.isValid()).isTrue();
    assertThat(jsonArrayDecoder.getAsDBObject()).hasSize(1);
  }

  @Test public void supportEmptyObject() throws Exception {
    jsonArrayDecoder.addData("{}");
    assertThat(jsonArrayDecoder.isValid()).isTrue();
    assertThat(jsonArrayDecoder.getAsDBObject()).hasSize(1);
  }

  @Test public void handleInvalidJsonEntries() {
    jsonArrayDecoder.addData("a");
    assertThat(jsonArrayDecoder.isValid()).isFalse();
  }

  @Test public void throwOnForceFetchIfInvalid() {
    String invalidJson = "not json";
    jsonArrayDecoder.addData(invalidJson);
    assertThat(jsonArrayDecoder.isValid()).isFalse();
    try {
      jsonArrayDecoder.getAsDBObject();
      Assert.fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Parse error invalid json: \n" + invalidJson);
    }
  }

  @Test public void invalidWhenNoData() {
    assertThat(jsonArrayDecoder.isValid()).isFalse();
  }
}
