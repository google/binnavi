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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionListenerAdapter;
import com.google.security.zynamics.binnavi.API.helpers.IProgressThread;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.API.helpers.MessageBox;
import com.google.security.zynamics.binnavi.API.helpers.ProgressDialog;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

/**
 * Represents function nodes in the function tree.
 */
public final class FunctionTreeFunctionNode extends FunctionTreeNode implements IFunctionTreeNode {

  /**
   * Icon used by the node if the function is loaded.
   */
  private static final ImageIcon ICON_FUNCTION_LOADED = new ImageIcon(
      PathfinderPlugin.class.getResource("resources/graph2.png"));

  /**
   * Icon used by the node if the function is not loaded.
   */
  private static final ImageIcon ICON_FUNCTION_CLOSED = new ImageIcon(
      PathfinderPlugin.class.getResource("resources/graph2_gray.png"));

  /**
   * Parent of the tree the node belongs to.
   */
  private final JDialog m_parent;

  /**
   * Function represented by the node.
   */
  private final Function m_function;

  private final InternalFunctionListener m_listener = new InternalFunctionListener();

  /**
   * Creates a new function tree node that represents a function.
   * 
   * @param parent Parent of the tree the node belongs to.
   * @param function Function represented by the node.
   */
  public FunctionTreeFunctionNode(final JDialog parent, final Function function) {
    m_function = Preconditions.checkNotNull(function);
    m_parent = Preconditions.checkNotNull(parent);

    createBlockNodes();

    function.addListener(m_listener);

    updateIcon();
  }

  /**
   * Creates the block nodes for each block of the function.
   */
  private void createBlockNodes() {
    if (m_function.isLoaded()) {
      final List<BasicBlock> blocks = m_function.getGraph().getNodes();

      for (final BasicBlock block : blocks) {
        add(new FunctionTreeBlockNode(block));
      }
    }
  }

  private void updateIcon() {
    setIcon(m_function.isLoaded() ? ICON_FUNCTION_LOADED : ICON_FUNCTION_CLOSED);
  }

  public void dispose() {
    m_function.removeListener(m_listener);
  }

  @Override
  public void doubleClicked() {
    // When the user double-clicks on an unloaded function, the
    // function is loaded.

    if (!m_function.isLoaded()) {
      ProgressDialog.show(m_parent, "Loading Function", new IProgressThread() {
        @Override
        public boolean close() {
          return false;
        }

        @Override
        public void run() {
          try {
            m_function.load();
          } catch (final CouldntLoadDataException e) {
            Logger.logException(e);
            MessageBox.showException(m_parent, e, "Function could not be loaded.");
          }
        }
      });
    }
  }

  /**
   * Returns the function represented by the node.
   * 
   * @return The function represented by the node
   */
  public Function getFunction() {
    return m_function;
  }

  @Override
  public boolean isVisible() {
    // if no filter is set or the pattern matches, we are visible
    return (getFilter() == null) || getFilter().matchesFilter(m_function.getName());
  }

  @Override
  public String toString() {
    return m_function.getName();
  }

  private class InternalFunctionListener extends FunctionListenerAdapter {
    @Override
    public void loadedFunction(final Function function) {
      // When a formerly unloaded function is loaded, it is finally
      // possible to show nodes that represent the blocks of the function.

      updateIcon();

      createBlockNodes();
    }
  }
}
