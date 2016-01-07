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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * Interface that represents nodes in views.
 */
public interface INaviViewNode extends IViewNode<INaviEdge>, IDatabaseObject,
    IGraphNode<INaviViewNode>, Cloneable {
  /**
   * Adds a child node to the node.
   * 
   * @param child The child node to add.
   */
  void addChild(INaviViewNode child);

  /**
   * Adds an incoming edge to the node.
   * 
   * @param edge The incoming edge to add.
   */
  void addIncomingEdge(INaviEdge edge);

  /**
   * Adds a listener object to the node.
   * 
   * @param listener The listener object to add.
   */
  void addListener(final INaviViewNodeListener listener);

  /**
   * Adds an outgoing edge to the node.
   * 
   * @param edge The outgoing edge to add.
   */
  void addOutgoingEdge(INaviEdge edge);

  /**
   * Adds a parent node to the node.
   * 
   * @param parent The parent node.
   */
  void addParent(INaviViewNode parent);

  /**
   * Clones the node.
   * 
   * @return The cloned node.
   */
  INaviViewNode cloneNode();

  /**
   * Closes the node.
   */
  void close();

  @Override
  List<INaviEdge> getIncomingEdges();

  @Override
  List<INaviEdge> getOutgoingEdges();

  @Override
  INaviGroupNode getParentGroup();

  /**
   * Returns a copy of the tags the node is tagged with.
   * 
   * @return The tags the node is tagged with.
   */
  Set<CTag> getTags();

  /**
   * Returns an iterator over the set of tags. This makes more sense than returning a copy of the
   * set in many situations.
   */
  Iterator<CTag> getTagsIterator();


  /**
   * Returns whether the node is tagged at all.
   * 
   * @return True, if the node is tagged. False, otherwise.
   */
  boolean isTagged();

  /**
   * Returns whether the node is tagged with a given tag.
   * 
   * @param tag The tag to check for.
   * 
   * @return True, if the node is tagged with the tag. False, otherwise.
   */
  boolean isTagged(CTag tag);

  /**
   * Removes a child node from the node.
   * 
   * @param node The child node to remove.
   */
  void removeChild(INaviViewNode node);

  /**
   * Removes an incoming edge from the node.
   * 
   * @param edge The incoming edge to remove.
   */
  void removeIncomingEdge(INaviEdge edge);

  /**
   * Removes an outgoing edge from the node.
   * 
   * @param edge The outgoing edge to remove.
   */
  void removeOutgoingEdge(INaviEdge edge);

  /**
   * Removes a parent node from the node.
   * 
   * @param node The parent node from the node.
   */
  void removeParent(INaviViewNode node);

  /**
   * Removes a tag from the node.
   * 
   * @param tag The tag to remove.
   * 
   * @throws CouldntSaveDataException Thrown if the tag could not be removed from the node in the
   *         database.
   */
  void removeTag(CTag tag) throws CouldntSaveDataException;

  /**
   * Changes the parent group of the node.
   * 
   * @param node The new parent group of the node. This argument can be null.
   */
  void setParentGroup(INaviGroupNode node);

  /**
   * Tags the node with a given tag.
   * 
   * @param tag The tag to tag the node with.
   * 
   * @throws CouldntSaveDataException Thrown if the tagging information could not be stored in the
   *         database.
   */
  void tagNode(CTag tag) throws CouldntSaveDataException;
}
