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

import javax.swing.text.DefaultFormatterFactory;

import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveFormattedField;


/**
 * Save text field class that can display context-sensitive help.
 */
public final class CHelpSaveFormattedField extends CSaveFormattedField implements IHelpProvider
{
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = -7773608337971802530L;

	/**
	 * Provides context-sensitive help.
	 */
	private final IHelpInformation m_information;

	/**
	 * Creates a new formatted field object.
	 *
	 * @param formatter Formatter used to format the text field.
	 * @param information Provides context-sensitive help.
	 */
	public CHelpSaveFormattedField(final AbstractFormatter formatter, final IHelpInformation information)
	{
		super(formatter);

		m_information = information;
	}

	/**
	 * Creates a new formatted field object.
	 *
	 * @param factory Formatter factory used to format the text field.
	 * @param information Provides context-sensitive help.
	 */
	public CHelpSaveFormattedField(final DefaultFormatterFactory factory, final IHelpInformation information)
	{
		super(factory);

		m_information = information;
	}

	@Override
	public IHelpInformation getHelpInformation()
	{
		return m_information;
	}
}
