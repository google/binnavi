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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.types.Section;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLSectionCommentTests extends ExpensiveBaseTest {
  private Section section;

  @Before
  public void loadSection() throws CouldntLoadDataException, LoadCancelledException {
    section = loadSection(getNotepadModule());
  }

  @Test(expected = NullPointerException.class)
  public void appendSectionComment1() throws CouldntSaveDataException {
    getProvider().appendSectionComment(1, 1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void appendSectionComment2() throws CouldntSaveDataException {
    getProvider().appendSectionComment(1, 1, " FAIL ", null);
  }

  @Test(expected = NullPointerException.class)
  public void appendSectionComment3() throws CouldntSaveDataException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    getProvider().appendSectionComment(1, 1, null, user.getUserId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void appendSectionComment4() throws CouldntSaveDataException {
    getProvider().appendSectionComment(-1, 1, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void appendSectionComment5() throws CouldntSaveDataException {
    getProvider().appendSectionComment(1, -1, null, null);
  }

  @Test
  public void appendSectionComment6() throws CouldntSaveDataException, CouldntLoadDataException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final java.util.List<IComment> storedComments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);
    final String firstCommentString = " PASS SECTION COMMENT 1 ";
    final int firstCommentId = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), firstCommentString,
        user.getUserId());
    final IComment firstComment =
        new CComment(firstCommentId, user, lastComment, firstCommentString);

    final String secondCommentString = " PASS SECTION COMMENT 2 ";
    final int secondCommentId = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), secondCommentString,
        user.getUserId());
    final IComment secondComment =
        new CComment(secondCommentId, user, firstComment, secondCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(secondCommentId);

    assertNotNull(commentsFromDatabase);
    assertEquals(storedComments.size() + 2, commentsFromDatabase.size());
    assertTrue(commentsFromDatabase.contains(firstComment));
    assertTrue(commentsFromDatabase.contains(secondComment));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteSectionComment1() throws CouldntDeleteException {
    getProvider().deleteSectionComment(-1, 1, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteSectionComment2() throws CouldntDeleteException {
    getProvider().deleteSectionComment(1, -1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void deleteSectionComment3() throws CouldntDeleteException {
    getProvider().deleteSectionComment(1, 1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void deleteSectionComment4() throws CouldntDeleteException {
    getProvider().deleteSectionComment(1, 1, 1, null);
  }

  @Test
  public void deleteSectionComment5()
      throws CouldntDeleteException, CouldntSaveDataException, CouldntLoadDataException {

    final List<IComment> comments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();

    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);

    final String commentString = " TEST DELETE Section COMMENT ";

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final int commentId = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), commentString,
        user.getUserId());

    final IComment newComment = new CComment(commentId, user, lastComment, commentString);

    final ArrayList<IComment> storedComments = getProvider().loadCommentById(commentId);

    assertNotNull(storedComments);
    assertEquals(comments.size() + 1, storedComments.size());
    assertEquals(newComment, storedComments.get(storedComments.size() - 1));

    getProvider().deleteSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), commentId,
        newComment.getUser().getUserId());

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
  public void deleteSectionComment6()
      throws CouldntSaveDataException, CouldntLoadDataException, CouldntDeleteException {

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();
    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment1String,
        user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment2String,
        user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment3String,
        user.getUserId());
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

    getProvider().deleteSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment3Id,
        user.getUserId());

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
  public void deleteSectionComment7()
      throws CouldntSaveDataException, CouldntLoadDataException, CouldntDeleteException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment1String,
        user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment2String,
        user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment3String,
        user.getUserId());
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

    getProvider().deleteSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment2Id,
        user.getUserId());

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
  public void deleteSectionComment8()
      throws CouldntSaveDataException, CouldntDeleteException, CouldntLoadDataException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String comment1String = " Comment 1: ";
    final int comment1Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment1String,
        user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment2String,
        user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment3String,
        user.getUserId());
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

    getProvider().deleteSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), comment1Id,
        user.getUserId());

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

  @Test(expected = IllegalArgumentException.class)
  public void editSectionComment1() throws CouldntSaveDataException {
    getProvider().editSectionComment(-1, 1, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editSectionComment2() throws CouldntSaveDataException {
    getProvider().editSectionComment(1, 1, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editSectionComment3() throws CouldntSaveDataException {
    getProvider().editSectionComment(1, 1, 1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editSectionComment4() throws CouldntSaveDataException {
    getProvider().editSectionComment(1, 1, 1, 1, null);
  }

  @Test
  public void editSectionComment5() throws CouldntSaveDataException, CouldntLoadDataException {
    final List<IComment> comments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();

    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final String commentText = " SECTION COMMENT TEST BEFORE EDIT ";
    final Integer commentId = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), commentText,
        user.getUserId());
    final IComment newComment = new CComment(commentId, user, lastComment, commentText);
    final ArrayList<IComment> newComments = getProvider().loadCommentById(commentId);

    assertNotNull(newComments);
    assertEquals(comments.size() + 1, newComments.size());
    assertEquals(newComment, newComments.get(newComments.size() - 1));

    final String commentAfterEdit = " Section COMMENT TEST AFTER EDIT ";

    getProvider().editSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), commentId,
        user.getUserId(), commentAfterEdit);

    final ArrayList<IComment> commentsAfterEdit = getProvider().loadCommentById(commentId);

    assertEquals(commentAfterEdit, Iterables.getLast(commentsAfterEdit).getComment());
    assertEquals(commentsAfterEdit.size(), newComments.size());
  }

  @Test(expected = CouldntSaveDataException.class)
  public void editSectionComment6() throws CouldntSaveDataException, CouldntLoadDataException {
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();
    final List<IComment> storedComments =
        section.getComments() == null ? new ArrayList<IComment>() : section.getComments();

    final IComment lastComment =
        storedComments.isEmpty() ? null : Iterables.getLast(storedComments);

    final String beforeEditCommentString = " EDIT COMMENT BEFORE EDIT ";

    final int editedCommentId = getProvider().appendSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), beforeEditCommentString,
        user.getUserId());
    final IComment commentBeforeEdit =
        new CComment(editedCommentId, user, lastComment, beforeEditCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(editedCommentId);

    assertNotNull(commentsFromDatabase);
    assertTrue(commentsFromDatabase.contains(commentBeforeEdit));
    assertEquals(storedComments.size() + 1, commentsFromDatabase.size());

    final IUser wrongUser = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    getProvider().editSectionComment(
        section.getModule().getConfiguration().getId(), section.getId(), editedCommentId,
        wrongUser.getUserId(), " FAIL ");
  }
}
