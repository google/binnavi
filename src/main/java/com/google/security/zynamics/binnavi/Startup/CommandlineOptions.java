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

/**
 * Class that keeps track of the commandline options
 */
public final class CommandlineOptions {
  /**
   * String of the batch plugin to execute.
   */
  private String m_batchPlugin;

  /**
   * Flag that indicates that verbose logging mode should be switched on.
   */
  private boolean m_verboseMode;

  /**
   * Flag that indicates that very verbose logging mode should be switched on.
   */
  private boolean m_veryVerboseMode;

  /**
   * Returns the name of the batch plugin to execute.
   * 
   * @return The name of the batch plugin to execute. This value can be null.
   */
  public String getBatchPlugin() {
    return m_batchPlugin;
  }

  /**
   * Returns the flag that indicates whether verbose mode should be switched on.
   * 
   * @return The flag that indicates whether verbose mode should be switched on.
   */
  public boolean isVerboseMode() {
    return m_verboseMode;
  }

  /**
   * Returns the flag that indicates whether very verbose mode should be switched on.
   * 
   * @return The flag that indicates whether very verbose mode should be switched on.
   */
  public boolean isVeryVerboseMode() {
    return m_veryVerboseMode;
  }

  /**
   * Sets the name of the batch plugin to execute.
   * 
   * @param pluginName Name of the batch plugin.
   */
  public void setBatchPlugin(final String pluginName) {
    Preconditions.checkNotNull(pluginName, "IE00843: Plugin name argument can not be null");

    m_batchPlugin = pluginName;
  }

  /**
   * Sets the flag that enables verbose mode.
   */
  public void setVerboseMode() {
    m_verboseMode = true;
  }

  /**
   * Sets the flag that enables very verbose mode.
   */
  public void setVeryVerboseMode() {
    m_veryVerboseMode = true;
  }
}
