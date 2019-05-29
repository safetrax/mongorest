package in.mtap.iincube.mongoser.handlers;

import java.util.List;

public class DataBaseAccessChecker implements RequestInterceptor {
  private List<String> readRestrictedCollections;
  private List<String> writeRestrictedCollections;

  public DataBaseAccessChecker(List<String> readRestrictedCollections,
                                List<String> writeRestrictedCollections) {
    this.readRestrictedCollections = readRestrictedCollections;
    this.writeRestrictedCollections = writeRestrictedCollections;
  }

  @Override public boolean isReadRestricted(String dbName, String colName) {
    return readRestrictedCollections.contains(colName);
  }

  @Override public boolean isWriteRestricted(String dbName, String colName) {
    return writeRestrictedCollections.contains(colName);
  }
}
