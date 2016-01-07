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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Interface to be implemented by classes that want to provide the data used to create instructions
 * and basic blocks.
 * 
 * What basically happens with this class is that instruction parser objects use it as their data
 * providers. The whole process works by iterating over all instructions provided by the code node
 * provider and getting all data of each instruction individually.
 */
public interface ICodeNodeProvider {
  /**
   * Returns the address of the current instruction.
   * 
   * @return The address of the current instruction.
   * 
   * @throws ParserException Thrown if the address of the current instruction could not be
   *         determined.
   */
  IAddress getInstructionAddress() throws ParserException;

  /**
   * Returns the architecture of the current instruction.
   * 
   * @return The architecture of the current instruction.
   * 
   * @throws ParserException Thrown if the architecture of the current instruction could not be
   *         determined.
   */
  String getInstructionArchitecture() throws ParserException;

  /**
   * Returns the border color of the current basic block.
   * 
   * @return The border color of the current basic block.
   * 
   * @throws ParserException Thrown if the border color of the current basic block could not be
   *         determined.
   */
  int getBorderColor() throws ParserException;

  /**
   * Returns the color of the current basic block.
   * 
   * @return The color of the current basic block.
   * 
   * @throws ParserException Thrown if the color of the current basic block could not be determined.
   */
  int getColor() throws ParserException;

  /**
   * Returns the binary data of the current instruction.
   * 
   * @return The binary data of the current instruction.
   * 
   * @throws ParserException Thrown if the binary data of the current instruction could not be
   *         determined.
   */
  byte[] getData() throws ParserException;

  /**
   * Returns the expression ID of the current operand expression.
   * 
   * @return The expression ID of the current operand expression.
   * 
   * @throws ParserException Thrown if the expression ID of the current operand expression could not
   *         be determined.
   */
  int getExpressionTreeId() throws ParserException;

  /**
   * Returns the expression type of the current operand expression.
   * 
   * @return The expression type of the current operand expression.
   * 
   * @throws ParserException Thrown if the expression type of the current operand expression could
   *         not be determined.
   */
  int getExpressionTreeType() throws ParserException;

  /**
   * Returns the function address the operand refers to.
   * 
   * @return The function address or null if the operand does not refer to any function.
   * 
   * @throws ParserException Thrown if the function address could not be read.
   */
  IAddress getFunctionAddress() throws ParserException;

  /**
   * Returns the comment of the current instruction.
   * 
   * @return The comment of the current instruction.
   * 
   * @throws ParserException Thrown if the comment of the current instruction could not be
   *         determined.
   */
  Integer getGlobalInstructionCommentId() throws ParserException;

  /**
   * Returns the global comment of the current basic block.
   * 
   * @return The global comment of the current basic block.
   * 
   * @throws ParserException Thrown if the global comment of the current basic block could not be
   *         determined.
   */
  Integer getGlobalNodeCommentId() throws ParserException;

  /**
   * Returns the height of the basic block.
   * 
   * @return The height of the basic block.
   * 
   * @throws ParserException Thrown if the height of the basic block could not be read.
   */
  double getHeight() throws ParserException;

  /**
   * Returns the immediate value of the current operand expression.
   * 
   * @return The immediate value of the current operand expression.
   * 
   * @throws ParserException Thrown if the immediate value of the current operand expression could
   *         not be determined.
   */
  String getImmediate() throws ParserException;

  /**
   * Returns the local comment of the current instruction.
   * 
   * @return The local comment of the current instruction.
   * 
   * @throws ParserException Thrown if the local comment of the current instruction could not be
   *         determined.
   */
  Integer getLocalInstructionCommentId() throws ParserException;

  /**
   * Returns the local comment of the current basic block.
   * 
   * @return The local comment of the current basic block.
   * 
   * @throws ParserException Thrown if the local comment of the current basic block could not be
   *         determined.
   */
  Integer getLocalNodeCommentId() throws ParserException;

  /**
   * Returns the mnemonic of the current instruction.
   * 
   * @return The mnemonic of the current instruction.
   * 
   * @throws ParserException Thrown if the mnemonic of the current instruction could not be
   *         determined.
   */
  String getMnemonic() throws ParserException;

  /**
   * Returns the module ID of the current instruction.
   * 
   * @return The module ID of the current instruction.
   * 
   * @throws ParserException Thrown if the module ID of the current instruction could not be
   *         determined.
   */
  int getModule() throws ParserException;

  /**
   * Returns the ID of the current basic block.
   * 
   * @return The ID of the current basic block.
   * 
   * @throws ParserException Thrown if the ID of the current basic block could not be determined.
   */
  int getNodeId() throws ParserException;

