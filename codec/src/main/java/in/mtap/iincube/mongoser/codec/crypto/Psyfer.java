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

package in.mtap.iincube.mongoser.codec.crypto;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Cipher API that uses AES for easy encryption and decryption
 */
public final class Psyfer {
  private final Cipher eCipher;
  private final Cipher deCiper;
  private final Base64 base64 = new Base64();

  Psyfer(Cipher eCipher, Cipher deCiper) {
    this.eCipher = eCipher;
    this.deCiper = deCiper;
  }

  /** @return base64 encoded format of AES encrypted form of the @param data */
  public String encrypt(String data) {
    try {
      byte[] enc = eCipher.doFinal(data.getBytes("UTF-8"));
      return base64.encodeAsString(enc);
    } catch (Exception e) {
      throw new RuntimeException("[" + e.getClass().getSimpleName() + "] @ encrypt: "
          + e.getMessage());
    }
  }

  /**
   * Performs Base64 decode to extract RAW bytes, and does a decipher and returns UTF-8 string
   */
  public String decrypt(String data) {
    try {
      byte[] decoded = base64.decode(data);
      return new String(deCiper.doFinal(decoded), "UTF-8");
    } catch (Exception e) {
      // temporarily suppressing all exceptions under RuntimeException.
      throw new RuntimeException("[" + e.getClass().getSimpleName() + "] @ decrypt: "
          + e.getMessage());
    }
  }

  public static Psyfer getInstance(String secretKey)
      throws NoSuchAlgorithmException, UnsupportedEncodingException,
      NoSuchPaddingException, InvalidKeyException {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    byte[] key = digest.digest(secretKey.getBytes("UTF-8"));
    key = Arrays.copyOf(key, 16);
    SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
    Cipher eCipher = Cipher.getInstance("AES");
    Cipher deCipher = Cipher.getInstance("AES");
    eCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
    deCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
    return new Psyfer(eCipher, deCipher);
  }
}
