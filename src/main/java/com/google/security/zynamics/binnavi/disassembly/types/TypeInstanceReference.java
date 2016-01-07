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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public final class TypeInstanceReference {

  /**
   * The {@link IAddress} of the {@link INaviInstruction} where the {@link TypeInstanceReference} is
   * associated to.
   */
  private final IAddress address;

  /**
   * The operand position in the {@link INaviInstruction} to which the {@link TypeInstanceReference}
   * is associated to.
   */
  private final int position;

  /**
   * The {@link INaviOperandTreeNode} in the {@link INaviOperandTree} to which the
   * {@link TypeInstanceReference} is associated.
   */
  private Optional<INaviOperandTreeNode> node;

  /**
   * The {@link TypeInstance} which this {@link TypeInstanceReference} references.
   */
  private final TypeInstance typeInstance;

  /**
   * The {@link INaviView} in which the {@link INaviInstruction} is located to whose
   * {@link INaviOperandTreeNode} this {@link TypeInstanceReference} is associated.
   */
  private final INaviView view;

  /**
   * Creates a new {@link TypeInstanceReference}.
   *
   * @param address The {@link IAddress} of the {@link INaviInstruction} where the
   *        {@link TypeInstanceReference} is associated to.
   * @param position The position of the {@link INaviOperandTree} in the {@link INaviInstruction}
   *        where the {@link TypeInstanceReference} is associated to.
   * @param node The {@link INaviOperandTreeNode} to which the {@link TypeInstanceReference} is
   *        associated.
   * @param typeInstance The {@link TypeInstance} this {@link TypeInstanceReference} references.
   * @param view The {@link INaviView view} in which this reference is shown.
   */
  public TypeInstanceReference(final IAddress address, final int position,
      final Optional<INaviOperandTreeNode> node, final TypeInstance typeInstance,
      final INaviView view) {

    this.address = Preconditions.checkNotNull(address, "Error: address argument can not be null");
    Preconditions.checkArgument(position >= 0,
        "Error: the operand position must be larger or equal to zero");
    this.position = position;
    this.node = Preconditions.checkNotNull(node, "Error: node argument can not be null");
    this.typeInstance =
        Preconditions.checkNotNull(typeInstance, "Error: typeInstance argument can not be null");
    this.view = Preconditions.checkNotNull(view, "Error: view argument can not be null");
  }

  /**
   * Returns The {@link IAddress} of the instruction operand that references the type instance.
   *
   * @return The {@link IAddress} of the instruction operand the references the type instance.
   */
  public IAddress getAddress() {
    return address;
  }

  /**
   * Returns The operand position within the instruction that references the type instance.
   *
   * @return The operand position within the instruction that references the type instance.
   */
  public int getPosition() {
    return position;
  }

  /**
   * Returns the {@link INaviOperandTreeNode} to which this {@link TypeInstanceReference} is
   * associated.
   *
   * @return The {@link INaviOperandTreeNode} to which this {@link TypeInstanceReference} is
   *         associated.
   */
  public Optional<INaviOperandTreeNode> getTreeNode() {
    return node;
  }

  /**
   * Returns the {@link TypeInstance} that is referenced.
   *
   * @return The {@link TypeInstance} that is referenced.
   */
  public TypeInstance getTypeInstance() {
    return typeInstance;
  }

  /**
   * Returns the {@link INaviView} of the {@link TypeInstanceReference}.
   *
   * @return the {@link INaviView} of the {@link TypeInstanceReference}.
   */
  public INaviView getView() {
    return this.view;
  }

  /**
   * Sets the {@link INaviOperandTreeNode node} of the {@link TypeInstanceReference}.
   *
   * @param node The {@link INaviOperandTreeNode} to which this {@link TypeInstanceReference} is
   *        associated.
   */
  public void setTreeNode(final INaviOperandTreeNode node) {
    this.node = Optional.fromNullable(node);
  }

  /**
   * Converts the reference to a displayable string.
   */
  public String getDisplayableString() {
     return address.toHexString() + " Operand " + position;
  }
}
