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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode;

import java.util.List;

import javax.swing.JMenu;

import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.Section;

/**
 * Provides a menu which allows the user to navigate to a list of sections.
 */
public final class GotoSectionMenu extends JMenu {

  /**
   * Creates a new GotoSectionMenu instance.
   *
   * @param sections The list of sections that contain the given address.
   * @param address The virtual address to jump to.
   * @param module The module that contains the given sections.
   */
  public GotoSectionMenu(
      final List<Section> sections, final long address, final INaviModule module) {
    setText("Jump to section");
    for (final Section section : sections) {
      add(new GotoSectionAction(section, address, module));
    }
  }
}
