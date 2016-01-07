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
package com.google.security.zynamics.binnavi.Gui.Progress;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;



/**
 * Model class that keeps track of active ongoing progresses.
 */
public final class CGlobalProgressManager {
  /**
   * Only valid instance of this class.
   */
  private static CGlobalProgressManager m_instance = new CGlobalProgressManager();

  /**
   * Managed operations.
   */
  private final List<IProgressOperation> m_operations = new ArrayList<IProgressOperation>();

  /**
   * Listeners that are notified about changes in the manager.
   */
  private final ListenerProvider<IGlobalProgressManagerListener> m_listeners =
      new ListenerProvider<IGlobalProgressManagerListener>();

  /**
   * Private constructor because this class is a singleton.
   */
  private CGlobalProgressManager() {
  }

  /**
   * Returns the only valid instance of this class.
   * 
   * @return The only valid instance of this class.
   */
  public static CGlobalProgressManager instance() {
    return m_instance;
  }

  /**
   * Adds a new ongoing operation.
   * 
   * @param operation The operation to add.
   */
  public synchronized void add(final IProgressOperation operation) {
    m_operations.add(operation);

    for (final IGlobalProgressManagerListener listener : m_listeners) {
      try {
        listener.added(operation);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a new listener that is notified about changes in the manager.
   * 
   * @param listener The listener object to add.
   */
  public synchronized void addListener(final IGlobalProgressManagerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the currently managed operations.
   * 
   * @return The currently managed operations.
   */
  public List<IProgressOperation> getOperations() {
    return new ArrayList<IProgressOperation>(m_operations);
  }

  /**
   * Removes an ongoing operation.
   * 
   * @param operation The operation to remove.
   */
  public synchronized void remove(final IProgressOperation operation) {
    m_operations.remove(operation);

    for (final IGlobalProgressManagerListener listener : m_listeners) {
      try {
        listener.removed(operation);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a listener object from the manager.
   * 
   * @param listener The listener object to remove.
   */
  public synchronized void removeListener(final IGlobalProgressManagerListener listener) {
    m_listeners.removeListener(listener);
  }
}
