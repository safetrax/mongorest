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

package in.mtap.iincube.mongoser.auth;

import in.mtap.iincube.mongoser.codec.io.RequestReader;
import in.mtap.iincube.mongoser.codec.io.Response;
import in.mtap.iincube.mongoser.servlet.MongoserServlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

public interface AuthFactory {
  /** recommended path to be used for auth servlet registration */
  String AUTH_PATH = "/auth";

  Filter getAuthFilter();

  HttpServlet getAuthServlet();

  /** No security */
  AuthFactory NONE = new AuthFactory() {
    @Override public Filter getAuthFilter() {
      return new Filter() {
        @Override public void init(FilterConfig filterConfig) throws ServletException {

        }

        @Override public void doFilter(ServletRequest request, ServletResponse response,
                                       FilterChain chain) throws IOException, ServletException {
          chain.doFilter(request, response);
        }

        @Override public void destroy() {

        }
      };
    }

    @Override public HttpServlet getAuthServlet() {
      return new MongoserServlet() {
        @Override protected void doGet(RequestReader reader, Response writer) throws IOException {
          super.doGet(reader, writer);
        }
      };
    }
  };
}
