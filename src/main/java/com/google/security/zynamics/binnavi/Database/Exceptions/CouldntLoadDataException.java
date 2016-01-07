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
 * Exception used to signal problems while loading data.
 */
public final class CouldntLoadDataException extends Exception {

  /**
   * Stack trace of the original exception.
   */
  private final StackTraceElement[] m_stacktrace;

  /**
   * Creates a new exception object.
   * 
   * @param exception The cause of the exception.
   */
  public CouldntLoadDataException(final Exception exception) {
    super(exception);

    m_stacktrace = exception.getStackTrace();
  }

  /**
   * Creates a new exception object.
   * 
   * @param msg The cause of the exception.
   */
  public CouldntLoadDataException(final String msg) {
    super(msg);

    m_stacktrace = super.getStackTrace();
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    return m_stacktrace.clone();
  }
}
