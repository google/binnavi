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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests all public methods of the TypeManager class. Each test is performed for the kernel32 and
 * the notepad module, respectively.
 *
 *  The tests basically verify two things: a) whether the output from the type manager is correct,
 * and b) whether the database tables have been updated correctly.
 *
 *  Each tests has to cleanup the types/members that were created, otherwise other tests might fail
 * or depend on the order of execution.
 *
 * @author jannewger@google.com (Jan Newger)
 *
 */
@RunWith(JUnit4.class)
public class TypeManagerExpensiveTests extends ExpensiveBaseTest {
  private static String TypeName = "test_type";
  private static String UpdatedTypeName = "updated_test_type";
  private static String CompoundTypeName = "test_compound_type";
  private static String MemberName = "test_member";
  private static String MemberName1 = "test_member_1";
  private static String MemberName2 = "test_member_2";
  private static String UpdatedMemberName = "updated_test_member";

  private static int indexOfMember(final BaseType compoundType, final TypeMember member) {
    int i = 0;
    for (final TypeMember m : compoundType) {
      if (m == member) {
        return i;
      }
      ++i;
    }
    return -1;
  }

  // Find all members that have the given name.
  private List<RawTypeMember> findRawMembers(final String name,
      final List<RawTypeMember> rawMembers) {
    final List<RawTypeMember> foundMembers = new ArrayList<RawTypeMember>();
    for (final RawTypeMember member : rawMembers) {
      if (member.getName().equals(name)) {
        foundMembers.add(member);
      }
    }
    return foundMembers;
  }

  // Find all types that have the given name.
  private List<RawBaseType> findRawTypes(final String name, final List<RawBaseType> rawTypes) {
    final List<RawBaseType> foundTypes = new ArrayList<RawBaseType>();
    for (final RawBaseType rawType : rawTypes) {
      if (rawType.getName().equals(name)) {
        foundTypes.add(rawType);
      }
    }
    return foundTypes;
  }

  // Find raw type substitution by the given id.
  private List<RawTypeSubstitution> findRawTypeSubstitutions(
      final List<RawTypeSubstitution> substitutions, final int expressionId) {
    final List<RawTypeSubstitution> foundSubstitutions = new ArrayList<RawTypeSubstitution>();
    for (final RawTypeSubstitution substitution : substitutions) {
      if (substitution.getExpressionId() == expressionId) {
        foundSubstitutions.add(substitution);
      }
    }
    return foundSubstitutions;
  }

  private RawBaseType findSingleRawType(final String name, final List<RawBaseType> rawTypes) {
    final List<RawBaseType> foundTypes = findRawTypes(name, rawTypes);
    Assert.assertTrue(foundTypes.size() == 1);
    return foundTypes.get(0);
  }

  // Make sure that the given raw member matches the created type member and that the containing as
  // well as the base type of the member are correct.
  private void matchMember(final TypeMember member, final RawTypeMember rawTypeMember) {
    Assert.assertEquals(rawTypeMember.getName(), member.getName());
    Assert.assertEquals(rawTypeMember.getNumberOfElements(), member.getNumberOfElements());
    Assert.assertEquals(rawTypeMember.getId(), member.getId());
    Assert.assertEquals(rawTypeMember.getOffset().get(), member.getBitOffset().get());
    Assert.assertEquals(rawTypeMember.getBaseTypeId(), member.getBaseType().getId());
    Assert.assertEquals(rawTypeMember.getParentId().intValue(), member.getParentType().getId());
  }

  // Re-Load types from the database and make sure the given base type is equivalent to the one in
  // the database.
  private void matchType(final BaseType baseType, final RawBaseType rawType) {
    Assert.assertEquals(rawType.getId(), baseType.getId());
    Assert.assertEquals(rawType.getName(), baseType.getName());
    Assert.assertEquals(rawType.getSize(), baseType.getBitSize());
    final BaseType pointerType = baseType.pointedToBy();
    Assert.assertEquals(rawType.getPointerId(), pointerType == null ? null : pointerType.getId());
  }

