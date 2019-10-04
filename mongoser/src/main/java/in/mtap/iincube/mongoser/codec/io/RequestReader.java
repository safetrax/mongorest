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

import com.mongodb.DBObject;
import in.mtap.iincube.mongoser.codec.JsonArrayDecoder;
import in.mtap.iincube.mongoser.codec.Result;
import in.mtap.iincube.mongoser.codec.SimpleLineDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clean API to extract Mongo db request from {@linkplain HttpServletRequest}
 * not threadsafe
 */
public class RequestReader {
  private static final String EXTRA_READER = "in.mtap.iincube.mongoser.codec.io.$reader";
  private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)(?:/([^/]+))?/?$");
  private static final Logger LOG = Logger.getLogger(RequestReader.class.getSimpleName());
  private final HttpServletRequest request;
  /** order matters */
  private final Decoder[] decoders = new Decoder[]{
      new JsonArrayDecoder(), new SimpleLineDecoder()
  };
  private String[] paths;
  private StringBuilder requestBody = new StringBuilder();
  private InputStream inputStream;

  RequestReader(HttpServletRequest request) {
    this.request = request;
    try {
      this.inputStream = request.getInputStream();
    } catch (IOException e) {
      LOG.warning("Input stream error : "
          + request.getMethod() + " -- " + request.getServletPath());
    }
  }

  /**
   * Uses key={@value #EXTRA_READER} to store RequestReader instance as attribute
   * in {@link HttpServletRequest#setAttribute(String, Object)}
   * to reuse the request reader created for each servlet request.
   * <p>
   * Note: do not use {@value #EXTRA_READER}
   * in {@link HttpServletRequest#setAttribute(String, Object)}
   */
  public static RequestReader from(HttpServletRequest request) {
    if (request.getAttribute(EXTRA_READER) != null) {
      return (RequestReader) request.getAttribute(EXTRA_READER);
    }
    RequestReader requestReader = new RequestReader(request);
    request.setAttribute(EXTRA_READER, requestReader);
    return requestReader;
  }

  private void addDataToDecoders(String data) {
    requestBody.append(data);
    for (Decoder decoder : decoders) {
      decoder.addData(data);
    }
  }

  private List<DBObject> extractDataFromDecoders() {
    for (Decoder decoder : decoders) {
      if (decoder.isValid())
        return decoder.getAsDBObject();
    }
    throw new IllegalArgumentException(
        "Couldn't extract data from the request by any available decoders");
  }

  private void closeDecoders() {
    for (Decoder decoder : decoders) {
      decoder.close();
    }
  }

  /**
   * Performs fresh io read on every call hence save the result
   * Also result will be treated as immutable changing the contents will not affect the next call
   * <p>
   * Note: Will not fail on IO error check {@link Result#isValid()}
   * to check if the extraction is successful
   */
  public Result<List<DBObject>> readResultDbObject() {
    try {
      Result<List<DBObject>> result = new Result<>(decodeAsDBObject(), true,
          requestBody.toString());
      requestBody.setLength(0);
      return result;
    } catch (Exception e) {
      return new Result<>(null, false);
    }
  }

  public InputStream getInputStream() throws IOException {
    return inputStream;
  }

  private List<DBObject> decodeAsDBObject() throws IOException {
    InputStream stream = getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String strData = reader.readLine();
    while (strData != null) {
      addDataToDecoders(strData);
      strData = reader.readLine();
    }
    List<DBObject> data = extractDataFromDecoders();
    closeDecoders();
    reader.close();
    return data;
  }

  /** read input stream as plain text */
  public String readAsString() throws IOException {
    InputStream stream = getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    StringBuilder resultData = new StringBuilder();
    String strData = reader.readLine();
    while (strData != null) {
      resultData.append(strData);
      strData = reader.readLine();
    }
    reader.close();
    return resultData.toString();
  }

  /** Supports only reading url param dbname=value */
  public String getDbName() {
    return request.getParameter("dbname");
  }

  /** Supports only reading url param colname=value */
  public String getCollectionName() {
    return request.getParameter("colname");
  }

  public int getParameterAsInt(String name) throws NumberFormatException {
    String paramValue = getUrlParameter(name);
    if (paramValue != null)
      return Integer.parseInt(paramValue);
    return -1;
  }

  @Deprecated public boolean getAsBoolean(String name) {
    return getParameterAsBoolean(name);
  }

  public boolean getParameterAsBoolean(String name) {
    String paramValue = getUrlParameter(name);
    if (paramValue != null)
      return Boolean.parseBoolean(paramValue);
    return false;
  }

  public Object getTaggedObject(String name) {
    return request.getAttribute(name);
  }

  public <T> T getTaggedObject(String name, Class<T> cls) {
    return (T) request.getAttribute(name);
  }

  /**
   * Gets appended url path information in {@code String[]}
   * <p>
   * Note: Max path decoding capacity is 2
   */
  @Deprecated public String[] getPathInfo() {
    if (paths != null)
      return paths;
    String pathInfo = request.getPathInfo();
    if (pathInfo == null)
      return null;
    Matcher matcher = PATH_PATTERN.matcher(pathInfo);
    if (matcher.find()) {
      paths = new String[2];
      paths[0] = matcher.group(1);
      paths[1] = matcher.group(2);
    }
    return paths;
  }

  public String getPath() {
    return request.getPathInfo();
  }

  public String getUrlParameter(String name) {
    return request.getParameter(name);
  }

  public File getFilePart(String fileKey) throws IOException, ServletException {
    Part filePart = request.getPart(fileKey);
    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
    InputStream is = filePart.getInputStream();

    File file = new File("/tmp/" + fileName);
    FileOutputStream fos = new FileOutputStream(file);
    int read = 0;
    byte[] bytes = new byte[1024];
    while ((read = is.read(bytes)) != -1) {
      fos.write(bytes, 0, read);
    }
    is.close();
    fos.close();
    return file;
  }

  public String getContentType() {
    return request.getContentType();
  }

  public boolean hasParam(String name) {
    return request.getParameter(name) != null;
  }

  public interface Decoder {
    void addData(String data);

    boolean isValid();

    List<DBObject> getAsDBObject();

    void close();
  }
}
