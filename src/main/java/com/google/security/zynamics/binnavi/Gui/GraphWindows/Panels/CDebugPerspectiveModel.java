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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CDebugEventNotifier;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * A debug perspective model encapsulates everything that is necessary for components of the debug
 * GUI perspective of a single graph window to know about. The information from this class can then
 * be used to update the appearance of individual GUI components and to synchronize different GUI
 * components.
 */
public final class CDebugPerspectiveModel implements IFrontEndDebuggerProvider {
  /**
   * Describes the graph debugged in the window.
   */
  private final IGraphModel m_model;

  /**
   * The currently active debugger.
   */
  private IDebugger m_activeDebugger = null;

  /**
   * The currently active address.
   */
  private IAddress m_activeAddress = null;

  /**
   * Listeners that are notified about changes in the debug perspective model.
   */
  private final ListenerProvider<IDebugPerspectiveModelListener> m_listeners =
      new ListenerProvider<IDebugPerspectiveModelListener>();

  /**
   * Provides the event loggers for all individual debuggers that are available in the debug
   * perspective.
   */
  private final Map<IDebugger, TraceLogger> m_eventLoggerMap =
      new HashMap<IDebugger, TraceLogger>();

  /**
   * Provides the event notifiers for all individual debuggers that are available in the debug
   * perspective.
   */
  private final Map<IDebugger, CDebugEventNotifier> m_notifierMap =
      new HashMap<IDebugger, CDebugEventNotifier>();

  /**
   * Provides the debug protocol dispatchers for all individual debuggers that are available in the
   * debug perspective.
   */
  // private final Map<IDebugger, CDebugProtocolDispatcher> m_protocolDispatcherMap = new
  // HashMap<IDebugger, CDebugProtocolDispatcher>();

  /**
   * The last address the user went to in the memory view.
   */
  private IAddress m_gotoAddress = null;

  /**
   * Creates a new debug perspective model.
   *
   * @param model Describes the graph debugged in the window.
   */
  public CDebugPerspectiveModel(final IGraphModel model) {
    m_model = Preconditions.checkNotNull(model, "IE01804: Graph model argument can not be null");
  }

  /**
   * Adds a listener that is notified about changes in the debug perspective.
   *
   * @param listener The listener to add.
   */
  public void addListener(final IDebugPerspectiveModelListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the currently active address.
   *
   * @return The currently active address.
   */
  public IAddress getActiveAddress() {
    return m_activeAddress;
  }

  @Override
  public IDebugger getCurrentSelectedDebugger() {
    return m_activeDebugger;
  }

  /**
   * Returns the last address the user went to in the memory view.
   *
   * @return The last address or null.
   */
  public IAddress getGotoAddress() {
    return m_gotoAddress;
  }

  /**
   * Returns the graph model used by the debug perspective.
   *
   * @return The graph model used by the debug perspective.
   */
  public IGraphModel getGraphModel() {
    return m_model;
  }

  @Override
  public CDebugEventNotifier getNotifier(final IDebugger debugger) {
    return m_notifierMap.get(debugger);
  }

  @Override
  public TraceLogger getTraceLogger(final IDebugger debugger) {
    return m_eventLoggerMap.get(debugger);
  }

  /**
   * Removes a listener that was notified about changes in the debug perspective model.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IDebugPerspectiveModelListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Sets the active debugger.
   *
   * @param debugger The new debugger.
   */
  public void setActiveDebugger(final IDebugger debugger) {
    if (m_activeDebugger == debugger) {
      return;
    }

    if ((debugger != null) && !m_eventLoggerMap.containsKey(debugger)) {
      m_notifierMap.put(debugger, new CDebugEventNotifier(m_model.getParent(), debugger,
          m_model.getDebuggerProvider().getDebugTarget(), m_model.getViewContainer()));
      m_eventLoggerMap.put(
          debugger, new TraceLogger(m_model.getViewContainer().getTraceProvider(), debugger));
    }

    final IDebugger oldDebugger = m_activeDebugger;
    m_activeDebugger = debugger;

    for (final IDebugPerspectiveModelListener listener : m_listeners) {
      // ESCA-JAVA0166: Calling a listener
      try {
        listener.changedActiveDebugger(oldDebugger, m_activeDebugger);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Sets the active memory address.
   *
   * @param address The new memory address.
   * @param focusMemoryWindow True, if the focus should be transferred to the memory view.
   */
  public void setActiveMemoryAddress(final IAddress address, final boolean focusMemoryWindow) {
    if ((address != null) && address.equals(m_activeAddress)) {
      return;
    } else if ((address == null) && (m_activeAddress == null)) {
      return;
    }

    m_activeAddress = address;

    for (final IDebugPerspectiveModelListener listener : m_listeners) {
      try {
        listener.changedActiveAddress(m_activeAddress, focusMemoryWindow);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Sets the last address the user went to in the hex view.
   *
   * @param address The new address.
   */
  public void setGotoAddress(final IAddress address) {
    m_gotoAddress = address;
  }
}
