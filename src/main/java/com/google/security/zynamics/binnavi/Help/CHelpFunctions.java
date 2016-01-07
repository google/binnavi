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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Resources.Constants;


/**
 * Contains helper functions for showing context-sensitive help.
 */
public final class CHelpFunctions
{
	/**
	 * Main window help file.
	 */
	public static final String MAIN_WINDOW_FILE = "main_window.htm";

	/**
	 * You are not supposed to instantiate this class.
	 */
	private CHelpFunctions()
	{
	}

	/**
	 * Creates a URL for the passed help file.
	 *
	 * @param helpfile The help file for which the URL is created.
	 *
	 * @return The URL for that help file.
	 */
	public static URL urlify(final String helpfile)
	{
		final String urlString = Constants.startPath + "manual/html/" + helpfile;

		try
		{
			return new File(urlString).toURI().toURL();
		}
		catch(final MalformedURLException exception)
		{
			CUtilityFunctions.logException(exception);

			return null;
		}
	}
}
