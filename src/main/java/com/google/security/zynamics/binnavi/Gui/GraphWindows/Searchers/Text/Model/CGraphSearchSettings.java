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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;



/**
 * Encapsulates possible search settings.
 */
public final class CGraphSearchSettings {
  /**
   * Flag for enabling or disabling regular expression search.
   */
  private boolean m_isRegEx;

  /**
   * Flag for enabling or disabling case sensitive search.
   */
  private boolean m_isCaseSensitive;

  /**
   * Flag for enabling or disabling searching only selected nodes.
   */
  private boolean m_onlySelected;

  /**
   * Flag for enabling or disabling searching only visible nodes.
   */
  private boolean m_onlyVisible;

  /**
   * Listeners that are notified about changes in the search settings.
   */
  private final ListenerProvider<IGraphSearchSettingsListener> m_listeners =
      new ListenerProvider<IGraphSearchSettingsListener>();

  /**
   * Adds a listener that is notified about changes in the graph settings.
   *
   * @param listener The listener to add.
   */
  public void addListener(final IGraphSearchSettingsListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns whether case sensitive search is enabled or disabled.
   *
   * @return True, if case sensitive search is enabled. False, if it is disabled.
   */
  public boolean isCaseSensitive() {
    return m_isCaseSensitive;
  }

  /**
   * Returns whether selected nodes search is enabled or disabled.
   *
   * @return True, if selected nodes search is enabled. False, if it is disabled.
   */
  public boolean isOnlySelected() {
    return m_onlySelected;
  }

  /**
   * Returns whether visible nodes search is enabled or disabled.
   *
   * @return True, if visible nodes search is enabled. False, if it is disabled.
   */
  public boolean isOnlyVisible() {
    return m_onlyVisible;
  }

  /**
   * Returns whether regular expression search is enabled or disabled.
   *
   * @return True, if regular expression search is enabled. False, if it is disabled.
   */
  public boolean isRegEx() {
    return m_isRegEx;
  }

  /**
   * Removes a listener that was previously added.
   *
   * @param listener The previously added listener.
   */
  public void removeListener(final IGraphSearchSettingsListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Toggles case sensitive search.
   *
   * @param caseSensitive True, to enable case sensitive search. False, to disable it.
   */
  public void setCaseSensitive(final boolean caseSensitive) {
    m_isCaseSensitive = caseSensitive;

    for (final IGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedCaseSensitive();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Toggles selected nodes only search.
   *
   * @param selected True, to search through selected nodes only. False, to search through
   *        unselected nodes too.
   */
  public void setOnlySelected(final boolean selected) {
    m_onlySelected = selected;

    for (final IGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedOnlySelected();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Toggles visible nodes only search.
   *
   * @param visible True, to search through visible nodes only. False, to search through invisible
   *        nodes too.
   */
  public void setOnlyVisible(final boolean visible) {
    m_onlyVisible = visible;

    for (final IGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedOnlyVisible();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Toggles regular expression search.
   *
   * @param regEx True, to enable regular expression search. False, to enable string comparison
   *        search.
   */
  public void setRegEx(final boolean regEx) {
    m_isRegEx = regEx;

    for (final IGraphSearchSettingsListener listener : m_listeners) {
      try {
        listener.changedRegEx();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
