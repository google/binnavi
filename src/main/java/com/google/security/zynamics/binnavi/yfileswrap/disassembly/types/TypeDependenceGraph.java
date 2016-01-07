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
package com.google.security.zynamics.binnavi.yfileswrap.disassembly.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import y.algo.Cycles;
import y.algo.Dfs;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Represents the dependence relations between all known types. This data structure allows us to
 * determine the set of dependent types as well as the set of referencing types.
 *
 * @author jannewger (Jan Newger)
 *
 */
public final class TypeDependenceGraph {

  // This graph contains an edge between type a and b iff type a is part of type b (as a member).
  private final Graph containedRelation = new Graph();

  private final BiMap<BaseType, Node> containedRelationMap = HashBiMap.create();

  public TypeDependenceGraph(final ImmutableCollection<BaseType> types,
      final ImmutableCollection<TypeMember> members) {
    Preconditions.checkNotNull(types, "IE02761: Types argument can not be null.");
    Preconditions.checkNotNull(members, "Error: Members argument can not be null.");
    // Note: the base types must be added prior to adding the members since adding a member does not
    // create type nodes!
    for (final BaseType baseType : types) {
      addType(baseType);
    }
    initializeMembers(members);
  }

  // Creates a new type node or returns an existing type node.
  private static Node createTypeNode(final BaseType baseType, final Graph graph,
      final BiMap<BaseType, Node> map) {
    final Node node = map.get(baseType);
    if (node == null) {
      final Node newNode = graph.createNode();
      map.put(baseType, newNode);
      return newNode;
    } else {
      return node;
    }
  }

  // Deletes all member relations from the graph.
  private ImmutableSet<BaseType> deleteMember(final BaseType containingType,
      final BaseType memberType) {
    final Node containingTypeNode = containedRelationMap.get(containingType);
    final Node memberTypeNode = containedRelationMap.get(memberType);
    final Edge memberEdge = memberTypeNode.getEdgeTo(containingTypeNode);
    containedRelation.removeEdge(memberEdge);
    return determineDependentTypes(containingType);
  }

  /**
   * Adds a member to the dependence graph and returns the set of base types that are affected by
   * the changed compound type. This method assumes that all base type nodes that correspond to the
   * member base type already exist.
   *
   * @return The set of base types that are affected by the new member.
   */
  public DependenceResult addMember(final BaseType parentType, final BaseType memberType) {
    Preconditions.checkNotNull(parentType, "IE02762: Parent type can not be null.");
    Preconditions.checkNotNull(memberType, "IE02763: Member type can not be null.");
    final Node memberTypeNode = Preconditions.checkNotNull(containedRelationMap.get(memberType),
        "Type node for member type must exist prior to adding a member.");
    final Node parentNode = Preconditions.checkNotNull(containedRelationMap.get(parentType),
        "Type node for member parent must exist prior to adding a member");

    if (willCreateCycle(parentType, memberType)) {
      return new DependenceResult(false, ImmutableSet.<BaseType>of());
    }
    containedRelation.createEdge(memberTypeNode, parentNode);
    final TypeSearch search = new TypeSearch(containedRelationMap.inverse());
    search.start(containedRelation, containedRelationMap.get(parentType));
    return new DependenceResult(true, search.getDependentTypes());
  }

  /**
   * Initializes a set of members in the type dependence graph. This function is supposed to be used
   * on database load only. The reason for this class is reduction of database load time. This is
   * due to the normal mechanism of adding a member where the type system ensures consistency. On
   * load time we assume that the data from the database is sane and will bail out otherwise.
   *
   * @param members The {@link ImmutableCollection collection} of {@link TypeMember type members} to
   *        add to the dependence graph.
   */
  private void initializeMembers(final ImmutableCollection<TypeMember> members) {
    Preconditions.checkNotNull(members, "Error: members argument can not be null.");
    for (TypeMember typeMember : members) {
      final Node memberTypeNode = containedRelationMap.get(typeMember.getBaseType());
      final Node parentNode = containedRelationMap.get(typeMember.getParentType());
      containedRelation.createEdge(memberTypeNode, parentNode);
    }

    if (!Cycles.findCycle(containedRelation, true).isEmpty()) {
      throw new IllegalStateException("Error: Dependence graph has cycles after initialization.");
    }
  }


