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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import java.math.BigInteger;
import java.security.SecureRandom;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;


public class UniqueTestUserGenerator {

  private final SecureRandom random;
  private final SQLProvider provider;

  public UniqueTestUserGenerator(final SQLProvider sqlProvider) {
    provider = sqlProvider;
    random = new SecureRandom();
  }

  public IUser nextActiveUser() throws CouldntSaveDataException {
    final CUserManager userManager = CUserManager.get(provider);
    final IUser user = userManager.addUser(new BigInteger(130, random).toString(32));
    userManager.setCurrentActiveUser(user);
    return user;
  }
}
