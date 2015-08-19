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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.yfileswrap.disassembly.types.TypeDependenceGraph;
import com.google.security.zynamics.binnavi.yfileswrap.disassembly.types.TypeDependenceGraph.DependenceResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

/**
 * Test suite for the type dependence graph class.
 */
@RunWith(JUnit4.class)
public class TypeDependenceGraphTests {
  private TestTypeSystem typeSystem;
  private TypeDependenceGraph dependenceGraph;

  @Before
  public void initializeTypeSystem() throws CouldntLoadDataException {
    typeSystem = new TestTypeSystem(new TypeManager(new TypeManagerMockBackend()));
    dependenceGraph = new TypeDependenceGraph(typeSystem.getTypes(), typeSystem.getTypeMembers());
  }

  @Test
  public void testAddMember() {
    final ImmutableSet<BaseType> affectedTypes =
        dependenceGraph.addMember(typeSystem.simpleStruct, typeSystem.intType).getAffectedTypes();
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.simpleStruct, typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct, typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testAddMemberNoDependency() {
    final ImmutableSet<BaseType> affectedTypes = dependenceGraph.addMember(
        typeSystem.doubleNestedStruct, typeSystem.intType).getAffectedTypes();
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.doubleNestedStruct, typeSystem.complexUnion),
        affectedTypes);
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullMember() {
    final TypeDependenceGraph dependenceGraph = new TypeDependenceGraph(
        ImmutableList.<BaseType>builder().build(), ImmutableList.<TypeMember>builder().build());
    dependenceGraph.addMember(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullType() {
    final TypeDependenceGraph dependenceGraph = new TypeDependenceGraph(
        ImmutableList.<BaseType>builder().build(), ImmutableList.<TypeMember>builder().build());
    dependenceGraph.addType(null);
  }

  @Test
  public void testAddType() {
    final BaseType newCompoundType =
        new BaseType(100, "new_compound_type", 0, false, BaseTypeCategory.STRUCT);
    dependenceGraph.addType(newCompoundType);
    final ImmutableSet<BaseType> affectedTypes =
        dependenceGraph.addMember(newCompoundType, typeSystem.simpleStruct).getAffectedTypes();
    assertEquals(ImmutableSet.<BaseType>of(newCompoundType), affectedTypes);

    final ImmutableSet<BaseType> newAffectedTypes =
        dependenceGraph.addMember(typeSystem.simpleStruct, typeSystem.intType).getAffectedTypes();
    assertEquals(ImmutableSet.<BaseType>of(newCompoundType, typeSystem.simpleStruct,
        typeSystem.nestedStruct, typeSystem.doubleNestedStruct, typeSystem.complexUnion),
        newAffectedTypes);
  }

  @Test
  public void testDeleteFooType() {
    final ImmutableSet<BaseType> affectedTypes =
        dependenceGraph.deleteType(typeSystem.doubleNestedStruct);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.doubleNestedStruct, typeSystem.complexUnion),
        affectedTypes);
  }

