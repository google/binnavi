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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.Variables;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences.ViewReferencesTableModel;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.types.TestTypeSystem;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManagerMockBackend;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ViewReferencesTableModelTest {
  TestTypeSystem typeSystem = null;
  INaviView view = null;

  @Before
  public void setup() throws CouldntLoadDataException {
    typeSystem = new TestTypeSystem(new TypeManager(new TypeManagerMockBackend()));
    view = MockView.getFullView(new MockSqlProvider(), ViewType.Native, 123);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor1() {
    new ViewReferencesTableModel(null);
  }

  // TODO(timkornau): Test if the tree is constructed and has all the elements it should have.
}
