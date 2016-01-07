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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Describes a graph shown in a graph panel.
 */
public interface IGraphModel {
  /**
   * Provides the database the graph belongs to.
   *
   * @return The database the graph belongs to.
   */
  IDatabase getDatabase();

  /**
   * Provides the debuggers available in the graph window.
   *
   * @return The debuggers available in the graph window.
   */
  BackEndDebuggerProvider getDebuggerProvider();

  /**
   * Returns the graph shown in the graph window.
   *
   * @return The graph shown in the graph window.
   */
  ZyGraph getGraph();

  /**
   * Returns the graph window in which the graph is open.
   *
   * @return The graph window in which the graph is open.
   */
  CGraphWindow getParent();

  /**
   * Returns the context in which the graph was opened.
   *
   * @return The context in which the graph was opened.
   */
  IViewContainer getViewContainer();
}
