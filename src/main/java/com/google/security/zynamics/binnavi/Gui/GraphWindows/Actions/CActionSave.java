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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSaver;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.ViewType;

/**
 * Action class used for saving graphs.
 */
public final class CActionSave extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 199722739187955737L;

  /**
   * Parent window used for dialogs.
   */
  private final WeakReference<JFrame> m_parent;

  /**
   * Graph to be saved.
   */
  private final WeakReference<ZyGraph> m_graph;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param graph Graph to be saved.
   */
  public CActionSave(final JFrame parent, final ZyGraph graph) {
    super("Save View");
    m_parent =
        new WeakReference<JFrame>(Preconditions.checkNotNull(parent,
            "IE01646: Parent can't be null"));
    m_graph =
        new WeakReference<ZyGraph>(Preconditions.checkNotNull(graph,
            "IE01222: Graph argument can not be null"));
    setEnabled(graph.getRawView().getType() != ViewType.Native);

    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_SAVE_VIEW_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final ZyGraph graph = m_graph.get();
    final JFrame parent = m_parent.get();

    if ((graph != null) && (parent != null)) {
      CGraphSaver.save(parent, graph);
    }
  }
}