  /**
   * Returns the position of the current operand.
   * 
   * @return The position of the current operand or null if the current instruction has no operands.
   * 
   * @throws ParserException Thrown if the position of the current operand could not be determined.
   */
  Integer getOperandPosition() throws ParserException;

  /**
   * Returns the parent function address of the current instruction.
   * 
   * @return The parent function address of the current instruction.
   * 
   * @throws ParserException Thrown if the parent function address of the current instruction could
   *         not be determined.
   */
  IAddress getParentFunction() throws ParserException;

  /**
   * Returns the parent ID of the current operand expression.
   * 
   * @return The parent ID of the current operand expression.
   * 
   * @throws ParserException Thrown if the parent ID of the current operand expression could not be
   *         determined.
   */
  int getParentId() throws ParserException;

  /**
   * Returns the outgoing reference of the current operand expression.
   * 
   * @return The outgoing reference of the current operand expression.
   * 
   * @throws ParserException Thrown if the outgoing reference of the current operand expression
   *         could not be determined.
   */
  CReference getReference() throws ParserException;

  /**
   * Returns the replacement value of the current operand expression.
   * 
   * @return The replacement value of the current operand expression.
   * 
   * @throws ParserException Thrown if the replacement value of the current operand expression could
   *         not be determined.
   */
  String getReplacement() throws ParserException;

  /**
   * Returns the offset used by the type substitution.
   * 
   * @return The offset in bits relative to the beginning of the base type.
   * @throws ParserException
   */
  int getSubstitutionOffset() throws ParserException;

  /**
   * Returns the zero-based index position of an operand within its instruction that has an attached
   * type substitution.
   * 
   * @return The index of the operand within its instruction.
   * @throws ParserException Thrown if the position of the type substitution could not be
   *         determined.
   */
  int getSubstitutionPosition() throws ParserException;

  /**
   * Returns the type id used by the type substitution or null if none exists.
   * 
   * @return The type id of the base type that is used in the type substitution or null.
   * @throws ParserException
   */
  Integer getSubstitutionTypeId() throws ParserException;

  /**
   * Returns the symbol value of the current operand expression.
   * 
   * @return The symbol value of the current operand expression.
   * 
   * @throws ParserException Thrown if the symbol value of the current operand expression could not
   *         be determined.
   */
  String getSymbol() throws ParserException;

  /**
   * Returns the type instance id of the current instruction if any or null of none exists.
   * 
   * @return The type instance id or null.
   * @throws ParserException Thrown if the type instance id could not be determined.
   */
  Integer getTypeInstanceId() throws ParserException;

  /**
   * Returns the width of the basic block.
   * 
   * @return The width of the basic block.
   * 
   * @throws ParserException Thrown if the width of the basic block could not be read.
   */
  double getWidth() throws ParserException;

  /**
   * Returns the X-Coordinate of the current basic block.
   * 
   * @return The X-Coordinate of the current basic block.
   * 
   * @throws ParserException Thrown if the X-coordinate of the current basic block could not be
   *         determined.
   */
  double getX() throws ParserException;

  /**
   * Returns the Y-Coordinate of the current basic block.
   * 
   * @return The Y-Coordinate of the current basic block.
   * 
   * @throws ParserException Thrown if the Y-coordinate of the current basic block could not be
   *         determined.
   */
  double getY() throws ParserException;

  /**
   * Determines whether the code node provider is done providing data.
   * 
   * @return True, if all data was provided. False, if there is more to come.
   * 
   * @throws ParserException Thrown if the data state could not be determined.
   */
  boolean isAfterLast() throws ParserException;

  /**
   * Returns the visibility state of the current basic block.
   * 
   * @return The visibility state of the current basic block.
   * 
   * @throws ParserException Thrown if the visibility state of the current basic block could not be
   *         determined.
   */
  boolean isSelected() throws ParserException;

  /**
   * Returns the selection state of the current basic block.
   * 
   * @return The selection state of the current basic block.
   * 
   * @throws ParserException Thrown if the selection state of the current basic block could not be
   *         determined.
   */
  boolean isVisible() throws ParserException;

  /**
   * Moves the cursor to the next line of data.
   * 
   * @return True, if there is more data to come. False, otherwise.
   * 
   * @throws ParserException Thrown if the cursor could not be moved.
   */
  boolean next() throws ParserException;

  /**
   * Moves the cursor to the previous line of data.
   * 
   * @return True, if there is more data to come. False, otherwise.
   * 
   * @throws ParserException Thrown if the cursor could not be moved.
   */
  boolean prev() throws ParserException;


  /**
   * Returns the path of member types for a type substitution.
   * 
   * @return a (possibly empty) Integer array of member types.
   * 
   * @throws ParserException
   */
  Integer[] getSubstitutionPath() throws ParserException;
}