  /**
   * Adds a new type to the dependence graph. Note: the members must be added separately since this
   * method won't create edges, but only type nodes. Only the the given base type itself is inserted
   * into the graph, i.e. the contained types are not added recursively.
   *
   * @param baseType The base type to be added to the graph.
   */
  public void addType(final BaseType baseType) {
    Preconditions.checkNotNull(baseType, "IE02764: Base type can not be null.");
    createTypeNode(baseType, containedRelation, containedRelationMap);
  }

  /**
   * Deletes the node corresponding to the given member from the dependence graph iff no other node
   * in the graph depends on that node. Deletes the edge that correspond to the contained in
   * relation from the graph.
   *
   */
  public ImmutableSet<BaseType> deleteMember(final TypeMember member) {
    Preconditions.checkNotNull(member, "IE02765: Member can not be null.");
    return deleteMember(member.getParentType(), member.getBaseType());
  }

  /**
   * Delete the given base type. If other types still depend on this type, the set of affected types
   * is returned.
   *
   * @param baseType The base type to delete.
   * @return Returns The set of base types that depend on the deleted type.
   */
  public ImmutableSet<BaseType> deleteType(final BaseType baseType) {
    Preconditions.checkNotNull(baseType, "IE02766: Base type can not be null.");
    final Node containedTypeNode = containedRelationMap.get(baseType);
    Preconditions.checkNotNull(containedTypeNode,
        "Unable to delete type: corresponding node not found in the dependence graph.");
    final ImmutableSet<BaseType> affectedTypes = determineDependentTypes(baseType);
    containedRelation.removeNode(containedTypeNode);
    containedRelationMap.remove(baseType);
    return affectedTypes;
  }

  /**
   * Determines the set of base types that are (transitively) contained within baseType. A type A is
   * said to be contained within B iff B has a member of type A, or if there exists a chain of
   * nested types within B leading to at least one type that has a member of type A.
   *
   * @param baseType The type for which to determine the set of dependent types.
   * @return The set of dependent types.
   */
  public ImmutableSet<BaseType> determineDependentTypes(final BaseType baseType) {
    Preconditions.checkNotNull(baseType, "Error: Base type can not be null.");
    final Node typeNode = containedRelationMap.get(baseType);
    final TypeSearch search = new TypeSearch(containedRelationMap.inverse());
    search.start(containedRelation, typeNode);
    return search.getDependentTypes();
  }

  /**
   * Tests whether baseType is (transitively) contained within containingType. Also see
   * {@link TypeDependenceGraph#determineDependentTypes determineDependentTypes}.
   *
   * @param containingType The type that is checked if it contains baseType.
   * @param baseType The type hat is checked if it is contained in containingType.
   * @return True iff baseType is contained in containingType.
   */
  public boolean isTypeContainedIn(final BaseType containingType, final BaseType baseType) {
    Preconditions.checkNotNull(containingType, "Error: Containing type can not be null.");
    Preconditions.checkNotNull(containingType, "Error: Base type can not be null.");
    return determineDependentTypes(baseType).contains(containingType);
  }

  /**
   * Determines the set of base types that are affected by an update to the given member. If the
   * change would result in an illegal state of the type system, the changes are not performed.
   *
   * @return The dependence result of the change.
   */
  public DependenceResult updateMember(final BaseType parentType, final BaseType oldMemberType,
      final BaseType newMemberType) {
    Preconditions.checkNotNull(parentType, "IE02767: Parent type can not be null.");
    Preconditions.checkNotNull(oldMemberType, "IE02768: Old member type can not be null.");
    Preconditions.checkNotNull(newMemberType, "IE02769: New member type can not be null.");
    final ImmutableSet<BaseType> affectedTypes = determineDependentTypes(parentType);
    if (affectedTypes.contains(parentType)) {
      return new DependenceResult(false, affectedTypes);
    } else {
      // TODO: check first if new member is valid and doesn't create a cycle.
      deleteMember(parentType, oldMemberType);
      addMember(parentType, newMemberType);
      return new DependenceResult(true, affectedTypes);
    }
  }

