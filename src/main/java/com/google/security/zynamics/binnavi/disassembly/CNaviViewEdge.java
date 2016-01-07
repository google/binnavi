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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdgeListener;

/**
 * Represents a single edge in a view.
 */
public final class CNaviViewEdge extends CViewEdge<INaviViewNode> implements INaviEdge {
  /**
   * Writes the edge to the database.
   */
  private final SQLProvider m_provider;

  /**
   * Synchronizes the global comment of the edge.
   */
  private final InternalCommentListener m_internalCommentListener = new InternalCommentListener();

  /**
   * Creates a new edge object.
   * 
   * @param id ID of the edge.
   * @param sourceNode Source node of the edge.
   * @param targetNode Target node of the edge.
   * @param type Type of the edge.
   * @param sourceX X-Coordinate of the source end of the edge.
   * @param sourceY Y-Coordinate of the source end of the edge.
   * @param targetX X-Coordinate of the target end of the edge.
   * @param targetY Y-Coordinate of the target end of the edge.
   * @param color Color of the edge.
   * @param selected Selection state of the edge.
   * @param visible Visibility state of the edge.
   * @param localComment Local comment of the edge.
   * @param edgePaths Edge paths of the edge.
   * @param provider Writes the edge to the database.
   */
  public CNaviViewEdge(final int id, final INaviViewNode sourceNode, final INaviViewNode targetNode,
      final EdgeType type, final double sourceX, final double sourceY, final double targetX,
      final double targetY, final Color color, final boolean selected, final boolean visible,
      final ArrayList<IComment> localComment, final List<CBend> edgePaths,
      final SQLProvider provider) {

    super(id, sourceNode, targetNode, type, sourceX, sourceY, targetX, targetY, color, selected,
        visible, edgePaths);

    m_provider = Preconditions.checkNotNull(provider, "IE00204: Provider argument can not be null");

    CNaviViewNode.link(sourceNode, targetNode);

    CommentManager.get(provider).addListener(m_internalCommentListener);
    if ((localComment != null) && !localComment.isEmpty()) {
      CommentManager.get(m_provider).initializeLocalEdgeComment(this, localComment);
    }
  }

  @Override
  public List<IComment> appendGlobalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    if ((getSource() instanceof IAddressNode) && (getSource() instanceof IAddressNode)) {
      return CommentManager.get(m_provider).appendGlobalEdgeComment(this, commentText);
    }
    return null;
  }

  @Override
  public List<IComment> appendLocalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(m_provider).appendLocalEdgeComment(this, commentText);
  }

  @Override
  public void deleteGlobalComment(final IComment comment) throws CouldntDeleteException {
    if ((getSource() instanceof IAddressNode) && (getTarget() instanceof IAddressNode)) {
      CommentManager.get(m_provider).deleteGlobalEdgeComment(this, comment);
    }
  }

  @Override
  public void deleteLocalComment(final IComment comment) throws CouldntDeleteException {
    CommentManager.get(m_provider).deleteLocalEdgeComment(this, comment);
  }

  @Override
  public void dispose() {
    CommentManager.get(m_provider).unloadGlobalEdgeComment(this, getGlobalComment());
    CommentManager.get(m_provider).unloadLocalEdgeComment(this, getLocalComment());
    CommentManager.get(m_provider).removeListener(m_internalCommentListener);
  }

  @Override
  public IComment editGlobalComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    if ((getSource() instanceof IAddressNode) && (getTarget() instanceof IAddressNode)) {
      return CommentManager.get(m_provider).editGlobalEdgeComment(this, oldComment, commentText);
    }
    return null;
  }

  @Override
  public IComment editLocalComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editLocalEdgeComment(this, oldComment, commentText);
  }

  @Override
  public List<IComment> getGlobalComment() {
    if ((getSource() instanceof IAddressNode) && (getTarget() instanceof IAddressNode)) {
      return CommentManager.get(m_provider).getGlobalEdgeComment(this);
    }
    return null;
  }

  @Override
  public List<IComment> getLocalComment() {
    return CommentManager.get(m_provider).getLocalEdgeComment(this);
  }

  @Override
  public void initializeGlobalComment(final List<IComment> comment) {
    if ((getSource() instanceof IAddressNode) && (getTarget() instanceof IAddressNode)) {
      CommentManager.get(m_provider).initializeGlobalEdgeComment(this, comment);
    }
  }

  @Override
  public void initializeLocalComment(final List<IComment> comments) {
    CommentManager.get(m_provider).initializeLocalEdgeComment(this, comments);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return m_provider.equals(provider);
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(m_provider).isOwner(comment);
  }

  @Override
  public boolean isStored() {
    return getId() != -1;
  }

  @Override
  public String toString() {
    return "[" + getId() + "] " + getSource() + " -> " + getTarget();
  }

  /**
   * Synchronizes the global comment of the edge.
   */
  private class InternalCommentListener extends CommentListenerAdapter {

    @Override
    public void appendedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).appendedGlobalEdgeComment(edge, comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void appendedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).appendedLocalEdgeComment(edge, comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void deletedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).deletedGlobalEdgeComment(edge, comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void deletedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).deletedLocalEdgeComment(edge, comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void editedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).editedGlobalEdgeComment(edge, comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void editedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).editedLocalEdgeComment(edge, comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void initializedGlobalEdgeComments(final INaviEdge edge, final List<IComment> comments) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).initializedGlobalEdgeComment(edge, comments);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void initializedLocalEdgeComments(final INaviEdge edge, final List<IComment> comments) {
      if (CNaviViewEdge.this.equals(edge)) {
        for (final IViewEdgeListener listener : m_listeners) {
          if (listener instanceof INaviEdgeCommentListener) {
            try {
              ((INaviEdgeCommentListener) listener).initializedLocalEdgeComment(edge, comments);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }
  }
}
