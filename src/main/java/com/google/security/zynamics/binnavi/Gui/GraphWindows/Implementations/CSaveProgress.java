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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

/**
 * Class that is used to signal when a save progress is complete.
 */
public final class CSaveProgress {
  /**
   * The current progress.
   */
  private boolean m_progress;

  /**
   * Creates a new progress object.
   *
   * @param progress The current progress.
   */
  public CSaveProgress(final boolean progress) {
    m_progress = progress;
  }

  /**
   * Returns whether saving is complete yet.
   *
   * @return True, if saving is complete. False, otherwise.
   */
  public boolean isDone() {
    return m_progress;
  }

  /**
   * Sets saving to complete.
   */
  public void setDone() {
    m_progress = true;
  }
}
