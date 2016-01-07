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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SectionContainer {

  /**
   * The {@link List} of {@link Section} known to the {@link SectionContainer}.
   */
  private final List<Section> sections;

  /**
   * A {@link Map} for fast lookup of section id to {@link Section}.
   */
  private final Map<Integer, Section> sectionsById = new HashMap<Integer, Section>();

  /**
   * The {@link SectionContainerBackend} used to access the database with.
   */
  private final SectionContainerBackend backend;

  /**
   * Creates a new {@link SectionContainer}.
   * 
   * @param backend The {@link SectionContainerBackend} which provides the database access to the
   *        {@link SectionContainer}.
   * 
   * @throws CouldntLoadDataException if the {@link Section} data could not be loaded from the
   *         database.
   */
  public SectionContainer(final SectionContainerBackend backend) throws CouldntLoadDataException {
    this.backend = Preconditions.checkNotNull(backend, "Error: backend argument can not be null");
    sections = backend.loadSections();
    Collections.sort(sections, new SectionComparator());
    for (final Section section : sections) {
      sectionsById.put(section.getId(), section);
    }
  }

  /**
   * Creates a new section instance and stores it in the database.
   * 
   * @param name The name {@link String} of the {@link Section}.
   * @param startAddress The start {@link IAddress} of the {@link Section}.
   * @param endAddress The end {@link IAddress} of the {@link Section}.
   * @param sectionPermission The {@link SectionPermission} of the {@link Section}.
   * @param data The data of the section.
   * 
   * @return A new {@link Section}.
   * 
   * @throws CouldntSaveDataException if the {@link Section} information could not be stored in the
   *         database.
   */
  public synchronized Section createSection(final String name, final IAddress startAddress,
      final IAddress endAddress, final SectionPermission sectionPermission, final byte[] data)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(name, "Error: name argument can not be null");
    Preconditions.checkNotNull(startAddress, "Error: startAddress argument can not be null");
    Preconditions.checkNotNull(endAddress, "Error: endAddress argument can not be null");
    Preconditions.checkNotNull(sectionPermission,
        "Error: sectionPermission argument can not be null");

    final Section section =
        backend.createSection(name, startAddress, endAddress, sectionPermission, data);
    sections.add(section);
    sectionsById.put(section.getId(), section);
    return section;
  }

  /**
   * Delete a section from the database and the internal {@link SectionContainer} storage.
   * 
   * @param section The {@link Section} to be deleted.
   * 
   * @throws CouldntLoadDataException if the Section could not be deleted from the database.
   */
  public synchronized void deleteSection(final Section section) throws CouldntLoadDataException {

    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    Preconditions.checkArgument(sections.contains(section),
        "Error: section is not known to the section container");

    backend.deleteSection(section);
    sections.remove(section);
    sectionsById.remove(section.getId());
  }

  /**
   * Returns a {@link List list} of {@link Section sections} containing all {@link Section sections}
   * that contain the given {@link IAddress}. The inclusion check is done as:
   * 
   * <pre>
   * [a..b] {x | a <= x <= b}
   * eg. [0x100..0x200] will include 0x100 up to 0x200 but not 0x99 and 0x201.
   * </pre>
   * 
   * @param address The {@link IAddress} that the {@link Section} should contain.
   * @return A {@link List} of {@link Section} elements which contain the {@link IAddress}.
   */
  public synchronized List<Section> findSections(final IAddress address) {
    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    final Predicate<Section> predicate = new Predicate<Section>() {
      @Override
      public boolean apply(final Section section) {
        return (address.toBigInteger().compareTo(section.getStartAddress().toBigInteger()) >= 0 && address
            .toBigInteger().compareTo(section.getEndAddress().toBigInteger()) <= 0);
      }
    };
    return Lists.newArrayList(Collections2.filter(sections, predicate));
  }

  /**
   * Returns the section instance corresponding to the given section id from the database.
   * 
   * @param sectionId The section id from the database.
   * @return The corresponding section instance.
   */
  public synchronized Section getSection(final int sectionId) {
    return sectionsById.get(sectionId);
  }

  /**
   * Returns an (unmodifiable) list of all sections.
   * 
   * @return The list of all sections.
   */
  public synchronized List<Section> getSections() {
    return Collections.unmodifiableList(sections);
  }

  /**
   * Renames a {@link Section} in the database and updates the internal storage with the changed
   * information.
   * 
   * @param section The {@link Section} which is renamed.
   * @param name The new name of the {@link Section}.
   * 
   * @throws CouldntSaveDataException if storing the new name in the database was not successful.
   */
  public synchronized Section renameSection(final Section section, final String name)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    Preconditions.checkArgument(sections.contains(section),
        "Error: section is not known to the section container");
    Preconditions.checkNotNull(name, "Error: name argument can not be null");

    final Section renamedSection = backend.renameSection(section, name);
    sections.set(sections.indexOf(section), renamedSection);
    sectionsById.put(renamedSection.getId(), renamedSection);
    return renamedSection;
  }

  /**
   * Compares sections by name.
   */
  private class SectionComparator implements Comparator<Section> {
    @Override
    public int compare(final Section lhs, final Section rhs) {
      return lhs.getName().compareTo(rhs.getName());
    }
  }
}
