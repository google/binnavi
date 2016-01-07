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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Custom component that is used to draw address cells in function tables.
 */
public final class CAddressLabel extends JLabel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3817187213259813776L;

  /**
   * Font that is used to display the names of normal functions.
   */
  private static Font normalFont;

  /**
   * Font that is used to display the names of normal functions, which are currently loaded and
   * displayed in a window.
   */
  private static Font normalBoldFont;

  /**
   * Table where the rendering happens.
   */
  private final JTable table;

  /**
   * View to be rendered.
   */
  private final INaviView view;

  /**
   * The debugger responsible for breakpoints of the rendered function.
   */
  private final IDebugger debugger;

  /**
   * The function to be rendered.
   */
  private final INaviFunction function;

  /**
   * Image shown for stared icons.
   */
  private static Image starImage;

  /**
   * Creates a new address label object.
   * 
   * @param table Table where the rendering happens.
   * @param view View to be rendered.
   * @param debugger The debugger responsible for breakpoints of the rendered function.
   * @param function The function to be rendered.
   * @param backgroundColor The color used for the background of the address label.
   */
  public CAddressLabel(final JTable table, final INaviView view, final IDebugger debugger,
      final INaviFunction function, final Color backgroundColor, final Font font) {
    this.debugger = Preconditions.checkNotNull(debugger, "IE02019: Debugger argument can not be null");
    this.function = Preconditions.checkNotNull(function, "IE02020: Function argument can not be null");
    this.table = Preconditions.checkNotNull(table, "IE02348: Table argument can not be null");
    this.view = Preconditions.checkNotNull(view, "IE02349: View argument can not be null");

    if (starImage == null) {
      try {
        starImage =
            new ImageIcon(CMain.class.getResource("data/star.png").toURI().toURL()).getImage();
      } catch (MalformedURLException | URISyntaxException e) {
        // Ignore, this should never happen as we have specified the URL correctly.
      }
    }

    if (font == null) {
      normalFont = new Font(getFont().getFontName(), Font.PLAIN, 12);
      normalBoldFont = new Font(getFont().getFontName(), Font.BOLD, 12);
    } else {
      normalFont = new Font(font.getName(), Font.PLAIN, font.getSize());
      normalBoldFont = new Font(font.getName(), Font.BOLD, font.getSize());
    }

    setBackground(backgroundColor);

    setOpaque(true);
  }

  @Override
  public void paint(final Graphics graphics) {
    CLoadProgressPainter.paint(view, graphics, getWidth(), getHeight(), getBackground());

    if (view.isStared()) {
      graphics.drawImage(starImage, 0, 0, getHeight() - 2, getHeight() - 2, table);
    }

    final UnrelocatedAddress fileAddress = new UnrelocatedAddress(function.getAddress());

    if (CGraphDebugger.hasBreakpoint(debugger.getBreakpointManager(), function.getModule(),
        fileAddress)) {
      final BreakpointStatus breakpointStatus =
          CGraphDebugger.getBreakpointStatus(debugger.getBreakpointManager(),
              function.getModule(), fileAddress);

      final CBreakpointImage img = new CBreakpointImage(
          getBackground(), BreakpointManager.getBreakpointColor(breakpointStatus));

      final int x = getWidth() - img.getWidth() - 2;
      final int y = (getHeight() / 2) - (img.getHeight() / 2);

      ((Graphics2D) graphics).drawImage(img, null, x, y);
    }

    final boolean isOpen = CWindowManager.instance().isOpen(view);
    graphics.setColor(Color.BLACK);
    graphics.setFont(isOpen ? normalBoldFont : normalFont);
    graphics.drawString(function.getAddress().toHexString(), view.isStared() ? getHeight() : 0,
        12);
  }
}
