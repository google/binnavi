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

import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;

/**
 * This exception class is used whenever a load event fails because some other part has not yet been
 * loaded. For example, it is possible for a project view load operation to fail because not all
 * necessary modules have been loaded yet.
 */
public final class PartialLoadException extends Exception {

  /**
   * Name of the offending module.
   */
  private final String m_module;

  /**
   * Creates a new exception object.
   *
   * @param exception Cause of the exception
   */
  public PartialLoadException(final CPartialLoadException exception) {
    super(exception);

    m_module = exception.getModule().getConfiguration().getName();
  }

  /**
   * Returns the name of the module that is not yet loaded that led to the exception.
   *
   * @return The name of the offending module.
   */
  public String getModule() {
    return m_module;
  }
}
