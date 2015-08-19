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
package com.google.security.zynamics.binnavi.Gui.Users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Gui.Users.CUser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CUserTest {
  CUser TESTUSER_1 = new CUser(1, "TEST USER 1");
  CUser TESTUSER_2 = new CUser(2, "TEST USER 2");

  @Test
  public void testCUserConstructor() {
    final CUser user = new CUser(3, "TEST USER 3");
    assertNotNull(user);
    assertEquals(3, user.getUserId());
    assertEquals("TEST USER 3", user.getUserName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCUserConstructorNullPointerExceptionNoUserID() {
    new CUser((Integer) null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCUserConstructorNullPointerExceptionNoUserName() {
    new CUser(1, null);
  }

  @Test
  public void testGetUserId() {
    assertEquals(2, TESTUSER_2.getUserId());
  }

  @Test
  public void testGetUserName() {
    assertEquals("TEST USER 1", TESTUSER_1.getUserName());
  }

  @Test
  public void testObjectEquals() {
    assertTrue(TESTUSER_1.equals(TESTUSER_1));
    assertFalse(TESTUSER_1.equals(TESTUSER_2));
    assertFalse(TESTUSER_1.equals(null));
  }
}
