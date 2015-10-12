/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.IReplacement;

public class ReilOperandNode implements IOperandTreeNode {
  private final String m_value;

  private final List<IOperandTreeNode> children = new ArrayList<>();

  private final ExpressionType m_type;

  public ReilOperandNode(final String value, final ExpressionType type) {
    m_value = Preconditions.checkNotNull(value);
    m_type = type;
  }

  public static void link(final ReilOperandNode parent, final ReilOperandNode child) {
    parent.children.add(child);
  }

  @Override
  public List<IOperandTreeNode> getChildren() {
    return new ArrayList<>(children);
  }

  @Override
  public List<IReference> getReferences() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public IReplacement getReplacement() {
    return null;
  }

  @Override
  public ExpressionType getType() {
    return m_type;
  }

  @Override
  public String getValue() {
    return m_value;
  }
}
