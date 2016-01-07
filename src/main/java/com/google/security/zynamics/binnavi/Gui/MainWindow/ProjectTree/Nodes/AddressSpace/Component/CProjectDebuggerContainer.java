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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainerListener;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.IProjectListener;

/**
 * Wraps access to all debuggers of a project.
 */
public final class CProjectDebuggerContainer implements IDebuggerContainer {
  /**
   * Project whose debuggers are wrapped.
   */
  private final INaviProject m_project;

  /**
   * Listeners notified about changes in the project.
   */
  private final List<IProjectListener> m_wrappers = new ArrayList<IProjectListener>();

  /**
   * Creates a new container object.
   * 
   * @param project The project that contains the debuggers.
   */
  public CProjectDebuggerContainer(final INaviProject project) {
    Preconditions.checkNotNull(project, "IE01954: Project argument can not be null");

    m_project = project;
  }

  @Override
  public void addListener(final IDebuggerContainerListener listener) {
    final IProjectListener wrapper = new ListenerWrapper(listener);

    m_project.addListener(wrapper);

    m_wrappers.add(wrapper);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    for (final IProjectListener listener : m_wrappers) {
      m_project.removeListener(listener);
    }
  }

  @Override
  public List<DebuggerTemplate> getDebuggers() {
    return m_project.getConfiguration().getDebuggers();
  }

  /**
   * Wraps between project listeners and debugger container listeners.
   */
  private class ListenerWrapper extends CProjectListenerAdapter {
    /**
     * Listener notified about changes in the project.
     */
    private final IDebuggerContainerListener m_listener;

    /**
     * Creates a new wrapper object.
     * 
     * @param listener Listener notified about changes in the project.
     */
    private ListenerWrapper(final IDebuggerContainerListener listener) {
      m_listener = listener;
    }

    @Override
    public void addedDebugger(final INaviProject project, final DebuggerTemplate debugger) {
      m_listener.addedDebugger(CProjectDebuggerContainer.this, debugger);
    }

    @Override
    public void removedDebugger(final INaviProject project, final DebuggerTemplate debugger) {
      m_listener.removedDebugger(CProjectDebuggerContainer.this, debugger);
    }
  }
}
