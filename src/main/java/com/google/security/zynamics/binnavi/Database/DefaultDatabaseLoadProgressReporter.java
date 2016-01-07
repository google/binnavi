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
package com.google.security.zynamics.binnavi.Database;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseLoadProgressReporter;

/**
 * Mapper between low level database loading events and higher level database loading events.
 * 
 * @param <T> Type of the reported events.
 */
public abstract class DefaultDatabaseLoadProgressReporter<T> implements
    IDatabaseLoadProgressReporter<T> {
  /**
   * Counts the number of issued events.
   */
  private int m_counter = INACTIVE;

  /**
   * Reports an event.
   * 
   * @param event The event to report.
   * @param counter The number of the event.
   * 
   * @return True, to continue loading. False, to cancel loading.
   */
  protected abstract boolean report(T event, int counter);

  /**
   * Returns the index of the last completed step.
   * 
   * @return The index of the last completed step.
   */
  public int getStep() {
    return m_counter;
  }

  @Override
  public boolean report(final T event) {
    return report(event, m_counter++);
  }

  @Override
  public void start() {
    m_counter = 0;
  }

  @Override
  public void stop() {
    m_counter = INACTIVE;
  }
}
