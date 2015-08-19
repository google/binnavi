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
package com.google.security.zynamics.binnavi.API.disassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Tag;
import com.google.security.zynamics.binnavi.API.disassembly.TagManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TagManagerTest {
  private final SQLProvider m_provider = new MockSqlProvider();

  private final ITagManager manager = new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(1,
      "root", "", TagType.NODE_TAG, m_provider))), TagType.NODE_TAG, m_provider);

  private final TagManager tagManager = new TagManager(manager);

  @Test
  public void testAddTag() throws CouldntSaveDataException {
    final MockTagManagerListener listener = new MockTagManagerListener();

    tagManager.addListener(listener);

    final Tag firstTag = tagManager.addTag(null, "Hannes");

    assertEquals("Hannes", tagManager.getRootTags().get(0).getName());
    assertEquals("Hannes", manager.getRootTag().getChildren().get(0).getObject().getName());
    assertEquals("addedTag;", listener.events);

    @SuppressWarnings("unused")
    final Tag secondTag = tagManager.addTag(firstTag, "Fork");

    assertEquals("Fork", tagManager.getRootTags().get(0).getChildren().get(0).getName());
    assertEquals("Fork", manager.getRootTag().getChildren().get(0).getChildren().get(0).getObject()
        .getName());
    assertEquals("addedTag;addedTag;", listener.events);

    tagManager.removeListener(listener);
  }

  @Test
  public void testConstructor() {
    assertEquals("Tag Manager", tagManager.toString());
    assertEquals(0, tagManager.getRootTags().size());
  }

  @Test
  public void testDeleteTag() throws CouldntSaveDataException, CouldntDeleteException {
    final MockTagManagerListener listener = new MockTagManagerListener();

    final Tag firstTag = tagManager.insertTag(null, "Hannes");

    final Tag secondTag = tagManager.insertTag(null, "Fork");

    final Tag thirdTag = tagManager.insertTag(secondTag, "FooBar");

    tagManager.addListener(listener);

    tagManager.deleteTag(thirdTag);

    assertEquals(1, tagManager.getRootTags().size());
    assertEquals(secondTag, tagManager.getRootTags().get(0));
    assertEquals("Fork", manager.getRootTag().getChildren().get(0).getObject().getName());
    assertEquals(firstTag, tagManager.getRootTags().get(0).getChildren().get(0));
    assertEquals("Hannes", manager.getRootTag().getChildren().get(0).getChildren().get(0)
        .getObject().getName());
    assertEquals("deletedTag;", listener.events);

    tagManager.deleteTag(firstTag);

    assertEquals(1, tagManager.getRootTags().size());
    assertEquals(secondTag, tagManager.getRootTags().get(0));
    assertEquals("Fork", manager.getRootTag().getChildren().get(0).getObject().getName());
    assertEquals("deletedTag;deletedTag;", listener.events);

    tagManager.deleteTag(secondTag);

    assertEquals(0, tagManager.getRootTags().size());
    assertEquals("deletedTag;deletedTag;deletedTag;", listener.events);

    tagManager.removeListener(listener);
  }

  @Test
  public void testDeleteTagSubtree() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException {
    final MockTagManagerListener listener = new MockTagManagerListener();

    final Tag firstTag = tagManager.insertTag(null, "Hannes");

    final Tag secondTag = tagManager.insertTag(firstTag, "Fork");

    @SuppressWarnings("unused")
    final Tag thirdTag = tagManager.insertTag(secondTag, "FooBar");

    tagManager.addListener(listener);

    manager.deleteTagSubTree(manager.getRootTag().getChildren().get(0));

    assertEquals(0, tagManager.getRootTags().size());
    assertEquals("deletedTagTree;", listener.events);

    tagManager.removeListener(listener);
  }

  @Test
  public void testGetTag() throws com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException {
    final TreeNode<CTag> rootNode =
        new TreeNode<CTag>(new CTag(1, "root", "", TagType.NODE_TAG, m_provider));

    final Tree<CTag> tree = new Tree<CTag>(rootNode);

    final ITagManager manager = new CTagManager(tree, TagType.NODE_TAG, m_provider);

    final ITreeNode<CTag> secondNode = manager.addTag(rootNode, "2nd");
    final ITreeNode<CTag> thirdNode = manager.addTag(rootNode, "3rd");

    final TagManager tagManager = new TagManager(manager);

    assertNotNull(tagManager.getTag(rootNode.getObject()));
    assertNotNull(tagManager.getTag(secondNode.getObject()));
    assertNotNull(tagManager.getTag(thirdNode.getObject()));
  }

  @Test
  public void testInsertTag() throws CouldntSaveDataException {
    final MockTagManagerListener listener = new MockTagManagerListener();

    tagManager.addListener(listener);

    final Tag firstTag = tagManager.insertTag(null, "Hannes");

    assertEquals(1, tagManager.getRootTags().size());
    assertEquals(firstTag, tagManager.getRootTags().get(0));
    assertEquals("Hannes", manager.getRootTag().getChildren().get(0).getObject().getName());
    assertEquals("insertedTag;", listener.events);

    final Tag secondTag = tagManager.insertTag(null, "Fork");

    assertEquals(1, tagManager.getRootTags().size());
    assertEquals(secondTag, tagManager.getRootTags().get(0));
    assertEquals("Fork", manager.getRootTag().getChildren().get(0).getObject().getName());
    assertEquals(firstTag, tagManager.getRootTags().get(0).getChildren().get(0));
    assertEquals("Hannes", manager.getRootTag().getChildren().get(0).getChildren().get(0)
        .getObject().getName());
    assertEquals("insertedTag;insertedTag;", listener.events);

    final Tag thirdTag = tagManager.insertTag(secondTag, "FooBar");

    assertEquals(1, tagManager.getRootTags().size());
    assertEquals(secondTag, tagManager.getRootTags().get(0));
    assertEquals("Fork", manager.getRootTag().getChildren().get(0).getObject().getName());
    assertEquals(thirdTag, tagManager.getRootTags().get(0).getChildren().get(0));
    assertEquals("FooBar", manager.getRootTag().getChildren().get(0).getChildren().get(0)
        .getObject().getName());
    assertEquals(firstTag, tagManager.getRootTags().get(0).getChildren().get(0).getChildren()
        .get(0));
    assertEquals("Hannes", manager.getRootTag().getChildren().get(0).getChildren().get(0)
        .getChildren().get(0).getObject().getName());
    assertEquals("insertedTag;insertedTag;insertedTag;", listener.events);

    tagManager.removeListener(listener);
  }
}
