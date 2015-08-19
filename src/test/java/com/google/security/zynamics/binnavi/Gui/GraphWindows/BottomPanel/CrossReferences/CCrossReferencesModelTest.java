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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences.CCrossReference;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences.CCrossReferencesModel;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;


@RunWith(JUnit4.class)
public class CCrossReferencesModelTest {
  @Test
  public void test1Simple() {
    final CCrossReferencesModel model = new CCrossReferencesModel();
    final CCrossReference reference = new CCrossReference(new MockFunction(), new MockFunction());
    final List<CCrossReference> crossReferences = new ArrayList<CCrossReference>();

    crossReferences.add(reference);
    model.setCrossReferences(crossReferences);
  }

  @Test
  public void test2GetColumnCount() {
    final CCrossReferencesModel model = new CCrossReferencesModel();
    final CCrossReference reference = new CCrossReference(new MockFunction(), new MockFunction());
    final List<CCrossReference> crossReferences = new ArrayList<CCrossReference>();

    crossReferences.add(reference);
    model.setCrossReferences(crossReferences);

    assertEquals(2, model.getColumnCount());
  }

  @Test
  public void test3getCoulmName() {
    final CCrossReferencesModel model = new CCrossReferencesModel();
    final CCrossReference reference = new CCrossReference(new MockFunction(), new MockFunction());
    final List<CCrossReference> crossReferences = new ArrayList<CCrossReference>();

    crossReferences.add(reference);
    model.setCrossReferences(crossReferences);

    assertEquals("Called Function", model.getColumnName(0));
    assertEquals("Calling Function", model.getColumnName(1));
  }

  @Test
  public void test4getRowCount() {
    final CCrossReferencesModel model = new CCrossReferencesModel();
    final CCrossReference reference = new CCrossReference(new MockFunction(), new MockFunction());
    final List<CCrossReference> crossReferences = new ArrayList<CCrossReference>();

    crossReferences.add(reference);
    model.setCrossReferences(crossReferences);

    assertEquals(1, model.getRowCount());
  }

  @Test
  public void test5ValueAt() {
    final CCrossReferencesModel model = new CCrossReferencesModel();
    final CCrossReference reference = new CCrossReference(new MockFunction(), new MockFunction());
    final List<CCrossReference> crossReferences = new ArrayList<CCrossReference>();

    crossReferences.add(reference);
    model.setCrossReferences(crossReferences);

    assertEquals("Mock Function", model.getValueAt(0, 0));
    assertEquals("Mock Function", model.getValueAt(0, 1));

  }
}
