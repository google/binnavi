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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.SectionPermission;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.Map;

@RunWith(JUnit4.class)
public class PostgreSQLSectionFunctionsTests extends ExpensiveBaseTest {
  @Test(expected = IllegalArgumentException.class)
  public void testCreateSection1() throws CouldntSaveDataException {
    getProvider().createSection(-1, null, null, null, null, null, new byte[] {});
  }

  @Test(expected = NullPointerException.class)
  public void testCreateSection2()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    getProvider().createSection(getKernel32Module().getConfiguration().getId(),
        null,
        null,
        null,
        null,
        null,
        new byte[] {});
  }

  @Test(expected = NullPointerException.class)
  public void testCreateSection3()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    getProvider().createSection(getKernel32Module().getConfiguration().getId(),
        " SECTION NAME ",
        null,
        null,
        null,
        null,
        new byte[] {});
  }

  @Test(expected = NullPointerException.class)
  public void testCreateSection4()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    getProvider().createSection(getKernel32Module().getConfiguration().getId(),
        " SECTION NAME ",
        null,
        new BigInteger("10000", 16),
        null,
        null,
        new byte[] {});
  }

  @Test(expected = NullPointerException.class)
  public void testCreateSection5()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    getProvider().createSection(getKernel32Module().getConfiguration().getId(),
        " SECTION NAME ",
        null,
        new BigInteger("10000", 16),
        new BigInteger("20000", 16),
        null,
        new byte[] {});
  }

  @Test
  public void testCreateSection6()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {

    final INaviModule module = getKernel32Module();

    final String sectionName = " SECTION NAME ";
    final BigInteger startAddress = new BigInteger("10000", 16);
    final BigInteger endAddress = new BigInteger("20000", 16);


    final int sectionId = getProvider().createSection(module.getConfiguration().getId(),
        sectionName,
        null,
        startAddress,
        endAddress,
        SectionPermission.READ_WRITE_EXECUTE,
        new byte[] {});

    module.close();
    module.load();

    final Section section = module.getContent().getSections().getSection(sectionId);
    Assert.assertEquals(section.getName(), sectionName);
    Assert.assertEquals(section.getStartAddress().toBigInteger(), startAddress);
    Assert.assertEquals(section.getEndAddress().toBigInteger(), endAddress);
    Assert.assertEquals(section.getSectionPermission(), SectionPermission.READ_WRITE_EXECUTE);

    module.close();
  }

  @Test(expected = NullPointerException.class)
  public void testLoadSections1() throws CouldntLoadDataException {
    getProvider().loadSections(null);
  }

  @Test
  public void testLoadSections2() throws CouldntLoadDataException, LoadCancelledException {
    final Map<Section, Integer> sections = getProvider().loadSections(getKernel32Module());
    Assert.assertNotNull(sections);
    Assert.assertFalse(sections.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSectionName1() throws CouldntSaveDataException {
    getProvider().setSectionName(0, 0, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSectionName2() throws CouldntSaveDataException {
    getProvider().setSectionName(1, -1, null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetSectionName3() throws CouldntSaveDataException {
    getProvider().setSectionName(1, 1, null);
  }

  @Test
  public void testSetSectionName4()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    final String newName = " NEW SECTION NAME ";
    final INaviModule module = getKernel32Module();

    final Map<Section, Integer> sections = getProvider().loadSections(module);
    final Section section = sections.keySet().iterator().next();
    getProvider().setSectionName(module.getConfiguration().getId(), section.getId(), newName);
    module.close();
    module.load();
    final Section section2 = module.getContent().getSections().getSection(section.getId());
    Assert.assertEquals(newName, section2.getName());
  }

  @Test
  public void testDeleteSection1() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getKernel32Module();
    final Map<Section, Integer> sections = getProvider().loadSections(module);
    final int numberOfSections = sections.size();
    final Section section = sections.keySet().iterator().next();

    getProvider().deleteSection(section);
    module.close();
    module.load();
    final Map<Section, Integer> sections2 = getProvider().loadSections(module);
    Assert.assertEquals(numberOfSections - 1, sections2.size());
  }
}
