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

import com.google.common.collect.Iterators;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.Users.CUser;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CUserManagerTest {
  private SQLProvider m_sql;

  @Test
  public void addListenerTest() {
    final CUserManager manager = CUserManager.get(m_sql);
    final MockUserManagerListener listener = new MockUserManagerListener();
    manager.addListener(listener);
    assertEquals(1, Iterators.size(manager.getListeners()));
    assertTrue(Iterators.contains(manager.getListeners(), listener));
  }

  @Test
  public void addUserTestCorrectArgument() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    final MockUserManagerListener listener = new MockUserManagerListener();
    manager.addListener(listener);
    manager.addUser(" ADD USER CORRECT ARGUMENTS ");
    assertEquals("addedUser;", listener.m_events.get(0));
  }

  @Test(expected = NullPointerException.class)
  public void addUserTestWrongArgument1() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.addUser(null);
  }

  @Test(expected = IllegalStateException.class)
  public void addUserTestWrongArgument2() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.addUser("ONE");
    manager.addUser("ONE");
  }

  @Test
  public void deleteUserCorrectArgumentTest() throws CouldntSaveDataException,
      CouldntDeleteException {
    final CUserManager manager = CUserManager.get(m_sql);
    final IUser user = manager.addUser("ONE");
    manager.deleteUser(user);
  }

  @Test(expected = NullPointerException.class)
  public void deleteUserWrongArgumentTest1() throws CouldntDeleteException {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.deleteUser(null);
  }

  @Test(expected = IllegalStateException.class)
  public void deleteUserWrongArgumentTest2() throws CouldntDeleteException {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.deleteUser(new CUser(1, "FAKE USER"));
  }

  @Test
  public void editUserCorrectArgumentsTest() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    final IUser user1 = manager.addUser("TWO");
    final IUser user2 = manager.editUserName(user1, "TWO AFTER EDIT");
    assertEquals("TWO", user1.getUserName());
    assertEquals("TWO AFTER EDIT", user2.getUserName());
    assertTrue(user1.getUserId() == user2.getUserId());
  }

  @Test(expected = NullPointerException.class)
  public void editUserWrongArgumentsTest1() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.editUserName(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void editUserWrongArgumentsTest2() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    final IUser user = manager.addUser("TEST USER 12412");
    manager.editUserName(user, null);
  }

  @Test(expected = IllegalStateException.class)
  public void editUserWrongArgumentsTest3() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    final IUser user = new CUser(2123, "TEST USER 12412");
    manager.editUserName(user, "FOOBAR");
  }

  @Test
  public void getCurrentActiveUserTest() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    final IUser activeUser = manager.addUser(" ACTIVE USER ");
    manager.setCurrentActiveUser(activeUser);
    assertEquals(activeUser, manager.getCurrentActiveUser());
  }

  @Test(expected = IllegalStateException.class)
  public void getCurrentActiveUserTestFail() {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.getCurrentActiveUser();
  }

  @Test
  public void getWithCorrectArgumentTest() {
    assertNotNull(CUserManager.get(m_sql));
  }

  @Test(expected = NullPointerException.class)
  public void getWithNullArgumentTest() {
    CUserManager.get(null);
  }

  @Test
  public void removeListenerTest() {
    final CUserManager manager = CUserManager.get(m_sql);
    final MockUserManagerListener listener = new MockUserManagerListener();
    manager.addListener(listener);
    assertTrue(Iterators.contains(manager.getListeners(), listener));
    manager.removeListener(listener);
    assertFalse(Iterators.contains(manager.getListeners(), listener));
  }

  @Test
  public void setCurrentActiveUserCorrectArgumentsTest() throws CouldntSaveDataException {
    final CUserManager manager = CUserManager.get(m_sql);
    final IUser user = manager.addUser(" TEST SET CURRENT ACTIVE USER ");
    manager.setCurrentActiveUser(user);
  }

  @Test(expected = NullPointerException.class)
  public void setCurrentActiveUserWrongArgumentsTest() {
    final CUserManager manager = CUserManager.get(m_sql);
    manager.setCurrentActiveUser(null);
  }

  @Before
  public void setUp() throws IllegalStateException {
    m_sql = new MockSqlProvider();
  }
}
