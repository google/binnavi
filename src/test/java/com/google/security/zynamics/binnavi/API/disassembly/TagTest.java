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
import static org.junit.Assert.assertNull;

import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Tag;
import com.google.security.zynamics.binnavi.API.disassembly.TagType;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TagTest {
  private final ITreeNode<CTag> tagNode = new TreeNode<CTag>(new CTag(1, "Tag Name",
      "Tag Description", com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG, new MockSqlProvider()));

  private final Tag tag = new Tag(tagNode);

  @Test
  public void testConstructor() {

    assertEquals("Tag 'Tag Name'", tag.toString());
    assertEquals("Tag Description", tag.getDescription());
    assertNull(tag.getParent());
    assertEquals(0, tag.getChildren().size());
    assertEquals(TagType.NodeTag, tag.getType());
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException {
    final MockTagListener listener = new MockTagListener();

    tag.addListener(listener);

    tag.setDescription("D1");

    assertEquals("D1", tag.getDescription());
    assertEquals("D1", tagNode.getObject().getDescription());
    assertEquals("changedDescription;", listener.events);

    tagNode.getObject().setDescription("D2");

    assertEquals("D2", tag.getDescription());
    assertEquals("D2", tagNode.getObject().getDescription());
    assertEquals("changedDescription;changedDescription;", listener.events);

    tag.removeListener(listener);
  }

  @Test
  public void testSetName() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException {
    final MockTagListener listener = new MockTagListener();

    tag.addListener(listener);

    tag.setName("N1");

    assertEquals("N1", tag.getName());
    assertEquals("N1", tagNode.getObject().getName());
    assertEquals("changedName;", listener.events);

    tagNode.getObject().setName("N2");

    assertEquals("N2", tag.getName());
    assertEquals("N2", tagNode.getObject().getName());
    assertEquals("changedName;changedName;", listener.events);

    tag.removeListener(listener);
  }
}
