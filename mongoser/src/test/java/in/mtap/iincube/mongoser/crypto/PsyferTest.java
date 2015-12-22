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

package in.mtap.iincube.mongoser.crypto;

import in.mtap.iincube.mongoser.codec.crypto.Psyfer;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class PsyferTest {
  private Psyfer newPsyfer(String key) throws Exception {
    return Psyfer.getInstance(key);
  }

  @Test public void withValidKey() throws Exception {
    Psyfer psyfer = newPsyfer("secretKey");
    assertThat(psyfer.decrypt(psyfer.encrypt("this"))).isEqualTo("this");
  }

  @Test(expected = RuntimeException.class)
  public void withInvalidKey() throws Exception {
    Psyfer psyfer = newPsyfer("secretKey");
    String encrypted = psyfer.encrypt("something");
    Psyfer withInvalidKey = newPsyfer("secretkey");
    assertThat("something").isNotEqualTo(withInvalidKey.decrypt(encrypted));
  }
}
