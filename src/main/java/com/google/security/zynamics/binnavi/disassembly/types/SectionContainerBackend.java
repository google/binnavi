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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Contains functionality to create or modify section objects and store them in the database.
 */
public class SectionContainerBackend {

  private final SQLProvider provider;
  private final INaviModule module;

  /**
   * Creates a new section container.
   * 
   * @param provider The {@link SQLProvider} used to access the database.
   * @param module The {@link INaviModule} to which this section container is associated.
   */
  public SectionContainerBackend(final SQLProvider provider, final INaviModule module) {
    this.provider =
        Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
    this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
  }

  /**
   * Creates a {@link Section} in the database.
   * 
   * @param name The name of the {@link Section}.
   * @param startAddress The start {@link IAddress} of the {@link Section}.
   * @param endAddress The end {@link IAddress} of the {@link Section}.
   * @param permission The {@link SectionPermission} of the {@link Section}.
   * @param data The data of the section as a {@link Byte}[].
   * 
   * @return The {@link Section} generated in the database.
   * 
   * @throws CouldntSaveDataException if the {@link Section} could not be created in the database.
   */
  protected Section createSection(final String name, final IAddress startAddress,
      final IAddress endAddress, final SectionPermission permission, final byte[] data)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(name, "Error: name argument can not be null");
    Preconditions.checkNotNull(startAddress, "Error: start address argument can not be null");
    Preconditions.checkNotNull(endAddress, "Error: end address argument can not be null");
    Preconditions.checkNotNull(permission, "Error: permission argument can not be null");

    final int sectionId =
        provider.createSection(module.getConfiguration().getId(), name, null,
            startAddress.toBigInteger(), endAddress.toBigInteger(), permission, data);
    return new Section(sectionId, name, CommentManager.get(provider), module, startAddress,
        endAddress, permission, data);
  }

  /**
   * Delete a {@link Section} from the database.
   * 
   * @param section The {@link Section} to be deleted.
   * 
   * @throws CouldntLoadDataException if the {@link Section} could not be deleted from the database.
   */
  protected void deleteSection(final Section section) throws CouldntLoadDataException {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    provider.deleteSection(section);
  }

  /**
   * Loads all sections from the database.
   * 
   * @return The list of all sections.
   * @throws CouldntLoadDataException Thrown if the list of sections could not be determined.
   */
  protected List<Section> loadSections() throws CouldntLoadDataException {

    final Map<Section, Integer> sectionToComment = provider.loadSections(module);

    final Map<Section, Integer> sectionWithCommemnt =
        Maps.filterValues(sectionToComment, new Predicate<Integer>() {
          @Override
          public boolean apply(final Integer commentId) {
            return commentId != null;
          }
        });

    final CommentManager manager = CommentManager.get(provider);
    final HashMap<Integer, ArrayList<IComment>> typeInstanceTocomments =
        provider.loadMultipleCommentsById(sectionWithCommemnt.values());
    for (final Entry<Section, Integer> entry : sectionWithCommemnt.entrySet()) {
      manager
          .initializeSectionComment(entry.getKey(), typeInstanceTocomments.get(entry.getValue()));
    }

    return Lists.newArrayList(sectionToComment.keySet());
  }

  /**
   * Sets a new name for the given section in the database.
   * 
   * @param section The section to be renamed.
   * @param name The new name.
   * @throws CouldntSaveDataException Thrown if the section name could not be written to the
   *         database.
   */
  protected Section renameSection(final Section section, final String name)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    Preconditions.checkNotNull(name, "Error: name argument can not be null");

    provider.setSectionName(module.getConfiguration().getId(), section.getId(), name);
    return new Section(section.getId(), name, CommentManager.get(provider), module,
        section.getStartAddress(), section.getEndAddress(), section.getSectionPermission(),
        section.getData());
  }
}
