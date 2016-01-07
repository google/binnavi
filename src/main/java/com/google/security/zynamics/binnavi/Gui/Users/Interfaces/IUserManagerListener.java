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
package com.google.security.zynamics.binnavi.Gui.Users.Interfaces;

public interface IUserManagerListener {
  /**
   * Invoked if a new user has been added to the user manager and has been stored in the database.
   * 
   * @param user The user which has been added.
   */
  void addedUser(final IUser user);

  /**
   * Invoked if a user has been deleted from the user manager and the corresponding record in the
   * database has been deleted.
   * 
   * @param user The user which has been deleted.
   */
  void deletedUser(final IUser user);

  /**
   * Invoked if a user has been edited in the user manager and the corresponding record in the
   * database has been updated.
   * 
   * @param user The user which has been edited.
   */
  void editedUser(final IUser user);
}
