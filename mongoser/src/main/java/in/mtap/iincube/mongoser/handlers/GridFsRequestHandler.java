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

package in.mtap.iincube.mongoser.handlers;

import com.mongodb.gridfs.GridFSDBFile;
import in.mtap.iincube.mongoapi.FsClient;
import in.mtap.iincube.mongoapi.GridFsRequestBuilder;
import in.mtap.iincube.mongoser.codec.io.Meta;
import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.model.Status;

import java.io.IOException;

import static in.mtap.iincube.mongoser.handlers.DbRequestHandler.hasValidParams;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class GridFsRequestHandler {
  private final FsClient fsClient;
  private final RequestInterceptor interceptor;

  public GridFsRequestHandler(FsClient fsClient) {
    this.fsClient = fsClient;
    this.interceptor = RequestInterceptor.ALLOW_ALL;
  }

  public GridFsRequestHandler(FsClient fsClient, RequestInterceptor interceptor) {
    this.fsClient = fsClient;
    this.interceptor = interceptor;
  }

  public void doReadGrid(RequestReader reader, Response response) throws IOException {
    if (!hasValidParams(reader)) {
      response.send(SC_BAD_REQUEST, Status.get("dbname=null or colname=null").toJsonTree());
      return;
    }
    if (interceptor.isReadRestricted(reader.getDbName(), reader.getCollectionName())) {
      response.send(SC_BAD_REQUEST,
              Status.get("Not allowed to read into this namespace").toJsonTree());
      return;
    }
    if (!reader.hasParam("filename")) {
      response.send(SC_BAD_REQUEST, Status.get("filename=null").toJsonTree());
      return;
    }
    String dbname = reader.getDbName();
    String bucketname = reader.getCollectionName();
    String filename = reader.getUrlParameter("filename");

    GridFsRequestBuilder<GridFSDBFile> gridFsReader = fsClient.gridFsRead(dbname, bucketname);
    GridFSDBFile dbFile = gridFsReader.filename(filename).execute();

    response.send(SC_OK, Meta.NOCACHE, dbFile);
  }

  public void doWriteGrid(RequestReader reader, Response response) throws IOException {
    if (!hasValidParams(reader)) {
      response.send(SC_BAD_REQUEST,
          Status.get("dbname=null or colname=null").toJsonTree());
      return;
    }
    if (interceptor.isWriteRestricted(reader.getDbName(), reader.getCollectionName())) {
      response.send(SC_BAD_REQUEST,
              Status.get("Not allowed to write into this namespace").toJsonTree());
      return;
    }
    if (!reader.hasParam("filename")) {
      response.send(SC_BAD_REQUEST, Status.get("filename=null").toJsonTree());
      return;
    }
    String dbname = reader.getDbName();
    String bucketname = reader.getCollectionName();
    String filename = reader.getUrlParameter("filename");
    try {
      fsClient.gridFsWrite(dbname, bucketname).filename(filename)
          .filestream(reader.getInputStream())
          .contentType(reader.getContentType()).execute();
    } catch (IllegalArgumentException e) {
      response.send(SC_BAD_REQUEST, Status.get(e.getMessage()).toJsonTree());
      return;
    }

    response.send(SC_OK, Status.OK.toJsonTree());
  }

  public void doUpdateGrid(RequestReader reader, Response response) throws IOException {
    if (!hasValidParams(reader)) {
      response.send(SC_BAD_REQUEST,
          Status.get("dbname=null or colname=null").toJsonTree());
      return;
    }
    if (!reader.hasParam("filename")) {
      response.send(SC_BAD_REQUEST, Status.get("filename=null").toJsonTree());
      return;
    }
    String dbname = reader.getDbName();
    String bucketname = reader.getCollectionName();
    String filename = reader.getUrlParameter("filename");
    try {
      fsClient.gridFsUpdate(dbname, bucketname).filename(filename)
          .filestream(reader.getInputStream())
          .contentType(reader.getContentType()).execute();
    } catch (IllegalArgumentException e) {
      response.send(SC_BAD_REQUEST, Status.get("file with name = "
          + filename + " doesn't exists use PUT instead ").toJsonTree());
      return;
    }

    response.send(SC_OK, Status.OK.toJsonTree());
  }
}
