package in.mtap.iincube.mongoser.codec.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.Iterator;
import java.util.Set;

/**
 * Utility class to convert Mongo Java objects into Google Json (Gson API) objects.
 * <p>
 * This class has three APIs
 * <ul>
 * <li>Convert given Mongo BasicDBList object to Google Gson JsonArray object</li>
 * <li>Convert given Mongo BasicDBObject object to Google Gson JsonObject object</li>
 * <li>Convert given primitive data object such as Long, Double,
 * Boolean as well as String to Google Gson JsonPrimitive object</li>
 * </ul>
 *
 * The APIs are recursive and hence support nested objects.
 */
public final class Mongo2Gson {

  /**
   * Convert the given mongo BasicDBList object to JsonArray.
   *
   * @param object BasicDBList
   * @return JsonArray
   */
  public static JsonArray getAsJsonArray(DBObject object) {
    if (!(object instanceof BasicDBList)) {
      throw new IllegalArgumentException("Expected BasicDBList as argument type!");
    }
    BasicDBList list = (BasicDBList) object;
    JsonArray jsonArray = new JsonArray();
    for (int i = 0; i < list.size(); i++) {
      Object dbObject = list.get(i);
      if (dbObject instanceof BasicDBList) {
        jsonArray.add(getAsJsonArray((BasicDBList) dbObject));
      } else if (dbObject instanceof BasicDBObject) { // it's an object
        jsonArray.add(getAsJsonObject((BasicDBObject) dbObject));
      } else {   // it's a primitive type number or string
        jsonArray.add(getAsJsonPrimitive(dbObject, dbObject.toString()));
      }
    }
    return jsonArray;
  }

  /**
   * Convert the given mongo BasicDBObject to JsonObject.
   *
   * @param object BasicDBObject
   * @return JsonObject
   */
  public static JsonObject getAsJsonObject(DBObject object) {
    if (!(object instanceof BasicDBObject)) {
      throw new IllegalArgumentException("Expected BasicDBObject as argument type!");
    }
    BasicDBObject dbObject = (BasicDBObject) object;
    Set<String> keys = dbObject.keySet();
    Iterator<String> iterator = keys.iterator();
    JsonObject jsonObject = new JsonObject();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Object innerObject = dbObject.get(key);
      if (innerObject instanceof BasicDBList) {
        jsonObject.add(key, getAsJsonArray((BasicDBList) innerObject));
      } else if (innerObject instanceof BasicDBObject) {
        jsonObject.add(key, getAsJsonObject((BasicDBObject) innerObject));
      } else {
        jsonObject.add(key, getAsJsonPrimitive(innerObject, key));
      }
    }
    return jsonObject;
  }

  private static JsonElement getAsJsonElement(String key, JsonElement value) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(key, value);
    return jsonObject;
  }

  /**
   * Convert the given object to Json primitive JsonElement based on the type.
   *
   * @param value Object
   * @return JsonElement
   */
  public static JsonElement getAsJsonPrimitive(Object value, String key) {
    if (value instanceof String) {
      return new JsonPrimitive((String) value);
    } else if (value instanceof Character) {
      return new JsonPrimitive((Character) value);
    } else if (value instanceof Integer) {
      return new JsonPrimitive((Integer) value);
    } else if (value instanceof Long) {
      return new JsonPrimitive((Long) value);
    } else if (value instanceof Double) {
      return new JsonPrimitive((Double) value);
    } else if (value instanceof Boolean) {
      return new JsonPrimitive((Boolean) value);
    } else if (value instanceof ObjectId) {
      JsonObject idObject = new JsonObject();
      idObject.addProperty("$oid", value.toString());
      return idObject;
    } else {
      return new JsonPrimitive("Unsupported data type: " + value.getClass().getSimpleName());
    }
  }
}