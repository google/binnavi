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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link TypeMember}.
 */
@RunWith(JUnit4.class)
public class TypeMemberTest {
  private TestTypeSystem typeSystem;
  private TypeMember memberOffset0Id0, memberOffset1Id0, memberOffset0Id1, memberOffset1Id1;

  @Before
  public void initializeTestTypeSystem() throws CouldntLoadDataException {
    typeSystem = new TestTypeSystem(new TypeManager(new TypeManagerMockBackend()));
    memberOffset0Id0 =
        TypeMember.createStructureMember(0, typeSystem.simpleStruct, typeSystem.intType, "", 0);
    memberOffset1Id0 =
        TypeMember.createStructureMember(0, typeSystem.simpleStruct, typeSystem.intType, "", 1);
    memberOffset0Id1 =
        TypeMember.createStructureMember(1, typeSystem.simpleStruct, typeSystem.intType, "", 0);
    memberOffset1Id1 =
        TypeMember.createStructureMember(1, typeSystem.simpleStruct, typeSystem.intType, "", 1);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullParentStructure() {
    TypeMember.createStructureMember(0, null, typeSystem.intType, "", 0);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_NullName() {
    TypeMember.createStructureMember(0, typeSystem.simpleStruct, typeSystem.intType, null, 0);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_NullType() {
    TypeMember.createStructureMember(0, typeSystem.simpleStruct, null, "", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_RecursiveDefinition() {
    TypeMember.createStructureMember(0, typeSystem.simpleStruct, typeSystem.simpleStruct, "", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NegativeNumberElements() {
    TypeMember.createArrayMember(0, typeSystem.simpleStruct, typeSystem.intType, "", -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NegativeOffset() {
    TypeMember.createStructureMember(0, typeSystem.simpleStruct, typeSystem.intType, "", -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NegativeIndex() {
    TypeMember.createFunctionPrototypeMember(0, typeSystem.simpleStruct, typeSystem.intType, "",
        -1);
  }

  @Test
  public void testCompareTo_Irreflexivity() {
    // !(A < A)
    Assert.assertFalse(memberOffset0Id0.compareTo(memberOffset0Id0) < 0);
    Assert.assertFalse(memberOffset1Id0.compareTo(memberOffset1Id0) < 0);
    Assert.assertFalse(memberOffset0Id1.compareTo(memberOffset0Id1) < 0);
    Assert.assertFalse(memberOffset1Id1.compareTo(memberOffset1Id1) < 0);
  }

  @Test
  public void testCompareTo_AntiSymmetry() {
    // A < B => !(B < A)
    Assert.assertTrue(memberOffset0Id0.compareTo(memberOffset1Id0) < 0);
    Assert.assertFalse(memberOffset1Id0.compareTo(memberOffset0Id0) < 0);
    Assert.assertTrue(memberOffset0Id0.compareTo(memberOffset0Id1) < 0);
    Assert.assertFalse(memberOffset0Id1.compareTo(memberOffset0Id0) < 0);
  }

  @Test
  public void testCompareTo_Transitivity() {
    // A < B && B < C => A < C.
    Assert.assertTrue(memberOffset0Id0.compareTo(memberOffset1Id0) < 0);
    Assert.assertTrue(memberOffset1Id0.compareTo(memberOffset1Id1) < 0);
    Assert.assertTrue(memberOffset0Id0.compareTo(memberOffset1Id1) < 0);
  }
}
