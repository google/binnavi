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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;



/**
 * Abstract base class for all criteria.
 */
public abstract class CAbstractCriterium implements ICriterium {
  /**
   * Listeners that are notified about changes in the criterium.
   */
  private final ListenerProvider<ICriteriumListener> m_listeners =
      new ListenerProvider<ICriteriumListener>();

  @Override
  public void addListener(final ICriteriumListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public void dispose() {
  }

  /**
   * Notifies listeners that the criterium changed.
   */
  public void notifyListeners() {
    for (final ICriteriumListener listener : m_listeners) {
      try {
        listener.criteriumChanged();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeListener(final ICriteriumListener listener) {
    m_listeners.removeListener(listener);
  }
}
