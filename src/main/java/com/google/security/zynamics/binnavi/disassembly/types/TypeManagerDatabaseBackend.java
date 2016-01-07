/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.List;

/**
 * The database backend used by the TypeManager to persistently store types, members and type
 * substitution for a given module.
 */
final public class TypeManagerDatabaseBackend implements TypeManagerBackend {
  private final SQLProvider provider;
  private final INaviModule module;

  public TypeManagerDatabaseBackend(final SQLProvider provider, final INaviModule module) {
    this.provider =
        Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
    this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
  }

  @Override
  public BaseType createType(final String name, final int size, final boolean signed,
      final Integer childPointerTypeId, final BaseTypeCategory category)
      throws CouldntSaveDataException {
    final int typeId = provider.createType(module.getConfiguration().getId(),
        name,
        size,
        childPointerTypeId,
        signed,
        category);
    return new BaseType(typeId, name, size, signed, category);
  }

  @Override
  public TypeSubstitution createTypeSubstitution(final INaviOperandTreeNode node,
      final BaseType baseType,
      final List<Integer> memberPath,
      final int position,
      final int offset,
      final IAddress address) throws CouldntSaveDataException {
    provider.createTypeSubstitution(node.getId(),
        baseType.getId(),
        memberPath,
        position,
        offset,
        address,
        module);
    return new TypeSubstitution(node, baseType, node.getId(), position, offset, address);
  }

  @Override
  public void deleteMember(final TypeMember member) throws CouldntDeleteException {
    provider.deleteMember(member, module);
  }

  @Override
  public void deleteType(final BaseType baseType) throws CouldntDeleteException {
    provider.deleteType(baseType, module);
  }

  @Override
  public void deleteTypeSubstitution(final TypeSubstitution typeSubstitution)
      throws CouldntDeleteException {
    provider.deleteTypeSubstitution(module, typeSubstitution);
  }

  @Override
  public BaseType loadRawBaseType(final int baseTypeId) throws CouldntLoadDataException {
    final RawBaseType rawBaseType = provider.loadType(module, baseTypeId);
    return new BaseType(rawBaseType.getId(), rawBaseType.getName(), rawBaseType.getSize(),
        rawBaseType.isSigned(), rawBaseType.getCategory());
  }

  @Override
  public List<RawBaseType> loadRawBaseTypes() throws CouldntLoadDataException {
    return provider.loadTypes(module);
  }

  @Override
  public RawTypeMember loadRawTypeMember(final int typeMemberId) throws CouldntLoadDataException {
    return provider.loadTypeMember(module, typeMemberId);
  }

  @Override
  public List<RawTypeMember> loadRawTypeMembers() throws CouldntLoadDataException {
    return provider.loadTypeMembers(module);
  }

  @Override
  public void updateMemberOffsets(final List<Integer> updatedMembers, final int delta,
      final List<Integer> implicitlyUpdatedMembers, final int implicitDela)
      throws CouldntSaveDataException {
    provider.updateMemberOffsets(updatedMembers, delta, implicitlyUpdatedMembers, implicitDela,
        module);
  }

  @Override
  public void updateSubstitution(final TypeSubstitution substitution, final BaseType baseType,
      final List<Integer> memberPath, final int offset) throws CouldntSaveDataException {
    provider.updateTypeSubstitution(substitution, baseType, memberPath, offset, module);
  }

  @Override
  public void updateType(final BaseType baseType, final String name, final int size,
      final boolean isSigned) throws CouldntSaveDataException {
    provider.updateType(baseType, name, size, isSigned, module);
  }

  @Override
  public void updateArrayMember(final TypeMember member, final BaseType baseType,
      final int numberOfElements) throws CouldntSaveDataException {
    provider.updateMember(member,
        member.getName(),
        baseType,
        member.getBitOffset(),
        Optional.<Integer>of(numberOfElements),
        member.getArgumentIndex(),
        module);
  }

  @Override
  public TypeMember createFunctionPrototypeMember(BaseType containingType, BaseType memberType,
      String membeName, int memberArgumentIndex) throws CouldntSaveDataException {
    final int typeId = provider.createTypeMember(module,
        containingType.getId(),
        memberType.getId(),
        membeName,
        Optional.<Integer>absent(), /* offset */
        Optional.<Integer>absent(), /* number of elements */
        Optional.of(memberArgumentIndex)); /* argument index */
    return TypeMember.createFunctionPrototypeMember(typeId, containingType, memberType, membeName,
        memberArgumentIndex);
  }

  @Override
  public TypeMember createStructureMember(BaseType containingType, BaseType memberType,
      String memberName, int memberOffset) throws CouldntSaveDataException {
    final int typeId = provider.createTypeMember(module,
        containingType.getId(),
        memberType.getId(),
        memberName,
        Optional.of(memberOffset), /* offset */
        Optional.<Integer>absent(), /* number of elements */
        Optional.<Integer>absent() /* argument index */);
    return TypeMember.createStructureMember(typeId, containingType, memberType, memberName,
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
    final int typeId = provider.createTypeMember(module,
        containingType.getId(),
        memberType.getId(),
        memberName,
        Optional.<Integer>absent(), /* offset */
        Optional.of(memberNumberOfElements), /* number of elements */
        Optional.<Integer>absent() /* argument index */);
    return TypeMember.createArrayMember(typeId, containingType, memberType, memberName,
        memberNumberOfElements);
  }

  @Override
  public void updateStructureMember(TypeMember member, BaseType newMemberBaseType,
      String newMemberName, int newMemberOffset) throws CouldntSaveDataException {
    provider.updateMember(member, newMemberName, newMemberBaseType, Optional.of(newMemberOffset), /* offset */
    Optional.<Integer>absent(), /* number of elements */
    Optional.<Integer>absent(), /* argument index */
    module);
  }

  @Override
  public void updateUnionMember(TypeMember member, BaseType newMemberBaseType, String newMemberName,
      int newMemberOffset) throws CouldntSaveDataException {
    updateStructureMember(member, newMemberBaseType, newMemberName, newMemberOffset);
  }

  @Override
  public void updateFunctionPrototypeMember(TypeMember member, BaseType newMemberBaseType,
      String newMemberName, int newMemberArgumentIndex) throws CouldntSaveDataException {
    provider.updateMember(member, newMemberName, newMemberBaseType, Optional.<Integer>absent(), /* offset */
    Optional.<Integer>absent(), /* number of elements */
    Optional.of(newMemberArgumentIndex), /* argument index */
    module);
  }
}
