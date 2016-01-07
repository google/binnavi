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
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that's used to store REIL instructions.
 */
public final class ReilInstruction implements IInstruction, Comparable<ReilInstruction> {
  /**
   * Address of the REIL instruction. Can't be final because it has to be
   * changeable to clarify the x86 translation code. 
   */
  private IAddress address;

  /**
   * Mnemonic of the REIL instruction.
   */
  private final Integer mnemonic;

  /**
   * First operand of the REIL instruction.
   */
  private final ReilOperand firstOperand;

  /**
   * Second operand of the REIL instruction.
   */
  private final ReilOperand secondOperand;

  /**
   * Third operand of the REIL instruction.
   */
  private final ReilOperand thirdOperand;

  /**
   * Metadata of the REIL instruction.
   */
  private final Map<String, String> metaData = new HashMap<>();

  /**
   * Creates a new ReilInstruction object.
   * 
   * @param address The address of the REIL instruction.
   * @param mnemonic The mnemonic of the REIL instruction.
   * @param firstOperand The first operand of the REIL instruction.
   * @param secondOperand The second operand of the REIL instruction.
   * @param thirdOperand The third operand of the REIL instruction.
   */
  public ReilInstruction(final IAddress address, final String mnemonic,
      final ReilOperand firstOperand, final ReilOperand secondOperand,
      final ReilOperand thirdOperand) {
    Preconditions.checkNotNull(mnemonic, "Argument mnemonic can't be null");
    this.mnemonic = ReilHelpers.MnemonicToMnemonicCode(mnemonic);
    this.firstOperand =
        Preconditions.checkNotNull(firstOperand);
    this.secondOperand =
        Preconditions.checkNotNull(secondOperand);
    this.thirdOperand =
        Preconditions.checkNotNull(thirdOperand);

    this.address = address;
  }

  /**
   * Creates the string representation of meta-data.
   * 
   * @return The string representation of meta-data.
   */
  private String getMetaDataString() {
    return metaData.keySet()
            .stream()
            .map(key -> key + " : " + metaData.get(key))
            .collect(Collectors.joining(", "));
  }

  @Override
  public int compareTo(final ReilInstruction o) {
    return address.compareTo(o.address);
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof ReilInstruction)) {
      return false;
    }

    final ReilInstruction rhsInstruction = (ReilInstruction) rhs;

    return address.equals(rhsInstruction.getAddress())
        && rhsInstruction.getMnemonic().equals(mnemonic)
        && firstOperand.equals(rhsInstruction.getFirstOperand())
        && secondOperand.equals(rhsInstruction.getSecondOperand())
        && thirdOperand.equals(rhsInstruction.getThirdOperand())
        && metaData.equals(rhsInstruction.getMetaData());
  }

  /**
   * Returns the address of the REIL instruction (native address shifted plus
   * offset).
   * 
   * @return The address of the REIL instruction.
   */
  @Override
  public IAddress getAddress() {
    return address;
  }

  @Override
  public String getArchitecture() {
    return "REIL";
  }

  @Override
  public byte[] getData() {
    return new byte[0];
  }

  /**
   * Returns the first operand of the REIL instruction.
   * 
   * @return The first operand of the REIL instruction.
   */
  public ReilOperand getFirstOperand() {
    return firstOperand;
  }

  @Override
  public long getLength() {
    return 1;
  }

  /**
   * Returns all pieces of metadata.
   * 
   * @return A map of metadata information.
   */
  public Map<String, String> getMetaData() {
    return new HashMap<>(metaData);
  }

  /**
   * Returns a piece of metadata.
   * 
   * @param key The key of the metadata.
   * @return The piece of metadata that belongs to the key.
   */
  public String getMetaData(final String key) {
    // TODO: Handle invalid keys

    Preconditions.checkNotNull(key, "Argument key can't be null");

    return metaData.get(key);
  }

  @Override
  public String getMnemonic() {
    return ReilHelpers.MnemonicCodeToMnemonic(mnemonic);
  }

  /**
   * Returns the mnemonic of the REIL instruction.
   * 
   * @return The mnemonic of the REIL instruction.
   */
  @Override
  public Integer getMnemonicCode() {
    return mnemonic;
  }

  @Override
  public List<IOperandTree> getOperands() {
    final List<IOperandTree> operands = new ArrayList<>();

    operands.add(firstOperand);
    operands.add(secondOperand);
    operands.add(thirdOperand);

    return operands;
  }

  /**
   * Returns the second operand of the REIL instruction.
   * 
   * @return The second operand of the REIL instruction.
   */
  public ReilOperand getSecondOperand() {
    return secondOperand;
  }

  /**
   * Returns the third operand of the REIL instruction.
   * 
   * @return The third operand of the REIL instruction.
   */
  public ReilOperand getThirdOperand() {
    return thirdOperand;
  }

  /**
   * Returns the tuple string representation of the REIL instruction.
   * 
   * @return The tuple string representation of the REIL instruction.
   */
  public String getTupleString() {
    return String.format("%010X: (%s, (%s, %s, %s), (%s, %s, %s), [%s])", address, mnemonic,
        firstOperand.getValue(), secondOperand.getValue(), thirdOperand.getValue(),
        firstOperand.getSize(), secondOperand.getSize(), thirdOperand.getSize(),
        getMetaDataString());
  }

  @Override
  public int hashCode() {
    return address.hashCode() * (mnemonic + 1) * firstOperand.hashCode() * secondOperand.hashCode()
        * thirdOperand.hashCode() * metaData.hashCode();
  }

  /**
   * Adds metadata to the REIL instruction.
   * 
   * @param key The key of the metadata.
   * @param value The value of the metadata.
   */
  public void setMetaData(final String key, final String value) {
    // TODO: Check for duplicates.

    Preconditions.checkNotNull(key, "Argument key can't be null");

    if (value == null) {
      metaData.remove(key);
    } else {
      metaData.put(key, value);
    }
  }

  /**
   * Returns the string representation of the REIL instruction.
   * 
   * @return The string representation of the operand.
   */
  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(address.toHexString());
    stringBuilder.append(": ");
    stringBuilder.append(ReilHelpers.MnemonicCodeToMnemonic(mnemonic));
    stringBuilder.append(" [");
    stringBuilder.append(firstOperand.getSize());
    stringBuilder.append(" ");
    stringBuilder.append(firstOperand);
    stringBuilder.append(", ");
    stringBuilder.append(secondOperand.getSize());
    stringBuilder.append(" ");
    stringBuilder.append(secondOperand);
    stringBuilder.append(", ");
    stringBuilder.append(thirdOperand.getSize());
    stringBuilder.append(" ");
    stringBuilder.append(thirdOperand);
    stringBuilder.append("]");
    return stringBuilder.toString(); 
  }

  /**
   * Allow the resetting of an instruction address.
   */
  public void setAddress(IAddress newAddress) {
    address = newAddress;
  }
}
