package in.mtap.iincube.mongoapi.internal;

public class Utility {

  public static void assertNotNull(Object object, String message) {
    if (object == null)
      error(message);
  }

  /** @throws IllegalArgumentException exception */
  public static void error(String msg) {
    throw new IllegalArgumentException(msg);
  }
}
