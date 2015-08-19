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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
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

import java.util.List;

@RunWith(JUnit4.class)
public class SectionContainerTests {
  SQLProvider provider = new MockSqlProvider();
  INaviModule module = new MockModule(provider);
  SectionContainerBackend backend = new SectionContainerBackend(provider, module);

  @Test(expected = NullPointerException.class)
  public void sectionContainerConstructorTest1() throws CouldntLoadDataException {
    new SectionContainer(null);
  }

  @Test
  public void sectionContainerConstructorTest2() throws CouldntLoadDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    Assert.assertEquals(0, sectionContainer.getSections().size());
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    Assert.assertEquals(0, sectionContainer.getSections().size());
    sectionContainer.createSection(null, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    Assert.assertEquals(0, sectionContainer.getSections().size());
    sectionContainer.createSection(".text", null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest3() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    Assert.assertEquals(0, sectionContainer.getSections().size());
    sectionContainer.createSection(".text", new CAddress("100", 16), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createSectionTest4() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    Assert.assertEquals(0, sectionContainer.getSections().size());
    sectionContainer.createSection(
        ".text", new CAddress("100", 16), new CAddress("200", 16), null, null);
  }

  @Test
  public void createSectionTest5() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final int numberOfSections = sectionContainer.getSections().size();
    final Section section = sectionContainer.createSection(
        ".text", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, null);

    Assert.assertEquals(".text", section.getName());
    Assert.assertEquals(new CAddress("100", 16), section.getStartAddress());
    Assert.assertEquals(new CAddress("200", 16), section.getEndAddress());
    Assert.assertEquals(SectionPermission.READ_WRITE_EXECUTE, section.getSectionPermission());
    Assert.assertNull(section.getData());

    Assert.assertEquals(numberOfSections + 1, sectionContainer.getSections().size());
    Assert.assertTrue(sectionContainer.getSections().contains(section));
    Assert.assertEquals(section, sectionContainer.getSection(section.getId()));
  }

  @Test
  public void createSectionTest6() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final int numberOfSections = sectionContainer.getSections().size();
    final Section section = sectionContainer.createSection(
        ".text", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    Assert.assertEquals(".text", section.getName());
    Assert.assertEquals(new CAddress("100", 16), section.getStartAddress());
    Assert.assertEquals(new CAddress("200", 16), section.getEndAddress());
    Assert.assertEquals(SectionPermission.READ_WRITE_EXECUTE, section.getSectionPermission());
    org.junit.Assert.assertArrayEquals(
        new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF}, section.getData());

    Assert.assertEquals(numberOfSections + 1, sectionContainer.getSections().size());
    Assert.assertTrue(sectionContainer.getSections().contains(section));
    Assert.assertEquals(section, sectionContainer.getSection(section.getId()));
  }

