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
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.UniqueTestUserGenerator;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Loader.CGraphBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLTextNodeCommentTests extends ExpensiveBaseTest {
  private INaviView globalView;

  private INaviTextNode setupTextNode()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(1);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction("SetCommState");
    final INaviView view = module.getContent().getViewContainer().getView(function);
    view.load();

    final INaviCodeNode codeNode = view.getContent().getBasicBlocks().get(5);

    final INaviTextNode textNode = view.getContent().createTextNode(null);
    view.getContent().createEdge(codeNode, textNode, EdgeType.TEXTNODE_EDGE);

    final ZyGraph graph = CGraphBuilder.buildGraph(view);

    globalView = graph.saveAs(
        new CModuleContainer(getDatabase(), module), " TEST TEXT NODE COMMENTS ",
        " TESTING TEXT NODE COMMENTS ");

    INaviTextNode savedTextNode = null;
    for (final INaviViewNode node : globalView.getGraph().getNodes()) {
      if (node instanceof INaviTextNode) {
        savedTextNode = (INaviTextNode) node;
      }
    }

    return savedTextNode;
  }

  @Test(expected = NullPointerException.class)
  public void appendTextNodeComment1()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {
    setupTextNode();
    getProvider().appendTextNodeComment(null, "", 1);
  }

  @Test(expected = NullPointerException.class)
  public void appendTextNodeComment2()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {
    final INaviTextNode textNode = setupTextNode();
    getProvider().appendTextNodeComment(textNode, null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void appendTextNodeComment3()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {
    final INaviTextNode textNode = setupTextNode();
    getProvider().appendTextNodeComment(textNode, "", null);
  }

  @Test
  public void appendTextNodeComment4()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final String firstCommentString = " APPEND GROUP NODE COMMENT WITHOUT PARENT ID ";
    final Integer firstCommentId =
        getProvider().appendTextNodeComment(textNode, firstCommentString, user.getUserId());
    final IComment firstComment = new CComment(firstCommentId, user, null, firstCommentString);

    final String secondCommentString = " APPEND GROUP NODE COMMENT WITH PARENT ID ";
    final Integer secondCommentId =
        getProvider().appendTextNodeComment(textNode, secondCommentString, user.getUserId());
    final IComment secondComment =
        new CComment(secondCommentId, user, firstComment, secondCommentString);

    final ArrayList<IComment> commentsFromDatabase = getProvider().loadCommentById(secondCommentId);

    assertNotNull(commentsFromDatabase);
    assertEquals(2, commentsFromDatabase.size());
    assertTrue(commentsFromDatabase.contains(firstComment));
    assertTrue(commentsFromDatabase.contains(secondComment));

    globalView.close();
    globalView.load();

    INaviTextNode savedTextNode;
    for (final INaviViewNode node : globalView.getGraph().getNodes()) {
      if (node instanceof INaviTextNode) {
        savedTextNode = (INaviTextNode) node;
        assertNotNull(savedTextNode.getComments());
        assertEquals(2, (savedTextNode.getComments().size()));
        assertTrue(savedTextNode.getComments().contains(firstComment));
        assertTrue(savedTextNode.getComments().contains(secondComment));
      }
    }
  }

  @Test(expected = NullPointerException.class)
  public void deleteTextNodeComment1() throws CouldntDeleteException {
    getProvider().deleteTextNodeComment(null, 1, 1);
  }

  @Test(expected = NullPointerException.class)
  public void deleteTextNodeComment2()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    getProvider().deleteTextNodeComment(textNode, null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void deleteTextNodeComment3()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    getProvider().deleteTextNodeComment(textNode, 1, null);
  }

  @Test
  public void deleteTextNodeComment4()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> comments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);

    final String commentString = " TEST DELETE TEXT NODE COMMENT ";

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final int commentId =
        getProvider().appendTextNodeComment(textNode, commentString, user.getUserId());
    final IComment newComment = new CComment(commentId, user, lastComment, commentString);
    final ArrayList<IComment> storedComments = getProvider().loadCommentById(commentId);

    assertNotNull(storedComments);
    assertEquals(comments.size() + 1, storedComments.size());
    assertEquals(newComment, Iterables.getLast(storedComments));

    getProvider().deleteTextNodeComment(textNode, commentId, user.getUserId());

    final ArrayList<IComment> commentsAfterDelete = getProvider().loadCommentById(commentId);

    assertNotNull(commentsAfterDelete);
    assertTrue(commentsAfterDelete.isEmpty());
  }

  @Test(expected = CouldntDeleteException.class)
  public void deleteTextNodeComment5()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> comments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);

    final String commentString = " TEST DELETE TEXT NODE COMMENT WRONG USER ";

    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final int commentId =
        getProvider().appendTextNodeComment(textNode, commentString, user.getUserId());
    final IComment newComment = new CComment(commentId, user, lastComment, commentString);
    final ArrayList<IComment> storedComments = getProvider().loadCommentById(commentId);

    assertNotNull(storedComments);
    assertEquals(comments.size() + 1, storedComments.size());
    assertEquals(newComment, Iterables.getLast(storedComments));

    final IUser wrongUser = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    getProvider().deleteTextNodeComment(textNode, commentId, wrongUser.getUserId());
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
  public void deleteTextNodeComment6()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> storedComments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment =
        storedComments.size() == 0 ? null : Iterables.getLast(storedComments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendTextNodeComment(textNode, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendTextNodeComment(textNode, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendTextNodeComment(textNode, comment3String, user.getUserId());
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

    getProvider().deleteTextNodeComment(textNode, comment3Id, user.getUserId());

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
  public void deleteTextNodeComment7()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> storedComments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment =
        storedComments.size() == 0 ? null : Iterables.getLast(storedComments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendTextNodeComment(textNode, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendTextNodeComment(textNode, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendTextNodeComment(textNode, comment3String, user.getUserId());
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

    getProvider().deleteTextNodeComment(textNode, comment2Id, user.getUserId());

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
  public void deleteTextNodeComment8()
      throws CouldntDeleteException,
      CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> storedComments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment =
        storedComments.size() == 0 ? null : Iterables.getLast(storedComments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final String comment1String = " Comment 1: ";
    final int comment1Id =
        getProvider().appendTextNodeComment(textNode, comment1String, user.getUserId());
    final IComment comment1 = new CComment(comment1Id, user, lastComment, comment1String);

    final String comment2String = " Comment 2: ";
    final int comment2Id =
        getProvider().appendTextNodeComment(textNode, comment2String, user.getUserId());
    final IComment comment2 = new CComment(comment2Id, user, comment1, comment2String);

    final String comment3String = " Comment 3: ";
    final int comment3Id =
        getProvider().appendTextNodeComment(textNode, comment3String, user.getUserId());
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

    getProvider().deleteTextNodeComment(textNode, comment1Id, user.getUserId());

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
  public void editTextNodeComment1() throws CouldntSaveDataException {
    getProvider().editTextNodeComment(null, 1, 1, "");
  }

  @Test(expected = NullPointerException.class)
  public void editTextNodeComment2()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    getProvider().editTextNodeComment(textNode, null, 1, "");
  }

  @Test(expected = NullPointerException.class)
  public void editTextNodeComment3()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    getProvider().editTextNodeComment(textNode, 1, null, "");
  }

  @Test(expected = NullPointerException.class)
  public void editTextNodeComment4()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    getProvider().editTextNodeComment(textNode, 1, 1, null);
  }

  @Test
  public void editTextNodeComment5()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> comments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final String commentText = " TEXT NODE COMMENT TEST BEFORE EDIT ";

    final Integer commentId =
        getProvider().appendTextNodeComment(textNode, commentText, user.getUserId());

    final IComment newComment = new CComment(commentId, user, lastComment, commentText);

    final ArrayList<IComment> newComments = getProvider().loadCommentById(commentId);

    assertNotNull(newComments);
    assertEquals(comments.size() + 1, newComments.size());
    assertEquals(newComment, Iterables.getLast(newComments));

    final String commentAfterEdit = " TEXT NODE COMMENT TEST AFTER EDIT ";

    getProvider().editTextNodeComment(textNode, commentId, user.getUserId(), commentAfterEdit);
    final ArrayList<IComment> commentsAfterEdit =
        PostgreSQLCommentFunctions.loadCommentByCommentId(getProvider(), commentId);

    assertEquals(commentAfterEdit, Iterables.getLast(commentsAfterEdit).getComment());

    assertEquals(commentsAfterEdit.size(), newComments.size());
  }

  @Test(expected = CouldntSaveDataException.class)
  public void editTextNodeComment6()
      throws CouldntLoadDataException,
      LoadCancelledException,
      MaybeNullException,
      CPartialLoadException,
      CouldntSaveDataException {

    final INaviTextNode textNode = setupTextNode();

    final List<IComment> comments =
        textNode.getComments() == null ? new ArrayList<IComment>() : textNode.getComments();
    final IComment lastComment = comments.size() == 0 ? null : Iterables.getLast(comments);
    final IUser user = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    final String commentText = " TEXT NODE COMMENT TEST BEFORE EDIT ";

    final Integer commentId =
        getProvider().appendTextNodeComment(textNode, commentText, user.getUserId());

    final IComment newComment = new CComment(commentId, user, lastComment, commentText);

    final ArrayList<IComment> newComments = getProvider().loadCommentById(commentId);

    assertNotNull(newComments);
    assertEquals(comments.size() + 1, newComments.size());
    assertEquals(newComment, Iterables.getLast(newComments));

    final IUser wrongUser = new UniqueTestUserGenerator(getProvider()).nextActiveUser();

    getProvider().editTextNodeComment(textNode, commentId, wrongUser.getUserId(), " FAIL ");
  }
}
