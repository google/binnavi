/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import java.util.Objects;

public class CUser implements IUser {
  /**
   * Field saves the id of the user. if this is negative the user has not been saved to the database
   * yet.
   */
  private final Integer m_id;

  /**
   * Field saves the user name of the user.
   */
  private final String m_userName;

  public CUser(final Integer userId, final String userName) {
    Preconditions.checkArgument((userId != null) && (userId > 0),
        "Error: userId must be non null and larger then zero.");
    m_id = userId;
    m_userName = Preconditions.checkNotNull(userName, "IE02715: userName argument can not be null");
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    final CUser other = (CUser) object;

    return Objects.equals(this.m_id, other.m_id) && Objects.equals(this.m_userName, other.m_userName);
  }

  @Override
  public int getUserId() {
    return m_id;
  }

  @Override
  public String getUserName() {
    return m_userName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUserId(), getUserName());
  }
}
