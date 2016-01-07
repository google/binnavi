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

/**
 * The interface for the BinNavi user management.
 * 
 * User management in BinNavi is strictly for convenience and concurrency simplification and does
 * not provide any security.
 */
public interface IUser {
  /**
   * Function to get the user id for a user.
   * 
   * @return The id of the current user object.
   */
  int getUserId();

  /**
   * Function to get the user name for a user.
   * 
   * @return The name of the current user object.
   */
  String getUserName();
}
