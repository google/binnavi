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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Iterator;
import java.util.List;

/**
 * Tests all methods related to {@link BaseType base types} without going through the type manager.
 */
@RunWith(JUnit4.class)
public class BaseTypeTest {

  private TestTypeSystem typeSystem;
  private BaseType testStruct;
  private TypeMember testMember0;
  private TypeMember testMember1;
  private TypeMember testMember2;
  private TypeMember testMember3;

  private static final String NAME = "NAME";

  private void addMembersAndVerifyOperation() {
    final List<TypeMember> members =
        Lists.newArrayList(testMember0, testMember1, testMember2, testMember3);
    for (TypeMember member : members) {
      testStruct.addMember(member);
    }
    assertEquals(testMember0.getBitSize() + testMember1.getBitSize() + testMember2.getBitSize()
        + testMember3.getBitSize(), testStruct.getBitSize());
    assertEquals(4, testStruct.getMemberCount());
    assertEquals(testMember3, testStruct.getLastMember());
  }

  @Before
  public void initializeTypeSystem() throws CouldntLoadDataException {
    typeSystem = new TestTypeSystem(new TypeManager(new TypeManagerMockBackend()));
    // Note: type ids must not collide with the ones from RawTestTypeSystem so we start at 100.
    int typeId = 100;
    testStruct = new BaseType(++typeId, "test_struct", 0, false, BaseTypeCategory.STRUCT);
    testMember0 = TypeMember.createStructureMember(++typeId, testStruct, typeSystem.uintType,
        "testMember0", 0);
    testMember1 = TypeMember.createStructureMember(++typeId, testStruct, typeSystem.uintType,
        "testMember1", typeSystem.uintType.getBitSize());
    testMember2 = TypeMember.createStructureMember(++typeId, testStruct, typeSystem.uintType,
        "testMember2", testMember1.getBitOffset().get() + typeSystem.uintType.getBitSize());
    testMember3 = TypeMember.createStructureMember(++typeId, testStruct, typeSystem.uintType,
        "testMember3", testMember2.getBitOffset().get() + typeSystem.uintType.getBitSize());
  }

