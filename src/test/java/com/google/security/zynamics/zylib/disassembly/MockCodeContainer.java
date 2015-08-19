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

public class MockCodeContainer implements ICodeContainer<MockInstruction> {
  public List<MockInstruction> m_instructions = new ArrayList<MockInstruction>();
  public List<ICodeEdge<?>> m_outgoingEdges = new ArrayList<ICodeEdge<?>>();
  public IAddress m_address = new MockAddress(0x1000);

  @Override
  public IAddress getAddress() {
    return m_instructions.get(0).getAddress();
  }

  @Override
  public Iterable<MockInstruction> getInstructions() {
    return m_instructions;
  }

  @Override
  public MockInstruction getLastInstruction() {
    return m_instructions.isEmpty() ? null : m_instructions.get(m_instructions.size() - 1);
  }

  @Override
  public List<? extends ICodeEdge<?>> getOutgoingEdges() {
    return new ArrayList<ICodeEdge<?>>(m_outgoingEdges);
  }

  @Override
  public boolean hasInstruction(final MockInstruction instruction) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String toString() {
    return m_instructions.toString();
  }
}
