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
package com.google.security.zynamics.binnavi.Database.Exceptions;

/**
 * Exception used to signal connection problems to the SQL database.
 */
public final class CouldntConnectException extends Exception {

  /**
   * Stack trace of the original exception.
   */
  private final StackTraceElement[] m_stackStacktrace;

  /**
   * Specific error code for the connection problems.
   */
  private final int m_errorCode;

  /**
   * Indicates the SQL error state.
   */
  private final String m_sqlState;

  /**
   * Creates a new exception object.
   * 
   * @param exception Cause of the exception.
   * @param errorCode Specific error code for the connection problems.
   */
  public CouldntConnectException(final Exception exception, final int errorCode,
      final String sqlState) {
    super(exception);

    m_errorCode = errorCode;
    m_sqlState = sqlState;
    m_stackStacktrace = exception.getStackTrace();
  }

  /**
   * Returns the error code of the connection problems.
   * 
   * @return The error code of the connection problems.
   */
  public int getErrorCode() {
    return m_errorCode;
  }

  /**
   * Returns the sql error state.
   * 
   * @return The sql error state string.
   */
  public String getSqlState() {
    return m_sqlState;
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    return m_stackStacktrace.clone();
  }
}
