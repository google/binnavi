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
package com.google.security.zynamics.binnavi.Database.Interfaces;

/**
 * Listener interface for events that relate to {@link SQLProvider provider} changes
 */
public interface SQLProviderListener {

  /**
   * Method to notify that the {@link SQLProvider provider} is closing.
   *
   * @param provider the {@link SQLProvider provider} that is closed.
   */
  public void providerClosing(final SQLProvider provider);
}
