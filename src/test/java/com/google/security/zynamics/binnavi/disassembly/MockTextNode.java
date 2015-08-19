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
package com.google.security.zynamics.binnavi.disassembly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNodeListener;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNodeListener;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNodeListener;

public final class MockTextNode implements INaviTextNode {
  @Override
  public void addChild(final INaviViewNode child) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addIncomingEdge(final INaviEdge edge) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addListener(final INaviTextNodeListener listener) {
  }

  @Override
  public void addListener(final INaviViewNodeListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addListener(final IViewNodeListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addOutgoingEdge(final INaviEdge edge) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addParent(final INaviViewNode parent) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<IComment> appendComment(final String comment) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public MockTextNode cloneNode() {
    return new MockTextNode();
  }

  @Override
  public void close() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void deleteComment(final IComment comment) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IComment editComment(final IComment comment, final String commentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Color getBorderColor() {
    return Color.black;
  }

  @Override
  public List<INaviViewNode> getChildren() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Color getColor() {
    return Color.black;
  }

  @Override
  public ArrayList<IComment> getComments() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public double getHeight() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public int getId() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviEdge> getIncomingEdges() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviEdge> getOutgoingEdges() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviGroupNode getParentGroup() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviViewNode> getParents() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Set<CTag> getTags() {
    return new HashSet<CTag>();
  }

  @Override
  public Iterator<CTag> getTagsIterator() {
    return null;
  }

  @Override
  public double getWidth() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public double getX() {
    return 0;
  }

  @Override
  public double getY() {
    return 0;
  }

  @Override
  public void initializeComment(final List<IComment> comments) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isOwner(final IComment comment) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isStored() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isTagged() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isTagged(final CTag tag) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isVisible() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeChild(final INaviViewNode node) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeIncomingEdge(final INaviEdge edge) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final INaviTextNodeListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final IViewNodeListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeOutgoingEdge(final INaviEdge edge) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeParent(final INaviViewNode node) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeTag(final CTag tag) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setBorderColor(final Color color) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setColor(final Color color) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setHeight(final double height) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setId(final int id) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setParentGroup(final INaviGroupNode node) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setSelected(final boolean value) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setVisible(final boolean value) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setWidth(final double width) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setX(final double xpos) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setY(final double ypos) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void tagNode(final CTag tag) {
    throw new IllegalStateException("Not yet implemented");
  }
}