  @Test(expected = NullPointerException.class)
  public void testAddMember_NullMember() {
    testStruct.addMember(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddMember_OtherParent() {
    testStruct.addMember(typeSystem.ssIntMember);
  }

  @Test
  public void testAddMember3() {
    testStruct.addMember(testMember0);
    assertEquals(testMember0.getBitSize(), testStruct.getBitSize());
    assertEquals(1, testStruct.getMemberCount());
    assertEquals(testMember0, testStruct.getLastMember());
  }

  @Test
  public void testAddMember4() {
    testStruct.addMember(testMember0);
    testStruct.addMember(testMember0);
    testStruct.addMember(testMember0);
    testStruct.addMember(testMember0);
    assertEquals(testMember0.getBitSize(), testStruct.getBitSize());
    assertEquals(1, testStruct.getMemberCount());
    assertEquals(testMember0, testStruct.getLastMember());
  }

  @Test
  public void testAddMember5() {
    testStruct.addMember(testMember0);
    testStruct.addMember(testMember1);
    testStruct.addMember(testMember2);
    testStruct.addMember(testMember3);
    assertEquals(testMember0.getBitSize() + testMember1.getBitSize() + testMember2.getBitSize()
        + testMember3.getBitSize(), testStruct.getBitSize());
    assertEquals(4, testStruct.getMemberCount());
    assertEquals(testMember3, testStruct.getLastMember());
  }

  @Test
  public void testAddMembers3() {
    addMembersAndVerifyOperation();
  }

  @Test(expected = NullPointerException.class)
  public void testAppendToPointerHierarchy1() {
    BaseType.appendToPointerHierarchy(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testAppendToPointerHierarchy2() {
    BaseType.appendToPointerHierarchy(typeSystem.uintType, null);
  }

  @Test
  public void testAppendToPointerHierarchy3() {
    assertEquals(0, typeSystem.uintType.getPointerLevel());
    assertEquals(0, typeSystem.intType.getPointerLevel());

    BaseType.appendToPointerHierarchy(typeSystem.uintType, typeSystem.intType);
    assertEquals(typeSystem.uintType, typeSystem.intType.pointsTo());
    assertEquals(typeSystem.intType, typeSystem.uintType.pointedToBy());
    assertEquals(1, typeSystem.intType.getPointerLevel());
    assertEquals(0, typeSystem.uintType.getPointerLevel());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAppendToPointerHierarchy4() {
    assertEquals(0, typeSystem.uintType.getPointerLevel());
    assertEquals(0, typeSystem.intType.getPointerLevel());

    BaseType.appendToPointerHierarchy(typeSystem.uintType, typeSystem.intType);
    assertEquals(typeSystem.uintType, typeSystem.intType.pointsTo());
    assertEquals(typeSystem.intType, typeSystem.uintType.pointedToBy());
    assertEquals(1, typeSystem.intType.getPointerLevel());
    assertEquals(0, typeSystem.uintType.getPointerLevel());
    BaseType.appendToPointerHierarchy(typeSystem.intType, typeSystem.uintType);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBaseTypeConstructor1() {
    new BaseType(-1, null, -1, false, BaseTypeCategory.ATOMIC);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBaseTypeConstructor2() {
    new BaseType(0, null, -1, false, BaseTypeCategory.ATOMIC);
  }

  @Test(expected = NullPointerException.class)
  public void testBaseTypeConstructor3() {
    new BaseType(1, null, -1, false, BaseTypeCategory.ATOMIC);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBaseTypeConstructor4() {
    new BaseType(1, NAME, -1, false, BaseTypeCategory.ATOMIC);
  }

  @Test
  public void testBaseTypeConstructor5() {
    final BaseType baseType = new BaseType(1, NAME, 4, false, BaseTypeCategory.ATOMIC);

    // explicit

    assertEquals(1, baseType.getId());
    assertEquals(NAME, baseType.getName());
    assertEquals(4, baseType.getBitSize());
    assertEquals(1, baseType.getByteSize());
    assertEquals(false, baseType.isSigned());

    // implicit

    assertNull(baseType.pointedToBy());
    assertNull(baseType.pointsTo());
    assertFalse(baseType.isStackFrame());
  }

  @Test
  public void testDeleteMember1() {
    addMembersAndVerifyOperation();
    testStruct.deleteMember(testMember0);
    assertEquals(testMember0.getBaseType().getBitSize() + testMember1.getBaseType().getBitSize()
        + testMember2.getBaseType().getBitSize() + testMember3.getBaseType().getBitSize(),
        testStruct.getBitSize());
    assertEquals(3, testStruct.getMemberCount());
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteMember2() {
    addMembersAndVerifyOperation();
    testStruct.deleteMember(null);
  }

  @Test
  public void testSetSize_Atomic() {
    typeSystem.intType.setSize(123);
    assertEquals(123, typeSystem.intType.getBitSize());
  }

  @Test
  public void testSetSize_Pointer() {
    typeSystem.uintPointerType.setSize(123);
    assertEquals(123, typeSystem.uintPointerType.getBitSize());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSize_Array() {
    typeSystem.uintArrayType.setSize(100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSize_Struct() {
    typeSystem.simpleStruct.setSize(123);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSize_Union() {
    typeSystem.simpleUnion.setSize(123);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSize_Prototype() {
    typeSystem.voidFunctionPrototype.setSize(123);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSize_Negative() {
    typeSystem.uintType.setSize(-1);
  }

  @Test
  public void testSetSize_Zero() {
    typeSystem.uintType.setSize(0);
    assertEquals(0, typeSystem.uintType.getBitSize());
  }

  @Test
  public void testGetBitSize_Atomic() {
    assertEquals(32, typeSystem.intType.getBitSize());
  }

  @Test
  public void testGetBitSize_Pointer() {
    assertEquals(32, typeSystem.uintPointerType.getBitSize());
  }

  @Test
  public void testGetBitSize_Array() {
    assertEquals(10 * 32, typeSystem.uintArrayType.getBitSize());
  }

  @Test
  public void testGetBitSize_Struct() {
    assertEquals(10 * 32 + 32 + 32, typeSystem.simpleStruct.getBitSize());
  }

  @Test
  public void testGetBitSize_Union() {
    assertEquals(typeSystem.uintArrayType.getBitSize(), typeSystem.simpleUnion.getBitSize());
    assertEquals(typeSystem.doubleNestedStruct.getBitSize(), typeSystem.complexUnion.getBitSize());
  }

  @Test
  public void testGetBitSize_Prototype() {
    assertEquals(0, typeSystem.voidFunctionPrototype.getBitSize());
  }

  @Test
  public void testGetByteSize() {
    assertEquals(4, typeSystem.uintType.getByteSize());
  }

  @Test
  public void testGetLastMember1() {
    assertNull(typeSystem.uintType.getLastMember());
  }

  @Test
  public void testGetLastMember2() {
    addMembersAndVerifyOperation();
    assertEquals(testMember3, testStruct.getLastMember());
  }

  @Test
  public void testGetMemberCount() {
    assertEquals(0, typeSystem.uintType.getMemberCount());
    addMembersAndVerifyOperation();
    assertEquals(4, testStruct.getMemberCount());
  }

  @Test
  public void testGetPointerLevel() {
    assertEquals(0, typeSystem.uintType.getPointerLevel());
    assertEquals(0, typeSystem.intType.getPointerLevel());
    BaseType.appendToPointerHierarchy(typeSystem.uintType, typeSystem.intType);
    assertEquals(0, typeSystem.uintType.getPointerLevel());
    assertEquals(1, typeSystem.intType.getPointerLevel());
  }

  @Test(expected = NullPointerException.class)
  public void testGetPointerTypeName1() {
    BaseType.getPointerTypeName(null, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetPointerTypeName2() {
    BaseType.getPointerTypeName(typeSystem.uintType, 0);
  }

  @Test
  public void testGetPointerTypeName3() {
    final String pointerName = BaseType.getPointerTypeName(typeSystem.uintType, 10);
    assertEquals(typeSystem.uintType.getName() + " " + Strings.repeat("*", 10), pointerName);
  }

  @Test(expected = NullPointerException.class)
  public void testGetValueType1() {
    BaseType.getValueType(null);
  }

  @Test
  public void testGetValueType2() {
    BaseType.appendToPointerHierarchy(typeSystem.uintType, typeSystem.intType);
    assertEquals(typeSystem.uintType, BaseType.getValueType(typeSystem.intType));
  }

  @Test(expected = NullPointerException.class)
  public void testGetValueTypeName1() {
    BaseType.getValueTypeName(null);
  }

  @Test
  public void testGetValueTypeName2() {
    BaseType.appendToPointerHierarchy(typeSystem.uintType, typeSystem.intType);
    assertEquals(typeSystem.uintType.getName(), BaseType.getValueTypeName(typeSystem.intType));
  }

  @Test
  public void testGetValueTypeName3() {
    assertEquals(typeSystem.intType.getName(), BaseType.getValueTypeName(typeSystem.intType));
  }

  @Test
  public void testHasMembers() {
    assertEquals(false, testStruct.hasMembers());
    addMembersAndVerifyOperation();
    assertEquals(true, testStruct.hasMembers());
  }

  @Test
  public void testIterator() {
    addMembersAndVerifyOperation();
    int offset = 0;
    for (final TypeMember member : testStruct) {
      Preconditions.checkArgument(member.getBitOffset().get() >= offset);
      offset = member.getBitOffset().get();
    }
  }

  @Test
  public void testMoveMemberInBetween() {
    typeSystem.simpleStruct.moveMembers(Sets.newTreeSet(typeSystem.ssIntMember), 32);
    assertEquals(Integer.valueOf(0), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(32), typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(Integer.valueOf(64), typeSystem.ssArrayMember.getBitOffset().get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoveMemberInvalid1() {
    typeSystem.simpleStruct.moveMembers(Sets.newTreeSet(typeSystem.ssIntMember), -100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoveMemberInvalid2() {
    typeSystem.simpleStruct.moveMembers(
        Sets.newTreeSet(typeSystem.ssIntMember, typeSystem.ssUintMember), 999);
  }

  @Test
  public void testMoveMembersToBeginning() {
    typeSystem.simpleStruct.moveMembers(
        Sets.newTreeSet(typeSystem.ssUintMember, typeSystem.ssArrayMember), -32);
    assertEquals(Integer.valueOf(0), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(32), typeSystem.ssArrayMember.getBitOffset().get());
    assertEquals(Integer.valueOf(352), typeSystem.ssIntMember.getBitOffset().get());
  }

  @Test
  public void testMoveMembersToBeginningReverse() {
    typeSystem.simpleStruct.moveMembers(
        Sets.newTreeSet(typeSystem.ssArrayMember, typeSystem.ssUintMember), -32);
    assertEquals(Integer.valueOf(0), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(32), typeSystem.ssArrayMember.getBitOffset().get());
    assertEquals(Integer.valueOf(352), typeSystem.ssIntMember.getBitOffset().get());
  }

  @Test
  public void testMoveMembersToEnd() {
    typeSystem.simpleStruct.moveMembers(
        Sets.<TypeMember>newTreeSet(typeSystem.ssIntMember, typeSystem.ssUintMember), 320);
    assertEquals(Integer.valueOf(320), typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(Integer.valueOf(352), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(0), typeSystem.ssArrayMember.getBitOffset().get());
  }

  @Test
  public void testMoveMembersToEndReverse() {
    typeSystem.simpleStruct.moveMembers(
        Sets.newTreeSet(typeSystem.ssUintMember, typeSystem.ssIntMember), 320);
    assertEquals(Integer.valueOf(320), typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(Integer.valueOf(352), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(0), typeSystem.ssArrayMember.getBitOffset().get());
  }

  @Test
  public void testMoveMemberToBeginning() {
    typeSystem.simpleStruct.moveMembers(Sets.newTreeSet(typeSystem.ssUintMember), -32);
    assertEquals(Integer.valueOf(0), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(32), typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(Integer.valueOf(64), typeSystem.ssArrayMember.getBitOffset().get());
  }

  @Test
  public void testMoveMemberToEnd() {
    typeSystem.simpleStruct.moveMembers(Sets.newTreeSet(typeSystem.ssIntMember), 64);
    assertEquals(Integer.valueOf(0), typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(Integer.valueOf(32), typeSystem.ssArrayMember.getBitOffset().get());
    assertEquals(Integer.valueOf(64), typeSystem.ssIntMember.getBitOffset().get());
  }

  @Test
  public void testSetIsStackFrame() {
    typeSystem.uintType.setIsStackFrame(true);
    assertEquals(true, typeSystem.uintType.isStackFrame());
    typeSystem.uintType.setIsStackFrame(false);
    assertEquals(false, typeSystem.uintType.isStackFrame());
  }

  @Test(expected = NullPointerException.class)
  public void testSetName1() {
    typeSystem.uintType.setName(null);
  }

  @Test
  public void testSetName2() {
    typeSystem.uintType.setName("NEW NAME");
    assertEquals("NEW NAME", typeSystem.uintType.getName());
  }

  @Test
  public void testSetSigned() {
    typeSystem.uintType.setSigned(true);
    assertEquals(true, typeSystem.uintType.isSigned());
    typeSystem.uintType.setSigned(false);
    assertEquals(false, typeSystem.uintType.isSigned());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSubsequentMembers_Underflow() {
    typeSystem.simpleStruct.getSubsequentMembersInclusive(-1);
  }

  @Test
  public void testGetSubsequentMembers_FirstMember() {
    ImmutableList<TypeMember> expectedMembers = ImmutableList.<TypeMember>of(typeSystem.ssIntMember,
        typeSystem.ssUintMember, typeSystem.ssArrayMember);
    assertEquals(expectedMembers, typeSystem.simpleStruct.getSubsequentMembersInclusive(0));
  }

  @Test
  public void testGetSubsequentMembers_SecondMember() {
    final ImmutableList<TypeMember> members = typeSystem.simpleStruct.getSubsequentMembersInclusive(
        typeSystem.ssUintMember.getBitOffset().get());
    final ImmutableList<TypeMember> expectedMembers =
        ImmutableList.<TypeMember>of(typeSystem.ssUintMember, typeSystem.ssArrayMember);
    assertEquals(expectedMembers, members);
  }

  @Test
  public void testGetSubsequentMembers_ThirdMember() {
    final ImmutableList<TypeMember> members = typeSystem.simpleStruct.getSubsequentMembersInclusive(
        typeSystem.ssArrayMember.getBitOffset().get());
    final ImmutableList<TypeMember> expectedMembers =
        ImmutableList.<TypeMember>of(typeSystem.ssArrayMember);
    assertEquals(expectedMembers, members);
  }

  @Test
  public void testGetSubsequentMembers_PastEnd() {
    final ImmutableList<TypeMember> members =
        typeSystem.simpleStruct.getSubsequentMembersInclusive(typeSystem.simpleStruct.getBitSize());
    assertEquals(ImmutableList.<TypeMember>of(), members);
  }

  @Test
  public void testMemberOrderingConsistency0() {
    typeSystem.ssIntMember.setOffset(Optional.<Integer>of(typeSystem.simpleStruct.getBitSize()));
    final Iterator<TypeMember> iterator = typeSystem.simpleStruct.iterator();
    assertEquals(typeSystem.ssUintMember, iterator.next());
    assertEquals(typeSystem.ssArrayMember, iterator.next());
    assertEquals(typeSystem.ssIntMember, iterator.next());
  }

  @Test
  public void testMemberOrderingConsistency1() {
    typeSystem.ssUintMember.setOffset(Optional.<Integer>of(typeSystem.simpleStruct.getBitSize()));
    final Iterator<TypeMember> iterator = typeSystem.simpleStruct.iterator();
    assertEquals(typeSystem.ssIntMember, iterator.next());
    assertEquals(typeSystem.ssArrayMember, iterator.next());
    assertEquals(typeSystem.ssUintMember, iterator.next());
  }

  @Test
  public void testMemberOrderingConsistency2() {
    typeSystem.ssArrayMember.setOffset(Optional.<Integer>of(typeSystem.simpleStruct.getBitSize()));
    final Iterator<TypeMember> iterator = typeSystem.simpleStruct.iterator();
    assertEquals(typeSystem.ssIntMember, iterator.next());
    assertEquals(typeSystem.ssUintMember, iterator.next());
    assertEquals(typeSystem.ssArrayMember, iterator.next());
  }
}
