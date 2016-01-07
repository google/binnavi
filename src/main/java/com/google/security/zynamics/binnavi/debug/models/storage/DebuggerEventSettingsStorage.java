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
package com.google.security.zynamics.binnavi.debug.models.storage;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;

/**
 * Class to store an instance of the CDebuggerEeventSetting class in the database.
 */
public class DebuggerEventSettingsStorage extends DebuggerSetting<DebuggerEventSettings> {
  private static final String LOAD_DLL = "loadDll";
  private static final String UNLOAD_DLL = "unloadDll";

  /**
   * Creates a new instance of the serializer class.
   *
   * @param debugger The currently active debugger instance.
   * @param debugTarget The currently active debugger target.
   */
  public DebuggerEventSettingsStorage(final IDebugger debugger,
      final DebugTargetSettings debugTarget) {
    super(debugger, debugTarget);
  }

  @Override
  public DebuggerEventSettings deserialize() throws CouldntLoadDataException {
    return new DebuggerEventSettings(readBoolSetting(LOAD_DLL), readBoolSetting(UNLOAD_DLL));
  }

  @Override
  public void serialize(final DebuggerEventSettings instance) throws CouldntSaveDataException {
    writeBoolSetting(LOAD_DLL, instance.getBreakOnDllLoad());
    writeBoolSetting(UNLOAD_DLL, instance.getBreakOnDllUnload());
  }
}
