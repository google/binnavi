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
package com.google.security.zynamics.binnavi.Tagging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CTagTest {
  private CTag m_tag;

  private final MockTagListener m_listener = new MockTagListener();

  @Before
  public void setUp() {
    m_tag = new CTag(1, "Tag Name", "Tag Description", TagType.NODE_TAG, new MockSqlProvider());

    m_tag.addListener(m_listener);
  }

  @Test
  public void test_C_Constructor() {
    try {
      new CTag(-1, "Tag Name", "Tag Description", TagType.NODE_TAG, new MockSqlProvider());
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      new CTag(0, null, "Tag Description", TagType.NODE_TAG, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CTag(0, "Tag Name", null, TagType.NODE_TAG, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CTag(0, "Tag Name", "Tag Description", null, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CTag(0, "Tag Name", "Tag Description", TagType.NODE_TAG, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals(1, m_tag.getId());
    assertEquals("Tag Name", m_tag.getName());
    assertEquals("Tag Description", m_tag.getDescription());
    assertEquals(TagType.NODE_TAG, m_tag.getType());
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    try {
      m_tag.setDescription(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals("Tag Description", m_tag.getDescription());

    m_tag.setDescription("Test Description");

    // Check listener events
    assertEquals("changedDescription=Test Description/", m_listener.eventList);

    // Check module
    assertEquals("Test Description", m_tag.getDescription());

    m_tag.setDescription("Tag Description");

    assertEquals("changedDescription=Test Description/changedDescription=Tag Description/",
        m_listener.eventList);

    m_tag.setDescription("Tag Description");

    assertEquals("changedDescription=Test Description/changedDescription=Tag Description/",
        m_listener.eventList);

    assertEquals("Tag Description", m_tag.getDescription());
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    try {
      m_tag.setName(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals("Tag Name", m_tag.getName());

    m_tag.setName("Test Name");

    // Check listener events
    assertEquals("changedName=Test Name/", m_listener.eventList);

    // Check module
    assertEquals("Test Name", m_tag.getName());

    m_tag.setName("Tag Name");

    assertEquals("changedName=Test Name/changedName=Tag Name/", m_listener.eventList);

    m_tag.setName("Tag Name");

    assertEquals("changedName=Test Name/changedName=Tag Name/", m_listener.eventList);

    assertEquals("Tag Name", m_tag.getName());
  }
}
