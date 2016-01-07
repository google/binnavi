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
package com.google.security.zynamics.zylib.gui.sliders;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;

public class DoubleLabeledSlider extends JPanel {
  private static final long serialVersionUID = 3181291967149555468L;

  private final JSlider m_slider;

  public DoubleLabeledSlider(final String leftText, final String rightText, final int min,
      final int max) {
    setLayout(new BorderLayout());

    final JLabel leftLabel = new JLabel(leftText);

    m_slider = new JSlider(min, max);
    m_slider.setMinorTickSpacing(1);
    m_slider.setPaintTicks(true);
    m_slider.setPaintLabels(true);

    final JLabel rightLabel = new JLabel(rightText);

    add(leftLabel, BorderLayout.WEST);
    add(m_slider);
    add(rightLabel, BorderLayout.EAST);
  }

  public DoubleLabeledSlider(final String leftText, final String rightText, final int min,
      final int max, final boolean trackbar, final Border border) {
    this(leftText, rightText, min, max);

    m_slider.setPaintTrack(trackbar);

    setBorder(border);
  }

  public static void main(final String[] args) {
    final JFrame frame = new JFrame();

    final DoubleLabeledSlider slider = new DoubleLabeledSlider("Low", "High", 0, 5);

    frame.add(slider);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setSize(400, 400);
    frame.setVisible(true);
  }

  public int getValue() {
    return m_slider.getValue();
  }

  public void setInverted(final boolean inverted) {
    m_slider.setInverted(inverted);
  }

  public void setValue(final int value) {
    m_slider.setValue(value);
  }
}
