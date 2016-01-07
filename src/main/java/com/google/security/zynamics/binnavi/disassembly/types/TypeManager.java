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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.disassembly.types.TypeDependenceGraph;
import com.google.security.zynamics.binnavi.yfileswrap.disassembly.types.TypeDependenceGraph.DependenceResult;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type manager class holds a collection of all existing base types, members and type
 * substitutions and allows clients to retrieve, edit, delete or create new ones.
 *
 *  In order to have types, members and type substitutions backed by the database, the type manager
 * is the one central authority to instantiate objects of those types. This also means that any
 * changes to instances of BaseType, TypeMember or TypeSubstitutions must be signaled to the type
 * manager. This way, arbitrary clients can be notified about changes in the type system represented
 * by the type manager.
 *
 *  The type system induces a hierarchy between two types whereas we define the pointer level to be
 * the number of pointer indirections to reach the underlying value type. So for each two types t0
 * and t1: t0 < t1 iff the pointer level of t0 is strictly smaller than that of t1.
 *
 *  For example, assume that t0 is "int" and t1 is "int*", then t0 < t1. Here, t0 has a pointer
 * level of zero and t1 has a pointer level of one. We also call t0 the parent of t1.
 *
 *  Usage:
 *
 * TypeManager manager = new TypeManager(backend);
 */
public class TypeManager {

  private final TypesContainer typesContainer;
  private final ListenerProvider<TypeChangedListener> typeListeners =
      new ListenerProvider<TypeChangedListener>();
  private final ListenerProvider<TypeSubstitutionChangedListener> substitutionListeners =
      new ListenerProvider<TypeSubstitutionChangedListener>();
  private final TypeManagerBackend backend;

  /**
   * Initializes the type system by loading all {@link RawBaseType raw types} and
   * {@link RawTypeMember raw members} via the backend. Corresponding {@link BaseType base type} and
   * {@link TypeMember type member} instances are created from the raw types. These instances are
   * owned exclusively by this TypeManager instance.
   *
   * @param backend The (database) backend which is used to persistently store the type system.
   * @throws CouldntLoadDataException if the {@link TypeManagerBackend backend} could not load the
   *         raw types / members from the storage.
   */
  public TypeManager(final TypeManagerBackend backend) throws CouldntLoadDataException {
    this.backend = Preconditions.checkNotNull(backend, "IE02774: Backend can not be null.");
    typesContainer = new TypesContainer(backend.loadRawBaseTypes(), backend.loadRawTypeMembers());
  }

  private static String buildArrayName(final BaseType elementType, final int numberElements) {
    return String.format("%s[%d]", elementType.getName(), numberElements);
  }

  private static boolean canDeletePointerType(final BaseType baseType) {
    return baseType.pointedToBy() == null;
  }

  private static List<Integer> membersToIds(final List<TypeMember> members) {
    final List<Integer> result = Lists.newArrayList();
    for (final TypeMember member : members) {
      result.add(member.getId());
    }
    return result;
  }

  private List<TypeMember> idsToMembers(final Integer[] memberIds) {
    final List<TypeMember> members = Lists.newArrayList();
    for (final int id : memberIds) {
      members.add(typesContainer.getTypeMemberById(id));
    }
    return members;
  }

  /**
   * Clears all members from a set of types that have a base type which was deleted.
   *
   * @param deletedType The type that was deleted from the type system and which can not be
   *        referenced by members anymore.
   * @param affectedTypes The set of types that possibly contains one or more members that are of
   *        the deleted type.
   */
  private void clearMembers(final BaseType deletedType, final Set<BaseType> affectedTypes) {
    // Note: we don't delete members explicitly from the database since foreign key constraints will
    // trigger this automatically.
    notifyMembersDeleted(typesContainer.clearMembers(deletedType, affectedTypes));
  }

  /**
   * Removes all type substitutions with the given base type from the backend and notifies
   * listeners.
   *
   * @param deletedType The base type that was deleted.
   * @throws CouldntDeleteException Thrown if the substitutions could not be deleted from the
   *         database.
   */
  private void clearTypeSubstitutions(final BaseType deletedType) throws CouldntDeleteException {
    final Set<TypeSubstitution> substitutions =
        typesContainer.deleteSubstitutionsByType(deletedType);
    if (!substitutions.isEmpty()) {
      for (final TypeSubstitution substitution : substitutions) {
        // TODO(jannewger): this operation could be batched.
        backend.deleteTypeSubstitution(substitution);
      }
      notifySubstitutionsDeleted(substitutions);
    }
  }

  // TODO(jannewger): pass this information to the constructor.
  private int getDefaultPointerSize() {
    return 32;
  }

  /**
   * Creates a new array {@link TypeMember member} via the backend and updates the corresponding
   * base type. No listeners are notified.
   */
  private TypeMember createArrayMember(final BaseType containingType, final BaseType memberType,
      final String memberName, final int memberNumberOfElements) throws CouldntSaveDataException {
    if (!typesContainer.willTypeCreateCyclicReference(containingType, memberType)) {
      final TypeMember member =
          backend.createArrayMember(containingType, memberType, memberName, memberNumberOfElements);
      typesContainer.addMember(member);
      return member;
    } else {
      throw new IllegalStateException("Error: cannot create cyclic member declaration.");
    }
  }

  /**
   * Creates a new base type via the back end and updates all internal data structures. No listeners
   * are notified.
   *
   * @param name The Name of the type to be created.
   * @param size The size of the type to be created in bits.
   * @param signed A {@link Boolean flag} indicating if the type is signed.
   * @param childPointer A {@link BaseType type} to which this type points.
   * @return The generated {@link BaseType type}.
   *
   * @throws CouldntSaveDataException If the changes could not be saved to the database.
   */
  private BaseType instantiateType(final String name, final int size, final boolean signed,
      final BaseType childPointer, final BaseTypeCategory category)
      throws CouldntSaveDataException {
    final BaseType baseType = backend.createType(name, size, signed,
        childPointer == null ? null : childPointer.getId(), category);
    if (childPointer != null) {
      BaseType.appendToPointerHierarchy(childPointer, baseType);
    }
    typesContainer.addBaseType(baseType);
    return baseType;
  }

