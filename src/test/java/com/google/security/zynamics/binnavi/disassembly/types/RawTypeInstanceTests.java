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
package com.google.security.zynamics.binnavi.disassembly.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class contains tests related to {@link RawTypeInstance raw type instances}.
 */
@RunWith(JUnit4.class)
public class RawTypeInstanceTests {
  private static final int MODULE_ID = 1;
  private static final int ID = 123;
  private static final String NAME = "test_raw_instance";
  private static final Integer COMMENT_ID = 234;
  private static final int TYPE_ID = 456;
  private static final int SECTION_ID = 567;
  private static final long SECTION_OFFSET = 1000;

  @Test
  public void testConstructor() {
    final RawTypeInstance rawInstance =
        new RawTypeInstance(MODULE_ID, ID, NAME, COMMENT_ID, TYPE_ID, SECTION_ID, SECTION_OFFSET);
    assertEquals(MODULE_ID, rawInstance.getModuleId());
    assertEquals(ID, rawInstance.getId());
    assertEquals(NAME, rawInstance.getName());
    assertEquals(COMMENT_ID, rawInstance.getCommentId());
    assertEquals(TYPE_ID, rawInstance.getTypeId());
    assertEquals(SECTION_ID, rawInstance.getSectionId());
    assertEquals(SECTION_OFFSET, rawInstance.getSectionOffset());
  }

  @Test(expected = NullPointerException.class)
  public void testIinvalidConstruction0() {
    new RawTypeInstance(MODULE_ID, ID, null, COMMENT_ID, TYPE_ID, SECTION_ID, SECTION_OFFSET);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIinvalidConstruction1() {
    new RawTypeInstance(MODULE_ID, ID, NAME, COMMENT_ID, TYPE_ID, SECTION_ID, -100);
  }
}