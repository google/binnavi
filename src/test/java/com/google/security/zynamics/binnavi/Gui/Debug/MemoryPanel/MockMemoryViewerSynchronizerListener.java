/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel;


import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.IMemoryViewerSynchronizerListener;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public final class MockMemoryViewerSynchronizerListener implements
    IMemoryViewerSynchronizerListener {
  public String events = "";

  @Override
  public void addressTurnedInvalid(final long currentOffset) {
    events += "addressTurnedInvalid;";
  }

  @Override
  public void requestedUnsectionedAddress(final IAddress address) {
    events += "requestedUnsectionedAddress;";
  }
}
