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
package com.google.security.zynamics.binnavi.debug.debugger;

/**
 * Exception class that is used to signal that something went wrong during debugging. This class is
 * used as a wrapper class for more concrete exceptions like IOException or others.
 */
public final class DebugExceptionWrapper extends Exception {

  /**
   * The more concrete exception that is wrapped.
   */
  private final Exception exception;

  /**
   * Creates a new debug exception object.
   *
   * @param exception The more concrete exception that is wrapped.
   */
  public DebugExceptionWrapper(final Exception exception) {
    super(exception);
    this.exception = exception;
  }

  /**
   * Creates a new debug exception object.
   *
   * @param message The detail exception message.
   */
  public DebugExceptionWrapper(final String message) {
    super(message);
    exception = null;
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    return exception == null ? super.getStackTrace() : exception.getStackTrace();
  }
}
