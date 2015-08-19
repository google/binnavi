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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SectionTests {
  final SQLProvider provider = new MockSqlProvider();
  final INaviModule module = new MockModule(provider);
  final CommentManager commentManager = CommentManager.get(provider);

  @Test(expected = IllegalArgumentException.class)
  public void SectionConstructorTest1() {
    new Section(-1, null, null, null, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionConstructorTest2() {
    new Section(1, "SECTION1", null, null, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionConstructorTest3() {
    new Section(1, "SECTION1", commentManager, null, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionConstructorTest4() {
    new Section(1, "SECTION1", commentManager, module, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionConstructorTest5() {
    new Section(1, "SECTION1", commentManager, module, new CAddress("100", 16), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionConstructorTest6() {
    new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("100", 16),
        new CAddress("200", 16),
        null,
        null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void SectionConstructorTest7() {
    new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("200", 16),
        new CAddress("100", 16),
        null,
        null);
  }

  @Test
  public void SectionConstructorTest8() {
    final Section section = new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("100", 16),
        new CAddress("200", 16),
        SectionPermission.READ,
        null);

    Assert.assertNotNull(section);
    Assert.assertEquals("SECTION1", section.getName());
    Assert.assertEquals(module, section.getModule());
    Assert.assertEquals(new CAddress("100", 16), section.getStartAddress());
    Assert.assertEquals(new CAddress("200", 16), section.getEndAddress());
    Assert.assertEquals(SectionPermission.READ, section.getSectionPermission());
    Assert.assertNull(section.getData());
  }

  @Test
  public void getRawSizeTest1() {
    final Section section = new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("100", 16),
        new CAddress("200", 16),
        SectionPermission.READ,
        null);
    Assert.assertEquals(0, section.getRawSize());
  }

  @Test
  public void getRawSizeTest2() {
    final Section section = new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("100", 16),
        new CAddress("200", 16),
        SectionPermission.READ,
        new byte[] {(byte) 0x90, (byte) 0xFF, (byte) 0x00});
    Assert.assertEquals(3, section.getRawSize());
  }

  @Test
  public void getVirtualSizeTest1() {
    final Section section = new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("100", 16),
        new CAddress("100", 16),
        SectionPermission.READ,
        null);
    Assert.assertEquals(0, section.getVirtualSize());
  }

  @Test
  public void getVirtualSizeTest2() {
    final Section section = new Section(1,
        "SECTION1",
        commentManager,
        module,
        new CAddress("100", 16),
        new CAddress("200", 16),
        SectionPermission.READ,
        null);
    Assert.assertEquals(256, section.getVirtualSize());
  }
}