  /**
   * Determines the set of base types that are affected by the changes made to the given base type.
   *
   * @param baseType The base type that was changed.
   * @return The set of base types that are affected by the changes made to the given base type.
   */
  public ImmutableSet<BaseType> updateType(final BaseType baseType) {
    Preconditions.checkNotNull(baseType, "IE02770: Base type can not be null.");
    return determineDependentTypes(baseType);
  }

  /**
   * Tests if adding a member of type memberType to containingType would create a cyclic type
   * declaration.
   *
   * @param containingType The base type that contains memberType.
   * @param memberType The base type of a member contained in containingType.
   * @return Returns whether adding memberType to containingType would create a cyclic type
   *         declaration.
   */
  public boolean willCreateCycle(final BaseType containingType, final BaseType memberType) {
    Preconditions.checkNotNull(containingType, "Error: Containing type can not be null.");
    Preconditions.checkNotNull(memberType, "Error: Member type can not be null.");
    if (containingType == memberType) {
      return true;
    }
    // 0) The pre-condition is that the existing graph is cycle free.
    // 1) Contained types is the set of types that are explicitly and implicitly contained within
    // the structure this member belongs to.
    // 2) The intersection of this set and all types that are contained by parentType must be empty.
    // TODO(jannewger): instead of creating a copy, the implementation for determining dependent
    // types should be parameterizable whether to include the contained type or not.
    final Set<BaseType> containedTypes = Sets.newHashSet(determineDependentTypes(containingType));
    containedTypes.remove(containingType);
    final Node startNode = containedRelationMap.get(memberType);
    final Queue<Edge> edgesToVisit = new LinkedList<Edge>();
    for (final EdgeCursor ec = startNode.inEdges(); ec.ok(); ec.next()) {
      edgesToVisit.add((Edge) ec.current());
    }
    final Set<Node> visitednodes = new HashSet<Node>();
    while (!edgesToVisit.isEmpty()) {
      final Edge currentEdge = edgesToVisit.poll();
      final Node nextNode = currentEdge.source();
      final BaseType baseType = containedRelationMap.inverse().get(nextNode);
      if (containedTypes.contains(baseType)) {
        return true;
      }
      if (!visitednodes.contains(nextNode)) {
        for (final EdgeCursor ec = nextNode.inEdges(); ec.ok(); ec.next()) {
          edgesToVisit.add((Edge) ec.current());
        }
      }
      visitednodes.add(nextNode);
    }
    return false;
  }

  // Collects the set of nodes that depend on a specific node within one connected component.
  private static class TypeSearch extends Dfs {
    private final Builder<BaseType> builder = ImmutableSet.<BaseType>builder();
    private final Map<Node, BaseType> nodesToTypes;

    public TypeSearch(final Map<Node, BaseType> nodesToTypes) {
      this.nodesToTypes = nodesToTypes;
      setLookFurtherMode(false);
      setDirectedMode(true);
    }

    public ImmutableSet<BaseType> getDependentTypes() {
      return builder.build();
    }

    @Override
    public void preVisit(final Node node, final int dfsNumber) {
      builder.add(nodesToTypes.get(node));
    }
  }

  /**
   * Contains information about whether a change to the type system is valid or not. Also contains
   * the set of types that are affected by that change.
   *
   * @author jannewger (Jan Newger)
   *
   */
  public class DependenceResult {

    private final boolean isValid;
    private final ImmutableSet<BaseType> affectedTypes;

    public DependenceResult(final boolean isValid, final ImmutableSet<BaseType> affectedTypes) {
      this.isValid = isValid;
      this.affectedTypes = affectedTypes;
    }

    /**
     * Returns the set of types that are affected by the changes made to the type system.
     *
     * @return The set of types that are affected by the changes.
     */
    public ImmutableSet<BaseType> getAffectedTypes() {
      return affectedTypes;
    }

    /**
     * Returns whether the change leaves the type system in a valid state, e.g., recursive
     * declarations are not allowed.
     *
     * @return True iff the type system is in a valid state after the change.
     */
    public boolean isValid() {
      return isValid;
    }
  }
}
