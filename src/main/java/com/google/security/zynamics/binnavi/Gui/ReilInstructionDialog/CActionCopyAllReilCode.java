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
package com.google.security.zynamics.binnavi.Gui.ReilInstructionDialog;

import com.google.security.zynamics.zylib.general.ClipboardHelpers;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;


public final class CActionCopyAllReilCode extends AbstractAction {
  private static final long serialVersionUID = 3360637791138556693L;
  /**
   * Area from which the REIL code is copied.
   */
  private final JTextArea m_textArea;

  /**
   * Creates a new action object.
   * 
   * @param textArea Area from which the REIL code is copied.
   */
  public CActionCopyAllReilCode(final JTextArea textArea) {
    super("Copy to Clipboard");

    m_textArea = textArea;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    ClipboardHelpers.copyToClipboard(m_textArea.getText());
  }
}
