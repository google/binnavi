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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Module;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JDialog;


/**
 * The invisible root node of the function tree.
 */
public final class FunctionTreeRootNode extends FunctionTreeNode implements IFunctionTreeNode {

  /**
   * Creates a root node object.
   * 
   * @param parent Parent of the tree the node belongs to.
   * @param module The module that provides the functions and basic blocks to display.
   */
  public FunctionTreeRootNode(final JDialog parent, final Module module) {
    // Create tree nodes for each function of the module.

    final List<Function> functions = module.getFunctions();

    Collections.sort(functions, new Comparator<Function>() {
      @Override
      public int compare(final Function o1, final Function o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });

    for (final Function function : functions) {
      add(new FunctionTreeFunctionNode(parent, function));
    }
  }

  public void dispose() {
    for (int i = 0; i < getChildCount(); i++) {
      final FunctionTreeFunctionNode node = (FunctionTreeFunctionNode) getChildAt(i);

      node.dispose();
    }
  }

  @Override
  public void doubleClicked() {
    // The root node is invisible, it can not be double-clicked.
  }

  @Override
  public boolean isVisible() {
    return false;
  }
}
