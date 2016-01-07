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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Encapsulates comments for code nodes.
 */
public final class CCodeNodeComments {
  /**
   * The code node the comments object belongs to.
   */
  private final CCodeNode m_codeNode;

  /**
   * Parent function of the code node.
   */
  private final INaviFunction m_parentFunction;

  /**
   * Used to write changes in comments to the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listener that keeps the code node synchronized with changes in global comments.
   */
  private final CommentListener m_internalCommentListener = new InternalCommentListener();

  /**
   * Listeners that are notified when comments are updated.
   */
  private final ListenerProvider<INaviCodeNodeListener> m_listeners;

  /**
   * Creates a new code node comments object.
   * 
   * @param codeNode The code node the comments object belongs to.
   * @param parentFunction Parent function of the code node.
   * @param localComments Local comment of the code node.
   * @param listeners Listeners that are notified when comments are updated.
   * @param provider Used to write changes in comments to the database.
   */
  public CCodeNodeComments(final CCodeNode codeNode, final INaviFunction parentFunction,
      final List<IComment> localComments, final ListenerProvider<INaviCodeNodeListener> listeners,
      final SQLProvider provider) {

    m_codeNode = Preconditions.checkNotNull(codeNode, "IE02391: codeNode argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE02392: provider argument can not be null");

    m_parentFunction = parentFunction;
    m_listeners = listeners;

    CommentManager.get(m_provider).addListener(m_internalCommentListener);
    if ((localComments != null) && !localComments.isEmpty()) {
      CommentManager.get(m_provider).initializeLocalCodeNodeComment(m_codeNode, localComments);
    }
  }

