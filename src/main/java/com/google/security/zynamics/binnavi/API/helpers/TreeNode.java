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
package com.google.security.zynamics.binnavi.API.helpers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import java.util.Objects;

// ! Represents a node of a tree.
/**
 * Represents a node of a tree.
 * 
 * @param <ObjectType> Type of the object stored in the node.
 */
public final class TreeNode<ObjectType> {
  /**
   * Parent node of the tree node.
   */
  private TreeNode<ObjectType> m_parent;

  /**
   * Child nodes of the tree node.
   */
  private final List<TreeNode<ObjectType>> m_children = new ArrayList<TreeNode<ObjectType>>();

  /**
   * Object stored in the tree node.
   */
  private final ObjectType m_object;

  // / @cond INTERNAL
  /**
   * Creates a new tree node object.
   * 
   * @param object The object stored in the tree node.
   */
  public TreeNode(final ObjectType object) {
    m_object = Preconditions.checkNotNull(object, "Error: Node argument can not be null");
  }

  /**
   * Links two tree nodes.
   * 
   * @param <ObjectType> Type of the objects stored in the tree nodes.
   * 
   * @param parent Parent node to link.
   * @param child Child node to link.
   */
  public static <ObjectType> void link(final TreeNode<ObjectType> parent,
      final TreeNode<ObjectType> child) {
    parent.m_children.add(child);
    child.m_parent = parent;
  }

  /**
   * Adds a child to the tree node.
   * 
   * @param child The child to add.
   */
  // / @endcond
  public void addChild(final TreeNode<ObjectType> child) {
    m_children.add(child);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    @SuppressWarnings("unchecked")
    final TreeNode<ObjectType> safeNode = (TreeNode<ObjectType>) object;
    return (Objects.equals(this.m_object, safeNode.m_object));
  }

  // ! Child nodes of the tree node.
  /**
   * Returns the children of the tree node.
   * 
   * @return A list of tree nodes.
   */
  public List<TreeNode<ObjectType>> getChildren() {
    return new ArrayList<TreeNode<ObjectType>>(m_children);
  }

  // ! Object stored in the tree node.
  /**
   * Returns the object stored in the tree node.
   * 
   * @return The object stored in the tree node.
   */
  public ObjectType getObject() {
    return m_object;
  }

  // ! Parent node of the tree node.
  /**
   * Returns the parent of the tree node. If the node is the root node of the tree, the return value
   * is null.
   * 
   * @return The parent node of the node or null.
   */
  public TreeNode<ObjectType> getParent() {
    return m_parent;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getChildren(), getObject());
  }

  // / @cond INTERNAL
  /**
   * Removes a child from the tree node.
   * 
   * @param child The child to remove.
   */
  public void removeChild(final TreeNode<ObjectType> child) {
    m_children.remove(child);
  }

  /**
   * Removes a parent from the tree node.
   * 
   * @param parent The parent to remove.
   */
  // / @endcond
  public void setParent(final TreeNode<ObjectType> parent) {
    m_parent = parent;
  }

  // ! Printable representation of the tree node.
  /**
   * Returns a string representation of the tree node.
   * 
   * @return A string representation of the tree node.
   */
  @Override
  public String toString() {
    return m_object.toString();
  }
}
