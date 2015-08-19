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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;

public final class MockOperandTreeNode implements INaviOperandTreeNode {
  private final ListenerProvider<INaviOperandTreeNodeListener> m_listeners =
      new ListenerProvider<INaviOperandTreeNodeListener>();

  private final List<IReference> m_references = new ArrayList<IReference>();
  private int id;
  private TypeSubstitution substitution;

  private final List<TypeInstanceReference> typeInstanceReferences = Lists.newArrayList();

  @Override
  public void addInstanceReference(final TypeInstanceReference reference) {
    typeInstanceReferences.add(reference);
  }

  @Override
  public void addListener(final INaviOperandTreeNodeListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public void addReference(final IReference reference) {
    m_references.add(reference);

    for (final INaviOperandTreeNodeListener listener : m_listeners) {
      listener.addedReference(this, reference);
    }
  }

  @Override
  public void close() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void deleteReference(final IReference reference) {
    m_references.remove(reference);

    for (final INaviOperandTreeNodeListener listener : m_listeners) {
      listener.removedReference(this, reference);
    }
  }

  @Override
  public long determineAddendValue() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviOperandTreeNode> getChildren() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public OperandDisplayStyle getDisplayStyle() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public IAddress getInstructionAddress() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviOperandTree getOperand() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public int getOperandPosition() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviOperandTreeNode getParent() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<IReference> getReferences() {
    return new ArrayList<IReference>(m_references);
  }

  @Override
  public INaviReplacement getReplacement() {
    return new CStringReplacement("Mock Replacement");
  }

  @Override
  public ExpressionType getType() {
    return ExpressionType.REGISTER;
  }

  @Override
  public List<TypeInstanceReference> getTypeInstanceReferences() {
    return typeInstanceReferences;
  }

  @Override
  public TypeSubstitution getTypeSubstitution() {
    return substitution;
  }

  @Override
  public String getValue() {
    return "Mock Value";
  }

  @Override
  public boolean hasAddendSibling() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final INaviOperandTreeNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setId(final int id) {
    this.id = id;
  }

  @Override
  public void setTypeSubstitution(final TypeSubstitution substitution) {
    this.substitution = substitution;
  }
}
