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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.UniqueTestUserGenerator;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLFunctionCommentTests extends ExpensiveBaseTest {
  private INaviFunction function;

  @Before
  public void loadFunction()
      throws CouldntLoadDataException, MaybeNullException, LoadCancelledException {
    function = loadFunction(getNotepadModule(), "sub_1002B87");
  }

  @Test(expected = NullPointerException.class)
  public void appendFunctionComment1() throws CouldntSaveDataException {
    getProvider().appendFunctionComment((INaviFunction) null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void appendFunctionComment2() throws CouldntSaveDataException {
    getProvider().appendFunctionComment(function, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void appendFunctionComment3() throws CouldntSaveDataException {

    getProvider().appendFunctionComment(function, " FAIL ", null);

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    getProvider().appendFunctionComment(function, " PASS FUNCTION NODE COMMENT ", user.getUserId());
  }

  @Test
  public void appendFunctionComment4()
      throws CouldntLoadDataException, LoadCancelledException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunctions().get(123);

    function.load();

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final java.util.List<IComment> storedComments =
        function.getGlobalComment() == null ? new ArrayList<IComment>()
            : function.getGlobalComment();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String firstCommentString = " PASS FUNCTION NODE COMMENT 1 ";
    final int firstCommentId =
        getProvider().appendFunctionComment(function, firstCommentString, user.getUserId());
    final IComment firstComment =
        new CComment(firstCommentId, user, lastComment, firstCommentString);

    final String secondCommentString = " PASS FUNCTION NODE COMMENT 2 ";
    final int secondCommentId =
        getProvider().appendFunctionComment(function, secondCommentString, user.getUserId());
    final IComment secondComment =
        new CComment(secondCommentId, user, firstComment, secondCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(secondCommentId);

    assertNotNull(commentsFromDatabase);
    assertEquals(storedComments.size() + 2, commentsFromDatabase.size());
    assertTrue(commentsFromDatabase.contains(firstComment));
    assertTrue(commentsFromDatabase.contains(secondComment));

    function.close();
    module.close();
  }

  @Test(expected = NullPointerException.class)
  public void deleteFunctionComment1() throws CouldntDeleteException {
    getProvider().deleteFunctionComment(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void deleteFunctionComment2() throws CouldntDeleteException {

    getProvider().deleteFunctionComment(function, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void deleteFunctionComment3() throws CouldntDeleteException {
    getProvider().deleteFunctionComment(function, 1, null);
  }

  @Test
  public void deleteFunctionComment4()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {

    final List<IComment> comments = function.getGlobalComment() == null ? new ArrayList<IComment>()
        : function.getGlobalComment();

    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);

    final String commentString = " TEST DELETE FUNCTION COMMENT ";

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final int commentId =
        getProvider().appendFunctionComment(function, commentString, user.getUserId());

    final IComment newComment = new CComment(commentId, user, lastComment, commentString);

    final ArrayList<IComment> storedComments = getProvider().loadCommentById(commentId);

    assertNotNull(storedComments);
    assertEquals(comments.size() + 1, storedComments.size());
    assertEquals(newComment, storedComments.get(storedComments.size() - 1));

    getProvider().deleteFunctionComment(function, commentId, newComment.getUser().getUserId());

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
  public void deleteFunctionComment5()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        function.getGlobalComment() == null ? new ArrayList<IComment>()
            : function.getGlobalComment();
    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendFunctionComment(function, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendFunctionComment(function, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendFunctionComment(function, comment3String, user.getUserId());
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

    getProvider().deleteFunctionComment(function, comment3Id, user.getUserId());

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
  public void deleteFunctionComment6()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        function.getGlobalComment() == null ? new ArrayList<IComment>()
            : function.getGlobalComment();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendFunctionComment(function, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendFunctionComment(function, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendFunctionComment(function, comment3String, user.getUserId());
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

    getProvider().deleteFunctionComment(function, comment2Id, user.getUserId());

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
  public void deleteFunctionComment7()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        function.getGlobalComment() == null ? new ArrayList<IComment>()
            : function.getGlobalComment();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendFunctionComment(function, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendFunctionComment(function, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendFunctionComment(function, comment3String, user.getUserId());
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

    getProvider().deleteFunctionComment(function, comment1Id, user.getUserId());

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

  @Test(expected = NullPointerException.class)
  public void editFunctionComment1() throws CouldntSaveDataException {
    getProvider().editFunctionComment(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editFunctionComment2() throws CouldntSaveDataException {
    getProvider().editFunctionComment(function, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editFunctionComment3() throws CouldntSaveDataException {
    getProvider().editFunctionComment(function, 1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editFunctionComment4() throws CouldntSaveDataException {
    getProvider().editFunctionComment(function, 1, 1, null);
  }

  @Test
  public void editFunctionComment5() throws CouldntLoadDataException, CouldntSaveDataException {
    final List<IComment> comments = function.getGlobalComment() == null ? new ArrayList<IComment>()
        : function.getGlobalComment();

    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final String commentText = " FUNCTION COMMENT TEST BEFORE EDIT ";
    final Integer commentId =
        getProvider().appendFunctionComment(function, commentText, user.getUserId());
    final IComment newComment = new CComment(commentId, user, lastComment, commentText);
    final ArrayList<IComment> newComments = getProvider().loadCommentById(commentId);

    assertNotNull(newComments);
    assertEquals(comments.size() + 1, newComments.size());
    assertEquals(newComment, newComments.get(newComments.size() - 1));

    final String commentAfterEdit = " FUNCTION COMMENT TEST AFTER EDIT ";

    getProvider().editFunctionComment(function, commentId, user.getUserId(), commentAfterEdit);

    final ArrayList<IComment> commentsAfterEdit = getProvider().loadCommentById(commentId);

    assertEquals(commentAfterEdit, Iterables.getLast(commentsAfterEdit).getComment());
    assertEquals(commentsAfterEdit.size(), newComments.size());
  }

  @Test(expected = CouldntSaveDataException.class)
  public void editFunctionComment6() throws CouldntLoadDataException, CouldntSaveDataException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        function.getGlobalComment() == null ? new ArrayList<IComment>()
            : function.getGlobalComment();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String beforeEditCommentString = " EDIT COMMENT BEFORE EDIT ";

    final int editedCommentId =
        getProvider().appendFunctionComment(function, beforeEditCommentString, user.getUserId());
    final IComment commentBeforeEdit =
        new CComment(editedCommentId, user, lastComment, beforeEditCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(editedCommentId);

    assertNotNull(commentsFromDatabase);
    assertTrue(commentsFromDatabase.contains(commentBeforeEdit));
    assertEquals(storedComments.size() + 1, commentsFromDatabase.size());

    final IUser wrongUser = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    getProvider().editFunctionComment(function, editedCommentId, wrongUser.getUserId(), " FAIL ");
  }
}
