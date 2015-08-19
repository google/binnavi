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
package com.google.security.zynamics.binnavi.disassembly.types;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class contains tests related to {@link RawTypeInstanceReference raw type instance
 * references}.
 */
@RunWith(JUnit4.class)
public class RawTypeInstanceReferenceTests {
  private static final int MODULE_ID = 123;
  private static final int VIEW_ID = 234;
  private static final IAddress ADDRESS = new CAddress(0x666);
  private static final int OPERAND_POSITION = 1;
  private static final int EXPRESSION_ID = 200;
  private static final int TYPE_INSTANCE_ID = 300;

  @Test
  public void testConstructor0() {
    final RawTypeInstanceReference rawReference = new RawTypeInstanceReference(
        MODULE_ID, VIEW_ID, ADDRESS, OPERAND_POSITION, EXPRESSION_ID, TYPE_INSTANCE_ID);
    assertEquals(MODULE_ID, rawReference.getModuleId());
    assertEquals(VIEW_ID, rawReference.getViewId());
    assertEquals(ADDRESS, rawReference.getAddress());
    assertEquals(OPERAND_POSITION, rawReference.getOperandPosition());
    assertEquals(EXPRESSION_ID, rawReference.getExpressionId());
    assertEquals(TYPE_INSTANCE_ID, rawReference.getTypeInstanceId());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor1() {
    new RawTypeInstanceReference(
        MODULE_ID, VIEW_ID, null, OPERAND_POSITION, EXPRESSION_ID, TYPE_INSTANCE_ID);
  }
}