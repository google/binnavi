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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.commentTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.UniqueTestUserGenerator;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLLocalEdgeCommentTests extends ExpensiveBaseTest {
  private CNaviViewEdge edge;

  @Before
  public void loadEdge() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException, MaybeNullException {
    edge = (CNaviViewEdge) loadFlowGraphEdge(getNotepadModule(), "sub_1002B87");
  }

  @Test(expected = NullPointerException.class)
  public void appendLocalEdgeComment1() throws CouldntSaveDataException {
    getProvider().appendLocalEdgeComment(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void appendLocalEdgeComment2() throws CouldntSaveDataException {
    getProvider().appendLocalEdgeComment(edge, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void appendLocalEdgeComment3() throws CouldntSaveDataException {
    getProvider().appendLocalEdgeComment(edge, " FAIL ", null);
  }

  @Test
  public void appendLocalEdgeComment4() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CPartialLoadException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();
    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String firstCommentString = " PASS LOCAL EDGE COMMENT WITHOUT PARENT ID ";
    final int firstCommentId =
        getProvider().appendLocalEdgeComment(edge, firstCommentString, user.getUserId());
    final IComment firstComment =
        new CComment(firstCommentId, user, lastComment, firstCommentString);

    final String secondCommentString = " PASS LOCAL EDGE COMMENT WITH PARENT ID ";
    final int secondCommentId =
        getProvider().appendLocalEdgeComment(edge, secondCommentString, user.getUserId());
    final IComment secondComment =
        new CComment(secondCommentId, user, firstComment, secondCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(secondCommentId);

    assertNotNull(commentsFromDatabase);
    assertEquals(storedComments.size() + 2, commentsFromDatabase.size());
    assertTrue(commentsFromDatabase.contains(firstComment));
    assertTrue(commentsFromDatabase.contains(secondComment));

    final CNaviViewEdge callgraphEdge = (CNaviViewEdge) loadCallGraphEdge(getKernel32Module());

    getProvider().appendLocalEdgeComment(
        callgraphEdge, " PASS LOCAL CALLGRAPH EDGE COMMENT WITHOUT PARENT ID ", user.getUserId());
  }

  @Test(expected = NullPointerException.class)
  public void deleteLocalEdgeComment1() throws CouldntDeleteException {
    getProvider().deleteLocalEdgeComment(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void deleteLocalEdgeComment2() throws CouldntDeleteException {
    getProvider().deleteLocalEdgeComment(edge, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void deleteLocalEdgeComment3() throws CouldntDeleteException {
    getProvider().deleteLocalEdgeComment(edge, 1, null);
  }

  /**
   * This test checks if the deletion of a single local edge comment works consistently.
   */
  @Test
  public void deleteLocalEdgeComment4()
      throws CouldntSaveDataException, CouldntLoadDataException, CouldntDeleteException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final List<IComment> comments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();

    final IComment lastComment = comments.isEmpty() ? null : Iterables.getLast(comments);
    final String commentText = " COMMENT TO BE DELETED ";

    final Integer commentId =
        getProvider().appendLocalEdgeComment(edge, commentText, user.getUserId());

    final IComment commentToBeDelete = new CComment(commentId, user, lastComment, commentText);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(commentId);

    assertNotNull(commentsFromDatabase);
    assertEquals(comments.size() + 1, commentsFromDatabase.size());
    assertTrue(commentsFromDatabase.contains(commentToBeDelete));

    getProvider().deleteLocalEdgeComment(edge, commentId, user.getUserId());

    final ArrayList<IComment> commentsAfterDelete = getProvider().loadCommentById(commentId);

    assertNotNull(commentsAfterDelete);
    assertTrue(commentsAfterDelete.isEmpty());
  }

  /**
   * This test checks if the delete of a comment in a series of comments works if the comment is the
   * last comment.
   *
   * <pre>
   * Comment 1:      Comment 1:
   * Comment 2:  ->  Comment 2:
   * Comment 3:
   * </pre>
   *
   */
  @Test
  public void deleteLocalEdgeComment5()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();
    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendLocalEdgeComment(edge, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendLocalEdgeComment(edge, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendLocalEdgeComment(edge, comment3String, user.getUserId());
    final IComment comment3 = new CComment(comment3Id, user, comment2, comment3String);

    final ArrayList<IComment> commentsBeforeDelete = getProvider().loadCommentById(comment3Id);

    assertNotNull(commentsBeforeDelete);
    assertEquals(storedComments.size() + 3, commentsBeforeDelete.size());
    assertTrue(commentsBeforeDelete.contains(comment1));
    assertTrue(commentsBeforeDelete.contains(comment2));
    assertTrue(commentsBeforeDelete.contains(comment3));
    assertEquals(comment3, Iterables.getLast(commentsBeforeDelete));
    assertEquals(comment2, commentsBeforeDelete.get(commentsBeforeDelete.size() - 2));
    assertEquals(comment1, commentsBeforeDelete.get(commentsBeforeDelete.size() - 3));

    getProvider().deleteLocalEdgeComment(edge, comment3Id, user.getUserId());

    final ArrayList<IComment> commentsAfterDelete1 = getProvider().loadCommentById(comment3Id);

    assertNotNull(commentsAfterDelete1);
    assertTrue(commentsAfterDelete1.isEmpty());

    final ArrayList<IComment> commentsAfterDelete2 = getProvider().loadCommentById(comment2Id);

    assertNotNull(commentsAfterDelete2);
    assertEquals(storedComments.size() + 2, commentsAfterDelete2.size());
    assertTrue(commentsAfterDelete2.contains(comment2));
    assertTrue(commentsAfterDelete2.contains(comment1));
    assertEquals(comment2, Iterables.getLast(commentsAfterDelete2));
    assertEquals(comment1, commentsAfterDelete2.get(commentsAfterDelete2.size() - 2));
  }


  /**
   * This test checks if the delete of a comment in a series of comments works if the comment is a
   * comment in the middle.
   *
   * <pre>
   * Comment 1:      Comment 1:
   * Comment 2:  ->  
   * Comment 3:      Comment 3:
   * </pre>
   *
   */
  @Test
  public void deleteLocalEdgeComment6()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();
    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendLocalEdgeComment(edge, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendLocalEdgeComment(edge, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendLocalEdgeComment(edge, comment3String, user.getUserId());
    final IComment comment3 = new CComment(comment3Id, user, comment2, comment3String);

    final ArrayList<IComment> commentsBeforeDelete = getProvider().loadCommentById(comment3Id);

    assertNotNull(commentsBeforeDelete);
    assertEquals(storedComments.size() + 3, commentsBeforeDelete.size());
    assertTrue(commentsBeforeDelete.contains(comment1));
    assertTrue(commentsBeforeDelete.contains(comment2));
    assertTrue(commentsBeforeDelete.contains(comment3));
    assertEquals(comment3, Iterables.getLast(commentsBeforeDelete));
    assertEquals(comment2, commentsBeforeDelete.get(commentsBeforeDelete.size() - 2));
    assertEquals(comment1, commentsBeforeDelete.get(commentsBeforeDelete.size() - 3));

    getProvider().deleteLocalEdgeComment(edge, comment2Id, user.getUserId());

    final ArrayList<IComment> commentsAfterDelete1 = getProvider().loadCommentById(comment2Id);

    assertNotNull(commentsAfterDelete1);
    assertTrue(commentsAfterDelete1.isEmpty());

    final ArrayList<IComment> commentsAfterDelete2 = getProvider().loadCommentById(comment3Id);

    assertNotNull(commentsAfterDelete2);
    assertEquals(storedComments.size() + 2, commentsAfterDelete2.size());

    final IComment comment3AfterDelete = new CComment(comment3Id, user, comment1, comment3String);

    assertTrue(commentsAfterDelete2.contains(comment3AfterDelete));
    assertTrue(commentsAfterDelete2.contains(comment1));
    assertEquals(comment3AfterDelete, Iterables.getLast(commentsAfterDelete2));
    assertEquals(comment1, commentsAfterDelete2.get(commentsAfterDelete2.size() - 2));
  }

  /**
   * This test checks if the delete of a comment in a series of comments works if the comment is the
   * first comment.
   *
   * <pre>
   * Comment 1:      
   * Comment 2:  ->  Comment 2:
   * Comment 3:      Comment 3:
   * </pre>
   *
   */
  @Test
  public void deleteLocalEdgeComment7()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();
    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendLocalEdgeComment(edge, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendLocalEdgeComment(edge, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendLocalEdgeComment(edge, comment3String, user.getUserId());
    final IComment comment3 = new CComment(comment3Id, user, comment2, comment3String);

    final ArrayList<IComment> commentsBeforeDelete = getProvider().loadCommentById(comment3Id);

    assertNotNull(commentsBeforeDelete);
    assertEquals(storedComments.size() + 3, commentsBeforeDelete.size());
    assertTrue(commentsBeforeDelete.contains(comment1));
    assertTrue(commentsBeforeDelete.contains(comment2));
    assertTrue(commentsBeforeDelete.contains(comment3));
    assertEquals(comment3, Iterables.getLast(commentsBeforeDelete));
    assertEquals(comment2, commentsBeforeDelete.get(commentsBeforeDelete.size() - 2));
    assertEquals(comment1, commentsBeforeDelete.get(commentsBeforeDelete.size() - 3));

    getProvider().deleteLocalEdgeComment(edge, comment1Id, user.getUserId());

    final ArrayList<IComment> commentsAfterDelete1 = getProvider().loadCommentById(comment1Id);

    assertNotNull(commentsAfterDelete1);
    assertTrue(commentsAfterDelete1.isEmpty());

    final ArrayList<IComment> commentsAfterDelete2 = getProvider().loadCommentById(comment3Id);

    final IComment comment2AfterDelete =
        new CComment(comment2Id, user, lastComment, comment2String);
    final IComment comment3AfterDelete =
        new CComment(comment3Id, user, comment2AfterDelete, comment3String);


    assertNotNull(commentsAfterDelete2);
    assertEquals(storedComments.size() + 2, commentsAfterDelete2.size());
    assertTrue(commentsAfterDelete2.contains(comment3AfterDelete));
    assertTrue(commentsAfterDelete2.contains(comment2AfterDelete));
    assertEquals(comment3AfterDelete, Iterables.getLast(commentsAfterDelete2));
    assertEquals(comment2AfterDelete, commentsAfterDelete2.get(commentsAfterDelete2.size() - 2));
  }

  /**
   * This tests checks that a comment from another user can not be deleted by a different user id.
   */
  @Test(expected = CouldntDeleteException.class)
  public void deleteLocalEdgeComment8()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> comments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();

    final IComment lastComment = comments.isEmpty() ? null : Iterables.getLast(comments);
    final String commentText = " COMMENT TO BE DELETED ";

    final Integer commentId =
        getProvider().appendLocalEdgeComment(edge, commentText, user.getUserId());

    final IComment commentToBeDelete = new CComment(commentId, user, lastComment, commentText);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(commentId);

    assertNotNull(commentsFromDatabase);
    assertEquals(comments.size() + 1, commentsFromDatabase.size());
    assertTrue(commentsFromDatabase.contains(commentToBeDelete));

    final IUser wrongUser = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    assertFalse(wrongUser.equals(user));

    getProvider().deleteLocalEdgeComment(edge, commentId, wrongUser.getUserId());
  }

  @Test(expected = NullPointerException.class)
  public void editLocalEdgeComment1() throws CouldntSaveDataException {
    getProvider().editLocalEdgeComment(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editLocalEdgeComment2() throws CouldntSaveDataException {
    getProvider().editLocalEdgeComment(edge, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editLocalEdgeComment3() throws CouldntSaveDataException {
    getProvider().editLocalEdgeComment(edge, 1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editLocalEdgeComment4() throws CouldntSaveDataException {
    getProvider().editLocalEdgeComment(edge, 1, 1, null);
  }

  @Test
  public void editLocalEdgeComment5() throws CouldntSaveDataException, CouldntLoadDataException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);
    final String beforeEditCommentString = " EDIT LOCAL EDGE COMMENT BEFORE EDIT ";

    final int editedCommentId =
        getProvider().appendLocalEdgeComment(edge, beforeEditCommentString, user.getUserId());
    final IComment commentBeforeEdit =
        new CComment(editedCommentId, user, lastComment, beforeEditCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(editedCommentId);

    assertNotNull(commentsFromDatabase);
    assertTrue(commentsFromDatabase.contains(commentBeforeEdit));
    assertEquals(storedComments.size() + 1, commentsFromDatabase.size());

    final String afterEditCommentString = " EDIT LOCAL EDGE COMMENT AFTER EDIT ";
    getProvider()
        .editLocalEdgeComment(edge, editedCommentId, user.getUserId(), afterEditCommentString);

    final IComment commentAfterEdit =
        new CComment(editedCommentId, user, lastComment, afterEditCommentString);

    final ArrayList<IComment> commentsAfterEdit = getProvider().loadCommentById(editedCommentId);

    assertNotNull(commentsAfterEdit);
    assertTrue(commentsAfterEdit.contains(commentAfterEdit));
  }

  @Test(expected = CouldntSaveDataException.class)
  public void editLocalEdgeComment6() throws CouldntLoadDataException, CouldntSaveDataException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        edge.getLocalComment() == null ? new ArrayList<IComment>() : edge.getLocalComment();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String beforeEditCommentString = " EDIT LOCAL EDGE COMMENT BEFORE EDIT ";

    final int editedCommentId =
        getProvider().appendLocalEdgeComment(edge, beforeEditCommentString, user.getUserId());
    final IComment commentBeforeEdit =
        new CComment(editedCommentId, user, lastComment, beforeEditCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(editedCommentId);

    assertNotNull(commentsFromDatabase);
    assertTrue(commentsFromDatabase.contains(commentBeforeEdit));
    assertEquals(storedComments.size() + 1, commentsFromDatabase.size());

    final IUser wrongUser = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    getProvider().editLocalEdgeComment(edge, editedCommentId, wrongUser.getUserId(), " FAIL ");
  }
}
