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
package com.google.security.zynamics.binnavi.Help;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;



/**
 * Manages the currently visible context-sensitive help.
 */
public final class CHelpManager
{
	/**
	 * Help dialog that shows context-sensitive help.
	 */
	private final CHelpDialog m_helpDialog = new CHelpDialog();

	/**
	 * Cursor shown in context-sensitive help mode.
	 */
	private final Cursor m_helpCursor;

	/**
	 * The only valid instance of the help manager.
	 */
	private static CHelpManager m_instance = new CHelpManager();

	/**
	 * Window in which context-sensitive help mode is active.
	 */
	private Window m_activeWindow = null;

	/**
	 * Suppresses mouse clicks on glass panels.
	 */
	private final MouseAdapter m_listener = new MouseAdapter() { };

	/**
	 * Creates a new help manager object.
	 */
	private CHelpManager()
	{
		final Image cursorImage = Toolkit.getDefaultToolkit().getImage(CMain.class.getResource("data/help.png"));
		final java.awt.Point cursorHotSpot = new java.awt.Point(0,0);
		m_helpCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, cursorHotSpot, "helpCursor");

		Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
		{
			@Override
			public void eventDispatched(final AWTEvent event)
			{
				handleEvent(event);
			}
		},
		AWTEvent.MOUSE_EVENT_MASK);
	}

	/**
	 * Returns the only valid instance of the help manager.
	 *
	 * @return The only valid instance of the help manager.
	 */
	public static CHelpManager instance()
	{
		return m_instance;
	}

	/**
	 * Returns a point that describes the upper left corner of the dialog
	 * to show. The function makes sure that the point is chosen so that
	 * the whole dialog is visible.
	 *
	 * @param event The mouse event that contains the click information.
	 *
	 * @return The normalized coordinates of the dialog.
	 */
	private Point getDialogLocation(final MouseEvent event)
	{
		// Get the default toolkit
		final Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Get the current screen size
		final Dimension scrnsize = toolkit.getScreenSize();

		final int x = event.getXOnScreen(); 
		final int y = event.getYOnScreen(); 

		return new Point(normalize(x, m_helpDialog.getWidth(), scrnsize.width), normalize(y, m_helpDialog.getHeight(), scrnsize.height));
	}

	/**
	 * Handles an AWT event and shows display context-sensitive help if
	 * necessary.
	 *
	 * @param event The event to handle.
	 */
	private void handleEvent(final AWTEvent event)
	{
		final MouseEvent kevent = (MouseEvent) event;

		if (kevent.getID() == MouseEvent.MOUSE_CLICKED && SwingUtilities.isLeftMouseButton(kevent))
		{
			final Window window = SwingUtilities.getWindowAncestor(kevent.getComponent());

			if (m_activeWindow == window) 
			{
				final JFrame frame = (JFrame) window;

				frame.getGlassPane().setVisible(false);
				final Component component = GuiHelper.findComponentAt(frame, kevent.getLocationOnScreen());
				frame.getGlassPane().setVisible(true);

				if (component instanceof IHelpProvider)
				{
					final IHelpProvider helpProvider = (IHelpProvider) component;
					final IHelpInformation help = helpProvider.getHelpInformation();

					m_helpDialog.setInformation(help);
					m_helpDialog.setLocation(getDialogLocation(kevent));
					m_helpDialog.setVisible(true);
				}
			}
		}
		else if (kevent.getID() == MouseEvent.MOUSE_CLICKED && SwingUtilities.isRightMouseButton(kevent))
		{
			final Window window = SwingUtilities.getWindowAncestor(kevent.getComponent());

			if (m_activeWindow == window) 
			{
				((JFrame)window).getGlassPane().setVisible(false);
				((JFrame)window).getGlassPane().removeMouseListener(m_listener);
				window.setCursor(Cursor.getDefaultCursor());

				m_activeWindow = null; 
			}
		}
	}

	/**
	 * Normalizes a coordinate to make sure the whole dialog is visible.
	 *
	 * @param coordinate The coordinate to normalize.
	 * @param dialogSize Size of the dialog to consider.
	 * @param maximum Maximum possible return value.
	 *
	 * @return The normalized coordinate.
	 */
	private int normalize(final int coordinate, final int dialogSize, final int maximum)
	{
		if (coordinate + dialogSize > maximum)
		{
			return maximum - dialogSize - 10;
		}
		else
		{
			return coordinate;
		}
	}

	/**
	 * Starts context-sensitive mode in a given window.
	 *
	 * @param window The window in which context-sensitive mode is activated.
	 */
	public void start(final JFrame window)
	{
		if (m_activeWindow != window) 
		{
			window.getGlassPane().setVisible(true);
			window.getGlassPane().addMouseListener(m_listener);
			window.setCursor(m_helpCursor);

			m_activeWindow = window;
		}
	}
}
