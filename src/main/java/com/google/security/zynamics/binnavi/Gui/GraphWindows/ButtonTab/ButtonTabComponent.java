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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.ButtonTab;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and a JButton to close
 * the tab it belongs to
 */
public final class ButtonTabComponent extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6626650103082832554L;

  /**
   * Color used to paint the X when the mouse is hovering over the X.
   */
  private static final Color ROLL_OVER_COLOR = new Color(160, 0, 0);

  /**
   * Mouse listener used for updating the button on mouse events.
   */
  private static final MouseListener buttonMouseListener = new MouseAdapter() {
    @Override
    public void mouseEntered(final MouseEvent event) {
      final Component component = event.getComponent();
      if (component instanceof AbstractButton) {
        final AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }

    @Override
    public void mouseExited(final MouseEvent event) {
      final Component component = event.getComponent();
      if (component instanceof AbstractButton) {
        final AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }
  };

  /**
   * Listeners that are notified when the X was clicked to close the tab.
   */
  private final ListenerProvider<IButtonTabListener> m_listeners =
      new ListenerProvider<IButtonTabListener>();

  /**
   * Tabbed pane the button tab component belongs to.
   */
  private final JTabbedPane m_pane;

  /**
   * Creates a new button tab component.
   * 
   * @param pane pane the button tab component belongs to.
   */
  public ButtonTabComponent(final JTabbedPane pane) {
    // unset default FlowLayout' gaps
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));

    m_pane = Preconditions.checkNotNull(pane, "IE01213: TabbedPane is null");
    setOpaque(false);

    // make JLabel read titles from JTabbedPane
    final JLabel label = new JLabel() {
      private static final long serialVersionUID = 8139543899934835869L;

      @Override
      public String getText() {
        final int index = pane.indexOfTabComponent(ButtonTabComponent.this);
        if (index != -1) {
          return pane.getTitleAt(index);
        }
        return null;
      }
    };

    add(label);
    // add more space between the label and the button
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    // tab button
    final JButton button = new TabButton();
    add(button);
    // add more space to the top of the component
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  /**
   * Adds a new listener that is notified about clicks on the X.
   * 
   * @param listener The listener to add.
   */
  public void addListener(final IButtonTabListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Removes a listener from being notified about clicks on the X.
   * 
   * @param listener The listener to remove.
   */
  public void removeListener(final IButtonTabListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * The button that is shown in the corner of the tab.
   */
  private class TabButton extends JButton implements ActionListener {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 5815406628571547063L;

    /**
     * Creates a new tab button object.
     */
    public TabButton() {
      final int size = 17;
      setPreferredSize(new Dimension(size, size));
      setToolTipText("Close this tab.");
      // Make the button look the same for all Laf's
      setUI(new BasicButtonUI());
      // Make it transparent
      setContentAreaFilled(false);
      // No need to be focusable
      setFocusable(false);
      setBorder(BorderFactory.createBevelBorder(1)); // createEtchedBorder());
      setBorderPainted(false);
      // Making nice rollover effect
      // we use the same listener for all buttons
      addMouseListener(buttonMouseListener);
      setRolloverEnabled(true);
      // Close the proper tab by clicking the button
      addActionListener(this);
    }

    // paint the cross
    @Override
    protected void paintComponent(final Graphics graphics) {
      super.paintComponent(graphics);
      final Graphics2D graphics2d = (Graphics2D) graphics.create();

      graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

      // shift the image for pressed buttons
      if (getModel().isPressed()) {
        graphics2d.translate(1, 1);
      }
      graphics2d.setStroke(new BasicStroke(3));
      graphics2d.setColor(Color.BLACK);
      if (getModel().isRollover()) {
        graphics2d.setColor(ROLL_OVER_COLOR);
      }
      final int delta = 6;
      graphics2d.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
      graphics2d.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
      graphics2d.dispose();
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final int index = m_pane.indexOfTabComponent(ButtonTabComponent.this);

      if (index != -1) {
        for (final IButtonTabListener listener : m_listeners) {
          try {
            if (!listener.closing(ButtonTabComponent.this)) {
              return;
            }
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }

        m_pane.remove(index);
      }
    }

    @Override
    public void updateUI() {
      // we don't want to update UI for this button
    }
  }
}
