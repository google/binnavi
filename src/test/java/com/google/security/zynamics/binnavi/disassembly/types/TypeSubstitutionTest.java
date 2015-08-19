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

import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * Tests for non-trivial public methods of {@link TypeSubstitution}.
 */
@RunWith(JUnit4.class)
public class TypeSubstitutionTest {
  private TestTypeSystem typeSystem;

  @Before
  public void initializeTypeSystem() throws CouldntLoadDataException {
    typeSystem = new TestTypeSystem(new TypeManager(new TypeManagerMockBackend()));
  }

  private static TypeSubstitution createSubstitution(final BaseType baseType, final int offset) {
    return new TypeSubstitution(
        mock(INaviOperandTreeNode.class), baseType, 0, 0, offset, new CAddress(123));
  }

  private static TypeSubstitution createSubstitution(
      final BaseType baseType, final List<TypeMember> memberPath) {
    return new TypeSubstitution(
        mock(INaviOperandTreeNode.class), baseType, memberPath, 0, 0, 0, new CAddress(123));
  }

  @Test
  public void testGenerateTypeString_Array() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.uintArrayType, 0), 0);
    Assert.assertTrue(typeString.equals("unsigned int[0]"));
  }

  @Test
  public void testGenerateTypeString_ArrayOverflow() {
    final String typeString = TypeSubstitution.generateTypeString(
        createSubstitution(typeSystem.uintArrayType, 0),
        typeSystem.uintArrayType.getByteSize() + 1);
    Assert.assertEquals("unsigned int[10]" + "+1", typeString);
  }

  @Test
  public void testGenerateTypeString_ArrayUnderflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.uintArrayType, 0), -1);
    Assert.assertEquals("unsigned int[0]" + "-1", typeString);
  }

  @Test
  public void testGenerateTypeString_Atomic() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.intType, 0), 0);
    Assert.assertEquals(typeSystem.intType.getName(), typeString);
  }

  @Test
  public void testGenerateTypeString_AtomicOverflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.intType, 0), 1);
    Assert.assertEquals(typeSystem.intType.getName() + "+1", typeString);
  }

  @Test
  public void testGenerateTypeString_AtomicUnderflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.intType, 0), -1);
    Assert.assertEquals(typeSystem.intType.getName() + "-1", typeString);
  }

  @Test
  public void testGenerateTypeString_Pointer() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.uintPointerType, 0), 0);
    Assert.assertEquals(typeSystem.uintPointerType.getName(), typeString);
  }

  @Test
  public void testGenerateTypeString_PointerOverflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.uintPointerType, 0), 1);
    Assert.assertEquals(typeSystem.uintPointerType.getName() + "+1", typeString);
  }

  @Test
  public void testGenerateTypeString_PointerUnderflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.uintPointerType, 0), -1);
    Assert.assertEquals(typeSystem.uintPointerType.getName() + "-1", typeString);
  }

  @Test
  public void testGenerateTypeString_Struct() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.nestedStruct, 0), 8);
    Assert.assertEquals("NestedStruct.ns_simple_struct_member.ss_uint_member", typeString);
  }

  @Test
  public void testGenerateTypeString_Struct2() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.nestedStruct, 0), 16);
    Assert.assertEquals("NestedStruct.ns_simple_struct_member.ss_array_member[1]", typeString);
  }

  @Test
  public void testGenerateTypeString_StructOverflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.nestedStruct, 0), 1);
    Assert.assertEquals(typeSystem.nestedStruct.getName() + "+1", typeString);
  }

  @Test
  public void testGenerateTypeString_StructUnderflow() {
    final String typeString =
        TypeSubstitution.generateTypeString(createSubstitution(typeSystem.nestedStruct, 0), -1);
    Assert.assertEquals(typeSystem.nestedStruct.getName() + "-1", typeString);
  }

  @Test
  public void testGenerateTypeString_Union() {
    final String typeString = TypeSubstitution.generateTypeString(
        createSubstitution(typeSystem.simpleUnion, Lists.newArrayList(typeSystem.suUintMember)), 0);
    Assert.assertEquals(
        String.format("%s.%s", typeSystem.simpleUnion.getName(), typeSystem.suUintMember.getName()),
        typeString);
  }

  @Test
  public void testGenerateTypeString_UnionOverflow() {
    final String typeString = TypeSubstitution.generateTypeString(
        createSubstitution(typeSystem.simpleUnion, Lists.newArrayList(typeSystem.suUintMember)), 1);
    Assert.assertEquals(String.format(
        "%s.%s+1", typeSystem.simpleUnion.getName(), typeSystem.suUintMember.getName()),
        typeString);
  }

  @Test
  public void testGenerateTypeString_UnionUnderflow() {
    final String typeString = TypeSubstitution.generateTypeString(
        createSubstitution(typeSystem.simpleUnion, Lists.newArrayList(typeSystem.suUintMember)),
        -1);
    Assert.assertEquals(String.format(
        "%s.%s-1", typeSystem.simpleUnion.getName(), typeSystem.suUintMember.getName()),
        typeString);
  }

  @Test
  public void testGenerateTypeString_ComplexUnion() {
    final String typeString = TypeSubstitution.generateTypeString(createSubstitution(
        typeSystem.complexUnion, Lists.newArrayList(
            typeSystem.cuDoubleNestedStructMember, typeSystem.dnsNestedStructMember,
            typeSystem.nsSimpleStructMember, typeSystem.ssUintMember)), 0);
    Assert.assertEquals(String.format("%s.%s.%s.%s.%s", typeSystem.complexUnion.getName(),
        typeSystem.cuDoubleNestedStructMember.getName(), typeSystem.dnsNestedStructMember.getName(),
        typeSystem.nsSimpleStructMember.getName(), typeSystem.ssUintMember.getName()), typeString);
  }

  @Test
  public void testGenerateTypeString_ComplexUnionOverflow() {
    final String typeString = TypeSubstitution.generateTypeString(createSubstitution(
        typeSystem.complexUnion, Lists.newArrayList(
            typeSystem.cuDoubleNestedStructMember, typeSystem.dnsNestedStructMember,
            typeSystem.nsSimpleStructMember, typeSystem.ssUintMember)), +1);
    Assert.assertEquals(String.format("%s.%s.%s.%s.%s+1", typeSystem.complexUnion.getName(),
        typeSystem.cuDoubleNestedStructMember.getName(), typeSystem.dnsNestedStructMember.getName(),
        typeSystem.nsSimpleStructMember.getName(), typeSystem.ssUintMember.getName()), typeString);
  }

  @Test
  public void testGenerateTypeString_ComplexUnionUnderflow() {
    final String typeString = TypeSubstitution.generateTypeString(createSubstitution(
        typeSystem.complexUnion, Lists.newArrayList(
            typeSystem.cuDoubleNestedStructMember, typeSystem.dnsNestedStructMember,
            typeSystem.nsSimpleStructMember, typeSystem.ssUintMember)), -1);
    Assert.assertEquals(String.format("%s.%s.%s.%s.%s-1", typeSystem.complexUnion.getName(),
        typeSystem.cuDoubleNestedStructMember.getName(), typeSystem.dnsNestedStructMember.getName(),
        typeSystem.nsSimpleStructMember.getName(), typeSystem.ssUintMember.getName()), typeString);
  }
}
