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
package com.google.security.zynamics.binnavi.Gui.Debug.StatusLabel;

import javax.swing.JLabel;

import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;


/**
 * Updates the label on relevant events in the target process.
 */
public class CProcessListener extends ProcessManagerListenerAdapter {
  /**
   * Label where the debug events are shown.
   */
  private final JLabel m_label;

  /**
   * Creates a new process listener.
   * 
   * @param label Label where the debug events are shown.
   */
  public CProcessListener(final JLabel label) {
    m_label = label;
  }

  private static String createExceptionMessage(final DebuggerException exception) {
    return "An exception occurred in the target process. "
        + (exception.getExceptionName().isEmpty() ? String.format("Exception code: 0x%X",
            exception.getExceptionCode()) : String.format(
            "Exception name: %s, exception code: 0x%X", exception.getExceptionName(),
            exception.getExceptionCode()));
  }

  @Override
  public void addedModule(final MemoryModule module) {
    m_label.setText(String.format("Loaded module %s at base address %s", module.getName(), module
        .getBaseAddress().getAddress().toHexString()));
  }

  @Override
  public void attached() {
    m_label.setText("Successfully attached to the target process");
  }

  @Override
  public void detached() {
    m_label.setText("Detached from the target process");
  }

  @Override
  public void raisedException(final DebuggerException exception) {
    m_label.setText(createExceptionMessage(exception));
  }

  @Override
  public void removedModule(final MemoryModule module) {
    m_label.setText(String.format("Unloaded module %s from base address %s", module.getName(),
        module.getBaseAddress().getAddress().toHexString()));
  }

  @Override
  public void removedNonExistingModule(final MemoryModule module) {
    m_label.setText(String.format("Unloaded unknown module from base address %s", module
        .getBaseAddress().getAddress().toHexString()));
  }
}
