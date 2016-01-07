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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;

import java.util.List;

public interface INaviOperandTreeNode extends IOperandTreeNode {

  /**
   * Adds a listener object that is notified about changes in the operand tree node.
   *
   * @param listener The listener object to add.
   */
  void addListener(INaviOperandTreeNodeListener listener);

  /**
   * Adds an outgoing code or data reference to the operand tree node.
   *
   * @param reference The reference to add.
   *
   * @throws CouldntSaveDataException Thrown if the reference could not be saved in the database.
   */
  void addReference(IReference reference) throws CouldntSaveDataException;

  void close();

  void deleteReference(IReference reference) throws CouldntDeleteException;

  /**
   * Determines the immediate value of the addend in a two component expression given the
   * non-immediate sibling node in bytes, e.g. given the node corresponding to esp, this method
   * returns 4 for the two component expression [esp+4]. The caller has to ensure that the addend
   * sibling actually exists by invoking {@link INaviOperandTreeNode#hasAddendSibling}.
   *
   *  If the value of the addend doesn't fit into a long then only the lower 64bits are returned.
   * Regarding value truncation it behaves in the same way as the BigInteger#longValue longValue
   * method.
   *
   * @return The immediate value of the sibling addend node in bytes.
   */
  long determineAddendValue();

  @Override
  List<INaviOperandTreeNode> getChildren();

  /**
   * The display style defines the way the contained values are formatted when displayed to the
   * user.
   *
   * @return The current display style of the node.
   */
  OperandDisplayStyle getDisplayStyle();

  int getId();

  /**
   * Returns the address of the instruction belonging to this node.
   */
  IAddress getInstructionAddress();

  /**
   * Returns the operand that contains this node.
   *
   * @return The operand that contains this node.
   */
  INaviOperandTree getOperand();

  /**
   * Returns the zero-based position within the belonging instruction of the operand that
   * corresponds to this tree node.
   *
   * @return The corresponding operand position.
   */
  int getOperandPosition();

  /**
   * Returns the parent node of the current node.
   *
   * @return The parent node of the current node or null if the node has no parent.
   */
  INaviOperandTreeNode getParent();

  @Override
  INaviReplacement getReplacement();

  void addInstanceReference(TypeInstanceReference reference);

  List<TypeInstanceReference> getTypeInstanceReferences();

  TypeSubstitution getTypeSubstitution();

  void setTypeSubstitution(TypeSubstitution substitution);

  /**
   * Determines whether this node has a sibling node that is an immediate value and both nodes
   * belong to a sum expression.
   *
   * @return True if this node has a sibling that is part of a two component sum expression.
   */
  boolean hasAddendSibling();

  void removeListener(INaviOperandTreeNodeListener listener);

  void setId(int treeNodeId);

}
