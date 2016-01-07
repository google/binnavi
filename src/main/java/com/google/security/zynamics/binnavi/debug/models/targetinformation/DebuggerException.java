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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

import com.google.common.base.Preconditions;

/**
 * Represents a platform specific exception which is received from a debugger client
 */
public final class DebuggerException {
  /**
   * The exception code used by the specific platform
   */
  private final long exceptionCode;

  /**
   * The corresponding string representation of the exception
   */
  private final String name;

  /**
   * Specifies whether the exception should be ignored by the debugger
   */
  private final DebuggerExceptionHandlingAction exceptionAction;

  /**
   * Creates a new platform exception object
   *
   * @param exceptionName The corresponding name of the exception
   * @param exceptionCode The exception code
   * @param exceptionAction THe action which should be performed by the debugger in response to this
   *        exception
   */
  public DebuggerException(final String exceptionName, final long exceptionCode,
      final DebuggerExceptionHandlingAction exceptionAction) {
    Preconditions.checkArgument(!exceptionName.isEmpty(),
        "IE00181: Exception name can not be empty");
    this.name = Preconditions.checkNotNull(exceptionName,
        "IE00047: exceptionName argument can not be null");
    this.exceptionCode = exceptionCode;
    this.exceptionAction = Preconditions.checkNotNull(exceptionAction,
        "IE02119: exceptionAction argument can not be null");
  }

  /**
   * Creates a string representation which can be used as a unique key for storage in the per-module
   * database table.
   *
   * @param exception The instance from which to derive the key.
   * @param debuggerId The Id of the currently used debugger.
   *
   * @return The string representation for the key
   */
  public static String getSettingKey(final DebuggerException exception, final int debuggerId) {
    return "dbg_" + debuggerId + "_exception_" + exception.getExceptionCode();
  }

  public DebuggerExceptionHandlingAction getExceptionAction() {
    return exceptionAction;
  }

  public long getExceptionCode() {
    return exceptionCode;
  }

  public String getExceptionName() {
    return name;
  }
}
