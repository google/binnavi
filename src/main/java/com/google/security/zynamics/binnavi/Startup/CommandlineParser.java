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
package com.google.security.zynamics.binnavi.Startup;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;

import java.util.logging.Level;

/**
 * Parses command lines passed to BinNavi.
 */
public final class CommandlineParser {
  /**
   * You are not supposed to instantiate this class.
   */
  private CommandlineParser() {}

  /**
   * Processes the parsed command line options.
   *
   * @param commandLine
   */
  private static void processCommandLineOptions(final CommandlineOptions commandLine) {
    if (commandLine.isVeryVerboseMode()) {
      NaviLogger.setLevel(Level.ALL);
    } else if (commandLine.isVerboseMode()) {
      NaviLogger.setLevel(Level.INFO);
    }
  }

  /**
   * Parses the command line arguments passed to BinNavi.
   *
   * @param arguments The command line arguments passed to BinNavi.
   *
   * @return The command line options parsed from the arguments.
   */
  public static CommandlineOptions parseCommandLine(final String[] arguments) {
    Preconditions.checkNotNull(arguments, "IE02087: Arguments argument can not be null");

    final CommandlineOptions options = new CommandlineOptions();

    for (final String argument : arguments) {
      if ("-v".equals(argument)) {
        options.setVerboseMode();
      } else if ("-vv".equals(argument)) {
        options.setVeryVerboseMode();
      } else if (argument.startsWith("-X:")) {
        options.setBatchPlugin(arguments[0].substring(3));
      }
    }

    processCommandLineOptions(options);

    return options;
  }
}
