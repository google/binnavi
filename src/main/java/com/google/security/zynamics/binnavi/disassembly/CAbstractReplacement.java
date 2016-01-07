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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;



/**
 * Base class for all kinds of operand expression replacements.
 */
public abstract class CAbstractReplacement implements INaviReplacement {
  /**
   * Listeners that are notified about changes in the operand.
   */
  private final ListenerProvider<INaviReplacementListener> m_listeners =
      new ListenerProvider<INaviReplacementListener>();

  /**
   * Notifies all listeners that the replacement changed.
   */
  protected void notifyListeners() {
    for (final INaviReplacementListener listener : m_listeners) {
      try {
        listener.changed(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addListener(final INaviReplacementListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public abstract CAbstractReplacement cloneReplacement();

  @Override
  public void removeListener(final INaviReplacementListener listener) {
    m_listeners.removeListener(listener);
  }
}
