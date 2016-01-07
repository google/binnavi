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
package com.google.security.zynamics.binnavi.Database.Exceptions;

import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Exception class used to signal improperly loaded modules.
 */
public final class CPartialLoadException extends Exception {

  /**
   * The unloaded module that must be loaded.
   */
  private final INaviModule m_module;

  /**
   * Creates a new exception object.
   * 
   * @param message Cause of the exception.
   * @param module The unloaded module that must be loaded.
   */
  public CPartialLoadException(final String message, final INaviModule module) {
    super(message);

    m_module = module;
  }

  /**
   * Returns the module to be loaded.
   * 
   * @return The unloaded module that must be loaded.
   */
  public INaviModule getModule() {
    return m_module;
  }
}
