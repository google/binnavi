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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.List;

import com.google.security.zynamics.binnavi.disassembly.INaviFunction;


// / Interface for objects that contain views.
/**
 * Interface for unifying functions on Project objects and Module objects.
 */
public interface ViewContainer {
  // ! Creates a new view.
  /**
   * Creates a new view that is added to the container.
   *
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The newly created view.
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are null.
   */
  View createView(final String name, final String description);

  // ! Database the view container belongs to.
  /**
   * Returns the database the view container belongs to.
   *
   * @return The database the view container belongs to.
   */
  Database getDatabase();

  // / @cond INTERNAL
  /**
   * Returns the API function object that wraps a given internal function object.
   *
   * @param function The internal function object.
   *
   * @return The API function object.
   */
  // / @endcond
  Function getFunction(INaviFunction function);

  // ! All functions of the view container.
  /**
   * Returns all functions that belong to view container.
   *
   * @return A list of functions.
   */
  List<Function> getFunctions();
}
