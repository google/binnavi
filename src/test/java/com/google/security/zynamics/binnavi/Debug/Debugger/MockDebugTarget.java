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
package com.google.security.zynamics.binnavi.Debug.Debugger;

import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MockDebugTarget implements DebugTargetSettings {
  private final Map<String, String> m_storage = new HashMap<String, String>();

  @Override
  public List<INaviView> getViewsWithAddresses(
      final List<UnrelocatedAddress> offset, final boolean all) {
    throw new IllegalStateException("Not implemented");
  }

  @Override
  public String readSetting(final String key) {
    return m_storage.get(key);
  }

  @Override
  public void writeSetting(final String key, final String value) {
    m_storage.put(key, value);
  }
}
