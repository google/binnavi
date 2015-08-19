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
package com.google.security.zynamics.binnavi.API.disassembly;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.ExpressionType;
import com.google.security.zynamics.binnavi.API.disassembly.OperandExpression;
import com.google.security.zynamics.binnavi.API.disassembly.Reference;
import com.google.security.zynamics.binnavi.API.disassembly.ReferenceType;
import com.google.security.zynamics.binnavi.disassembly.MockOperandTreeNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public final class OperandExpressionTest {
  @Test
  public void testConstructor() {
    final MockOperandTreeNode node = new MockOperandTreeNode();

    final OperandExpression expression = new OperandExpression(node);

    assertEquals(ExpressionType.Register, expression.getType());
    assertEquals("Mock Replacement", expression.getReplacement());
    assertEquals("Mock Value", expression.toString());
  }

  @Test
  public void testReferences() throws CouldntSaveDataException, CouldntDeleteException {
    final MockOperandExpressionListener listener = new MockOperandExpressionListener();

    final MockOperandTreeNode node = new MockOperandTreeNode();

    final OperandExpression expression = new OperandExpression(node);

    expression.addListener(listener);

    final Reference reference1 =
        expression.addReference(new Address(0x123), ReferenceType.CALL_VIRTUAL);
    final Reference reference2 = expression.addReference(new Address(0x124), ReferenceType.DATA);

    final List<Reference> references = expression.getReferences();

    assertEquals(2, references.size());
    assertEquals(reference1, references.get(0));
    assertEquals(reference2, references.get(1));
    assertEquals("addedReference;addedReference;", listener.events);

    expression.deleteReference(reference1);

    assertEquals(1, expression.getReferences().size());
    assertEquals("addedReference;addedReference;removedReference;", listener.events);

    expression.removeListener(listener);
  }
}