  @Test
  public void testDeleteIntType() {
    final ImmutableSet<BaseType> affectedTypes = dependenceGraph.deleteType(typeSystem.intType);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.intType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testDeleteMember() {
    final TypeMember member = typeSystem.simpleStruct.iterator().next();
    final ImmutableSet<BaseType> affectedTypes = dependenceGraph.deleteMember(member);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.simpleStruct, typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct, typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testDeleteMemberTwoDependencies() {
    final TypeMember member = typeSystem.nestedStruct.iterator().next();
    final ImmutableSet<BaseType> affectedTypes = dependenceGraph.deleteMember(member);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.nestedStruct, typeSystem.doubleNestedStruct,
        typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testDeleteSimpleStruct() {
    final ImmutableSet<BaseType> affectedTypes =
        dependenceGraph.deleteType(typeSystem.simpleStruct);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.simpleStruct, typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct, typeSystem.complexUnion), affectedTypes);
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteNullType() {
    dependenceGraph.deleteType(null);
  }

  @Test
  public void testDeleteUintType() {
    final ImmutableSet<BaseType> affectedTypes = dependenceGraph.deleteType(typeSystem.uintType);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.uintType,
        typeSystem.uintArrayType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testIsContainedIn() {
    assertTrue(dependenceGraph.isTypeContainedIn(typeSystem.simpleStruct, typeSystem.intType));
    assertTrue(dependenceGraph.isTypeContainedIn(typeSystem.simpleStruct, typeSystem.uintType));
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.simpleStruct, typeSystem.uintArrayType));
  }

  @Test
  public void testIsContainedInDoubleNested() {
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.doubleNestedStruct, typeSystem.intType));
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.doubleNestedStruct, typeSystem.uintType));
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.doubleNestedStruct, typeSystem.uintArrayType));
    assertTrue(dependenceGraph.isTypeContainedIn(typeSystem.doubleNestedStruct,
        typeSystem.uintPointerType));
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.doubleNestedStruct, typeSystem.simpleStruct));
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.doubleNestedStruct, typeSystem.nestedStruct));
  }

  @Test
  public void testIsContainedInNested() {
    assertTrue(dependenceGraph.isTypeContainedIn(typeSystem.nestedStruct, typeSystem.intType));
    assertTrue(dependenceGraph.isTypeContainedIn(typeSystem.nestedStruct, typeSystem.uintType));
    assertTrue(
        dependenceGraph.isTypeContainedIn(typeSystem.nestedStruct, typeSystem.uintArrayType));
    assertTrue(dependenceGraph.isTypeContainedIn(typeSystem.nestedStruct, typeSystem.simpleStruct));
  }

  @Test(expected = NullPointerException.class)
  public void testNullInstantiation() {
    new TypeDependenceGraph(null, null);
  }

  @Test
  public void testRecursiveType() {
    final DependenceResult result =
        dependenceGraph.addMember(typeSystem.simpleStruct, typeSystem.doubleNestedStruct);
    assertFalse(result.isValid());
  }

  @Test
  public void testRecursiveTypeWithSelf() {
    final DependenceResult result =
        dependenceGraph.addMember(typeSystem.simpleStruct, typeSystem.simpleStruct);
    assertFalse(result.isValid());
  }

  @Test
  public void testUpdateDoubleNestedStructMembers() {
    final BaseType newMemberType = typeSystem.intType;
    final BaseType parentType = typeSystem.doubleNestedStruct;
    for (final TypeMember member : parentType) {
      final ImmutableSet<BaseType> affectedTypes = dependenceGraph.updateMember(parentType,
          member.getBaseType(), newMemberType).getAffectedTypes();
      assertEquals(
          ImmutableSet.<BaseType>of(typeSystem.doubleNestedStruct, typeSystem.complexUnion),
          affectedTypes);
    }
  }

  @Test
  public void testUpdateIntType() {
    final Set<BaseType> affectedTypes = dependenceGraph.updateType(typeSystem.intType);
    assertEquals(Sets.newHashSet(typeSystem.intType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testUpdateSimpleStructMembers() {
    for (final TypeMember member : typeSystem.simpleStruct) {
      final Set<BaseType> affectedTypes = dependenceGraph.updateMember(member.getParentType(),
          member.getBaseType(), typeSystem.intType).getAffectedTypes();
      assertEquals(ImmutableSet.<BaseType>of(typeSystem.simpleStruct, typeSystem.nestedStruct,
          typeSystem.doubleNestedStruct, typeSystem.complexUnion), affectedTypes);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testUpdateNullMember() {
    dependenceGraph.updateMember(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testUpdateNullType() {
    dependenceGraph.updateType(null);
  }

  @Test
  public void testUpdateTypeTransitive() {
    // "NestedStruct" is only transitively reachable from "unsigned int" so this test is
    // conceptually different from testUpdateType().
    final Set<BaseType> affectedTypes = dependenceGraph.updateType(typeSystem.uintType);
    assertEquals(Sets.newHashSet(typeSystem.uintType,
        typeSystem.uintArrayType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testUpdateUintType() {
    final ImmutableSet<BaseType> affectedTypes = dependenceGraph.updateType(typeSystem.uintType);
    assertEquals(ImmutableSet.<BaseType>of(typeSystem.uintType,
        typeSystem.uintArrayType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion), affectedTypes);
  }

  @Test
  public void testUpdateNestedStructMembers() {
    for (final TypeMember member : typeSystem.nestedStruct) {
      final ImmutableSet<BaseType> affectedTypes = dependenceGraph.updateMember(
          member.getParentType(), member.getBaseType(), typeSystem.intType).getAffectedTypes();
      assertEquals(ImmutableSet.<BaseType>of(typeSystem.nestedStruct, typeSystem.doubleNestedStruct,
          typeSystem.complexUnion), affectedTypes);
    }
  }
}
