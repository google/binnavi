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
package com.google.security.zynamics.binnavi.Gui.Progress;

import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

/**
 * Default implementation for progress operations.
 */
public final class CDefaultProgressOperation implements IProgressOperation {
  /**
   * The description to be shown in the progress dialog.
   */
  private final String m_description;

  /**
   * The progress panel to be shown in the progress dialog.
   */
  private final CProgressPanel m_progressPanel;

  /**
   * Creates a new progress operation object.
   * 
   * @param description The description to be shown in the progress dialog.
   * @param indeterminate True, to show an endless progress dialog. False, to show a stepped
   *        progress dialog.
   * @param showCancelButton True, to show a Cancel button. False, to hide it.
   */
  public CDefaultProgressOperation(final String description, final boolean indeterminate,
      final boolean showCancelButton) {
    m_description = description;

    m_progressPanel = new CProgressPanel("", indeterminate, showCancelButton);

    m_progressPanel.start();

    CGlobalProgressManager.instance().add(this);
  }

  @Override
  public String getDescription() {
    return m_description;
  }

  @Override
  public CProgressPanel getProgressPanel() {
    return m_progressPanel;
  }

  /**
   * Advances the progress panel to the next state.
   */
  public void next() {
    m_progressPanel.next();
  }

  /**
   * Stops the progress operation.
   */
  public void stop() {
    m_progressPanel.stop();

    CGlobalProgressManager.instance().remove(this);
  }
}
