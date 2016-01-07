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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.List;

/**
 * The interface that describes the types that can be used as
 */
public interface TypeManagerBackend {

  /**
   * Creates a new function prototype member in the backend.
   *
   * @param containingType The {@link BaseType base type} that contains the new member.
   * @param memberType The {@link BaseType base type} of the {@link TypeMember new member}.
   * @param membeName The name of the new {@link TypeMember member}.
   * @param memberArgumentIndex The index of the {@link TypeMember new member} in the function
   *        prototype.
   * @return The newly created {@link TypeMember member instance}.
   *
   * @throws CouldntSaveDataException Thrown if the {@link TypeMember member} could not be created
   *         in the backend.
   */
  public TypeMember createFunctionPrototypeMember(final BaseType containingType,
      final BaseType memberType, final String membeName, int memberArgumentIndex)
      throws CouldntSaveDataException;

  /**
   * Creates a new structure member in the backend.
   *
   * @param containingType The {@link BaseType base type} that contains the new member.
   * @param memberType The {@link BaseType base type} of the {@link TypeMember new member}.
   * @param memberName The name of the new {@link TypeMember member}.
   * @param memberOffset The offset of the {@link TypeMember new member} in the structure.
   * @return The newly created {@link TypeMember member instance}.
   *
   * @throws CouldntSaveDataException Thrown if the {@link TypeMember member} could not be created
   *         in the backend.
   */
  public TypeMember createStructureMember(final BaseType containingType, final BaseType memberType,
      final String memberName, int memberOffset) throws CouldntSaveDataException;

  /**
   * Creates a new union member in the backend.
   *
   * @param containingType The {@link BaseType base type} that contains the new member.
   * @param memberType The {@link BaseType base type} of the {@link TypeMember new member}.
   * @param memberName The name of the new {@link TypeMember member}.
   * @param memberOffset The offset of the {@link TypeMember new member} in the union.
   * @return The newly created {@link TypeMember member instance}.
   *
   * @throws CouldntSaveDataException Thrown if the {@link TypeMember member} could not be created
   *         in the backend.
   */
  public TypeMember createUnionMember(final BaseType containingType, final BaseType memberType,
      final String memberName, int memberOffset) throws CouldntSaveDataException;

  /**
   * Creates a new array member in the backend.
   *
   * @param containingType The {@link BaseType base type} that contains the new member.
   * @param memberType The {@link BaseType base type} of the {@link TypeMember new member}.
   * @param memberName The name of the new {@link TypeMember member}.
   * @param memberNumberOfElements The number of elements of the {@link TypeMember new member} in
   *        the array.
   * @return The newly created {@link TypeMember member instance}.
   *
   * @throws CouldntSaveDataException Thrown if the {@link TypeMember member} could not be created
   *         in the backend.
   */
  public TypeMember createArrayMember(final BaseType containingType, final BaseType memberType,
      final String memberName, int memberNumberOfElements) throws CouldntSaveDataException;

  /**
   * Creates a new base type in the backend.
   *
   * @param name The name of the new base type.
   * @param size The size in bits of the new base type.
   * @param signed Specifies whether the new type is a signed data type.
   * @param childPointerTypeId The id of the type that is the child of the new type with regards to
   *        the pointer hierarchy (see BaseType for details on pointer hierarchy).
   * @param category The type category of the new base type.
   *
   * @return The newly created base type instance.
   *
   * @throws CouldntSaveDataException Thrown if the new base type could not be created in the
   *         backend.
   */
  public BaseType createType(final String name, final int size, final boolean signed,
      final Integer childPointerTypeId, BaseTypeCategory category) throws CouldntSaveDataException;

  /**
   * Creates a new type substitution for the given operand tree node.
   *
   * @param selectedNode The operand tree node to create a type substitution for.
   * @param baseType The base type to be used for the substitution.
   * @param memberPath The sequence of member ids to unambiguously address a member when dealing
   *        with unions. Can be empty if base type and offset form a unambiguous reference.
   * @param position The zero-based index position of the operand within its instruction.
   * @param offset The value to add as an offset relative to the beginning of the base type.
   * @param address The address of the operand where this type substitution will be created.
   *
   * @return The newly created type substitution instance.
   *
   * @throws CouldntSaveDataException Thrown if the new type substitution could not be created in
   *         the backend.
   */
  public TypeSubstitution createTypeSubstitution(final INaviOperandTreeNode selectedNode,
      final BaseType baseType,
      final List<Integer> memberPath,
      final int position,
      final int offset,
      final IAddress address) throws CouldntSaveDataException;

  /**
   * Deletes the given member from the back end.
   *
   * @param member The member to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the member could not be deleted from the back end.
   */
  public void deleteMember(final TypeMember member) throws CouldntDeleteException;

  /**
   * Deletes the given base type from the back end.
   *
   * @param baseType The base type to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the base type could not be deleted from the back end.
   */
  public void deleteType(final BaseType baseType) throws CouldntDeleteException;

  /**
   * Deletes the given type substitution from the back end.
   *
   * @param substitution The type substitution to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the type substitution could not be deleted from the
   *         back end.
   */
  public void deleteTypeSubstitution(final TypeSubstitution substitution)
      throws CouldntDeleteException;

