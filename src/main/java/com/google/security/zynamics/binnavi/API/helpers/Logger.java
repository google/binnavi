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
package com.google.security.zynamics.binnavi.API.helpers;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;

// / Used to log messages.
/**
 * This class can be used to log exceptions to the default BinNavi log.
 */
public final class Logger {
  /**
   * The default constructor is private because you are not supposed to instantiate this class.
   */
  private Logger() {
    // You are not supposed to instantiate this class
  }

  // ! Logs a string at log level INFO.
  /**
   * Logs a string at log level INFO.
   *
   * @param message The string to log (including format specifiers).
   * @param objects The objects used to fill the format specifiers.
   */
  public static void info(final String message, final Object... objects) {
    NaviLogger.info(message, objects);
  }

  // ! Logs an exception.
  /**
   * Logs an exception to the BinNavi log.
   *
   * @param exception The exception to log.
   */
  public static void logException(final Exception exception) {
    CUtilityFunctions.logException(exception);
  }

  // ! Logs a string at log level SEVERE.
  /**
   * Logs a string at log level SEVERE.
   *
   * @param message The string to log (including format specifiers).
   * @param objects The objects used to fill the format specifiers.
   */
  public static void severe(final String message, final Object... objects) {
    NaviLogger.severe(message, objects);
  }

  // ! Logs a string at log level WARNING.
  /**
   * Logs a string at log level WARNING.
   *
   * @param message The string to log (including format specifiers).
   * @param objects The objects used to fill the format specifiers.
   */
  public static void warning(final String message, final Object... objects) {
    NaviLogger.warning(message, objects);
  }
}
