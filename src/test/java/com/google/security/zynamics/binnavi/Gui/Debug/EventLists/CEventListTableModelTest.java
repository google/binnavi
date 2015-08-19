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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CEventListTableModel;
import com.google.security.zynamics.binnavi.debug.models.trace.ModuleTraceProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CEventListTableModelTest {
  /**
   * This test makes sure that the listener issue described in Case 2056 does not happen again.
   * 
   * @throws CouldntLoadDataException
   * @throws LoadCancelledException
   * @throws CouldntSaveDataException
   */
  @Test
  public void test2056() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    final INaviModule module = new MockModule(false);
    module.getContent().getTraceContainer().createTrace("foo", "bar");

    final ITraceListProvider traceListProvider = new ModuleTraceProvider(module);

    final CEventListTableModel model = new CEventListTableModel(traceListProvider);

    module.load();

    model.delete();
  }
}
