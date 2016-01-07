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
package com.google.security.zynamics.binnavi.API.debug;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerExceptionHandlingAction;

public class DebuggerDebugException {

  private final DebuggerException debugException;

  public DebuggerDebugException(final DebuggerException debugException) {
    this.debugException =
        Preconditions
            .checkNotNull(debugException, "Error: debugException argument can not be null");
  }

  public DebuggerDebugExceptionHandlingAction getExceptionAction() {
    return DebuggerDebugExceptionHandlingAction.convertTo(debugException.getExceptionAction());
  }

  public long getExceptionCode() {
    return debugException.getExceptionCode();
  }

  public String getExceptionName() {
    return debugException.getExceptionName();
  }

  public DebuggerException getNative() {
    return debugException;
  }

  enum DebuggerDebugExceptionHandlingAction {
    Continue, Halt, Ignore;
    public static DebuggerDebugExceptionHandlingAction convertTo(
        final DebuggerExceptionHandlingAction action) {
      switch (action) {
        case Continue:
          return Continue;
        case Halt:
          return Halt;
        case Ignore:
          return Ignore;
        default:
          return Continue;
      }
    }

    public static DebuggerExceptionHandlingAction convertBack(
        final DebuggerDebugExceptionHandlingAction action) {
      switch (action) {
        case Continue:
          return DebuggerExceptionHandlingAction.Continue;
        case Halt:
          return DebuggerExceptionHandlingAction.Halt;
        case Ignore:
          return DebuggerExceptionHandlingAction.Ignore;
        default:
          return DebuggerExceptionHandlingAction.Continue;
      }
    }
  }
}