  private void notifyMemberAdded(final TypeMember member) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.memberAdded(member);
    }
  }

  private void notifyMemberDeleted(final TypeMember member) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.memberDeleted(member);
    }
  }

  private void notifyMembersDeleted(final List<TypeMember> membersToDelete) {
    // TODO(jannewger): this could be batched into a single event.
    for (final TypeMember member : membersToDelete) {
      notifyMemberDeleted(member);
    }
  }

  private void notifyMembersMoved(final Set<BaseType> affectedTypes) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.membersMoved(affectedTypes);
    }
  }

  private void notifyMemberUpdated(final TypeMember member) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.memberUpdated(member);
    }
  }

  private void notifySubstitutionAdded(final TypeSubstitution substitution) {
    for (final TypeSubstitutionChangedListener listener : substitutionListeners) {
      listener.substitutionsAdded(Sets.newHashSet(substitution));
    }
  }

  /**
   * Notify listeners that a single type substitution was changed.
   *
   * @param substitution The type substitution that changed (e.g. its base type).
   */
  private void notifySubstitutionChanged(final TypeSubstitution substitution) {
    for (final TypeSubstitutionChangedListener listener : substitutionListeners) {
      listener.substitutionsChanged(Sets.newHashSet(substitution));
    }
  }

  /**
   * Trigger a substitution changed notification for all type substitutionsByType corresponding to
   * the given set of base types.
   *
   * @param baseTypes The base types that changed.
   */
  private void notifySubstitutionsChanged(final Set<BaseType> baseTypes) {
    final Set<TypeSubstitution> changedSubstitutions =
        typesContainer.getAffectedTypeSubstitutions(baseTypes);
    for (final TypeSubstitutionChangedListener listener : substitutionListeners) {
      listener.substitutionsChanged(changedSubstitutions);
    }
  }

  private void notifySubstitutionsDeleted(final Set<TypeSubstitution> deletedSubstitutions) {
    for (final TypeSubstitutionChangedListener listener : substitutionListeners) {
      listener.substitutionsDeleted(deletedSubstitutions);
    }
  }

  private void notifyTypeAdded(final BaseType baseType) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.typeAdded(baseType);
    }
  }

  private synchronized void notifyTypeDeleted(final BaseType deletedType) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.typeDeleted(deletedType);
    }
  }

  private synchronized void notifyTypesUpdated(final ImmutableSet<BaseType> baseType) {
    for (final TypeChangedListener listener : typeListeners) {
      listener.typesUpdated(baseType);
    }
  }

  public synchronized void addListener(final TypeChangedListener listener) {
    typeListeners.addListener(listener);
  }

  public synchronized void addListener(final TypeSubstitutionChangedListener listener) {
    substitutionListeners.addListener(listener);
  }

  private static Integer determineAppendOffset(final BaseType baseType) {
    Preconditions.checkArgument(BaseTypeCategory.isOffsetCategory(baseType.getCategory()),
        "Error: Can only determine append offset if base type is of offset category.");
    if (!baseType.hasMembers()) {
      return 0;
    }
    final TypeMember lastMember = baseType.getLastMember();
    return lastMember.getBitOffset().get() + lastMember.getBitSize();
  }

  private static Integer determineAppendIndex(final BaseType baseType) {
    Preconditions.checkArgument(baseType.getCategory() == BaseTypeCategory.FUNCTION_PROTOTYPE,
        "Error: Can only determine append index if base type is of index category.");
    if (baseType.hasMembers()) {
      final TypeMember lastMember = baseType.getLastMember();
      return lastMember.getArgumentIndex().get() + 1;
    } else {
      return 0;
    }
  }

  /**
   * Append a member to the end of the list of members.
   *
   * @param containingType The type which should contain the new member.
   * @param memberType The base type of the member itself.
   * @param memberName The name of the member.
   * @return The member that was created or null if adding the member would have created a recursive
   *         declaration.
   * @throws CouldntSaveDataException Thrown if the member could not be saved to the database.
   */
  public synchronized TypeMember appendMember(final BaseType containingType,
      final BaseType memberType, final String memberName) throws CouldntSaveDataException {
    Preconditions.checkNotNull(containingType, "IE02775: Containing type can not be null.");
    Preconditions.checkNotNull(memberType, "IE02776: Base type can not be null.");
    Preconditions.checkNotNull(memberName, "IE02777: Member name can not be null.");
    switch (containingType.getCategory()) {
      case STRUCT:
        return createStructureMember(containingType, memberType, memberName,
            determineAppendOffset(containingType));
      case UNION:
        return createUnionMember(containingType, memberType, memberName);
      case FUNCTION_PROTOTYPE:
        return createFunctionPrototypeMember(containingType, memberType, memberName,
            determineAppendIndex(containingType));
      default:
        throw new IllegalStateException("Error: cannot insert member into non-compound type.");
    }
  }

  public synchronized TypeMember createFunctionPrototypeMember(final BaseType containingType,
      final BaseType memberType, final String memberName, int memberArgumentIndex)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(containingType, "Error: containing type argument can not be null");
    Preconditions.checkNotNull(memberType, "Error: member type can not be null");
    Preconditions.checkArgument(containingType.getCategory() == BaseTypeCategory.FUNCTION_PROTOTYPE,
        "Error: the base type category is not of type function prototype");
    Preconditions.checkNotNull(memberName, "Error: name argument can not be null");
    Preconditions.checkArgument(!memberName.isEmpty(), "Error: name argument can not be empty");
    Preconditions.checkArgument(memberArgumentIndex >= 0,
        "Error: argument index argument can not be smaller than zero");
    if (typesContainer.willTypeCreateCyclicReference(containingType, memberType)) {
      return null;
    }
    // TODO(timkornau): include a method to make sure there are no two arguments at the same index.
    final TypeMember member = backend.createFunctionPrototypeMember(containingType, memberType,
        memberName, memberArgumentIndex);
    notifyMemberCreation(member, typesContainer.addMember(member));
    return member;
  }

  private synchronized void notifyMemberCreation(final TypeMember member,
      final ImmutableSet<BaseType> affectedTypes) {
    notifyMemberAdded(member);
    notifySubstitutionsChanged(affectedTypes);
  }

  private synchronized void notifyMemberUpdated(final TypeMember member,
      final ImmutableSet<BaseType> affectedTypes) {
    notifyMemberUpdated(member);
    notifySubstitutionsChanged(affectedTypes);
  }

  public synchronized TypeMember createStructureMember(final BaseType containingType,
      final BaseType memberType, final String memberName, int memberOffset)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(containingType, "Error: containing type argument can not be null");
    Preconditions.checkNotNull(memberType, "Error: member type can not be null");
    Preconditions.checkNotNull(memberName, "Error: name argument can not be null");
    Preconditions.checkArgument(!memberName.isEmpty(), "Error: name argument can not be empty");
    Preconditions.checkArgument(memberOffset >= 0,
        "Error: argument index argument can not be smaller than zero");
    if (typesContainer.willTypeCreateCyclicReference(containingType, memberType)) {
      return null;
    }
    // We need to determine the original type sizes before doing any changes to the type system!
    final ImmutableMap<BaseType, Integer> originalTypeSizes =
        captureTypeSizesState(typesContainer.getAffectedTypes(containingType));

    // We need to check if there is space in the struct for our new member.
    final ImmutableList<TypeMember> subsequentMembers =
        containingType.getSubsequentMembersInclusive(memberOffset);
    if (!subsequentMembers.isEmpty()) {
      final int moveDelta =
          (memberOffset + memberType.getBitSize()) - subsequentMembers.get(0).getBitOffset().get();
      if (moveDelta > 0) {
        for (final TypeMember member : subsequentMembers) {
          final int newOffset = member.getBitOffset().get() + moveDelta;
          backend.updateStructureMember(member, member.getBaseType(), member.getName(), newOffset);
          member.setOffset(Optional.of(newOffset));
          notifyMemberUpdated(member);
        }
      }
    }

    final TypeMember member =
        backend.createStructureMember(containingType, memberType, memberName, memberOffset);
    final ImmutableSet<BaseType> affectedTypes = typesContainer.addMember(member);
    notifyMemberCreation(member, affectedTypes);
    final Set<BaseType> inconsistentTypes = Sets.newHashSet(affectedTypes);
    inconsistentTypes.remove(containingType);
    ensureConsistencyAfterTypeUpdate(affectedTypes, inconsistentTypes, originalTypeSizes);
    return member;
  }

  public synchronized TypeMember createUnionMember(final BaseType containingType,
      final BaseType memberType, final String memberName) throws CouldntSaveDataException {
    Preconditions.checkArgument(containingType.getCategory() == BaseTypeCategory.UNION,
        "Error: can not create a union member in a non union compound type.");
    return createStructureMember(containingType, memberType, memberName, 0 /* member offset */);
  }

  /**
   * Creates a new array type and writes it to the database.
   *
   * @param elementType The base type of the array elements.
   * @param numberElements The number of elements in the array.
   * @throws CouldntSaveDataException Thrown if the array type could not be saved to the database.
   */
  public synchronized BaseType createArray(final BaseType elementType, final int numberElements)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(elementType, "Error: element type argument can not be null.");
    final String arrayName = buildArrayName(elementType, numberElements);
    final BaseType arrayType = instantiateType(arrayName, elementType.getBitSize() * numberElements,
        false, null, BaseTypeCategory.ARRAY);
    createArrayMember(arrayType, elementType, "array_elements", numberElements);
    notifyTypeAdded(arrayType);
    return arrayType;
  }

  /**
   * Creates a new union type and writes it to the backend.
   *
   * @param name The name of the union type.
   * @return The created base type instance.
   * @throws CouldntSaveDataException Thrown if the type couldn't be stored to the backend.
   */
  public synchronized BaseType createUnion(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "Error: type name can not be null.");
    Preconditions.checkArgument(!name.isEmpty(), "Error: type name can not be empty.");
    final BaseType unionType = instantiateType(name, 0 /* size */, false /* signed */, null,
        BaseTypeCategory.UNION);
    notifyTypeAdded(unionType);
    return unionType;
  }

  /**
   * Creates a new prototype type and writes it to the backend.
   *
   * @return The created {@link BaseType base type} instance.
   * @throws CouldntSaveDataException Thrown if the type could not be stored to the backend.
   */
  public synchronized BaseType createPrototype() throws CouldntSaveDataException {
    final BaseType prototypeType = instantiateType(null /* name */, 0 /* size */, false /* signed */
    , null /* child pointer */, BaseTypeCategory.FUNCTION_PROTOTYPE);
    notifyTypeAdded(prototypeType);
    return prototypeType;
  }

  /**
   * Creates a new base atomic base type instance.
   *
   * @param name The name of the new base type.
   * @param size The size of the base type in bits.
   * @param signed Specifies whether the type can represent signed numbers.
   * @return The newly created base types.
   * @throws CouldntSaveDataException Thrown if the type could not be written to the backend.
   */
  public synchronized BaseType createAtomicType(final String name, final int size,
      final boolean signed) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE02778: Type name can not be null.");
    Preconditions.checkArgument(size >= 0, "Size can not be negative.");
    final BaseType newType = instantiateType(name, size, signed, null, BaseTypeCategory.ATOMIC);
    notifyTypeAdded(newType);
    return newType;
  }

  /**
   * Creates a new pointer type relative to the given base type and inserts it into the pointer
   * hierarchy. A new pointer type is only created if the given base type does not already have a
   * parent pointer type.
   *
   * @param baseType The base type for which to create a new parent pointer type.
   * @return The newly created pointer type or the next pointer type in the existing hierarchy.
   * @throws CouldntSaveDataException Thrown if the new pointer type could not be saved to the
   *         backend.
   */
  public synchronized BaseType createPointerType(final BaseType baseType)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(baseType, "IE02781: Base type can not be null.");
    if (baseType.pointedToBy() != null) {
      return baseType.pointedToBy();
    } else {
      final String newTypeName =
          BaseType.getPointerTypeName(baseType, baseType.getPointerLevel() + 1);
      final BaseType newType = instantiateType(newTypeName, getDefaultPointerSize(), false,
          baseType, BaseTypeCategory.POINTER);
      notifyTypeAdded(newType);
      return newType;
    }
  }

  /**
   * Associates a base type with the given operand, creates a new type substitution instance, stores
   * it in the database and assigns it to the given operand tree node. Finally, listeners are
   * notified about the newly added type substitution.
   *
   *  If baseType and offset cannot unambiguously reference the desired member (e.g. if a (nested)
   * union exists in baseType) a memberPath must be supplied to resolve this unambiguity.
   *
   * @param node The operand tree node that should be associated with the given base type.
   * @param baseType The base type that is associated with the given node.
   * @param memberPath A sequence of members starting at baseType that contains all nested members
   *        until the final member that corresponds to the actual type of the expression. Can be
   *        empty if base type and offset unambiguously identify the desired member.
   * @param position The zero-based index position of the operand within its instruction.
   * @param offset Specifies an additional offset relative to the beginning of the base type so the
   *        type substitution can point to a specific member.
   * @param address The address of the instruction that contains the operand tree node that receives
   *        the type substitution.
   * @return The newly created, database backed type substitution.
   * @throws CouldntSaveDataException Thrown if the type substitution couldn't be stored in the
   *         database.
   */
  public synchronized TypeSubstitution createTypeSubstitution(final INaviOperandTreeNode node,
      final BaseType baseType,
      final List<TypeMember> memberPath,
      final int position,
      final int offset,
      final IAddress address) throws CouldntSaveDataException {
    Preconditions.checkNotNull(node, "IE02782: Operand tree node can not be null.");
    Preconditions.checkNotNull(baseType, "IE02783: Base type can not be null.");
    Preconditions.checkArgument(offset >= 0, "Offset can not be negative.");
    Preconditions.checkNotNull(address, "IE02784: Address can not be null.");
    final TypeSubstitution substitution = backend.createTypeSubstitution(node,
        baseType,
        membersToIds(memberPath),
        position,
        offset,
        address);
    typesContainer.addTypeSubstitution(substitution);
    node.setTypeSubstitution(substitution);
    notifySubstitutionAdded(substitution);
    return substitution;
  }

  /**
   * Works in the same way as the overloaded method but uses an empty member path.
   */
  public synchronized TypeSubstitution createTypeSubstitution(final INaviOperandTreeNode node,
      final BaseType baseType, final int position, final int offset, final IAddress address)
      throws CouldntSaveDataException {
    return createTypeSubstitution(node,
        baseType,
        new ArrayList<TypeMember>(),
        position,
        offset,
        address);
  }

  /**
   * Deletes the given member from its containing base type.
   *
   * @param member The member to delete from its containing base type.
   * @throws CouldntDeleteException Thrown if the member couldn't be deleted from the backend.
   * @throws CouldntSaveDataException Thrown if changes to the type system couldn't be saved to the
   *         backend.
   */
  public synchronized void deleteMember(final TypeMember member) throws CouldntDeleteException,
      CouldntSaveDataException {
    Preconditions.checkNotNull(member, "IE02785: Member can not be null.");
    backend.deleteMember(member);
    final BaseType containingType = member.getParentType();
    boolean sizeChanged = member == containingType.getLastMember();
    final ImmutableMap<BaseType, Integer> originalTypeSizes =
        (sizeChanged) ? captureTypeSizesState(typesContainer.getAffectedTypes(containingType))
            : ImmutableMap.<BaseType, Integer>of();
    final ImmutableSet<BaseType> affectedTypes = typesContainer.deleteTypeMember(member);
    notifySubstitutionsChanged(affectedTypes);
    notifyTypesUpdated(affectedTypes);
    notifyMemberDeleted(member);
    if (sizeChanged) {
      ensureConsistencyAfterTypeUpdate(affectedTypes, Sets.newHashSet(affectedTypes),
          originalTypeSizes);
    }
  }

  /**
   * Deletes a type from the collection of known types. The corresponding members and type
   * substitutions are deleted from the database as well.
   *
   * @param baseType The base type to be removed.
   * @throws CouldntDeleteException Thrown if the type could not be deleted from the database.
   */
  public synchronized boolean deleteType(final BaseType baseType) throws CouldntDeleteException {
    Preconditions.checkNotNull(baseType, "IE02786: Base type can not be null.");
    if (!canDeletePointerType(baseType)) {
      return false;
    }
    final Set<BaseType> affectedTypes = typesContainer.getAffectedTypes(baseType);
    clearMembers(baseType, affectedTypes);
    clearTypeSubstitutions(baseType);
    typesContainer.deleteBaseType(baseType);
    backend.deleteType(baseType);
    notifyTypeDeleted(baseType);
    return true;
  }

  /**
   * Creates a new empty structure type and stores it in the backend.
   *
   * @param name The name of the new type.
   * @return Returns the new type instance
   *
   * @throws CouldntSaveDataException Thrown if the type could not be written to the backend.
   */
  public synchronized BaseType createStructure(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "Name can not be null.");
    final BaseType structType = instantiateType(name, 0 /* size */, false, null,
        BaseTypeCategory.STRUCT);
    notifyTypeAdded(structType);
    return structType;
  }

  /**
   * Removes the type substitution from the given tree node and deletes the corresponding database
   * record.
   *
   * @param node The tree node whose type substitution should be removed.
   * @throws CouldntDeleteException Thrown if the type substitution couldn't be deleted.
   */
  public synchronized void deleteTypeSubstitution(final INaviOperandTreeNode node)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(node, "IE02787: Operand tree node can not be null.");
    final TypeSubstitution substitution = node.getTypeSubstitution();
    typesContainer.deleteTypeSubstitution(substitution);
    backend.deleteTypeSubstitution(substitution);
    node.setTypeSubstitution(null);
    notifySubstitutionsDeleted(Collections.singleton(substitution));
  }

  /**
   * Returns the base type instance that corresponds to the given base type id from the database.
   *
   * @param typeId The type id from the database.
   * @return The base type instance corresponding to the given database type id.
   */
  public synchronized BaseType getBaseType(final int typeId) {
    return typesContainer.getBaseTypeById(typeId);
  }

  /**
   * Returns an unmodifiable list of all types.
   *
   * @return The list of all types.
   */
  public synchronized List<BaseType> getTypes() {
    return typesContainer.getTypes();
  }

  /**
   * Converts the given raw type substitution to a TypeSubstitution instance and assigns it to the
   * given operand tree node. This function does not use the database back end.
   *
   * @param node The operand tree node for which to initialize the type substitution.
   * @param rawSubstitution The raw type substitution that is converted to an actual type
   *        substitution.
   */
  public synchronized void initializeTypeSubstitution(final INaviOperandTreeNode node,
      final RawTypeSubstitution rawSubstitution) {
    Preconditions.checkNotNull(node, "IE02420: Operand tree node can not be null.");
    Preconditions.checkNotNull(rawSubstitution, "IE02421: Raw type substitution can not be null.");
    final BaseType baseType = typesContainer.getBaseTypeById(rawSubstitution.getBaseTypeId());
    final TypeSubstitution substitution = new TypeSubstitution(node,
        baseType,
        rawSubstitution.getExpressionId(),
        rawSubstitution.getPosition(),
        rawSubstitution.getOffset(),
        rawSubstitution.getAddress());
    node.setTypeSubstitution(substitution);
    typesContainer.addTypeSubstitution(substitution);
    notifySubstitutionAdded(substitution);
  }

  /**
   * Creates a new member and inserts it right after an existing member in the corresponding
   * compound type. Subsequent members are moved towards the end of the compound type by the amount
   * required by the new member.
   *
   * @param existingMember The existing member after which the new member should be inserted.
   * @param memberType The base type of the new member.
   * @param memberName The name of the new member.
   * @return The new member that was inserted.
   * @throws CouldntSaveDataException Thrown if the new member could not be saved in the database.
   */
  public synchronized TypeMember insertMemberAfter(final TypeMember existingMember,
      final BaseType memberType, final String memberName) throws CouldntSaveDataException {
    Preconditions.checkNotNull(existingMember, "Error: existing member can not be null.");
    switch (existingMember.getParentType().getCategory()) {
      case STRUCT:
        return createStructureMember(existingMember.getParentType(), memberType, memberName,
            existingMember.getBitSize() + existingMember.getBitOffset().get());
      case UNION:
        return createUnionMember(existingMember.getParentType(), memberType, memberName);
      case FUNCTION_PROTOTYPE:
        return createFunctionPrototypeMember(existingMember.getParentType(), memberType, memberName,
            existingMember.getArgumentIndex().get() + 1);
      default:
        throw new IllegalStateException("Error: cannot insert member into non-compound type.");
    }
  }

  /**
   * Tests whether baseType is (transitively) contained within superType. A type A is said to be
   * contained within B iff B has a member of type A, or if there exists a chain of nested types
   * within B leading to at least one type that has a member of type A.
   *
   * @param superType The type that is checked if it includes baseType.
   * @param baseType The type for which to check if it is contained within superType.
   * @return True iff baseType is contained within superType.
   */
  public synchronized boolean isContainedIn(final BaseType superType, final BaseType baseType) {
    // TODO(jannewger): we should get rid off TypesContainer; it adds an unnecessary layer of
    // indirection to the implementation of the type manager. Also see comment in insertMemberAfter.
    Preconditions.checkNotNull(superType, "Error: Super type can not be null.");
    Preconditions.checkNotNull(baseType, "Error: Base type can not be null.");
    return typesContainer.isTypeContainedIn(superType, baseType);
  }

  /**
   * Tests whether a type with the given name exists.
   *
   * @param name The name of the base type.
   * @return True iff a base type with the given name exists.
   */
  public synchronized boolean isTypeExisting(final String name) {
    Preconditions.checkNotNull(name, "Error: Name can not be null.");
    return typesContainer.doesTypeNameExist(name);
  }

  /**
   * Loads an existing base type from the backend and creates a corresponding {@link BaseType
   * instance}.
   *
   * @param baseTypeId The backend id of the base type to load.
   * @throws CouldntLoadDataException Thrown if the base type could not be loaded by the backend.
   */
  public synchronized void loadAndInitializeBaseType(final int baseTypeId)
      throws CouldntLoadDataException {
    final BaseType baseType = backend.loadRawBaseType(baseTypeId);
    typesContainer.addBaseType(baseType);
    notifyTypeAdded(baseType);
  }

  /**
   * Loads an existing type member from the backend and creates a corresponding {@link TypeMember
   * instance}.
   *
   * @param rawMemberId The backend id of the member to load.
   * @throws CouldntLoadDataException Thrown if the member could not be loaded by the backend.
   */
  public synchronized void loadAndInitializeTypeMember(final int rawMemberId)
      throws CouldntLoadDataException {
    final RawTypeMember typeMember = backend.loadRawTypeMember(rawMemberId);
    final ImmutableSet<BaseType> affectedTypes = typesContainer.addMember(typeMember);
    notifyMemberAdded(typesContainer.getTypeMemberById(rawMemberId));
    notifyTypesUpdated(affectedTypes);
    notifySubstitutionsChanged(affectedTypes);
  }

  /**
   * Updates an existing {@link BaseType base type} instance by loading the corresponding data via
   * the backend. Does not alter the database. Notifies listeners about base types and type
   * substitutions that are affected by the updated base type.
   *
   * @param baseTypeId The id of the type that should be updated.
   * @throws CouldntLoadDataException Thrown if the backend could not load the type data.
   */
  public synchronized void loadAndUpdateBaseType(final int baseTypeId)
      throws CouldntLoadDataException {
    final BaseType newBaseType = backend.loadRawBaseType(baseTypeId);
    final BaseType oldBaseType = typesContainer.getBaseTypeById(baseTypeId);
    final ImmutableSet<BaseType> affectedTypes = typesContainer.updateBaseType(oldBaseType,
        newBaseType.getName(), newBaseType.isSigned(), newBaseType.getBitSize());
    notifyTypesUpdated(affectedTypes);
    notifySubstitutionsChanged(affectedTypes);
  }

  /**
   * Updates an existing {@link TypeMember member} instance by loading the member properties from
   * the backend. Does not alter the database. Notifies listeners about the changed member, affected
   * base types and type substitutions.
   *
   * @param typeMemberId The id of the {@link TypeMember type member} to load.
   * @throws CouldntLoadDataException Thrown if the data could not be loaded from the backend.
   */
  public synchronized void loadAndUpdateTypeMember(final int typeMemberId)
      throws CouldntLoadDataException {
    final TypeMember typeMember = typesContainer.getTypeMemberById(typeMemberId);
    final RawTypeMember rawTypeMember = backend.loadRawTypeMember(typeMemberId);
    final BaseType baseType = typesContainer.getBaseTypeById(rawTypeMember.getBaseTypeId());
    final ImmutableSet<BaseType> affectedTypes = typesContainer.updateTypeMember(typeMember,
        baseType,
        rawTypeMember.getName(),
        rawTypeMember.getOffset(),
        rawTypeMember.getNumberOfElements(),
        rawTypeMember.getArgumentIndex());
    notifyMemberUpdated(typeMember);
    notifySubstitutionsChanged(affectedTypes);
    notifyTypesUpdated(affectedTypes);
  }

  /**
   * Moves a set of members to a new offset within a parent. All members must be part of the same
   * base type, i.e. parentType. The relative distances between the given members are preserved.
   * Succeeding or preceding member's offsets are changed accordingly.
   *
   * @param parentType The parent type that contains the given members.
   * @param members The members that move to a new offset within their parent type.
   * @param delta The delta by which the members should be moved within parentType.
   * @throws CouldntSaveDataException Thrown if the member positions could not be written to the
   *         backend.
   */
  public synchronized void moveMembers(final BaseType parentType, final List<TypeMember> members,
      final int delta) throws CouldntSaveDataException {
    Preconditions.checkNotNull(parentType, "Error: parent type can not be null.");
    Preconditions.checkNotNull(members, "Error: members can not be null.");
    Preconditions.checkArgument(delta != 0, "Move delta can not be zero.");
    final MemberMoveResult result = parentType.moveMembers(Sets.newTreeSet(members), delta);
    final Set<BaseType> affectedTypes = typesContainer.getAffectedTypes(parentType);
    backend.updateMemberOffsets(membersToIds(members), delta,
        membersToIds(result.getImplicitlyMoved()), result.getImplicitlyMovedDelta());
    notifyMembersMoved(affectedTypes);
  }

  /**
   * Removes the {@link BaseType base type} instance corresponding to the given base type id from
   * the type manager without modifying the backend.
   *
   * @param baseTypeId The backend if of the base type that should be removed from the type manager.
   */
  public synchronized void removeBaseTypeInstance(final int baseTypeId) {
    final BaseType deletedType = typesContainer.getBaseTypeById(baseTypeId);
    final Set<BaseType> affectedTypes = typesContainer.deleteBaseTypeById(baseTypeId);
    clearMembers(deletedType, affectedTypes);
    notifySubstitutionsDeleted(typesContainer.deleteSubstitutionsByType(deletedType));
    notifyTypeDeleted(deletedType);
  }

  public synchronized void removeListener(final TypeChangedListener listener) {
    Preconditions.checkNotNull(listener, "IE02790: Listener argument can not be null.");
    typeListeners.removeListener(listener);
  }

  public synchronized void removeListener(final TypeSubstitutionChangedListener listener) {
    Preconditions.checkNotNull(listener, "Error: Listener argument can not be null.");
    substitutionListeners.removeListener(listener);
  }

  /**
   * Removes the {@link TypeMember member} instance corresponding to the given member id from the
   * type manager without modifying the backend.
   *
   * @param typeMemberId The backend id of the member that should be removed from the type manager.
   */
  public synchronized void removeMemberInstance(final int typeMemberId) {
    final TypeMember typeMember = typesContainer.getTypeMemberById(typeMemberId);
    final ImmutableSet<BaseType> affectedTypes = typesContainer.deleteTypeMemberById(typeMemberId);
    notifySubstitutionsChanged(affectedTypes);
    notifyTypesUpdated(affectedTypes);
    notifyMemberDeleted(typeMember);
  }

  /**
   * Removes the given {@link TypeSubstitution type substitution} from the type manager without
   * modifying the backend.
   *
   * @param substitution The {@link TypeSubstitution} to be deleted.
   */
  public synchronized void removeTypeSubstitutionInstance(final TypeSubstitution substitution) {
    Preconditions.checkNotNull(substitution, "Error: substitution argument can not be null");
    typesContainer.deleteTypeSubstitution(substitution);
    // TODO(jannewger): trigger notifications!
  }

  /**
   * Marks the given base type as being a stack frame. This property has no immediate database
   * record, and any changes made to it are thus not synchronized to the database (or other running
   * instances of BinNavi). When the database is loaded it is set iff a function has marked this
   * base type as the associated stack frame type.
   *
   * @param baseType The {@link BaseType} to mark as being a stack frame.
   */
  public synchronized void setStackFrame(final BaseType baseType) {
    Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null");
    baseType.setIsStackFrame(true);
  }

  /**
   * Updates an existing array type in the database.
   *
   * @param arrayType The existing array type.
   * @param elementType The new base type of the array elements.
   * @param numberOfElements The new number of elements.
   * @throws CouldntSaveDataException Thrown if the array type could not be updated in the backend.
   */
  public synchronized void updateArray(final BaseType arrayType, final BaseType elementType,
      final int numberOfElements) throws CouldntSaveDataException {
    Preconditions.checkNotNull(arrayType, "IE02791: Base type can not be null.");
    Preconditions.checkArgument(arrayType.getCategory() == BaseTypeCategory.ARRAY,
        "Base type must be an array.");
    Preconditions.checkNotNull(elementType, "IE02792: Element type can not be null.");
    Preconditions.checkArgument(numberOfElements > 0, "Number of elements must be above zero.");
    final TypeMember arrayMember = arrayType.iterator().next();
    typesContainer.updateTypeMember(arrayMember,
        elementType,
        arrayMember.getName(),
        arrayMember.getBitOffset(),
        Optional.of(numberOfElements),
        arrayMember.getArgumentIndex());
    final int newArraySize = arrayMember.getNumberOfElements().get() * arrayMember.getBitSize();
    final String newArrayName = buildArrayName(elementType, numberOfElements);
    final ImmutableSet<BaseType> affectedTypes =
        typesContainer.updateBaseType(arrayType, newArrayName, arrayType.isSigned(), newArraySize);
    backend.updateArrayMember(arrayMember, elementType, numberOfElements);
    backend.updateType(arrayType, newArrayName, arrayType.getBitSize(), arrayType.isSigned());
    notifyTypesUpdated(affectedTypes);
  }

  public synchronized void updateStructureMember(final TypeMember updatedMember,
      final BaseType newMemberBaseType, final String newMemberName, int newMemberOffset)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(updatedMember, "Error: updated member argument can not be null.");
    Preconditions.checkArgument(
        updatedMember.getParentType().getCategory() == BaseTypeCategory.STRUCT,
        "Error: updated member argument must be a member of a struct base type.");
    Preconditions.checkNotNull(newMemberBaseType,
        "Error: new member base type argument can not be null.");
    Preconditions.checkNotNull(newMemberName, "Error: new member name argument can not be null.");
    Preconditions.checkArgument(!newMemberName.isEmpty(),
        "Error: new member name argument can not be empty.");
    Preconditions.checkArgument(newMemberOffset >= 0,
        "Error: new member offset argument must be larger or equal to zero");
    final ImmutableMap<BaseType, Integer> originalTypeSizes =
        captureTypeSizesState(typesContainer.getAffectedTypes(updatedMember.getParentType()));
    final int memberSizeDelta =
        newMemberBaseType.getBitSize() - updatedMember.getBaseType().getBitSize();
    backend.updateStructureMember(updatedMember, newMemberBaseType, newMemberName, newMemberOffset);
    final ImmutableSet<BaseType> affectedTypes = typesContainer.updateTypeMember(updatedMember,
        newMemberBaseType,
        newMemberName,
        Optional.of(newMemberOffset),
        updatedMember.getNumberOfElements(),
        updatedMember.getArgumentIndex());
    notifyMemberUpdated(updatedMember, affectedTypes);
    for (final TypeMember member :
        updatedMember.getParentType().getSubsequentMembers(updatedMember)) {
      backend.updateStructureMember(member, member.getBaseType(), member.getName(),
          member.getBitOffset().get() + memberSizeDelta);
    }
    ensureConsistencyAfterTypeUpdate(affectedTypes, Sets.newHashSet(affectedTypes),
        originalTypeSizes);
  }

  public synchronized void updateUnionMember(final TypeMember updatedMember,
      final BaseType newMemberBaseType, final String newMemberName)
      throws CouldntSaveDataException {
    updateStructureMember(updatedMember, newMemberBaseType, newMemberName, 0 /* member offset */);
  }

  public synchronized void updateFunctionPrototypeMember(final TypeMember updatedMember,
      final BaseType newMemberBaseType, final String newMemberName, int newMemberArgumentIndex)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(updatedMember, "Error: updated member argument can not be null.");
    Preconditions.checkArgument(
        updatedMember.getParentType().getCategory() == BaseTypeCategory.STRUCT,
        "Error: updated member argument must be a member of a struct base type.");
    Preconditions.checkNotNull(newMemberBaseType,
        "Error: new member base type argument can not be null.");
    Preconditions.checkNotNull(newMemberName, "Error: new member name argument can not be null.");
    Preconditions.checkArgument(!newMemberName.isEmpty(),
        "Error: new member name argument can not be empty.");
    Preconditions.checkArgument(newMemberArgumentIndex >= 0,
        "Error: new member argument index argument must be larger or equal to zero");
    backend.updateFunctionPrototypeMember(updatedMember, newMemberBaseType, newMemberName,
        newMemberArgumentIndex);
    final ImmutableSet<BaseType> affectedTypes = typesContainer.updateTypeMember(updatedMember,
        newMemberBaseType,
        newMemberName,
        updatedMember.getBitOffset(),
        updatedMember.getNumberOfElements(),
        Optional.of(newMemberArgumentIndex));
    notifyMemberUpdated(updatedMember, affectedTypes);
  }

  private static ImmutableMap<BaseType, Integer> captureTypeSizesState(
      final Set<BaseType> baseTypes) {
    final Builder<BaseType, Integer> builder = ImmutableMap.<BaseType, Integer>builder();
    for (BaseType baseType : baseTypes) {
      if (baseType.getCategory() != BaseTypeCategory.FUNCTION_PROTOTYPE) {
        builder.put(baseType, baseType.getBitSize());
      }
    }
    return builder.build();
  }

  /**
   * Updates an existing base type with the new properties. Note: the pointer aspect of a base type
   * can not be changed after the type has been created in order to avoid the many side effects such
   * a change would cause.
   *
   * @param baseType The base type that should be updated.
   * @param name The new name of the base type.
   * @param size The new size in bits of the base type.
   * @param isSigned Specifies whether the updated base type is signed.
   * @throws CouldntSaveDataException Thrown if the base type could not be updated in the backend.
   */
  public synchronized void updateType(final BaseType baseType, final String name, final int size,
      final boolean isSigned) throws CouldntSaveDataException {
    Preconditions.checkNotNull(baseType, "IE02422: Base type argument can not be null.");
    Preconditions.checkNotNull(name, "IE02621: Name argument can not be null.");
    Preconditions.checkArgument(size >= 0, "Size argument can not be negative.");

    final boolean sizeChanged = baseType.getBitSize() != size;
    final ImmutableMap<BaseType, Integer> originalTypeSizes =
        (sizeChanged) ? captureTypeSizesState(typesContainer.getAffectedTypes(baseType))
            : ImmutableMap.<BaseType, Integer>of();
    backend.updateType(baseType, name, size, isSigned);
    final ImmutableSet<BaseType> affectedTypes =
        typesContainer.updateBaseType(baseType, name, isSigned, size);
    notifyTypesUpdated(affectedTypes);
    notifySubstitutionsChanged(affectedTypes);
    if (sizeChanged) {
      ensureConsistencyAfterTypeUpdate(affectedTypes, Sets.newHashSet(affectedTypes),
          originalTypeSizes);
    }
  }

  /**
   * Renames an existing base type and stores the changes to the backend.
   *
   * @param baseType The base type to rename.
   * @param newName The new name for the base type.
   * @throws CouldntSaveDataException Thrown if the changes couldn't be written to the backend.
   */
  public synchronized void renameType(final BaseType baseType, final String newName)
      throws CouldntSaveDataException {
    updateType(baseType, newName, baseType.getBitSize(), baseType.isSigned());
  }

  /**
   * Updates a type substitution only in the local storage not in the database.
   *
   * @param node The {@link INaviOperandTreeNode} to which the {@link TypeSubstitution type
   *        substitution} is associated.
   * @param baseTypeId The id of the {@link BaseType base type} which is the new {@link BaseType}.
   * @param memberPathIds The set of ids that describe a member path.
   * @param offset The new offset of the {@link TypeSubstitution type substitution}.
   */
  public synchronized void updateTypeSubstitution(final INaviOperandTreeNode node,
      final int baseTypeId, final Integer[] memberPathIds, final int offset) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null.");
    Preconditions.checkNotNull(memberPathIds, "Error: member path ids can not be null.");
    final BaseType baseType = typesContainer.getBaseTypeById(baseTypeId);
    final TypeSubstitution typeSubstitution = node.getTypeSubstitution();
    typesContainer.updateTypeSubstitution(typeSubstitution, baseType, idsToMembers(memberPathIds),
        offset);
    notifySubstitutionChanged(typeSubstitution);
  }

  /**
   * Updates the given type substitution in the back end and notifies listeners about the changed
   * substitution.
   *
   * @param node The operand tree node whose substitution should be updated.
   * @param substitution The substitution to update.
   * @param baseType The new base type for the substitution.
   * @param memberPath The sequence of members that unambiguously references a member.
   * @param offset The new offset for the substitution.
   * @throws CouldntSaveDataException Thrown if the type substitution could not be updated.
   */
  public synchronized void updateTypeSubstitution(final INaviOperandTreeNode node,
      final TypeSubstitution substitution, final BaseType baseType,
      final List<TypeMember> memberPath, final int offset) throws CouldntSaveDataException {
    Preconditions.checkNotNull(node, "IE02799: Operand tree node can not be null.");
    Preconditions.checkNotNull(substitution, "IE02800: Type subustitution can not be null.");
    Preconditions.checkNotNull(baseType, "IE02801: Base type can not be null.");
    typesContainer.updateTypeSubstitution(substitution, baseType, memberPath, offset);
    backend.updateSubstitution(substitution, baseType, membersToIds(memberPath), offset);
    notifySubstitutionChanged(substitution);
  }

  /**
   * Recurses over a set of types whose member offsets need to be fixed due to a size change of a
   * single base type. Since the order in which member offsets are fixed is important, the
   * implementation ensures that immediately affected types (i.e. types that have member of the
   * changed base type) are changed before implicitly affected types.
   */
  // TODO(jannewger): oldSizes and affectedTypes could be merged into one Map.
  private void adjustMemberOffsets(final BaseType structTypeToFix,
      final Set<BaseType> inconsistentTypes, final Map<BaseType, Integer> oldSizes,
      final Set<BaseType> affectedTypes) throws CouldntSaveDataException {
    if (!inconsistentTypes.contains(structTypeToFix)) {
      return;
    }
    // Find the first member that requires an update; all subsequent members require offset updates
    // as well.
    final List<TypeMember> affectedMembers =
        determineMembersToUpdate(structTypeToFix, affectedTypes);
    int sizeDelta = 0;
    for (final TypeMember member : affectedMembers) {
      final BaseType memberBaseType = member.getBaseType();
      if (inconsistentTypes.contains(memberBaseType)) {
        // If we encounter a type whose size was changed but that is not yet fixed, recurse.
        adjustMemberOffsets(memberBaseType, inconsistentTypes, oldSizes, affectedTypes);
      }
      if (sizeDelta != 0) {
        Integer newOffset = member.getBitOffset().get() + sizeDelta;
        // TODO(jannewger): collect all updateMember calls and send them out in one batch (once we
        // have the global database lock).
        backend.updateStructureMember(member, member.getBaseType(), member.getName(), newOffset);
        notifyMemberUpdated(member);
        member.setOffset(Optional.of(newOffset));
      }
      // If the size of the member type changed we need to update the size delta as well since that
      // affects subsequent member offsets.
      if (oldSizes.containsKey(memberBaseType)) {
        sizeDelta += memberBaseType.getBitSize() - oldSizes.get(memberBaseType);
      }
    }
    inconsistentTypes.remove(structTypeToFix);
  }

  private static ImmutableList<TypeMember> determineMembersToUpdate(final BaseType baseType,
      final Set<BaseType> affectedTypes) {
    final ImmutableList.Builder<TypeMember> builder = ImmutableList.<TypeMember>builder();
    boolean includeMember = false;
    for (TypeMember member : baseType) {
      if (affectedTypes.contains(member.getBaseType())) {
        // All members after the first one whose base type is contained in affectedTypes need
        // to be updated.
        if (!includeMember) {
          includeMember = true;
        }
      }
      if (includeMember) {
        builder.add(member);
      }
    }
    return builder.build();
  }

  /**
   * Transitively propagates effects of a size change of a single base type through the type system.
   *
   * @param affectedTypes The set of types that depend on the updated type.
   * @param inconsistentTypes The set of types that are still inconsistent due to the size change.
   * @param oldSizes A mapping of all affected types to their sizes before updatedType was changed.
   *
   * @throws CouldntSaveDataException Thrown if the transitive effects couldn't be written to the
   *         database.
   */
  private void ensureConsistencyAfterTypeUpdate(final ImmutableSet<BaseType> affectedTypes,
      final Set<BaseType> inconsistentTypes, final Map<BaseType, Integer> oldSizes)
      throws CouldntSaveDataException {
    while (!inconsistentTypes.isEmpty()) {
      final BaseType baseType = inconsistentTypes.iterator().next();
      if (baseType.getCategory() == BaseTypeCategory.STRUCT) {
        adjustMemberOffsets(baseType, inconsistentTypes, oldSizes, affectedTypes);
      } else {
        inconsistentTypes.remove(baseType);
      }
    }
  }

  private final class TypesContainer {
    private final Set<BaseType> types = new LinkedHashSet<BaseType>();
    private final Map<String, BaseType> typesByName = new HashMap<String, BaseType>();
    private final Map<Integer, BaseType> typesById = new HashMap<Integer, BaseType>();
    private final ArrayList<BaseType> stableTypeList = new ArrayList<BaseType>();
    private final HashMultimap<BaseType, TypeSubstitution> substitutionsByType =
        HashMultimap.create();
    private final Map<Integer, TypeMember> memberById = new HashMap<Integer, TypeMember>();
    private final TypeDependenceGraph dependenceGraph;

    public TypesContainer(final List<RawBaseType> rawBaseTypes,
        final List<RawTypeMember> rawMembers) {
      dependenceGraph = initializeTypeSystem(rawBaseTypes, rawMembers);
    }

    public void addBaseType(final BaseType baseType) {
      Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null");
      types.add(baseType);
      typesById.put(baseType.getId(), baseType);
      typesByName.put(baseType.getName(), baseType);
      stableTypeList.clear();
      dependenceGraph.addType(baseType);
    }

    public ImmutableSet<BaseType> addMember(final RawTypeMember rawTypeMember) {
      if (rawTypeMember.getOffset().isPresent()) {
        return addMember(TypeMember.createStructureMember(rawTypeMember.getId(),
            typesById.get(rawTypeMember.getParentId()),
            typesById.get(rawTypeMember.getBaseTypeId()), rawTypeMember.getName(),
            rawTypeMember.getOffset().get()));
      }
      if (rawTypeMember.getNumberOfElements().isPresent()) {
        return addMember(TypeMember.createArrayMember(rawTypeMember.getId(),
            typesById.get(rawTypeMember.getParentId()),
            typesById.get(rawTypeMember.getBaseTypeId()), rawTypeMember.getName(),
            rawTypeMember.getNumberOfElements().get()));
      }
      if (rawTypeMember.getArgumentIndex().isPresent()) {
        return addMember(TypeMember.createFunctionPrototypeMember(rawTypeMember.getId(),
            typesById.get(rawTypeMember.getParentId()),
            typesById.get(rawTypeMember.getBaseTypeId()), rawTypeMember.getName(),
            rawTypeMember.getArgumentIndex().get()));
      }
      throw new IllegalStateException("Error: member can only be added to compound types.");
    }

    public ImmutableSet<BaseType> addMember(final TypeMember member) {
      Preconditions.checkNotNull(member, "Error: typeMember argument can not be null");
      final DependenceResult result =
          dependenceGraph.addMember(member.getParentType(), member.getBaseType());
      if (!result.isValid()) {
        throw new IllegalStateException("Error: member would create cyclic reference.");
      }
      memberById.put(member.getId(), member);
      member.getParentType().addMember(member);
      return result.getAffectedTypes();
    }

    public void addTypeSubstitution(final TypeSubstitution substitution) {
      substitutionsByType.put(substitution.getBaseType(), substitution);
    }

    public List<TypeMember> clearMembers(final BaseType deletedType,
        final Set<BaseType> affectedTypes) {
      final List<TypeMember> deletedMembers = Lists.newArrayList();
      for (final BaseType currentType : affectedTypes) {
        final List<TypeMember> membersToDelete = Lists.newArrayList();
        for (final TypeMember member : currentType) {
          if (member.getBaseType() == deletedType) {
            membersToDelete.add(member);
          }
        }
        if (membersToDelete.size() > 0) {
          for (final TypeMember typeMembert : membersToDelete) {
            deleteTypeMember(typeMembert);
          }
          deletedMembers.addAll(membersToDelete);
        }
      }
      return deletedMembers;
    }

    public Set<BaseType> deleteBaseType(final BaseType baseType) {
      Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null");
      types.remove(baseType);
      typesById.remove(baseType.getId());
      typesByName.remove(baseType.getName());
      substitutionsByType.removeAll(baseType);
      stableTypeList.clear();
      return dependenceGraph.deleteType(baseType);
    }

    public Set<BaseType> deleteBaseTypeById(final int baseTypeId) {
      return deleteBaseType(typesById.get(baseTypeId));
    }

    public Set<TypeSubstitution> deleteSubstitutionsByType(final BaseType deletedType) {
      return substitutionsByType.removeAll(deletedType);
    }

    public ImmutableSet<BaseType> deleteTypeMember(final TypeMember typeMember) {
      Preconditions.checkNotNull(typeMember, "Error: typeMember argument can not be null");
      typeMember.getParentType().deleteMember(typeMember);
      memberById.remove(typeMember.getId());
      return dependenceGraph.deleteMember(typeMember);
    }

    public ImmutableSet<BaseType> deleteTypeMemberById(final int typeMemberId) {
      return deleteTypeMember(memberById.get(typeMemberId));
    }

    public void deleteTypeSubstitution(final TypeSubstitution typeSubstitution) {
      Preconditions.checkNotNull(typeSubstitution,
          "Error: typeSubstitution argument can not be null");
      substitutionsByType.remove(typeSubstitution.getBaseType(), typeSubstitution);
    }

    public boolean doesTypeNameExist(final String name) {
      return typesByName.containsKey(name);
    }

    private Set<TypeSubstitution> getAffectedTypeSubstitutions(final Set<BaseType> baseTypes) {
      final Set<BaseType> affectedTypes =
          Sets.intersection(baseTypes, substitutionsByType.keySet());
      final HashSet<TypeSubstitution> typeSubstitutions = Sets.newHashSet();
      for (final BaseType baseType : affectedTypes) {
        typeSubstitutions.addAll(substitutionsByType.get(baseType));
      }
      return typeSubstitutions;
    }

    public ImmutableSet<BaseType> getAffectedTypes(final BaseType baseType) {
      return dependenceGraph.determineDependentTypes(baseType);
    }

    public BaseType getBaseTypeById(final int baseTypeId) {
      return typesById.get(baseTypeId);
    }

    public TypeMember getTypeMemberById(final int typeMemberId) {
      return memberById.get(typeMemberId);
    }

    /**
     * Returns a stable {@link List list} of {@link BaseType base types}. The set which holds the
     * actual state of the stored {@link BaseType base types} can not be used by index which the GUI
     * requires to work efficiently.
     *
     * @return Stable {@link List list} of {@link BaseType base types}.
     */
    public List<BaseType> getTypes() {
      if (stableTypeList.isEmpty()) {
        stableTypeList.addAll(types);
      }
      return Collections.unmodifiableList(stableTypeList);
    }

    /**
     * Initializes the type system with the data passed as arguments. The function assumes that the
     * data is consistent. This especially is true for cyclic references within the source data.
     * Passing in inconsistent data will result in an exception.
     *
     * @param rawBaseTypes The {@link List list} of {@link RawBaseType raw base types} from which to
     *        create the {@link BaseType base types} for the type system.
     * @param rawMembers The {@link List list} of {@link RawTypeMember raw type members} from which
     *        to create the {@link TypeMember type members} for the type system.
     *
     * @return A {@link TypeDependenceGraph type dependence graph}
     */
    private TypeDependenceGraph initializeTypeSystem(final List<RawBaseType> rawBaseTypes,
        final List<RawTypeMember> rawMembers) {

      // 1) We add all base types without members first since members could reference types that
      // aren't converted to BaseType, yet.
      ImmutableList.Builder<BaseType> baseTypes = ImmutableList.<BaseType>builder();
      for (final RawBaseType rawType : rawBaseTypes) {
        final BaseType newType = new BaseType(rawType.getId(), rawType.getName(), rawType.getSize(),
            rawType.isSigned(), rawType.getCategory());
        types.add(newType);
        typesById.put(newType.getId(), newType);
        typesByName.put(newType.getName(), newType);
        stableTypeList.clear();
        baseTypes.add(newType);
      }

      // 2) Associate all base types with their members.
      ImmutableList.Builder<TypeMember> typeMembers = ImmutableList.<TypeMember>builder();
      for (final RawTypeMember rawMember : rawMembers) {
        final BaseType parentType = typesById.get(rawMember.getParentId());
        TypeMember newMember = null;
        if (rawMember.getOffset().isPresent()) {
          newMember = TypeMember.createStructureMember(rawMember.getId(), parentType,
              typesById.get(rawMember.getBaseTypeId()), rawMember.getName(),
              rawMember.getOffset().get());
        } else if (rawMember.getArgumentIndex().isPresent()) {
          newMember = TypeMember.createFunctionPrototypeMember(rawMember.getId(), parentType,
              typesById.get(rawMember.getBaseTypeId()), rawMember.getName(),
              rawMember.getArgumentIndex().get());
        } else if (rawMember.getNumberOfElements().isPresent()) {
          newMember = TypeMember.createArrayMember(rawMember.getId(), parentType,
              typesById.get(rawMember.getBaseTypeId()), rawMember.getName(),
              rawMember.getNumberOfElements().get());
        } else {
          throw new IllegalStateException(
              "Error: can not associate the raw member to a compound type.");
        }
        typeMembers.add(newMember);
        memberById.put(newMember.getId(), newMember);
        newMember.getParentType().addMember(newMember);
      }

      // 3) Link pointer types, i.e. create pointer hierarchy.
      for (final RawBaseType rawType : rawBaseTypes) {
        final Integer pointerId = rawType.getPointerId();
        if (pointerId != null) {
          final BaseType child = typesById.get(pointerId);
          final BaseType parent = typesById.get(rawType.getId());
          BaseType.appendToPointerHierarchy(child, parent);
        }
      }
      return new TypeDependenceGraph(baseTypes.build(), typeMembers.build());
    }

    public boolean isTypeContainedIn(final BaseType superType, final BaseType baseType) {
      return dependenceGraph.isTypeContainedIn(superType, baseType);
    }

    public ImmutableSet<BaseType> updateBaseType(final BaseType baseType, final String newName,
        final boolean signed, final int size) {
      Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null.");
      Preconditions.checkNotNull(newName, "Error: newName argument can not be null.");
      if (!baseType.getName().equals(newName)) {
        typesByName.remove(baseType.getName());
        typesByName.put(newName, baseType);
        baseType.setName(newName);
      }
      // It is crucial that the size is only updated if this is a non-compound type since sizes for
      // compound types are determined implicitly.
      if (baseType.getCategory() == BaseTypeCategory.ATOMIC
          || baseType.getCategory() == BaseTypeCategory.POINTER && baseType.getBitSize() != size) {
        baseType.setSize(size);
      }
      baseType.setSigned(signed);
      return dependenceGraph.updateType(baseType);
    }

    public ImmutableSet<BaseType> updateTypeMember(final TypeMember typeMember,
        final BaseType newMemberBaseType,
        final String newMemberName,
        final Optional<Integer> newMemberOffset,
        final Optional<Integer> newMemberNumberOfElements,
        final Optional<Integer> newMemberArgumentIndex) {
      Preconditions.checkNotNull(typeMember, "Error: typeMember argument can not be null.");
      Preconditions.checkNotNull(newMemberName, "Error: memberName argument can not be null.");
      Preconditions.checkNotNull(newMemberBaseType, "Error: baseType argument can not be null.");
      Preconditions.checkNotNull(newMemberNumberOfElements,
          "Error: number of elements argument can not be null.");
      Preconditions.checkNotNull(newMemberOffset, "Error: offset argument can not be null.");
      Preconditions.checkNotNull(newMemberArgumentIndex,
          "Error: argument index argument can not be null.");
      Preconditions.checkNotNull(newMemberBaseType, "Error: basetype argument can not be null.");
      typeMember.setBaseType(newMemberBaseType);
      typeMember.setName(newMemberName);
      typeMember.setNumberOfElements(newMemberNumberOfElements);
      typeMember.setOffset(newMemberOffset);
      typeMember.setArgumentIndex(newMemberArgumentIndex);
      return dependenceGraph.updateMember(typeMember.getParentType(), typeMember.getBaseType(),
          newMemberBaseType).getAffectedTypes();
    }

    public void updateTypeSubstitution(final TypeSubstitution typeSubstitution,
        final BaseType baseType, final List<TypeMember> memberPath, final int offset) {
      Preconditions.checkNotNull(typeSubstitution,
          "Error: TypeSubstitution argument can not be null.");
      Preconditions.checkNotNull(baseType, "Error: BaseType argument can not be null.");
      Preconditions.checkNotNull(memberPath, "Error: Member path can not be null.");
      substitutionsByType.remove(typeSubstitution.getBaseType(), typeSubstitution);
      typeSubstitution.setBaseType(baseType);
      typeSubstitution.setOffset(offset);
      typeSubstitution.setMemberPath(memberPath);
      substitutionsByType.put(baseType, typeSubstitution);
    }

    public boolean willTypeCreateCyclicReference(final BaseType containingType,
        final BaseType memberType) {
      return dependenceGraph.willCreateCycle(containingType, memberType);
    }
  }


}
