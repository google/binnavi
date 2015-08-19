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
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a mock backend to be used with the {@link TypeManager type manager} which consists of
 * the types provided by the {@link TestTypeSystem test type system}.
 */
public class TypeManagerMockBackend implements TypeManagerBackend {

  // Note: we assume that starting from 1000 all ids can be used and are not occupied by the types
  // of the test type system.
  private int typeMemberId = 1000;
  private int baseTypeId = 1000;
  // NOTE: we must not create instances of BaseType here since the TypeManager is the authority of
  // those instances!
  private final List<BaseType> types = new ArrayList<BaseType>();
  private final List<TypeMember> members = new ArrayList<TypeMember>();
  private final List<TypeSubstitution> substitutions = new ArrayList<TypeSubstitution>();

  private List<RawBaseType> rawTypes = new ArrayList<RawBaseType>();
  private List<RawTypeMember> rawMembers = new ArrayList<RawTypeMember>();

  public TypeManagerMockBackend() {
    final RawTestTypeSystem typeSystem = new RawTestTypeSystem();
    rawTypes = typeSystem.getRawTypes();
    rawMembers = typeSystem.getRawMembers();
  }

  private static RawTypeMember convertToRawMember(final TypeMember member) {
    // TODO(jannewger): pass appropriate value for argument as soon as function pointer
    // arguments have been implemented.
    return new RawTypeMember(member.getId(),
        member.getName(),
        member.getBaseType().getId(),
        member.getParentType().getId(),
        member.getBitOffset().orNull(),
        null /* argument */
        ,
        member.getNumberOfElements().orNull());
  }

  @Override
  public BaseType createType(final String name, final int size, final boolean isSigned,
      final Integer childPointerTypeId, final BaseTypeCategory category)
      throws CouldntSaveDataException {
    final BaseType baseType = new BaseType(++baseTypeId, name, size, isSigned, category);
    types.add(baseType);
    return baseType;
  }

  @Override
  public TypeSubstitution createTypeSubstitution(final INaviOperandTreeNode node,
      final BaseType baseType,
      final List<Integer> memberPath,
      final int position,
      final int offset,
      final IAddress address) throws CouldntSaveDataException {
    final TypeSubstitution substitution =
        new TypeSubstitution(node, baseType, node.getId(), position, offset, address);
    substitutions.add(substitution);
    return substitution;
  }


  @Override
  public void deleteMember(final TypeMember member) throws CouldntDeleteException {
    // TODO(jannewger): remove corresponding typemember and raw type member.
  }

  @Override
  public void deleteType(final BaseType baseType) throws CouldntDeleteException {
    // TODO(jannewger): remove corresponding basetype and raw base type.
  }

  @Override
  public void deleteTypeSubstitution(final TypeSubstitution typeSubstitution)
      throws CouldntDeleteException {
    // TODO(jannewger): remove corresponding raw type substitution.
  }

  @Override
  public BaseType loadRawBaseType(final int baseTypeId) throws CouldntLoadDataException {
    for (final BaseType baseType : types) {
      if (baseType.getId() == baseTypeId) {
        return baseType;
      }
    }
    throw new CouldntLoadDataException("Base type not found in type manager mock backend.");
  }

  @Override
  public List<RawBaseType> loadRawBaseTypes() throws CouldntLoadDataException {
    return rawTypes;
  }

  @Override
  public RawTypeMember loadRawTypeMember(final int typeMemberId) throws CouldntLoadDataException {
    for (final TypeMember member : members) {
      if (member.getId() == typeMemberId) {
        return convertToRawMember(member);
      }
    }
    throw new CouldntLoadDataException("Member not found in type manager mock backend.");
  }

  @Override
  public List<RawTypeMember> loadRawTypeMembers() throws CouldntLoadDataException {
    return rawMembers;
  }

  @Override
  public void updateMemberOffsets(final List<Integer> updatedMembers, final int delta,
      final List<Integer> implicitlyUpdatedMembers, final int implicitDelta)
      throws CouldntSaveDataException {
    // We don't have to do anything here since the type manager owns the member instance and changes
    // its properties accordingly.
  }

  @Override
  public void updateSubstitution(final TypeSubstitution substitution, final BaseType baseType,
      final List<Integer> memberPath, final int offset) {
    // We don't have to do anything here since the type manager owns the substitution instance and
    // changes its properties accordingly.
  }

  @Override
  public void updateType(final BaseType baseType, final String name, final int size,
      final boolean isSigned) throws CouldntSaveDataException {
    // We don't have to do anything here since the type manager owns the type instance and changes
    // its properties accordingly.
  }

  @Override
  public void updateArrayMember(TypeMember member, BaseType baseType, int numberOfElements)
      throws CouldntSaveDataException {
    // We don't have to do anything here since the type manager owns the member instance and changes
    // its properties accordingly.
  }

  @Override
  public TypeMember createFunctionPrototypeMember(BaseType containingType, BaseType memberType,
      String memberName, int memberArgumentIndex) throws CouldntSaveDataException {
    return TypeMember.createFunctionPrototypeMember(++typeMemberId, containingType, memberType,
        memberName, memberArgumentIndex);
  }

  @Override
  public TypeMember createStructureMember(BaseType containingType, BaseType memberType,
      String memberName, int memberOffset) throws CouldntSaveDataException {
    return TypeMember.createStructureMember(++typeMemberId, containingType, memberType, memberName,
        memberOffset);
  }

  @Override
  public TypeMember createUnionMember(BaseType containingType, BaseType memberType,
      String memberName, int memberOffset) throws CouldntSaveDataException {
    return createStructureMember(containingType, memberType, memberName, memberOffset);
  }

  @Override
  public TypeMember createArrayMember(BaseType containingType, BaseType memberType,
      String memberName, int memberNumberOfElements) throws CouldntSaveDataException {
    return TypeMember.createArrayMember(++typeMemberId, containingType, memberType, memberName,
        memberNumberOfElements);
  }

  @Override
  public void updateStructureMember(TypeMember member, BaseType newMemberBaseType,
      String newMemberName, int newMemberOffset) throws CouldntSaveDataException {}

  @Override
  public void updateUnionMember(TypeMember member, BaseType newMemberBaseType, String newMemberName,
      int newMemberOffset) throws CouldntSaveDataException {}

  @Override
  public void updateFunctionPrototypeMember(TypeMember member, BaseType newMemberBaseType,
      String newMemberName, int newMemberArgumentIndex) throws CouldntSaveDataException {}
}
