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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainerListener;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;

/**
 * Wraps access to all debuggers of a database.
 */
public final class CDatabaseDebuggerContainer implements IDebuggerContainer {
  /**
   * The database that contains the debuggers.
   */
  private final IDatabase m_database;

  /**
   * Listener wrappers that are notified about events in the debugger.
   */
  private final List<ListenerWrapper> m_wrappers = new ArrayList<ListenerWrapper>();

  /**
   * Creates a new container object.
   * 
   * @param database The database that contains the debuggers.
   */
  public CDatabaseDebuggerContainer(final IDatabase database) {
    m_database = Preconditions.checkNotNull(database, "IE01976: Database argument can not be null");
  }

  @Override
  public void addListener(final IDebuggerContainerListener listener) {
    final ListenerWrapper wrapper = new ListenerWrapper(listener);

    m_database.getContent().getDebuggerTemplateManager().addListener(wrapper);

    m_wrappers.add(wrapper);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    for (final ListenerWrapper wrapper : m_wrappers) {
      m_database.getContent().getDebuggerTemplateManager().removeListener(wrapper);
    }
  }

  @Override
  public List<DebuggerTemplate> getDebuggers() {
    return m_database.getContent().getDebuggerTemplateManager().getDebuggers();
  }

  /**
   * Wraps between debugger template listeners and debugger container listeners.
   */
  private class ListenerWrapper implements IDebuggerTemplateManagerListener {
    /**
     * Listener to be notified about events in the wrapper debugger.
     */
    private final IDebuggerContainerListener m_listener;

    /**
     * Creates a new listener wrapper object.
     * 
     * @param listener Listener to be notified about events in the wrapper debugger.
     */
    private ListenerWrapper(final IDebuggerContainerListener listener) {
      m_listener = listener;
    }

    @Override
    public void addedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      m_listener.addedDebugger(CDatabaseDebuggerContainer.this, debugger);
    }

    @Override
    public void removedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      m_listener.removedDebugger(CDatabaseDebuggerContainer.this, debugger);
    }
  }
}