  private void matchTypeSubstitution(final TypeSubstitution substitution,
      final RawTypeSubstitution rawSubstitution) {
    Assert.assertEquals(substitution.getAddress(), rawSubstitution.getAddress());
    Assert.assertTrue(substitution.getBaseType().getId() == rawSubstitution.getBaseTypeId());
    Assert.assertTrue(substitution.getExpressionId() == rawSubstitution.getExpressionId());
    Assert.assertTrue(substitution.getOffset() == rawSubstitution.getPosition());
  }

  private void testAddMember(final INaviModule module) throws CouldntSaveDataException,
      CouldntLoadDataException, CouldntDeleteException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType = manager.createAtomicType(TypeName, 32, true);
    final BaseType containingType = manager.createAtomicType(CompoundTypeName, 0, false);
    final TypeMember member = manager.appendMember(containingType, baseType, MemberName);
    final List<RawTypeMember> foundMembers =
        findRawMembers(MemberName, getProvider().loadTypeMembers(module));
    Assert.assertTrue(foundMembers.size() == 1);
    matchMember(member, foundMembers.get(0));

    manager.deleteMember(member);
    manager.deleteType(containingType);
    manager.deleteType(baseType);
  }

  private void testCreateType(final INaviModule module) throws CouldntLoadDataException,
      CouldntSaveDataException, CouldntDeleteException {
    final TypeManager manager = module.getTypeManager();
    final BaseType createdType = manager.createAtomicType(TypeName, 32, true);
    Assert.assertNotNull(createdType);
    Assert.assertEquals(createdType.getName(), TypeName);
    Assert.assertFalse(createdType.hasMembers());
    Assert.assertFalse(createdType.hasMembers());
    Assert.assertNull(createdType.pointedToBy());
    Assert.assertTrue(createdType.getPointerLevel() == 0);
    Assert.assertNull(createdType.pointsTo());
    matchType(createdType,
        findSingleRawType(createdType.getName(), getProvider().loadTypes(module)));
    manager.deleteType(createdType);
  }

  private void testCreateTypeSubstitution(final INaviModule module) throws CouldntSaveDataException,
      CouldntLoadDataException, CouldntDeleteException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType = manager.createAtomicType(TypeName, 32, true);
    final INaviOperandTreeNode treeNode = new MockOperandTreeNode();
    final int offset = 0;
    final CAddress address = new CAddress(0x1000);
    final TypeSubstitution substitution =
        manager.createTypeSubstitution(treeNode, baseType, 0, offset, address);
    Assert.assertEquals(substitution.getAddress(), address);
    Assert.assertTrue(substitution.getBaseType() == baseType);
    Assert.assertTrue(substitution.getOffset() == offset);
    Assert.assertTrue(substitution.getOperandTreeNode() == treeNode);
    Assert.assertTrue(substitution.getExpressionId() == treeNode.getId());
    final List<RawTypeSubstitution> substitutions = getProvider().loadTypeSubstitutions(module);
    final List<RawTypeSubstitution> foundSubstitutions =
        findRawTypeSubstitutions(substitutions, treeNode.getId());
    Assert.assertTrue(foundSubstitutions.size() == 1);
    final RawTypeSubstitution foundSubstitution = foundSubstitutions.get(0);
    matchTypeSubstitution(substitution, foundSubstitution);
    manager.deleteType(baseType);
  }

  private void testDeleteFirstMember(final INaviModule module) throws CouldntSaveDataException,
      CouldntDeleteException, CouldntLoadDataException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType = manager.createAtomicType(TypeName, 32, true);
    final BaseType compoundType = manager.createAtomicType(CompoundTypeName, 0, false);
    final TypeMember member0 = manager.appendMember(compoundType, baseType, MemberName);
    final TypeMember member1 = manager.appendMember(compoundType, baseType, MemberName1);
    final TypeMember member2 = manager.appendMember(compoundType, baseType, MemberName2);
    manager.deleteMember(member0);
    Assert.assertTrue(indexOfMember(compoundType, member0) == -1);
    Assert.assertTrue(indexOfMember(compoundType, member1) != -1);
    Assert.assertTrue(indexOfMember(compoundType, member2) != -1);
    final List<RawTypeMember> rawMembers = getProvider().loadTypeMembers(module);
    Assert.assertTrue(findRawMembers(MemberName, rawMembers).isEmpty());
    manager.deleteType(baseType);
    manager.deleteType(compoundType);
  }

  private void testDeleteMember(final INaviModule module) throws CouldntSaveDataException,
      CouldntDeleteException, CouldntLoadDataException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType = manager.createAtomicType(TypeName, 32, true);
    final BaseType compoundType = manager.createAtomicType(CompoundTypeName, 0, false);
    final TypeMember member = manager.appendMember(compoundType, baseType, MemberName);
    manager.deleteMember(member);
    Assert.assertTrue(compoundType.getMemberCount() == 0);
    final List<RawTypeMember> rawMembers = getProvider().loadTypeMembers(module);
    Assert.assertTrue(findRawMembers(MemberName, rawMembers).isEmpty());
    manager.deleteType(baseType);
    manager.deleteType(compoundType);
  }

  private void testDeleteType(final INaviModule module) throws CouldntSaveDataException,
      CouldntDeleteException, CouldntLoadDataException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType = manager.createAtomicType(TypeName, 32, true);
    manager.deleteType(baseType);
    final List<RawBaseType> foundTypes = findRawTypes(TypeName, getProvider().loadTypes(module));
    Assert.assertTrue(foundTypes.isEmpty());
  }

  private void testUpdateMember(final INaviModule module) throws CouldntSaveDataException,
      CouldntDeleteException, CouldntLoadDataException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType0 = manager.createAtomicType(TypeName, 32, true);
    final BaseType baseType1 = manager.createAtomicType(UpdatedTypeName, 32, true);
    final BaseType compoundType = manager.createStructure(CompoundTypeName);
    final TypeMember member = manager.appendMember(compoundType, baseType0, MemberName);
    manager.updateStructureMember(member, baseType0, UpdatedMemberName, 100);
    final List<RawTypeMember> members = getProvider().loadTypeMembers(module);
    Assert.assertTrue(findRawMembers(MemberName, members).isEmpty());
    final List<RawTypeMember> updatedMembers = findRawMembers(UpdatedMemberName, members);
    Assert.assertTrue(updatedMembers.size() == 1);
    matchMember(member, updatedMembers.get(0));

    manager.deleteMember(member);
    manager.deleteType(baseType0);
    manager.deleteType(baseType1);
    manager.deleteType(compoundType);
  }

  private void testUpdateType(final INaviModule module) throws CouldntSaveDataException,
      CouldntLoadDataException, CouldntDeleteException {
    final TypeManager manager = module.getTypeManager();
    final BaseType baseType = manager.createAtomicType(TypeName, 32, true);
    manager.updateType(baseType, UpdatedTypeName, 64, true);
    final List<RawBaseType> foundTypes =
        findRawTypes(UpdatedTypeName, getProvider().loadTypes(module));
    Assert.assertTrue(foundTypes.size() == 1);
    matchType(baseType, foundTypes.get(0));

    manager.deleteType(baseType);
  }

  @Test
  public void testAddMember() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testAddMember(getKernel32Module());
    testAddMember(getNotepadModule());
  }

  @Test
  public void testCreateType() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException, CouldntDeleteException {
    testCreateType(getKernel32Module());
    testCreateType(getNotepadModule());
  }

  @Test
  public void testCreateTypeSubstitution() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testCreateTypeSubstitution(getKernel32Module());
    testCreateTypeSubstitution(getNotepadModule());
  }

  @Test
  public void testDeleteFirstMember() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testDeleteFirstMember(getKernel32Module());
    testDeleteFirstMember(getNotepadModule());
  }

  @Test
  public void testDeleteMember() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testDeleteMember(getKernel32Module());
    testDeleteMember(getNotepadModule());
  }

  @Test
  public void testDeleteType() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testDeleteType(getKernel32Module());
    testDeleteType(getNotepadModule());
  }

  @Test
  public void testUpdateMember() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testUpdateMember(getKernel32Module());
    testUpdateMember(getNotepadModule());
  }

  @Test
  public void testUpdateType() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    testUpdateType(getKernel32Module());
    testUpdateType(getNotepadModule());
  }
}
