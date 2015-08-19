/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.zylib.disassembly;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class MockOperandTreeNode implements IOperandTreeNode {
  public final String m_value;

  public List<MockOperandTreeNode> m_children = new ArrayList<MockOperandTreeNode>();

  public ExpressionType m_type;

  public MockOperandTreeNode(final ExpressionType type, final String value) {
    m_type = Preconditions.checkNotNull(type, "Error: type argument can not be null");
    m_value = Preconditions.checkNotNull(value, "Error: value argument can not be null");
  }

  @Override
  public List<MockOperandTreeNode> getChildren() {
    return m_children;
  }

  @Override
  public List<IReference> getReferences() {
    return null;
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
