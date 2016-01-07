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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.common.config.ConfigHelper;
import com.google.security.zynamics.zylib.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



/**
 * Action class used to log what actions a user executed.
 */
public final class CActionLogger {
  /**
   * Keeps track how often actions are executed.
   */
  private static final Map<Long, Integer> m_countMap = new LinkedHashMap<Long, Integer>();

  /**
   * File where the action statistics are stored.
   */
  private static final File m_actionsLogFile = new File(
      ConfigHelper.getConfigurationDirectory(
          Constants.COMPANY_NAME, Constants.PROJECT_NAME) + "actions.log");

  /**
   * You are not supposed to instantiate this class.
   */
  private CActionLogger() {
  }

  /**
   * Loads the action statistics from the action statistics file.
   *
   * @param actionsLogFile The actions log file to load.
   *
   * @throws IOException if any IO error occurs.
   */
  private static void load(final File actionsLogFile) throws IOException {
    final List<String> lines = FileUtils.readTextfileLines(actionsLogFile);

    for (final String line : lines) {
      final String[] parts = line.split(":");

      if (parts.length == 2) {
        m_countMap.put(Long.valueOf(parts[0]), Integer.valueOf(parts[1]));
      }
    }
  }

  /**
   * Converts the action statistics map to a printable string.
   *
   * @return Printable string that shows how often actions were executed.
   */
  private static String mapToText() {
    final StringBuffer buffer = new StringBuffer();

    for (final Map.Entry<Long, Integer> entry : m_countMap.entrySet()) {
      buffer.append(String.format("%d:%d%n", entry.getKey(), entry.getValue()));
    }

    return buffer.toString();
  }

  /**
   * Loads existing actions log file if it exists.
   */
  public static void load() {
    if (m_actionsLogFile.exists()) {
      try {
        load(m_actionsLogFile);
      } catch (final IOException e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Logs the execution of a given action.
   *
   * @param actionId Identifier of the executed action.
   */
  public static void log(final long actionId) {
    if (!m_countMap.containsKey(actionId)) {
      m_countMap.put(actionId, 0);
    }

    m_countMap.put(actionId, m_countMap.get(actionId) + 1);

    try {
      FileUtils.writeTextFile(m_actionsLogFile, mapToText());
    } catch (final IOException e) {
      CUtilityFunctions.logException(e);
    }
  }
}
