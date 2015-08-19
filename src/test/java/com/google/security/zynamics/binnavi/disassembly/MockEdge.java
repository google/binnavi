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
import java.util.List;


import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdgeListener;

public final class MockEdge implements INaviEdge {

  @SuppressWarnings("unused")
  private Color m_color;
  private final List<IComment> m_globalEdgeComments = new ArrayList<IComment>();
  private final List<IComment> m_localEdgeComments = new ArrayList<IComment>();
  private SQLProvider m_provider = new MockSqlProvider();
  private Integer m_id = null;

  public MockEdge(final Integer id, final SQLProvider provider) {
    m_provider = provider;
    m_id = id;
  }

  @Override
  public void addBend(final double x, final double y) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addListener(final IViewEdgeListener listener) {
  }

  @Override
  public List<IComment> appendGlobalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(m_provider).appendGlobalEdgeComment(this, commentText);
  }

  @Override
  public List<IComment> appendLocalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(m_provider).appendLocalEdgeComment(this, commentText);
  }

  @Override
  public void clearBends() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void deleteGlobalComment(final IComment comment) {
    m_globalEdgeComments.remove(comment);
  }

  @Override
  public void deleteLocalComment(final IComment comment) {
    m_localEdgeComments.remove(comment);

  }

  @Override
  public void dispose() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IComment editGlobalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editGlobalEdgeComment(this, comment, commentText);
  }

  @Override
  public IComment editLocalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editLocalEdgeComment(this, comment, commentText);
  }

  @Override
  public int getBendCount() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<CBend> getBends() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Color getColor() {
    return Color.MAGENTA;
  }

  @Override
  public List<IComment> getGlobalComment() {
    return CommentManager.get(m_provider).getGlobalEdgeComment(this);
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public List<IComment> getLocalComment() {
    return CommentManager.get(m_provider).getLocalEdgeComment(this);
  }

  @Override
  public INaviViewNode getSource() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviViewNode getTarget() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public EdgeType getType() {
    return EdgeType.JUMP_UNCONDITIONAL;
  }

  @Override
  public double getX1() {
    return 0;
  }

  @Override
  public double getX2() {
    return 0;
  }

  @Override
  public double getY1() {
    return 0;
  }

  @Override
  public double getY2() {
    return 0;
  }

  @Override
  public void initializeGlobalComment(final List<IComment> globalComments) {
    CommentManager.get(m_provider).initializeGlobalEdgeComment(this, globalComments);
  }

  @Override
  public void initializeLocalComment(final List<IComment> localComments) {
    CommentManager.get(m_provider).initializeLocalEdgeComment(this, localComments);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return m_provider.equals(provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return m_provider.equals(provider);
  }

  @Override
  public void insertBend(final int index, final double x, final double y) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(m_provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isStored() {
    return m_id != -1;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public void removeBend(final int index) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final IViewEdgeListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setColor(final Color color) {
    m_color = color;
  }

  @Override
  public void setEdgeType(final EdgeType type) {
  }

  @Override
  public void setId(final int id) {
  }

  @Override
  public void setSelected(final boolean selected) {
  }

  @Override
  public void setVisible(final boolean visible) {
  }

  @Override
  public void setX1(final double x1) {
  }

  @Override
  public void setX2(final double x2) {
  }

  @Override
  public void setY1(final double y1) {
  }

  @Override
  public void setY2(final double y2) {
  }
}