  /**
   * Appends a single comment to the list of comments currently associated to this code node.
   * 
   * @param commentText The comment which will be appended to the list of comments from this node.
   * 
   * @throws CouldntSaveDataException Thrown if the global comment could not be saved.
   * @throws CouldntLoadDataException
   */
  public List<IComment> appendGlobalCodeNodeComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    if (m_parentFunction != null) {
      return CommentManager.get(m_provider).appendGlobalCodeNodeComment(m_codeNode, commentText);
    }
    return null;
  }

  /**
   * Appends a new local code node comment to the list of current local code node comments.
   * 
   * @param commentText The text of the appended comment.
   * @return The newly generated comment.
   * 
   * @throws CouldntSaveDataException if the changed could not be stored to the database.
   * @throws CouldntLoadDataException
   */
  public List<IComment> appendLocalCodeNodeComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(m_provider).appendLocalCodeNodeComment(m_codeNode, commentText);
  }


  /**
   * Appends a new local instruction comment to the list of current local instruction comments in
   * the code node.
   * 
   * @param instruction The instruction to which the comment is associated.
   * @param commentText The text of the comment.
   * 
   * @return The newly generated comment.
   * 
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public List<IComment> appendLocalInstructionComment(final INaviInstruction instruction,
      final String commentText) throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(m_provider).appendLocalInstructionComment(instruction, m_codeNode,
        commentText);
  }

  /**
   * Forwarder function to delete a global code node comment. The actual code for this function is
   * in the global comment manager.
   * 
   * @param comment The comment to be deleted.
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public void deleteGlobalCodeNodeComment(final IComment comment) throws CouldntDeleteException {
    if (m_parentFunction != null) {
      CommentManager.get(m_provider).deleteGlobalCodeNodeComment(m_codeNode, comment);
    }
  }

  /**
   * Deletes a local code node comment from the list of local code node comments associated to this
   * code node
   * 
   * @param comment The comment to be deleted.
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public void deleteLocalCodeNodeComment(final IComment comment) throws CouldntDeleteException {
    CommentManager.get(m_provider).deleteLocalCodeNodeComment(m_codeNode, comment);
  }

  /**
   * Deletes a local instruction comment from the code node.
   * 
   * @param instruction The instruction in this code node where to delete the comment.
   * @param comment The comment to be deleted.
   * 
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public void deleteLocalInstructionComment(final INaviInstruction instruction,
      final IComment comment) throws CouldntDeleteException {
    CommentManager.get(m_provider).deleteLocalInstructionComment(instruction, m_codeNode, comment);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    final CommentManager commentManager = CommentManager.get(m_provider);
    commentManager.unloadGlobalCodeNodeComment(m_codeNode, getGlobalCodeNodeComment());
    commentManager.unloadLocalCodeNodeComment(m_codeNode, getLocalCodeNodeComment());
    commentManager.removeListener(m_internalCommentListener);
  }

  /**
   * Forwarder function to edit a global code node function.
   * 
   * @param oldComment The old comment which will be the donor for the edit-
   * @param commentText The new comment text which will be saved.
   * @return The new comment.
   * @throws CouldntSaveDataException if the comment could not be edited in the database.
   */
  public IComment editGlobalCodeNodeComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    if (m_parentFunction != null) {
      return CommentManager.get(m_provider).editGlobalCodeNodeComment(m_codeNode, oldComment,
          commentText);
    }
    return null;
  }

  /**
   * This function edits a local code node comment. It stores the generated new comment in the place
   * where the old comment used to be in regards to the internal storage. It ensures that the user
   * who is currently active is allowed to edit the comment prior to any changes.
   * 
   * @param oldComment The old comment used as information source for the new comment.
   * @param commentText The comment text of the new comment.
   * @return The new comment.
   * @throws CouldntSaveDataException if the changes to the comment could not be stored to the
   *         database.
   */
  public IComment editLocalCodeNodeComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editLocalCodeNodeComment(m_codeNode, oldComment,
        commentText);
  }

  /**
   * This function edits a local instruction comment. It stores the generated new comment in the
   * place where the old comment used to be in regards to the internal storage. It ensures that the
   * user who is currently active is allowed to edit the comment prior to any changes.
   * 
   * @param instruction The instruction to which the comment is associated.
   * @param oldComment The old comment used as information source for the new comment.
   * @param commentText The comment text of the new comment.
   * @return The new comment.
   * @throws CouldntSaveDataException if the changes to the comment could not be stored to the
   *         database.
   */
  public IComment editLocalInstructionComment(final INaviInstruction instruction,
      final IComment oldComment, final String commentText) throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editLocalInstructionComment(m_codeNode, instruction,
        oldComment, commentText);
  }

  /**
   * Returns the global node comment string.
   * 
   * @return The global node comment string.
   */
  public List<IComment> getGlobalCodeNodeComment() {
    if (m_parentFunction != null) {
      return CommentManager.get(m_provider).getGlobalCodeNodeComment(m_codeNode);
    }
    return null;
  }

  /**
   * Returns the local node comment string.
   * 
   * @return The local node comment string.
   */
  public List<IComment> getLocalCodeNodeComment() {
    return CommentManager.get(m_provider).getLocalCodeNodeComment(m_codeNode);
  }

  /**
   * Returns a local instruction comment string.
   * 
   * @param instruction The instruction whose local comment string is returned.
   * 
   * @return The local comment string of the instruction.
   */
  public List<IComment> getLocalInstructionComment(final INaviInstruction instruction) {
    return CommentManager.get(m_provider).getLocalInstructionComment(instruction, m_codeNode);
  }

  /**
   * Initializes the global code node comments for this code node.
   * 
   * @param comments The List of comments to be associated with this code node
   */
  public void initializeGlobalCodeNodeComment(final List<IComment> comments) {
    if (m_parentFunction != null) {
      CommentManager.get(m_provider).initializeGlobalCodeNodeComment(m_codeNode, comments);
    }
  }

  /**
   * Initializes the local code node comments for this code node.
   * 
   * @param comments The List of comments to be associated with this code node
   */
  public void initializeLocalCodeNodeComment(final List<IComment> comments) {
    CommentManager.get(m_provider).initializeLocalCodeNodeComment(m_codeNode, comments);
  }

  /**
   * Initializes the local instruction comment for the given instruction in this code node.
   * 
   * @param instruction The instruction whose comment is initialized.
   * 
   * @param comments The list of comments to be associated to the instruction.
   */
  public void initializeLocalInstructionComment(final INaviInstruction instruction,
      final List<IComment> comments) {
    CommentManager.get(m_provider).initializeLocalInstructionComment(m_codeNode, instruction,
        comments);
  }

  /**
   * Updates the node when global comments associated with the node change.
   */
  private class InternalCommentListener extends CommentListenerAdapter {
    @Override
    public void appendedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.appendedGlobalCodeNodeComment(m_codeNode, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void appendedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.appendedLocalCodeNodeComment(m_codeNode, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void appendedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.appendedLocalInstructionComment(codeNode, instruction, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void deletedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.deletedGlobalCodeNodeComment(m_codeNode, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void deletedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.deletedLocalCodeNodeComment(m_codeNode, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void deletedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.deletedLocalInstructionComment(codeNode, instruction, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedGLobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.editedGlobalCodeNodeComment(m_codeNode, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.editedLocalCodeNodeComment(m_codeNode, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final IComment comment) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.editedLocalInstructionComment(codeNode, instruction, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void initializedGlobalCodeNodeComments(final INaviCodeNode codeNode,
        final List<IComment> comments) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.initializedGlobalCodeNodeComment(codeNode, comments);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void initializedLocalCodeNodeComments(final INaviCodeNode codeNode,
        final List<IComment> comments) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.initializedLocalCodeNodeComment(codeNode, comments);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void initializedLocalInstructionComments(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final List<IComment> comments) {
      if (m_codeNode.equals(codeNode)) {
        for (final INaviCodeNodeListener listener : m_listeners) {
          try {
            listener.initializedLocalInstructionComment(codeNode, instruction, comments);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }
}
