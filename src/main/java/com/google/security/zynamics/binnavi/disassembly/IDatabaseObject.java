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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;

/**
 * This interface must be implemented by all objects which are stored in the database. Its main
 * purpose is to make sure that no connections between objects in different databases are created.
 */
public interface IDatabaseObject {
  /**
   * Checks whether the database object and the given other database object are stored in the same
   * database.
   * 
   * @param rhs The other database object.
   * 
   * @return True, if both objects are stored in the same database. False, otherwise.
   */
  boolean inSameDatabase(IDatabaseObject rhs);

  /**
   * Checks whether the database object and a given SQL provider work on the same database.
   * 
   * @param provider The SQL provider.
   * 
   * @return True, if both objects work on the same database. False, otherwise.
   */
  boolean inSameDatabase(SQLProvider provider);
}
