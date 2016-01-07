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

import java.awt.AWTEvent;

import javax.swing.JLabel;

/**
 * Label class that can display context-sensitive help.
 */
public final class CHelpLabel extends JLabel implements IHelpProvider
{
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = 8932159322049738402L;

	/**
	 * Provides context-sensitive help.
	 */
	private final IHelpInformation m_information;

	/**
	 * Creates a new label object.
	 *
	 * @param text Text shown in the label.
	 * @param information Provides context-sensitive help.
	 */
	public CHelpLabel(final String text, final IHelpInformation information)
	{
		super(text);

		m_information = information;

		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	@Override
	public IHelpInformation getHelpInformation()
	{
		return m_information;
	}
}
