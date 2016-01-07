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
package com.google.security.zynamics.binnavi.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default logger used to log all BinNavi log messages.
 */
public final class NaviLogger {
  /**
   * Logger object used for logging.
   */
  private static final Logger log = Logger.getLogger("BinNavi");

  static {
    log.setLevel(Level.SEVERE);
  }

  /**
   * You are not supposed to instantiate this class.
   */
  private NaviLogger() {
  }

  /**
   * Adds a new log message handler.
   * 
   * @param handler The log message handler to add.
   */
  public static void addHandler(final Handler handler) {
    log.addHandler(handler);
  }

  /**
   * Logs a message with log level INFO.
   * 
   * @param string The message.
   * @param args Additional format strings of the message.
   */
  public static void info(final String string, final Object... args) {
    if (log.isLoggable(Level.INFO)) {
      log.info(String.format(string, args));
    }
  }

  /**
   * Changes the log level that decides what messages are logged.
   * 
   * @param level The new log level.
   */
  public static void setLevel(final Level level) {
    log.setLevel(level);
  }

  /**
   * Logs a message with log level SEVERE.
   * 
   * @param string The message.
   * @param args Additional format strings of the message.
   */
  public static void severe(final String string, final Object... args) {
    if (log.isLoggable(Level.SEVERE)) {
      log.severe(String.format(string, args));
    }
  }

  /**
   * Logs a message with log level WARNING.
   * 
   * @param string The message.
   * @param args Additional format strings of the message.
   */
  public static void warning(final String string, final Object... args) {
    if (log.isLoggable(Level.WARNING)) {
      log.warning(String.format(string, args));
    }
  }
}
