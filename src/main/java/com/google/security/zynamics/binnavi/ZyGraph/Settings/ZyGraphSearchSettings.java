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
package com.google.security.zynamics.binnavi.ZyGraph.Settings;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Contains search-related graph settings.
 */
public final class ZyGraphSearchSettings {
  /**
   * Flag that determines whether search operations only operate on visible nodes.
   */
  private boolean m_searchVisibleNodesOnly;

  /**
   * Flag that determines whether search operations only operate on selected nodes.
   */
  private boolean m_searchSelectedNodesOnly;

  /**
   * Flag that determines whether search operations are case sensitive.
   */
  private boolean m_searchCaseSensitive;

  /**
   * Flag that determines whether search operations search for regular expressions.
   */
  private boolean m_searchRegEx;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private final GraphSettingsConfigItem m_type;

  /**
   * Listeners that are notified about changes in the graph settings.
   */
  private final ListenerProvider<IZyGraphSearchSettingsListener> m_listeners =
      new ListenerProvider<IZyGraphSearchSettingsListener>();

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphSearchSettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE00873: Type argument can't be null");

    m_type = type;
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphSearchSettings(final ZyGraphSearchSettings settings) {
    m_type = null;

    m_searchCaseSensitive = settings.getSearchCaseSensitive();
    m_searchRegEx = settings.getSearchRegEx();
    m_searchSelectedNodesOnly = settings.getSearchSelectedNodesOnly();
    m_searchVisibleNodesOnly = settings.getSearchVisibleNodesOnly();
  }

  /**
   * Adds a listener object that is notified about changes in the search settings.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IZyGraphSearchSettingsListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the current case sensitive search setting.
   *
   * @return The current case sensitive search setting.
   */
  public boolean getSearchCaseSensitive() {
    return m_type == null ? m_searchCaseSensitive : m_type.isCaseSensitiveSearch();
  }

  /**
   * Returns the current regular expression search setting.
   *
   * @return The current regular expression search setting.
   */
  public boolean getSearchRegEx() {
    return m_type == null ? m_searchRegEx : m_type.isRegexSearch();
  }

  /**
   * Returns the current selected nodes only search setting.
   *
   * @return The current selected nodes only search setting.
   */
  public boolean getSearchSelectedNodesOnly() {
    return m_type == null ? m_searchSelectedNodesOnly : m_type.isSearchSelectedNodesOnly();
  }

  /**
   * Returns the current visible nodes only search setting.
   *
   * @return The current visible nodes only search setting.
   */
  public boolean getSearchVisibleNodesOnly() {
    return m_type == null ? m_searchVisibleNodesOnly : m_type.isSearchVisibleNodesOnly();
  }

  /**
   * Removes a previously attached listener object.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IZyGraphSearchSettingsListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the current case sensitive search setting.
   *
   * @param value The new value of the case sensitive search setting.
   */
  public void setSearchCaseSensitive(final boolean value) {
    if (value == getSearchCaseSensitive()) {
      return;
    }

    if (m_type == null) {
      m_searchCaseSensitive = value;
    } else {
      m_type.setCaseSensitiveSearch(value);
    }

    for (final IZyGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedSearchCaseSensitive(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current regular expression search setting.
   *
   * @param value The new value of the regular expression search setting.
   */
  public void setSearchRegEx(final boolean value) {
    if (value == getSearchRegEx()) {
      return;
    }

    if (m_type == null) {
      m_searchRegEx = value;
    } else {
      m_type.setRegexSearch(value);
    }

    for (final IZyGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedSearchRegEx(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current search selected nodes only setting.
   *
   * @param value The new value of the search selected nodes only setting.
   */
  public void setSearchSelectedNodesOnly(final boolean value) {
    if (value == getSearchSelectedNodesOnly()) {
      return;
    }

    if (m_type == null) {
      m_searchSelectedNodesOnly = value;
    } else {
      m_type.setSearchSelectedNodesOnly(value);
    }

    for (final IZyGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedSearchSelectionNodesOnly(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current search visible nodes only setting.
   *
   * @param value The new value of the search visible nodes only setting.
   */
  public void setSearchVisibleNodesOnly(final boolean value) {
    if (value == getSearchVisibleNodesOnly()) {
      return;
    }

    if (m_type == null) {
      m_searchVisibleNodesOnly = value;
    } else {
      m_type.setSearchVisibleNodesOnly(value);
    }

    for (final IZyGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedSearchVisibleNodesOnly(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
