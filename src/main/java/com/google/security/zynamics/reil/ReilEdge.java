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
package com.google.security.zynamics.reil;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.IGraphEdge;

public class ReilEdge implements IGraphEdge<ReilBlock>, ICodeEdge<ReilBlock> {
  private final ReilBlock m_source;
  private final ReilBlock m_target;
  private final EdgeType m_type;

  public ReilEdge(final ReilBlock source, final ReilBlock target, final EdgeType type) {
    m_source = Preconditions.checkNotNull(source, "Error: Source argument can't be null");
    m_target = Preconditions.checkNotNull(target, "Error: Target argument can't be null");
    m_type = type;
  }

  @Override
  public ReilBlock getSource() {
    return m_source;
  }

  @Override
  public ReilBlock getTarget() {
    return m_target;
  }

  @Override
  public EdgeType getType() {
    return m_type;
  }

  @Override
  public String toString() {
    return m_source.getAddress().toHexString() + " -> " + m_target.getAddress().toHexString();
  }
}
