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

import javax.swing.ImageIcon;

import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;


/**
 * Represents basic blocks in the function tree.
 */
public final class FunctionTreeBlockNode extends FunctionTreeNode implements IFunctionTreeNode {
  private static final ImageIcon ICON = new ImageIcon(
      PathfinderPlugin.class.getResource("resources/brick.png"));

  /**
   * Basic block represented by the node.
   */
  private final BasicBlock m_basicBlock;

  /**
   * Creates a new block node object.
   * 
   * @param basicBlock Basic block represented by the node.
   */
  public FunctionTreeBlockNode(final BasicBlock basicBlock) {
    m_basicBlock = basicBlock;

    setIcon(ICON);
  }

  @Override
  public void doubleClicked() {
    // Do nothing when a basic block node is double clicked.
  }

  /**
   * Returns the basic block represented by this node.
   * 
   * @return The basic block represented by this node.
   */
  public BasicBlock getBasicBlock() {
    return m_basicBlock;
  }

  @Override
  public boolean isVisible() {
    // always visible
    return true;
  }

  @Override
  public String toString() {
    return m_basicBlock.getAddress().toHexString();
  }

  @Override
  public int getChildCount() {
    return 0;
  }
}
