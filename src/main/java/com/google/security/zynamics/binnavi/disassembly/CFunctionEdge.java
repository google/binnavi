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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;

/**
 * Edge class used to represent edges of flowgraphs.
 */
public final class CFunctionEdge extends DefaultEdge<IBlockNode> implements IBlockEdge {
  /**
   * The type of the edge.
   */
  private final EdgeType m_type;

  /**
   * Creates a new function edge object.
   * 
   * @param source Source block node of the edge.
   * @param target Target block node of the edge.
   * @param type Type of the edge.
   */
  public CFunctionEdge(final IBlockNode source, final IBlockNode target, final EdgeType type) {
    super(source, target);

    m_type = Preconditions.checkNotNull(type, "IE01238: Type argument can not be null");
  }

  @Override
  public EdgeType getType() {
    return m_type;
  }
}
