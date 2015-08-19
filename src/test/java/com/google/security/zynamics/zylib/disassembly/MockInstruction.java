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

public class MockInstruction implements IInstruction {
  public IAddress address = new MockAddress();

  public String mnemonic = "nop";

  public List<MockOperandTree> operands = new ArrayList<MockOperandTree>();

  public long length = 5;

  public MockInstruction() {
    this(0x123, "nop", new ArrayList<MockOperandTree>());
  }

  public MockInstruction(final long address, final String mnemonic,
      final List<MockOperandTree> operands) {
    this.address = new MockAddress(address);
    this.mnemonic = mnemonic;
    this.operands = operands;
  }

  public MockInstruction(final String mnemonic, final List<MockOperandTree> operands) {
    this.mnemonic = mnemonic;
    this.operands = operands;
  }

  @Override
  public IAddress getAddress() {
    return address;
  }

  @Override
  public String getArchitecture() {
    return "x86-32";
  }

  @Override
  public byte[] getData() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public long getLength() {
    return length;
  }

  @Override
  public String getMnemonic() {
    return mnemonic;
  }

  @Override
  public Integer getMnemonicCode() {
    return mnemonic.hashCode();
  }


  @Override
  public List<MockOperandTree> getOperands() {
    return operands;
  }

  @Override
  public String toString() {
    return address.toHexString() + ": " + mnemonic;
  }

}
