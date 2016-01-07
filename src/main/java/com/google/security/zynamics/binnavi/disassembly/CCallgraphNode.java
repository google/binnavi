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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Node type that represents functions in Call graphs.
 */
public final class CCallgraphNode implements ICallgraphNode {
  /**
   * The function represented by the Call graph node.
   */
  private final INaviFunction m_function;

  /**
   * Parents of the Call graph node in the Call graph it belongs to.
   */
  private final List<ICallgraphNode> m_parents = new ArrayList<ICallgraphNode>();

  /**
   * Children of the Call graph node in the Call graph it belongs to.
   */
  private final List<ICallgraphNode> m_children = new ArrayList<ICallgraphNode>();

  /**
   * Creates a new Call graph node object.
   * 
   * @param function The function represented by the Call graph node.
   */
  public CCallgraphNode(final INaviFunction function) {
    m_function = Preconditions.checkNotNull(function, "IE01235: Function argument can not be null");
  }

  /**
   * Links two Call graph nodes.
   * 
   * @param parent The parent node to link.
   * @param child The child node to link.
   */
  public static void link(final CCallgraphNode parent, final CCallgraphNode child) {
    Preconditions.checkNotNull(parent, "IE01236: Parent argument can not be null");
    Preconditions.checkNotNull(child, "IE01237: Child argument can not be null");

    parent.m_children.add(child);
    child.m_parents.add(parent);
  }

  @Override
  public List<ICallgraphNode> getChildren() {
    return new ArrayList<ICallgraphNode>(m_children);
  }

  @Override
  public INaviFunction getFunction() {
    return m_function;
  }

  @Override
  public List<ICallgraphNode> getParents() {
    return new ArrayList<ICallgraphNode>(m_parents);
  }
}
