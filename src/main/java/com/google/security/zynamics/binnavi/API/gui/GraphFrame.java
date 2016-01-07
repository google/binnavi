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
package com.google.security.zynamics.binnavi.API.gui;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;


// / Window where graphs are shown
/**
 * Represents a single graph frame. A graph frame is the part of the GUI that contains the graph
 * view, the debugger panel, the tag panel, and so on.
 */
public final class GraphFrame {
  /**
   * Window the frame belongs to.
   */
  private final GraphWindow m_window;

  /**
   * The View2D object shown in the frame.
   */
  private final View2D m_view2d;

  /**
   * Provides the debuggers for debugging this graph.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * List of available debuggers in the frame.
   */
  private final List<Debugger> m_debuggers = new ArrayList<Debugger>();

  // / @cond INTERNAL
  /**
   * Creates a new frame object.
   *
   * @param window Window the frame belongs to.
   * @param view2d The View2D object shown in the frame.
   * @param debuggerProvider Provides the debuggers for debugging this graph.
   */
  // / @endcond
  public GraphFrame(final GraphWindow window, final View2D view2d,
      final BackEndDebuggerProvider debuggerProvider) {
    m_window = window;
    m_view2d = view2d;
    m_debuggerProvider = debuggerProvider;
  }

  // ! List of debuggers to be used with the graph.
  /**
   * Returns the debuggers that can be used to debug the graph shown in the frame.
   *
   * @return A list of debuggers.
   */
  public List<Debugger> getDebuggers() {
    if (m_debuggers.isEmpty()) {
      for (final IDebugger debugger : m_debuggerProvider.getDebuggers()) {
        m_debuggers.add(new Debugger(debugger));
      }
    }

    return new ArrayList<Debugger>(m_debuggers);
  }

  // ! View2D shown in the frame.
  /**
   * Returns the View2D object of the graph that is shown in the graph frame.
   *
   * @return A View2D object.
   */
  public View2D getView2D() {
    return m_view2d;
  }

  // ! Parent window of the frame.
  /**
   * Returns the parent window of the frame.
   *
   * @return The parent window of the frame.
   */
  public GraphWindow getWindow() {
    return m_window;
  }

  // ! Printable representation of the frame.
  /**
   * Returns a string representation of the frame.
   *
   * @return A string representation of the frame.
   */
  @Override
  public String toString() {
    return String.format("Graph frame of view '%s'", m_view2d.toString());
  }
}
