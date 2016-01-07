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

package com.google.security.zynamics.binnavi.standardplugins.utils;

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class IconNodeRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value,
      final boolean sel, final boolean expanded, final boolean leaf, final int row,
      final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    Icon icon = ((IconNode) value).getIcon();

    if (icon == null) {
      @SuppressWarnings("unchecked")
      final Hashtable<String, Icon> icons =
          (Hashtable<String, Icon>) tree.getClientProperty("JTree.icons");
      final String name = ((IconNode) value).getIconName();
      if ((icons != null) && (name != null)) {
        icon = icons.get(name);
        if (icon != null) {
          setIcon(icon);
        }
      }
    } else {
      setIcon(icon);
    }

    return this;
  }
}
