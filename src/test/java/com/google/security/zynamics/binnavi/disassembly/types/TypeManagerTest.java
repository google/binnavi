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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

/**
 * Tests if the type manager's notification mechanism works for all operations on base types and
 * members.
 */
@RunWith(JUnit4.class)
public class TypeManagerTest {
  private TypeManager typeManager;
  private TestTypeSystem typeSystem;

  @Before
  public void initializeTypeSystem() throws CouldntLoadDataException {
    typeManager = new TypeManager(new TypeManagerMockBackend());
    typeSystem = new TestTypeSystem(typeManager);
  }

  @Test
  public void testCreateMember_Overlapping() throws CouldntSaveDataException {
    final ImmutableMap<BaseType, Integer> oldSizes = captureTypeSizes();
    final ImmutableMap<TypeMember, Integer> oldOffsets = captureMemberOffsets();
    final BaseType memberType = typeSystem.intType;
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final int offset = typeSystem.simpleStruct.getBitSize() - 1;
    final int sizeDelta = typeSystem.ssArrayMember.getBitSize() + memberType.getBitSize() - 1;
    final TypeMember newMember =
        typeManager.createStructureMember(typeSystem.simpleStruct, memberType, "narf", offset);
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberUpdated(typeSystem.ssArrayMember);
    expectedEvents.memberAdded(newMember);
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    assertEquals(expectedEvents, events);
    assertEquals(offset + typeSystem.intType.getBitSize() + typeSystem.ssArrayMember.getBitSize(),
        typeSystem.simpleStruct.getBitSize());

    // Check if all sizes were changed correctly.
    assertEquals(oldSizes.get(typeSystem.simpleStruct) + sizeDelta,
        typeSystem.simpleStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.nestedStruct) + sizeDelta,
        typeSystem.nestedStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.doubleNestedStruct) + sizeDelta,
        typeSystem.doubleNestedStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.simpleUnion),
        Integer.valueOf(typeSystem.simpleUnion.getBitSize()));
    assertEquals(typeSystem.doubleNestedStruct.getBitSize(), typeSystem.complexUnion.getBitSize());

    // Check if all member offsets were changed correctly.
    // SimpleStruct.
    assertEquals(oldOffsets.get(typeSystem.ssIntMember),
        typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssUintMember),
        typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssArrayMember) + sizeDelta,
        (int) typeSystem.ssArrayMember.getBitOffset().get());
    // NestedStruct.
    assertEquals(oldOffsets.get(typeSystem.nsIntMember),
        typeSystem.nsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.nsSimpleStructMember),
        typeSystem.nsSimpleStructMember.getBitOffset().get());
    // DoubleNestedStruct.
    assertEquals(oldOffsets.get(typeSystem.dnsNestedStructMember),
        typeSystem.dnsNestedStructMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsIntMember) + sizeDelta,
        (int) typeSystem.dnsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsPointerMember) + sizeDelta,
        (int) typeSystem.dnsPointerMember.getBitOffset().get());
    for (TypeMember member : typeSystem.simpleUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    for (TypeMember member : typeSystem.complexUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
  }

  @Test
  public void testDeleteMember_End() throws CouldntSaveDataException, CouldntDeleteException {
    final ImmutableMap<TypeMember, Integer> oldOffsets = captureMemberOffsets();
    final ImmutableMap<BaseType, Integer> oldSizes = captureTypeSizes();
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final TypeMember deletedMember = typeSystem.simpleStruct.getLastMember();
    final int sizeDelta = -deletedMember.getBitSize();
    typeManager.deleteMember(deletedMember);

    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    expectedEvents.memberDeleted(deletedMember);
    expectedEvents.typesUpdated(Sets.<BaseType>newHashSet(typeSystem.simpleStruct,
        typeSystem.nestedStruct, typeSystem.doubleNestedStruct, typeSystem.complexUnion));
    assertEquals(expectedEvents, events);

    // Check if all sizes were changed correctly.
    assertEquals(oldSizes.get(typeSystem.simpleStruct) + sizeDelta,
        typeSystem.simpleStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.nestedStruct) + sizeDelta,
        typeSystem.nestedStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.doubleNestedStruct) + sizeDelta,
        typeSystem.doubleNestedStruct.getBitSize());
    for (TypeMember member : typeSystem.simpleUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    assertEquals(oldSizes.get(typeSystem.simpleUnion),
        Integer.valueOf(typeSystem.simpleUnion.getBitSize()));
    for (TypeMember member : typeSystem.complexUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    assertEquals(typeSystem.doubleNestedStruct.getBitSize(), typeSystem.complexUnion.getBitSize());

    // Check if all member offsets were changed correctly.
    // SimpleStruct.
    assertEquals(oldOffsets.get(typeSystem.ssIntMember),
        typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssUintMember),
        typeSystem.ssUintMember.getBitOffset().get());
    // NestedStruct.
    assertEquals(oldOffsets.get(typeSystem.nsIntMember),
        typeSystem.nsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.nsSimpleStructMember),
        typeSystem.nsSimpleStructMember.getBitOffset().get());
    // DoubleNestedStruct.
    assertEquals(oldOffsets.get(typeSystem.dnsNestedStructMember),
        typeSystem.dnsNestedStructMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsIntMember) + sizeDelta,
        (int) typeSystem.dnsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsPointerMember) + sizeDelta,
        (int) typeSystem.dnsPointerMember.getBitOffset().get());
    for (TypeMember member : typeSystem.simpleUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    for (TypeMember member : typeSystem.complexUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
  }

  @Test
  public void testCreateMember_Beginning() throws CouldntSaveDataException {
    final ImmutableMap<TypeMember, Integer> oldOffsets = captureMemberOffsets();
    final ImmutableMap<BaseType, Integer> oldSizes = captureTypeSizes();
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final BaseType memberType = typeSystem.intType;
    final int sizeDelta = memberType.getBitSize();
    final TypeMember newMember =
        typeManager.createStructureMember(typeSystem.simpleStruct, memberType, "narf", 0);
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberUpdated(typeSystem.ssIntMember);
    expectedEvents.memberUpdated(typeSystem.ssUintMember);
    expectedEvents.memberUpdated(typeSystem.ssArrayMember);
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    expectedEvents.memberAdded(newMember);
    assertEquals(expectedEvents, events);

    // Check if all sizes were changed correctly.
    assertEquals(oldSizes.get(typeSystem.simpleStruct) + sizeDelta,
        typeSystem.simpleStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.nestedStruct) + sizeDelta,
        typeSystem.nestedStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.doubleNestedStruct) + sizeDelta,
        typeSystem.doubleNestedStruct.getBitSize());
    for (TypeMember member : typeSystem.simpleUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    assertEquals(oldSizes.get(typeSystem.simpleUnion),
        Integer.valueOf(typeSystem.simpleUnion.getBitSize()));
    for (TypeMember member : typeSystem.complexUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    assertEquals(typeSystem.doubleNestedStruct.getBitSize(), typeSystem.complexUnion.getBitSize());

    // Check if all member offsets were changed correctly.
    // SimpleStruct.
    assertEquals(oldOffsets.get(typeSystem.ssIntMember) + sizeDelta,
        (int) typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssUintMember) + sizeDelta,
        (int) typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssArrayMember) + sizeDelta,
        (int) typeSystem.ssArrayMember.getBitOffset().get());
    // NestedStruct.
    assertEquals(oldOffsets.get(typeSystem.nsIntMember),
        typeSystem.nsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.nsSimpleStructMember),
        typeSystem.nsSimpleStructMember.getBitOffset().get());
    // DoubleNestedStruct.
    assertEquals(oldOffsets.get(typeSystem.dnsNestedStructMember),
        typeSystem.dnsNestedStructMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsIntMember) + sizeDelta,
        (int) typeSystem.dnsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsPointerMember) + sizeDelta,
        (int) typeSystem.dnsPointerMember.getBitOffset().get());
    for (TypeMember member : typeSystem.simpleUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    for (TypeMember member : typeSystem.complexUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
  }

  private ImmutableMap<BaseType, Integer> captureTypeSizes() {
    final Builder<BaseType, Integer> builder = ImmutableMap.<BaseType, Integer>builder();
    for (BaseType baseType : typeSystem.getTypes()) {
      builder.put(baseType, baseType.getBitSize());
    }
    return builder.build();
  }

  private ImmutableMap<TypeMember, Integer> captureMemberOffsets() {
    final Builder<TypeMember, Integer> builder = ImmutableMap.<TypeMember, Integer>builder();
    for (BaseType baseType : typeSystem.getTypes()) {
      for (TypeMember member : baseType) {
        if (member.getBitOffset().isPresent()) {
          builder.put(member, member.getBitOffset().get());
        }
      }
    }
    return builder.build();
  }

  @Test
  public void testCreateMember_End() throws CouldntSaveDataException {
    // Checks that all events were triggered and all implicit offsets changes were applied.
    final BaseType memberType = typeSystem.intType;
    final int sizeDelta = memberType.getBitSize();
    final ImmutableMap<BaseType, Integer> oldSizes = captureTypeSizes();
    final ImmutableMap<TypeMember, Integer> oldOffsets = captureMemberOffsets();
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final TypeMember newMember = typeManager.createStructureMember(typeSystem.simpleStruct,
        memberType, "narf", typeSystem.simpleStruct.getBitSize());
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberAdded(newMember);
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    assertEquals(expectedEvents, events);

    // Check if all sizes were changed correctly.
    assertEquals(oldSizes.get(typeSystem.simpleStruct) + sizeDelta,
        typeSystem.simpleStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.nestedStruct) + sizeDelta,
        typeSystem.nestedStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.doubleNestedStruct) + sizeDelta,
        typeSystem.doubleNestedStruct.getBitSize());
    assertEquals(oldSizes.get(typeSystem.simpleUnion),
        Integer.valueOf(typeSystem.simpleUnion.getBitSize()));
    assertEquals(typeSystem.doubleNestedStruct.getBitSize(), typeSystem.complexUnion.getBitSize());

    // Check if all member offsets were changed correctly.
    // SimpleStruct.
    assertEquals(oldOffsets.get(typeSystem.ssIntMember),
        typeSystem.ssIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssUintMember),
        typeSystem.ssUintMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.ssArrayMember),
        typeSystem.ssArrayMember.getBitOffset().get());
    // NestedStruct.
    assertEquals(oldOffsets.get(typeSystem.nsIntMember),
        typeSystem.nsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.nsSimpleStructMember),
        typeSystem.nsSimpleStructMember.getBitOffset().get());
    // DoubleNestedStruct.
    assertEquals(oldOffsets.get(typeSystem.dnsNestedStructMember),
        typeSystem.dnsNestedStructMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsIntMember) + sizeDelta,
        (int) typeSystem.dnsIntMember.getBitOffset().get());
    assertEquals(oldOffsets.get(typeSystem.dnsPointerMember) + sizeDelta,
        (int) typeSystem.dnsPointerMember.getBitOffset().get());
    for (TypeMember member : typeSystem.simpleUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
    for (TypeMember member : typeSystem.complexUnion) {
      assertEquals(Optional.<Integer>of(Integer.valueOf(0)), member.getBitOffset());
    }
  }

  @Test
  public void testInsertMemberAfter() throws CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final TypeMember member =
        typeManager.insertMemberAfter(typeSystem.ssIntMember, typeSystem.intType, "new_member");
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberAdded(member);
    expectedEvents.memberUpdated(typeSystem.ssUintMember);
    expectedEvents.memberUpdated(typeSystem.ssArrayMember);
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    assertEquals(expectedEvents, events);
    assertEquals(typeSystem.intType.getBitSize() + typeSystem.ssIntMember.getBitSize()
        + typeSystem.ssUintMember.getBitSize() + typeSystem.ssArrayMember.getBitSize(),
        typeSystem.simpleStruct.getBitSize());
  }

  @Test
  public void testNotifyMemberCreated() throws CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final BaseType containingType = typeSystem.simpleStruct;
    final BaseType baseType = typeSystem.intType;
    final TypeMember newMember = typeManager.appendMember(containingType, baseType, "new_member");
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberAdded(newMember);
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifyMemberDeleted() throws CouldntDeleteException, CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    typeManager.deleteMember(typeSystem.ssIntMember);
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.typesUpdated(Sets.<BaseType>newHashSet(typeSystem.simpleStruct,
        typeSystem.nestedStruct, typeSystem.doubleNestedStruct, typeSystem.complexUnion));
    expectedEvents.memberDeleted(typeSystem.ssIntMember);
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifyMemberUpdated() throws CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final TypeMember member = typeSystem.simpleStruct.iterator().next();
    typeManager.updateStructureMember(member, typeSystem.intType, "new_member_name",
        member.getBitOffset().get());
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberUpdated(member);
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifySubstitutionCreated() throws CouldntSaveDataException {
    final TypeSubstitutionChangedEventCollector events =
        new TypeSubstitutionChangedEventCollector();
    typeManager.addListener(events);
    final IAddress address = new CAddress(0x1000);
    final TypeSubstitution substitution = typeManager.createTypeSubstitution(
        new MockOperandTreeNode(), typeSystem.intType, 0, 0, address);
    final TypeSubstitutionChangedEventCollector expectedEvents =
        new TypeSubstitutionChangedEventCollector();
    expectedEvents.substitutionsAdded(Sets.newHashSet(substitution));
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifySubstitutionDeleted() throws CouldntDeleteException,
      CouldntSaveDataException {
    final CAddress address = new CAddress(0x1000);
    final INaviOperandTreeNode node = new MockOperandTreeNode();
    final TypeSubstitution substitution =
        typeManager.createTypeSubstitution(node, typeSystem.intType, 0, 0, address);
    final TypeSubstitutionChangedEventCollector events =
        new TypeSubstitutionChangedEventCollector();
    typeManager.addListener(events);
    typeManager.deleteTypeSubstitution(node);
    final TypeSubstitutionChangedEventCollector expectedEvents =
        new TypeSubstitutionChangedEventCollector();
    expectedEvents.substitutionsDeleted(Sets.newHashSet(substitution));
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifySubstitutionUpdated() throws CouldntSaveDataException {
    final IAddress address = new CAddress(0x1000);
    final INaviOperandTreeNode node = new MockOperandTreeNode();
    final TypeSubstitution substitution =
        typeManager.createTypeSubstitution(node, typeSystem.intType, 0, 0, address);
    final TypeSubstitutionChangedEventCollector events =
        new TypeSubstitutionChangedEventCollector();
    typeManager.addListener(events);
    typeManager.updateTypeSubstitution(node, substitution, typeSystem.uintType,
        new ArrayList<TypeMember>(), 0);
    typeManager.updateTypeSubstitution(node, typeSystem.uintType.getId(), new Integer[0], 0);
    final TypeSubstitutionChangedEventCollector expectedEvents =
        new TypeSubstitutionChangedEventCollector();
    expectedEvents.substitutionsChanged(Sets.newHashSet(substitution));
    expectedEvents.substitutionsChanged(Sets.newHashSet(substitution));
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifyTypeCreated() throws CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final BaseType baseType = typeManager.createAtomicType("new_base_type", 32, true);
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.typeAdded(baseType);
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifyTypeDeleted() throws CouldntDeleteException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.memberDeleted(typeSystem.ssIntMember);
    expectedEvents.memberDeleted(typeSystem.nsIntMember);
    expectedEvents.memberDeleted(typeSystem.dnsIntMember);
    expectedEvents.memberDeleted(typeSystem.suIntMember);
    expectedEvents.memberDeleted(typeSystem.cuIntMember);
    expectedEvents.typeDeleted(typeSystem.intType);
    typeManager.deleteType(typeSystem.intType);
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testNotifyTypeUpdated() throws CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    final BaseType baseType = typeSystem.intType;
    typeManager.updateType(baseType, "newName", 64, true);
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.typesUpdated(Sets.<BaseType>newHashSet(typeSystem.intType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion));
    expectedEvents.memberUpdated(typeSystem.ssUintMember);
    expectedEvents.memberUpdated(typeSystem.ssArrayMember);
    expectedEvents.memberUpdated(typeSystem.nsSimpleStructMember);
    expectedEvents.memberUpdated(typeSystem.dnsIntMember);
    expectedEvents.memberUpdated(typeSystem.dnsPointerMember);
    Assert.assertEquals(expectedEvents, events);
  }

  @Test
  public void testRenameType() throws CouldntSaveDataException {
    final TypeChangedEventCollector events = new TypeChangedEventCollector();
    typeManager.addListener(events);
    typeManager.renameType(typeSystem.intType, "NARF");
    final TypeChangedEventCollector expectedEvents = new TypeChangedEventCollector();
    expectedEvents.typesUpdated(Sets.<BaseType>newHashSet(typeSystem.intType,
        typeSystem.simpleStruct,
        typeSystem.nestedStruct,
        typeSystem.doubleNestedStruct,
        typeSystem.simpleUnion,
        typeSystem.complexUnion));
    Assert.assertEquals(expectedEvents, events);
  }
}
