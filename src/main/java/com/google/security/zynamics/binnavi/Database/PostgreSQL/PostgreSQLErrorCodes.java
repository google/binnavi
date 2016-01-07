/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

/**
 * Class which defines the used error codes from postgreSQL used in
 * com.google.security.zynamics.binnavi
 * 
 * The original source of the error codes:
 * http://www.postgresql.org/docs/9.0/static/errcodes-appendix.html
 * 
 */
public final class PostgreSQLErrorCodes {
  /**
   * Error code which is thrown if the database is not available.
   */
  public final static String POSTGRES_INVALID_CATALOG_NAME = "3D000";

  /**
   * Error code which is thrown if the password is invalid.
   */
  public final static String INVALID_PASSWORD = "28P01";

  /**
   * Error code which is thrown if the connection has gone away.
   */
  public final static String CONNECTION_DOES_NOT_EXIST = "08003";

  /**
   * Error code which is thrown if the socket has been closed.
   */
  public final static String CONNECTION_FAILURE = "08006";

  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLErrorCodes() {
    // You are not supposed to instantiate this class.
  }
}
