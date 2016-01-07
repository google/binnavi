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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Represents a type instance reference as it is stored in the database.
 */
public class RawTypeInstanceReference {

  /**
   * The {@link IAddress address} of the {@link INaviInstruction instruction} the
   * {@link RawTypeInstanceReference} is associated to.
   */
  private final IAddress address;

  /**
   * The postition of the {@link INaviOperandTree operand tree} in the {@link INaviInstruction
   * instruction} the {@link RawTypeInstanceReference} is associated to.
   */
  private final int operandPosition;

  /**
   * The id of the {@link INaviOperandTreeNode node} the {@link RawTypeInstanceReference reference}
   * is associated to.
   */
  private final int expressionId;

  /**
   * The id of the {@link TypeInstance type instance} the {@link RawTypeInstanceReference reference}
   * is associated to.
   */
  private final int typeInstanceId;

  /**
   * The id of the {@link INaviView view} the {@link RawTypeInstanceReference reference} is
   * associated to.
   */
  private final int viewId;

  /**
   * The id of the {@link INaviModule module} this {@link RawTypeInstanceReference reference}
   * belongs to.
   */
  private final int moduleId;

  /**
   * Creates a new type instance reference object.
   * 
   * @param moduleId The if of the {@link INaviModule module} to which the
   *        {@link RawTypeInstanceReference reference} belongs.
   * @param viewId The id of the view where the {@link INaviInstruction instruction} is located in
   *        that holds this {@link RawTypeInstanceReference reference}.
   * @param address The {@link IAddress address} of the {@link INaviInstruction instruction} that
   *        contains a reference to the type instance.
   * @param operandPosition The position of the operand that references the type instance.
   * @param expressionId The id of the expression node that represents the operand.
   * @param typeInstanceId The database id of the type instance referred to by the operand.
   */
  public RawTypeInstanceReference(final int moduleId, final int viewId, final IAddress address,
      final int operandPosition, final int expressionId, final int typeInstanceId) {
    this.moduleId = moduleId;
    this.viewId = viewId;
    this.address = Preconditions.checkNotNull(address, "Error: address can not be null.");
    this.operandPosition = operandPosition;
    this.expressionId = expressionId;
    this.typeInstanceId = typeInstanceId;
  }

  public IAddress getAddress() {
    return address;
  }

  public int getExpressionId() {
    return expressionId;
  }

  public int getModuleId() {
    return moduleId;
  }

  public int getOperandPosition() {
    return operandPosition;
  }

  public int getTypeInstanceId() {
    return typeInstanceId;
  }

  public int getViewId() {
    return viewId;
  }
}