  @Test(expected = NullPointerException.class)
  public void deleteSectionTest1() throws CouldntLoadDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    sectionContainer.deleteSection(null);
  }

  @Test
  public void deleteSectionTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final int numberOfSections = sectionContainer.getSections().size();
    final Section section = sectionContainer.createSection(
        ".text", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    Assert.assertEquals(".text", section.getName());
    Assert.assertEquals(new CAddress("100", 16), section.getStartAddress());
    Assert.assertEquals(new CAddress("200", 16), section.getEndAddress());
    Assert.assertEquals(SectionPermission.READ_WRITE_EXECUTE, section.getSectionPermission());
    org.junit.Assert.assertArrayEquals(
        new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF}, section.getData());

    final int numberOfSections2 = sectionContainer.getSections().size();
    Assert.assertEquals(numberOfSections + 1, numberOfSections2);
    Assert.assertTrue(sectionContainer.getSections().contains(section));
    Assert.assertEquals(section, sectionContainer.getSection(section.getId()));

    sectionContainer.deleteSection(section);

    Assert.assertEquals(numberOfSections2 - 1, sectionContainer.getSections().size());
    Assert.assertNull(sectionContainer.getSection(section.getId()));
    Assert.assertFalse(sectionContainer.getSections().contains(section));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteSectionTest3() throws CouldntLoadDataException {
    final Section section = new Section(1,
        "Foo",
        CommentManager.get(provider),
        module,
        new CAddress("100", 100),
        new CAddress("200", 16),
        SectionPermission.EXECUTE,
        null);

    final SectionContainer sectionContainer2 = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer2);
    sectionContainer2.deleteSection(section);
  }

  @Test(expected = NullPointerException.class)
  public void renameSectionTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    sectionContainer.renameSection(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void renameSectionTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final Section section = sectionContainer.createSection(
        ".text", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});
    sectionContainer.renameSection(section, null);
  }

  @Test
  public void renameSectionTest3() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final Section section = sectionContainer.createSection(
        ".text", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    final Section renamedSection = sectionContainer.renameSection(section, ".text2");

    Assert.assertTrue(sectionContainer.getSections().contains(renamedSection));
    Assert.assertFalse(sectionContainer.getSections().contains(section));
    Assert.assertEquals(renamedSection, sectionContainer.getSection(section.getId()));
    Assert.assertEquals(section.getId(), renamedSection.getId());
    Assert.assertEquals(section.getRawSize(), renamedSection.getRawSize());
    Assert.assertEquals(section.getStartAddress(), renamedSection.getStartAddress());
    Assert.assertEquals(section.getEndAddress(), renamedSection.getEndAddress());
    Assert.assertEquals(section.getData(), renamedSection.getData());
  }

  @Test(expected = NullPointerException.class)
  public void findSectionsTest1() throws CouldntLoadDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    sectionContainer.findSections(null);
  }

  @Test
  public void findSectionsTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final int numberOfSections = sectionContainer.getSections().size();
    final List<Section> checkForFirstSearch =
        sectionContainer.findSections(new CAddress("100", 16));
    final List<Section> checkForSecondSearch =
        sectionContainer.findSections(new CAddress("200", 16));
    final int numberOfFoundSectionsFirstSearch = checkForFirstSearch.size();

    final Section section1 = sectionContainer.createSection(
        "SECTION1", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    final Section section2 = sectionContainer.createSection(
        "SECTION2", new CAddress("199", 16), new CAddress("299", 16), SectionPermission.READ,
        new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    final Section section3 = sectionContainer.createSection(
        "SECTION3", new CAddress("300", 16), new CAddress("400", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    final Section section4 = sectionContainer.createSection(
        "SECTION4", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    final Section section5 = sectionContainer.createSection(
        "SECTION5", new CAddress("0", 16), new CAddress("800", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    final List<Section> secondSectionSearch =
        sectionContainer.findSections(new CAddress("100", 16));
    Assert.assertEquals(numberOfSections + 5, sectionContainer.getSections().size());
    Assert.assertEquals(numberOfFoundSectionsFirstSearch + 3, secondSectionSearch.size());
    Assert.assertTrue(secondSectionSearch.contains(section1));
    Assert.assertFalse(secondSectionSearch.contains(section2));
    Assert.assertFalse(secondSectionSearch.contains(section3));
    Assert.assertTrue(secondSectionSearch.contains(section4));
    Assert.assertTrue(secondSectionSearch.contains(section5));

    final List<Section> thirdSectionSearch = sectionContainer.findSections(new CAddress("200", 16));
    Assert.assertEquals(checkForSecondSearch.size() + 4, thirdSectionSearch.size());
    Assert.assertTrue(thirdSectionSearch.contains(section1));
    Assert.assertTrue(thirdSectionSearch.contains(section2));
    Assert.assertFalse(thirdSectionSearch.contains(section3));
    Assert.assertTrue(thirdSectionSearch.contains(section4));
    Assert.assertTrue(thirdSectionSearch.contains(section5));
  }

  @Test
  public void getSectionTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    final SectionContainer sectionContainer = new SectionContainer(backend);
    Assert.assertNotNull(sectionContainer);
    final Section section = sectionContainer.createSection(
        "SECTION1", new CAddress("100", 16), new CAddress("200", 16),
        SectionPermission.READ_WRITE_EXECUTE, new byte[] {(byte) 0x90, (byte) 0x00, (byte) 0xFF});

    Assert.assertEquals(section, sectionContainer.getSection(section.getId()));
  }
}
