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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CCommentTest {
  final CComment TEST_COMMENT_1 = new CComment(null, CommonTestObjects.TEST_USER_1, null,
      "TEST COMMENT 1");
  final CComment TEST_COMMENT_2 = new CComment(null, CommonTestObjects.TEST_USER_2, TEST_COMMENT_1,
      "TEST COMMENT 2");

  @Test(expected = NullPointerException.class)
  public void testConstructorNullPointerExceptionCommentNull() {
    new CComment(1, CommonTestObjects.TEST_USER_1, TEST_COMMENT_1, null);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullPointerExceptionIDNull() {
    new CComment((Integer) null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullPointerExceptionParentNull() {
    new CComment(1, CommonTestObjects.TEST_USER_1, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullPointerExceptionUserNull() {
    new CComment(1, null, null, null);
  }

  @Test
  public void testEquals() {
    assertTrue(TEST_COMMENT_1.equals(TEST_COMMENT_1));
    assertFalse(TEST_COMMENT_1.equals(null));
    assertFalse(TEST_COMMENT_1.equals(TEST_COMMENT_2));
    assertTrue(TEST_COMMENT_2.equals(new CComment(null, CommonTestObjects.TEST_USER_2,
        TEST_COMMENT_1, "TEST COMMENT 2")));
  }

  @Test
  public void testGetComment() {
    assertEquals("TEST COMMENT 1", TEST_COMMENT_1.getComment());
    assertEquals("TEST COMMENT 2", TEST_COMMENT_2.getComment());
  }

  @Test
  public void testGetParentId() {
    assertEquals(null, TEST_COMMENT_1.getParent());
    assertEquals(TEST_COMMENT_1, TEST_COMMENT_2.getParent());
  }

  @Test
  public void testGetUser() {
    assertEquals(CommonTestObjects.TEST_USER_1, TEST_COMMENT_1.getUser());
  }

  @Test
  public void testHasParent() {
    assertFalse(TEST_COMMENT_1.hasParent());
    assertTrue(TEST_COMMENT_2.hasParent());
  }
}
