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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;

// / Manages all known debugger templates
/**
 * The debugger template keeps track of predefined debugger templates. These templates can be
 * assigned to projects and modules where they are converted into real debugger objects that can be
 * used to debug the target processes.
 */
public final class DebuggerTemplateManager {
  /**
   * Wrapped internal debugger template manager object.
   */
  private final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager
      m_manager;

  /**
   * Managed debugger templates.
   */
  private final List<DebuggerTemplate> m_debuggerTemplates = new ArrayList<>();

  /**
   * Listeners that are notified about changes in the debugger template manager.
   */
  private final ListenerProvider<IDebuggerTemplateManagerListener> m_listeners =
      new ListenerProvider<IDebuggerTemplateManagerListener>();

  /**
   * Keeps the API debugger template object synchronized with the internal debugger template object.
   */
  private final InternalDebuggerTemplateListener m_internalListener =
      new InternalDebuggerTemplateListener();

  // / @cond INTERNAL
  /**
   * Creates a new API debugger template manager object.
   *
   * @param manager Wrapped internal debugger template manager object.
   */
  // / @endcond
  public DebuggerTemplateManager(
      final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager manager) {
    m_manager = manager;

    m_manager.addListener(m_internalListener);

    for (final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate
        debuggerTemplate : manager) {
      m_debuggerTemplates.add(new DebuggerTemplate(debuggerTemplate));
    }
  }

  // ! Adds a debugger template manager listener.
  /**
   * Adds an object that is notified about changes in the debugger template manager.
   *
   * @param listener The listener object that is notified about changes in the debugger template
   *        manager.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         debugger template manager.
   */
  public void addListener(final IDebuggerTemplateManagerListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Creates a new debugger template.
  /**
   * Creates a new debugger template.
   *
   * @param name The name of the new debugger template.
   * @param host The location of the debug client used by the new template.
   * @param port The port of the debug client used by the new template.
   *
   * @return The created debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the new debugger template could not be saved to the
   *         database.
   */
  public DebuggerTemplate createDebuggerTemplate(final String name, final String host,
      final int port) throws CouldntSaveDataException {
    try {
      final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate newTemplate =
          m_manager.createDebugger(name, host, port);

      return ObjectFinders.getObject(newTemplate, m_debuggerTemplates);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Deletes a debugger template.
  /**
   * Deletes a debugger template from the database.
   *
   * @param template The debugger template to delete.
   *
   * @throws CouldntDeleteException Thrown if the debugger template could not be deleted from the
   *         database.
   */
  public void deleteDebugger(final DebuggerTemplate template) throws CouldntDeleteException {
    Preconditions.checkNotNull(template, "Error: Template argument can't be null");

    try {
      m_manager.removeDebugger(template.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException e) {
      throw new CouldntDeleteException(e);
    }
  }

  // / @cond INTERNAL
  /**
   * Frees allocated resources.
   */
  // / @endcond
  public void dispose() {
    m_manager.removeListener(m_internalListener);

    for (final DebuggerTemplate debuggerTemplate : m_debuggerTemplates) {
      debuggerTemplate.dispose();
    }
  }

  // ! Returns a debugger template.
  /**
   * Returns the debugger template with a given index.
   *
   * @param index The debugger template index. (0 <= index < getDebuggerTemplateCount())
   *
   * @return The debugger template with the given index.
   */
  public DebuggerTemplate getDebuggerTemplate(final int index) {
    return m_debuggerTemplates.get(index);
  }

  // ! Returns the number of managed debugger templates.
  /**
   * Returns the number of debugger templates managed by the manager object.
   *
   * @return The number of managed debugger templates.
   */
  public int getDebuggerTemplateCount() {
    return m_debuggerTemplates.size();
  }

  // ! Returns all debugger templates.
  /**
   * Returns a list of all debugger templates managed by the manager object.
   *
   * @return A list of all managed debugger templates.
   */
  public List<DebuggerTemplate> getDebuggerTemplates() {
    return new ArrayList<DebuggerTemplate>(m_debuggerTemplates);
  }

  // ! Removes a debugger template manager listener.
  /**
   * Removes a listener object from the debugger template manager.
   *
   * @param listener The listener object to remove from the debugger template manager.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the debugger
   *         template manager.
   */
  public void removeListener(final IDebuggerTemplateManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public String toString() {
    return String.format("Debugger Template Manager (%d templates)", m_debuggerTemplates.size());
  }

  /**
   * Keeps the API debugger template object synchronized with the internal debugger template object.
   */
  private class InternalDebuggerTemplateListener implements
      com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener {
    @Override
    public void addedDebugger(
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager manager,
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      final DebuggerTemplate newTemplate = new DebuggerTemplate(debugger);

      m_debuggerTemplates.add(newTemplate);

      for (final IDebuggerTemplateManagerListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedDebuggerTemplate(DebuggerTemplateManager.this, newTemplate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedDebugger(
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager manager,
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      final DebuggerTemplate deletedView = ObjectFinders.getObject(debugger, m_debuggerTemplates);

      m_debuggerTemplates.remove(deletedView);

      for (final IDebuggerTemplateManagerListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.deletedDebuggerTemplate(DebuggerTemplateManager.this, deletedView);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
