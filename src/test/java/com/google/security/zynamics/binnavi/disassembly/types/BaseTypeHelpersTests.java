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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeHelpers.WalkResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class holds tests related to the BaseTypeHelpers.java.
 */
@RunWith(JUnit4.class)
public class BaseTypeHelpersTests {
  private TestTypeSystem typeSystem;

  @Before
  public void initializeTypeSystem() throws CouldntLoadDataException {
    typeSystem = new TestTypeSystem(new TypeManager(new TypeManagerMockBackend()));
  }

  @Test(expected = NullPointerException.class)
  public void testFindMemberArguments1() {
    BaseTypeHelpers.findMember(null, 0);
  }

  @Test
  public void testFindMemberArguments2() {
    WalkResult result = BaseTypeHelpers.findMember(typeSystem.uintType, -1);
    assertEquals("", result.getPathString());
    assertFalse(result.isValid());
    assertNull(result.getPath());
    assertNull(result.getMember());
  }

  @Test
  public void testFindMemberArrayType1() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.uintArrayType, 0);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertNotNull(result.getMember());
    assertNotNull(result.getPath());
    assertEquals(typeSystem.uintArrayTypeMember, result.getMember());
    assertEquals(Lists.newArrayList(typeSystem.uintArrayType), result.getPath());
    assertEquals("unsigned int[0]", result.getPathString());
  }

  @Test
  public void testFindMemberArrayType2() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.uintArrayType, 32);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertNotNull(result.getMember());
    assertEquals(typeSystem.uintArrayTypeMember, result.getMember());
    assertEquals(Lists.newArrayList(typeSystem.uintArrayTypeMember), result.getPath());
    assertEquals("unsigned int[1]", result.getPathString());
  }

  @Test
  public void testFindMemberArrayType3() {
    final WalkResult result = BaseTypeHelpers.findMember(
        typeSystem.uintArrayType, typeSystem.uintArrayType.getBitSize() + 1);
    assertTrue(result.isValid());
    assertEquals("unsigned int[10]+1", result.getPathString());
    assertEquals(Lists.newArrayList(typeSystem.uintArrayTypeMember), result.getPath());
    assertEquals(typeSystem.uintArrayTypeMember, result.getMember());
  }

  @Test
  public void testFindMemberAtomicType() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.uintType, 1);
    assertNotNull(result);
    assertFalse(result.isValid());
    assertNull(result.getMember());
    assertNull(result.getPath());
    assertEquals("", result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType1() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 0);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.nsIntMember, result.getMember());
    assertEquals(Lists.newArrayList(typeSystem.dnsNestedStructMember, typeSystem.nsIntMember),
        result.getPath());
    assertEquals(
        "DoubleNestedStruct.dns_nested_struct_member.ns_int_member", result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType2() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 32);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssIntMember, result.getMember());
    assertEquals(Lists.newArrayList(
        typeSystem.dnsNestedStructMember, typeSystem.nsSimpleStructMember, typeSystem.ssIntMember),
        result.getPath());
    assertEquals(
        "DoubleNestedStruct.dns_nested_struct_member.ns_simple_struct_member.ss_int_member",
        result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType3() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 64);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssUintMember, result.getMember());
    assertEquals(
        "DoubleNestedStruct.dns_nested_struct_member.ns_simple_struct_member.ss_uint_member",
        result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType4() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 96);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssArrayMember, result.getMember());
    assertEquals(Lists.newArrayList(
        typeSystem.dnsNestedStructMember, typeSystem.nsSimpleStructMember,
        typeSystem.ssArrayMember), result.getPath());
    assertEquals(typeSystem.doubleNestedStruct.getName() + "."
        + typeSystem.dnsNestedStructMember.getName() + "."
        + typeSystem.nsSimpleStructMember.getName() + "." + typeSystem.ssArrayMember.getName()
        + "[0]", result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType5() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 128);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssArrayMember, result.getMember());
    assertEquals(Lists.newArrayList(
        typeSystem.dnsNestedStructMember, typeSystem.nsSimpleStructMember,
        typeSystem.ssArrayMember), result.getPath());
    assertEquals(typeSystem.doubleNestedStruct.getName() + "."
        + typeSystem.dnsNestedStructMember.getName() + "."
        + typeSystem.nsSimpleStructMember.getName() + "." + typeSystem.ssArrayMember.getName()
        + "[1]", result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType6() {
    final WalkResult result =
        BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 96 + 320 - 32);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssArrayMember, result.getMember());
    assertEquals(Lists.newArrayList(
        typeSystem.dnsNestedStructMember, typeSystem.nsSimpleStructMember,
        typeSystem.ssArrayMember), result.getPath());
    assertEquals(typeSystem.doubleNestedStruct.getName() + "."
        + typeSystem.dnsNestedStructMember.getName() + "."
        + typeSystem.nsSimpleStructMember.getName() + "." + typeSystem.ssArrayMember.getName()
        + "[9]", result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType7() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 96 + 320);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.dnsIntMember, result.getMember());
    assertEquals(Lists.newArrayList(typeSystem.dnsIntMember),
        result.getPath());
    assertEquals(typeSystem.doubleNestedStruct.getName() + "." + typeSystem.dnsIntMember.getName(),
        result.getPathString());
  }

  @Test
  public void testFindMemberNestedStructType8() {
    final WalkResult result =
        BaseTypeHelpers.findMember(typeSystem.doubleNestedStruct, 96 + 320 + 32);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.dnsPointerMember, result.getMember());
    assertEquals(Lists.newArrayList(typeSystem.dnsPointerMember),
        result.getPath());
    assertEquals(
        typeSystem.doubleNestedStruct.getName() + "." + typeSystem.dnsPointerMember.getName(),
        result.getPathString());
  }

  @Test
  public void testFindMemberPointerType1() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.uintPointerType, 1);
    assertNotNull(result);
    assertFalse(result.isValid());
    assertNull(result.getMember());
    assertNull(result.getPath());
    assertEquals("", result.getPathString());
  }

  @Test
  public void testFindMemberStructType1() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.simpleStruct, 0);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssIntMember, result.getMember());
    assertEquals(
        Lists.newArrayList(typeSystem.ssIntMember), result.getPath());
    assertEquals("SimpleStruct.ss_int_member", result.getPathString());
  }

  @Test
  public void testFindMemberStructType2() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.simpleStruct, 32);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssUintMember, result.getMember());
    assertEquals(
        Lists.newArrayList(typeSystem.ssUintMember), result.getPath());
    assertEquals("SimpleStruct.ss_uint_member", result.getPathString());
  }

  @Test
  public void testFindMemberStructType3() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.simpleStruct, 64);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssArrayMember, result.getMember());
    assertEquals(
        Lists.newArrayList(typeSystem.ssArrayMember), result.getPath());
    assertEquals("SimpleStruct.ss_array_member[0]", result.getPathString());
  }

  @Test
  public void testFindMemberStructType4() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.simpleStruct, 96);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssArrayMember, result.getMember());
    assertEquals(
        Lists.newArrayList(typeSystem.ssArrayMember), result.getPath());
    assertEquals("SimpleStruct.ss_array_member[1]", result.getPathString());
  }

  @Test
  public void testFindMemberStructType5() {
    final WalkResult result = BaseTypeHelpers.findMember(typeSystem.simpleStruct, 352);
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals(typeSystem.ssArrayMember, result.getMember());
    assertEquals(
        Lists.newArrayList(typeSystem.ssArrayMember), result.getPath());
    assertEquals("SimpleStruct.ss_array_member[9]", result.getPathString());
  }

  @Test(expected = NullPointerException.class)
  public void testGetArrayElementByteSize1() {
    BaseTypeHelpers.getArrayElementByteSize(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetArrayElementByteSize2() {
    BaseTypeHelpers.getArrayElementByteSize(typeSystem.doubleNestedStruct);
  }

  @Test
  public void testGetArrayElementByteSize3() {
    assertEquals(4, BaseTypeHelpers.getArrayElementByteSize(typeSystem.uintArrayType));
  }

  @Test(expected = NullPointerException.class)
  public void testIsValidOffset1() {
    BaseTypeHelpers.isValidOffset(null, 0);
  }

  @Test
  public void testIsValidOffset2() {
    assertFalse(BaseTypeHelpers.isValidOffset(typeSystem.simpleStruct, -1));
  }

  @Test
  public void testIsValidOffset3() {
    assertFalse(BaseTypeHelpers.isValidOffset(typeSystem.doubleNestedStruct, 123));
  }

  @Test
  public void testIsValidOffset4() {
    assertFalse(BaseTypeHelpers.isValidOffset(typeSystem.doubleNestedStruct, 100000));
  }

}
