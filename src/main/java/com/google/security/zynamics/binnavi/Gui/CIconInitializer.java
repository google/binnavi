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
package com.google.security.zynamics.binnavi.Gui;

import java.awt.Image;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.google.security.zynamics.binnavi.CMain;

/**
 * Helper class used to initialize the icons of a window.
 */
public final class CIconInitializer {
  /**
   * 16x16 pixels version of the BinNavi icon.
   */
  public static final Image APPICON_16x16 =
      new ImageIcon(CMain.class.getResource("data/binnavi-16x16-rgba.png")).getImage();

  /**
   * 32x32 pixels version of the BinNavi icon.
   */
  private static final Image APPICON_32x32 =
      new ImageIcon(CMain.class.getResource("data/binnavi-32x32-rgba.png")).getImage();

  /**
   * 48x48 pixels version of the BinNavi icon.
   */
  public static final Image APPICON_48x48 =
      new ImageIcon(CMain.class.getResource("data/binnavi-48x48-rgba.png")).getImage();

  /**
   * You are not supposed to instantiate this class.
   */
  private CIconInitializer() {}

  /**
   * Initializes the icons shown in the title bar of a window.
   *
   * @param window The window whose items are initialized.
   */
  public static void initializeWindowIcons(final Window window) {
    final ArrayList<Image> imageList = new ArrayList<Image>();

    imageList.add(APPICON_16x16);
    imageList.add(APPICON_32x32);
    imageList.add(APPICON_48x48);

    window.setIconImages(imageList);
  }
}
