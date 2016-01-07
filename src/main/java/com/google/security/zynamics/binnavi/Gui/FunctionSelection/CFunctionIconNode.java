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
package com.google.security.zynamics.binnavi.Gui.FunctionSelection;

import javax.swing.Icon;
import javax.swing.ImageIcon;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Function node class for the function selection dialog.
 */
public final class CFunctionIconNode extends IconNode implements IFunctionTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1362416162400682740L;

  /**
   * Icon used for nodes of this type.
   */
  private static final ImageIcon ICON_FUNCTION = new ImageIcon(CMain.class.getResource(
      "data/projecttreeicons/flowgraph_views_container2.png"));

  /**
   * Function represented by the node.
   */
  private final INaviFunction m_function;

  /**
   * Creates a new function node object.
   *
   * @param function Function represented by the node.
   */
  public CFunctionIconNode(final INaviFunction function) {
    Preconditions.checkNotNull(function, "IE01571: Function argument can not be null");

    m_function = function;
  }

  @Override
  public void doubleClicked() {
    // Do nothing
  }

  /**
   * Returns the function represented by the node.
   *
   * @return The function represented by the node.
   */
  public INaviFunction getFunction() {
    return m_function;
  }

  @Override
  public Icon getIcon() {
    return ICON_FUNCTION;
  }

  @Override
  public String toString() {
    return m_function.getName();
  }
}
