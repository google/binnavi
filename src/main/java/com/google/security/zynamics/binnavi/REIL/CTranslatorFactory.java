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
package com.google.security.zynamics.binnavi.REIL;


import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.ITranslator;
import com.google.security.zynamics.reil.translators.arm.TranslatorARM;
import com.google.security.zynamics.reil.translators.ppc.TranslatorPPC;
import com.google.security.zynamics.reil.translators.reil.TranslatorREIL;
import com.google.security.zynamics.reil.translators.x86.TranslatorX86;

/**
 * Factory class that returns REIL translators depending on architecture strings.
 */
public final class CTranslatorFactory
{
	/**
	 * You are not supposed to instantiate this class.
	 */
	private CTranslatorFactory()
	{
	}

	/**
	 * Returns the REIL translator for a given architecture string-
	 *
	 * @param architecture The architecture string.
	 *
	 * @return The REIL translator for the architecture identified by the architecture string or null if there is no such translator.
	 */
	public static ITranslator<INaviInstruction> getTranslator(final String architecture)
	{
		if (architecture.equalsIgnoreCase("x86-32"))
		{
			return new TranslatorX86<INaviInstruction>();
		}
		else if (architecture.equalsIgnoreCase("PowerPC-32"))
		{
			return new TranslatorPPC<INaviInstruction>();
		}
		else if (architecture.equalsIgnoreCase("ARM-32"))
		{
			return new TranslatorARM<INaviInstruction>();
		}
		else if (architecture.equalsIgnoreCase("REIL"))
		{
			return new TranslatorREIL<INaviInstruction>();
		}
		else if (architecture.equalsIgnoreCase("MIPS-32"))
		{
			return new TranslatorREIL<INaviInstruction>();
		}
		else
		{
			return null;
		}
	}
}