  /**
   * Loads a single {@link RawBaseType base type} from the database
   *
   * @param baseTypeId The id of the {@link RawBaseType base type} to load from the database.
   *
   * @return A {@link BaseType base type} from the database with the given id.
   * @throws CouldntLoadDataException if the {@link RawBaseType base type} could not be loaded from
   *         the database.
   */
  public BaseType loadRawBaseType(final int baseTypeId) throws CouldntLoadDataException;

  /**
   * Loads all {@link RawBaseType} from the database.
   *
   * @return The {@link List} of {@link RawBaseType} in the database.
   * @throws CouldntLoadDataException if the {@link RawBaseType} could not be loaded from the
   *         database.
   */
  public List<RawBaseType> loadRawBaseTypes() throws CouldntLoadDataException;

  /**
   * Loads a single {@link RawTypeMember type member} from the database.
   *
   * @param typeMemberId The id of the {@link RawTypeMember type member} to load from the database.
   *
   * @return A {@link RawTypeMember type member} from the database with the given id.
   * @throws CouldntLoadDataException if the {@link RawTypeMember type member} could not be loaded
   *         from the database.
   */
  public RawTypeMember loadRawTypeMember(final int typeMemberId) throws CouldntLoadDataException;

  /**
   * Loads all {@link RawTypeMember} from the database.
   *
   * @return The {@link List} of {@link RawTypeMember} in the database.
   * @throws CouldntLoadDataException if the {@link RawTypeMember} could not be loaded from the
   *         database.
   */
  public List<RawTypeMember> loadRawTypeMembers() throws CouldntLoadDataException;

  /**
   * Updates a structure member in the back end.
   *
   * @param member The member to be updated.
   * @param newMemberBaseType The new {@link BaseType base type} of the member.
   * @param newMemberName The new {@link String name} of the member.
   * @param newMemberOffset The new {@link Integer offset} of the member.
   * @throws CouldntSaveDataException thrown if the changes could not be saved to the database.
   */
  public void updateStructureMember(final TypeMember member, final BaseType newMemberBaseType,
      String newMemberName, int newMemberOffset) throws CouldntSaveDataException;

  /**
   * Updates a union member in the back end.
   *
   * @param member The member to be updated.
   * @param newMemberBaseType The new {@link BaseType base type} of the member.
   * @param newMemberName The new {@link String name} of the member.
   * @param newMemberOffset The new {@link Integer offset} of the member.
   * @throws CouldntSaveDataException thrown if the changes could not be saved to the database.
   */
  public void updateUnionMember(final TypeMember member, final BaseType newMemberBaseType,
      String newMemberName, int newMemberOffset) throws CouldntSaveDataException;

  /**
   * Updates a function prototype member in the back end.
   *
   * @param member The member to be updated.
   * @param newMemberBaseType The new {@link BaseType base type} of the member.
   * @param newMemberName The new {@link String name} of the member.
   * @param newMemberArgumentIndex The new {@link Integer argument index} of the member.
   * @throws CouldntSaveDataException thrown if the changes could not be saved to the database.
   */
  public void updateFunctionPrototypeMember(final TypeMember member,
      final BaseType newMemberBaseType, String newMemberName, int newMemberArgumentIndex)
      throws CouldntSaveDataException;

  /**
   * Updates the given array-member in the back end.
   *
   * @param member The array member to be updated.
   * @param baseType The new base type of the array member.
   * @param numberOfElements The new number of elements for the member.
   * @throws CouldntSaveDataException Thrown if the member could not be updated in the back end.
   */
  public void updateArrayMember(final TypeMember member, final BaseType baseType,
      final int numberOfElements) throws CouldntSaveDataException;

  /**
   * Updates the offsets of lists of members by the given deltas. The delta can be positive or
   * negative but must be different from zero.
   *
   * @param updatedMembers The ids of members whose offsets should be changed in the backend.
   * @param delta The value to add to the current offset of each member in the backend.
   * @param implicitlyUpdatedMembers The member ids that are implicitly moved due to re-arrangements
   *        induced by the updated members.
   * @param implicitDelta The delta of the implicitly updated members.
   * @throws CouldntSaveDataException Thrown if the offsets could not be written to the database.
   */
  public void updateMemberOffsets(final List<Integer> updatedMembers, final int delta,
      final List<Integer> implicitlyUpdatedMembers, final int implicitDelta)
      throws CouldntSaveDataException;

  /**
   * Updates the given type substitution in the back end.
   *
   * @param substitution The type substitution to be updated.
   * @param baseType The new base type for the type substitution.
   * @param memberPath The sequence of members to unambiguously address a member inside a union.
   * @param offset The new offset for the type substitution.
   *
   * @throws CouldntSaveDataException Thrown if the type substitution could not be updated in the
   *         back end.
   */
  public void updateSubstitution(final TypeSubstitution substitution, final BaseType baseType,
      final List<Integer> memberPath, final int offset) throws CouldntSaveDataException;

  /**
   * Updates the given base type in the back end.
   *
   * @param baseType The base type to be updated.
   * @param name The new name of the base type.
   * @param size The new size of the base type.
   * @param isSigned The new signedness of the base type.
   *
   * @throws CouldntSaveDataException Thrown if the base type could not be updated in the back end.
   */
  public void updateType(final BaseType baseType, final String name, final int size,
      final boolean isSigned) throws CouldntSaveDataException;
}
