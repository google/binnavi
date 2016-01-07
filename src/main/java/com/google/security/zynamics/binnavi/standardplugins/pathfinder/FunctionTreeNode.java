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

import com.google.security.zynamics.binnavi.standardplugins.utils.IconNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all tree nodes in the function tree Every FunctionTreeNode applies the filter to
 * all of its children
 */
public abstract class FunctionTreeNode extends IconNode implements IFilterableNode,
    IFunctionTreeNode {

  /**
   * The filter object which is passed to each node to determine visibility
   */
  private TextPatternFilter m_filter;

  /**
   * Cache node visibility in order to improve performance
   */
  private final Map<FunctionTreeNode, Boolean> m_cachedVisibility =
      new HashMap<FunctionTreeNode, Boolean>();

  /**
   * Let subclasses get the current filter for this node
   * 
   * @return The currently set filter
   */
  protected TextPatternFilter getFilter() {
    return m_filter;
  }

  @Override
  public FunctionTreeNode getChildAt(final int index) {
    return (FunctionTreeNode) super.getChildAt(index);
    // int counter = 0;
    // for (int i = 0; i < super.getChildCount(); i++) {
    // final FunctionTreeNode node = (FunctionTreeNode) super.getChildAt(i);
    // if (!m_cachedVisibility.containsKey(node)) {
    // m_cachedVisibility.put(node, node.isVisible());
    // }
    //
    // if (m_cachedVisibility.get(node)) {
    // // node is visible so check if this is the node we are looking for...
    // if (counter == index) {
    // return node;
    // }
    // counter++;
    // }
    // }
    //
    // throw new IllegalStateException("Error: function tree node not found");
  }

  @Override
  public int getChildCount() {
    return super.getChildCount();
    // int counter = 0;
    // for (int i = 0; i < super.getChildCount(); i++) {
    // final FunctionTreeNode node = (FunctionTreeNode) super.getChildAt(i);
    // if (!m_cachedVisibility.containsKey(node)) {
    // m_cachedVisibility.put(node, node.isVisible());
    // }
    // if (m_cachedVisibility.get(node)) {
    // counter++;
    // }
    // }
    // return counter;
  }

  /**
   * invalidate cached visibility and propagate filter to children
   */
  @Override
  public void setFilter(final TextPatternFilter filter) {
    m_filter = filter;
    m_cachedVisibility.clear();

    for (int i = 0; i < super.getChildCount(); i++) {
      final FunctionTreeNode node = (FunctionTreeNode) super.getChildAt(i);
      node.setFilter(filter);
    }
  }
}
