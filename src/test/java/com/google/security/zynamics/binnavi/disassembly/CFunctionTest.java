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
package com.google.security.zynamics.binnavi.disassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CFunctionTest {
  private SQLProvider provider;
  private INaviModule module;
  private INaviView view;
  private IAddress address;

  @Before
  public void setUp() {
    provider = new MockSqlProvider();
    module = new MockModule(provider);
    view = new MockView(provider);
    address = new CAddress(0x12345678L);
  }

  @Test(expected = IllegalStateException.class)
  public void testClose() {
    final CFunction function =
        new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
            FunctionType.NORMAL, "", 1, null, null,null, provider);

    function.close();
  }

  @Test
  public void testConstructor1() {
    final CFunction function =
        new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
            FunctionType.NORMAL, "", 1, null, null, null, provider);

    assertNotNull(function);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor10() {
    new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, -1,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
    fail();
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor11() {
    new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0, null,
        "", 1, null, null, null, provider);
    fail();
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor12() {
    new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null,null, null, null);
    fail();
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor2() {
    new CFunction(null, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor3() {
    new CFunction(module, null, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor4() {
    new CFunction(module, view, null, "Name", "Original Name", "Description", 0, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor5() {
    new CFunction(module, view, address, "Name", null, "Description", 0, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
    fail();

  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor7() {
    new CFunction(module, view, address, "Name", "Original Name", "Description", -1, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor8() {
    new CFunction(module, view, address, "Name", "Original Name", "Description", 0, -1, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor9() {
    new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, -1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
    fail();
  }

  @Test
  public void testLoad() throws CouldntLoadDataException {
    final CFunction function =
        new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
            FunctionType.NORMAL, "", 1, null,null, null, provider);

    function.load();
    assertEquals(true, function.isLoaded());
    function.close();
  }

  @Test
  public void testMiscFunctions() throws CouldntSaveDataException {

    final CFunction function =
        new CFunction(module, view, address, "Name", "Original Name", "Description", 0, 0, 1, 0,
        FunctionType.NORMAL, "", 1, null, null, null, provider);
    final CFunction forwardFunction =
        new CFunction(module, view, address, "Name2", "Original Name2", "Description2", 0, 0, 1, 0,
        FunctionType.IMPORT, "", 1, null, null, null, provider);

    assertEquals(0, function.getIndegree());

    function.getOriginalModulename();

    function.getOriginalName();

    assertEquals(0, function.getOutdegree());

    function.getForwardedFunctionAddress();

    function.getForwardedFunctionModuleId();

    function.toString();

    forwardFunction.setForwardedFunction(function);

  }
}
