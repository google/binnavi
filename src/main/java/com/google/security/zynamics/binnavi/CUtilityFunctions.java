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
package com.google.security.zynamics.binnavi;

import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.zylib.general.StackTrace;



/**
 * This class provides a collection of static helper functions.
 *
 * Justification: Certain small helper functions are needed from many places in the project.
 */
public final class CUtilityFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CUtilityFunctions() {
  }

  /**
   * Creates a description string that is shown in error dialogs.
   *
   * @param description Description of the problem.
   * @param causes Potential causes of the problem.
   * @param impacts Impacts of the problem on the system.
   *
   * @return The created description string.
   */
  public static String createDescription(final String description, final String[] causes,
      final String[] impacts) {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(description);
    stringBuilder.append('\n');

    if (causes.length != 0) {
      stringBuilder.append('\n');
      stringBuilder.append("Possible causes" + ":\n");

      for (final String cause : causes) {
        stringBuilder.append("  - ");
        stringBuilder.append(cause);
        stringBuilder.append('\n');
      }
    }

    if (impacts.length != 0) {
      stringBuilder.append('\n');
      stringBuilder.append("Problem impact" + ":\n");

      for (final String impact : impacts) {
        stringBuilder.append("  - ");
        stringBuilder.append(impact);
        stringBuilder.append('\n');
      }
    }

    return stringBuilder.toString();
  }

  /**
   * Logs an exception to the default log file.
   *
   * @param exception The exception to log.
   */
  public static void logException(final Throwable exception) {
    NaviLogger.severe("Reason" + ": " + exception.getLocalizedMessage());

    NaviLogger.severe(StackTrace.toString(exception.getStackTrace()));
  }
}
