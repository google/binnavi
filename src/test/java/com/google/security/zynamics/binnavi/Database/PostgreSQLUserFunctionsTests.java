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
package com.google.security.zynamics.binnavi.Database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLUserFunctionsTests extends ExpensiveBaseTest {
  @Test
  public void testAddUser() throws CouldntSaveDataException {
    final IUser user = CGenericSQLUserFunctions.addUser(getProvider(), "TEST USER FOR INSERT TEST");

    assertNotNull(user);
    assertEquals("TEST USER FOR INSERT TEST", user.getUserName());
    assertNotNull(user.getUserId());
  }

  @Test
  public void testDeleteUser() throws CouldntSaveDataException, CouldntDeleteException {
    final IUser user = CGenericSQLUserFunctions.addUser(getProvider(), "TEST USER FOR DELETE TEST");

    assertNotNull(user);
    assertEquals("TEST USER FOR DELETE TEST", user.getUserName());
    assertNotNull(user.getUserId());

    CGenericSQLUserFunctions.deleteUser(getProvider(), user);
  }

  @Test
  public void testEditUser() throws CouldntSaveDataException {
    final IUser user =
        CGenericSQLUserFunctions.addUser(getProvider(), "TEST USER FOR EDIT TEST BEFORE EDIT");

    assertNotNull(user);
    assertEquals("TEST USER FOR EDIT TEST BEFORE EDIT", user.getUserName());
    assertNotNull(user.getUserId());

    final IUser newUser = CGenericSQLUserFunctions.editUserName(getProvider(), user,
        "TEST USER FOR EDIT TEST AFTER EDIT");

    assertNotNull(newUser);
    assertEquals(user.getUserId(), newUser.getUserId());
    assertEquals("TEST USER FOR EDIT TEST AFTER EDIT", newUser.getUserName());
  }

  @Test
  public void testLoadUsers() throws CouldntSaveDataException, CouldntLoadDataException {
    final IUser user1 = CGenericSQLUserFunctions.addUser(getProvider(), "1");
    final IUser user2 = CGenericSQLUserFunctions.addUser(getProvider(), "2");
    final IUser user3 = CGenericSQLUserFunctions.addUser(getProvider(), "3");
    final IUser user4 = CGenericSQLUserFunctions.addUser(getProvider(), "4");
    final IUser user5 = CGenericSQLUserFunctions.addUser(getProvider(), "5");
    final IUser user6 = CGenericSQLUserFunctions.addUser(getProvider(), "6");
    final IUser user7 = CGenericSQLUserFunctions.addUser(getProvider(), "7");
    final IUser user8 = CGenericSQLUserFunctions.addUser(getProvider(), "8");
    final IUser user9 = CGenericSQLUserFunctions.addUser(getProvider(), "9");
    final IUser user10 = CGenericSQLUserFunctions.addUser(getProvider(), "10");
    final IUser user11 = CGenericSQLUserFunctions.addUser(getProvider(), "11");
    final IUser user12 = CGenericSQLUserFunctions.addUser(getProvider(), "12");

    final List<IUser> users = CGenericSQLUserFunctions.loadUsers(getProvider());

    assertNotNull(users);
    assertTrue(users.size() >= 12);
    assertEquals(true, users.contains(user1));
    assertEquals(true, users.contains(user2));
    assertEquals(true, users.contains(user3));
    assertEquals(true, users.contains(user4));
    assertEquals(true, users.contains(user5));
    assertEquals(true, users.contains(user6));
    assertEquals(true, users.contains(user7));
    assertEquals(true, users.contains(user8));
    assertEquals(true, users.contains(user9));
    assertEquals(true, users.contains(user10));
    assertEquals(true, users.contains(user11));
    assertEquals(true, users.contains(user12));
  }
}
