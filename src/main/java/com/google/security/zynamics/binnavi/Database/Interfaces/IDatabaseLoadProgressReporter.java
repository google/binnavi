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
 * Interface used to show the user information about the database loading progress.
 * 
 * @param <T> Type of the issued events.
 */
public interface IDatabaseLoadProgressReporter<T> {
  /**
   * Default value to signal inactive load processes.
   */
  int INACTIVE = -1;

  /**
   * Invoked before a new load action starts.
   * 
   * @param event The load action that is about to start.
   * 
   * @return True, to continue loading. False, to stop loading.
   */
  boolean report(T event);

  /**
   * Invoked when a load operation starts.
   */
  void start();

  /**
   * Invoked when the load operation stops.
   */
  void stop();
}
