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
 * Contains all error codes that are send when something goes wrong on the debug client side. For a
 * precise definition of the error codes please see the documentation of the debug client. The
 * information about the error codes is not duplicated here on purpose to avoid synchronization
 * problems between the documentation here and the documentation of the debug client.
 */
public final class DebuggerErrorCodes {
  /**
   * Error code: Success
   */
  public static final int SUCCESS = 0;

  /**
   * Error Code: Debug server could not be started.
   */
  public static final int COULDNT_START_SERVER = 1;

  /**
   * Error Code: Debug client could not connect to com.google.security.zynamics.binnavi.
   */
  public static final int COULDNT_CONNECT_TO_BINNAVI = 2;

  /**
   * Error Code: Connection closed.
   */
  public static final int CONNECTION_CLOSED = 3;

  /**
   * Error Code: There was a problem with the connection between the debug client and
   * com.google.security.zynamics.binnavi.
   */
  public static final int CONNECTION_ERROR = 4;

  /**
   * Error Code: Trying to access a memory page that is not writable.
   */
  public static final int PAGE_NOT_WRITABLE = 5;

  /**
   * Error Code: Memory could not be written to.
   */
  public static final int COULDNT_WRITE_MEMORY = 6;

  /**
   * Error Code: Debug mode could not be entered.
   */
  public static final int COULDNT_ENTER_DEBUG_MODE = 7;

  /**
   * Error Code: Target process could not be opened.
   */
  public static final int COULDNT_OPEN_TARGET_PROCESS = 8;

  /**
   * Error Code: Target process could not be debugged.
   */
  public static final int COULDNT_DEBUG_TARGET_PROCESS = 9;

  /**
   * Error Code: Memory page could not read.
   */
  public static final int PAGE_NOT_READABLE = 14;

  /**
   * Error messages for the individual error codes.
   */
  private static final String[] ERROR_MESSAGES = new String[] {"Success",
      "Could not start the debug server.", "The debugger could not connect to BinNavi.",
      "The connection to the debugger closed.", "Connection error.",
      "An accessed page is not writable.", "Could not write to the memory of the target process.",
      "The debugger could not enter the debug mode.",
      "The debugger could not open the target process.",
      "The debugger could not debug the target process.", "An accessed page is not readable."};

  /**
   * You are not supposed to instantiate this class.
   */
  private DebuggerErrorCodes() {
  }


  /**
   * Converts an error code into a printable message.
   *
   * @param code The error code.
   *
   * @return The printable string of the error code.
   */
  public static String codeToMessage(final int code) {
    if (code >= 0 && code < ERROR_MESSAGES.length) {
      return ERROR_MESSAGES[code];
    }

    return String.format("Unknown error (Code %d)", code);
  }
}
