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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class SectionContainerBackendTest {
  private SQLProvider provider;
  private INaviModule module;

  @Before
  public void setUp() {
    provider = new MockSqlProvider();
    module = new MockModule(provider);
  }

  @Test(expected = NullPointerException.class)
  public void SectionContainerBackendTestConstructor1() {
    new SectionContainerBackend(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionContainerBackendTestConstructor2() {
    new SectionContainerBackend(provider, null);
  }

  @Test(expected = NullPointerException.class)
  public void SectionContainerBackendTestConstructor3() {
    new SectionContainerBackend(null, module);
  }

  @Test
  public void SectionContainerBackendTestConstructor4() {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest1() throws CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);

    sectionContainerBackend.createSection(null, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest2() throws CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);

    sectionContainerBackend.createSection(".data2", null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest3() throws CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);

    sectionContainerBackend.createSection(".data2", new CAddress("100", 16), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest4() throws CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);

    sectionContainerBackend.createSection(".data2", new CAddress("100", 16),
        new CAddress("200", 16), null, null);
  }

  @Test
  public void createSectionTest5() throws CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);

    final Section section =
        sectionContainerBackend.createSection(".data2", new CAddress("100", 16), new CAddress(
            "200", 16), SectionPermission.READ_WRITE_EXECUTE, null);

    Assert.assertNotNull(section);
    Assert.assertEquals(".data2", section.getName());
    Assert.assertEquals(new CAddress("100", 16), section.getStartAddress());
    Assert.assertEquals(new CAddress("200", 16), section.getEndAddress());
    Assert.assertEquals(SectionPermission.READ_WRITE_EXECUTE, section.getSectionPermission());
    Assert.assertNull(section.getData());

  }

  @Test
  public void createSectionTest6() throws CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);

    final Section section =
        sectionContainerBackend.createSection(".data2", new CAddress("100", 16), new CAddress(
            "200", 16), SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0xFF,
            (byte) 0x00});

    Assert.assertNotNull(section);
    Assert.assertEquals(".data2", section.getName());
    Assert.assertEquals(new CAddress("100", 16), section.getStartAddress());
    Assert.assertEquals(new CAddress("200", 16), section.getEndAddress());
    Assert.assertEquals(SectionPermission.READ_WRITE_EXECUTE, section.getSectionPermission());
    org.junit.Assert.assertArrayEquals(new byte[] {(byte) 0x90, (byte) 0xFF, (byte) 0x00},
        section.getData());
  }

  @Test
  public void loadSectionsTest1() throws CouldntSaveDataException, CouldntLoadDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    final int numberOfSections = sectionContainerBackend.loadSections().size();

    Assert.assertNotNull(sectionContainerBackend);

    sectionContainerBackend.createSection(".data2", new CAddress("100", 16),
        new CAddress("200", 16), SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90,
            (byte) 0xFF, (byte) 0x00});

    final List<Section> sectionList = sectionContainerBackend.loadSections();
    Assert.assertEquals(numberOfSections + 1, sectionList.size());
    final Section section = Iterables.find(sectionList, new Predicate<Section>() {
      @Override
      public final boolean apply(final Section section) {
        return section.getName().equals(".data2");
      }
    });
    Assert.assertNotNull(section);
    Assert.assertTrue(sectionList.contains(section));
  }

  @Test(expected = NullPointerException.class)
  public void deleteSectionTest1() throws CouldntLoadDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);
    sectionContainerBackend.deleteSection(null);
  }

  @Test
  public void deleteSectionTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);
    final int numberOfSections = sectionContainerBackend.loadSections().size();

    final Section section =
        sectionContainerBackend.createSection(".data2", new CAddress("100", 16), new CAddress(
            "200", 16), SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0xFF,
            (byte) 0x00});

    final int numberOfSections2 = sectionContainerBackend.loadSections().size();
    Assert.assertEquals(numberOfSections + 1, numberOfSections2);

    sectionContainerBackend.deleteSection(section);
    final int numberOfSections3 = sectionContainerBackend.loadSections().size();
    Assert.assertEquals(numberOfSections, numberOfSections3);
  }

  @Test
  public void renameSectionTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainerBackend sectionContainerBackend =
        new SectionContainerBackend(provider, module);

    Assert.assertNotNull(sectionContainerBackend);
    final int numberOfSections = sectionContainerBackend.loadSections().size();

    final Section section =
        sectionContainerBackend.createSection(".data2", new CAddress("100", 16), new CAddress(
            "200", 16), SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0xFF,
            (byte) 0x00});

    final int numberOfSections2 = sectionContainerBackend.loadSections().size();
    Assert.assertEquals(numberOfSections + 1, numberOfSections2);

    sectionContainerBackend.renameSection(section, "NEWNAME");

    final List<Section> sections = sectionContainerBackend.loadSections();
    for (final Section section2 : sections) {
      if (section2.getId() == section.getId()) {
        Assert.assertEquals("NEWNAME", section2.getName());
      }
    }
  }
}
