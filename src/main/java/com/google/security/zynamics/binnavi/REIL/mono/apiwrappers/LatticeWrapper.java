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
package com.google.security.zynamics.binnavi.REIL.mono.apiwrappers;

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.reil.mono.ILattice;
import com.google.security.zynamics.binnavi.API.reil.mono.ILatticeElement;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.IInfluencingState;

/**
 * Used to wrap lattice elements from the public API to use them in internal classes.
 *
 * @param <ApiLatticeElement> Type of the API lattice elements.
 * @param <ObjectType> Type of the additional object of a state.
 */
public final class LatticeWrapper<ApiLatticeElement extends ILatticeElement<ApiLatticeElement>, ObjectType> implements com.google.security.zynamics.reil.algorithms.mono.interfaces.ILattice<ApiLatticeElement, ObjectType>
{
	/**
	 * API lattice that is wrapped.
	 */
	private final ILattice<ApiLatticeElement, ObjectType> m_lattice;

	/**
	 * Creates a new lattice wrapper object.
	 *
	 * @param lattice API lattice that is wrapped.
	 */
	public LatticeWrapper(final ILattice<ApiLatticeElement, ObjectType> lattice)
	{
		Preconditions.checkNotNull(lattice, "IE02085: Lattice argument can not be null");

		m_lattice = lattice;
	}

	/**
	 * Wraps a list of API lattice elements into internal lattice elements.
	 *
	 * @param states The lattice elements to wrap.
	 *
	 * @return The wrapped lattice elements.
	 */
	private List<com.google.security.zynamics.binnavi.API.reil.mono.IInfluencingState<ApiLatticeElement, ObjectType>> wrap(final List<IInfluencingState<ApiLatticeElement, ObjectType>> states)
	{
		final List<com.google.security.zynamics.binnavi.API.reil.mono.IInfluencingState<ApiLatticeElement, ObjectType>> wrapped = new ArrayList<com.google.security.zynamics.binnavi.API.reil.mono.IInfluencingState<ApiLatticeElement,ObjectType>>();

		for (final IInfluencingState<ApiLatticeElement,ObjectType> influencingState : states)
		{
			wrapped.add(new StateWrapper<ApiLatticeElement, ObjectType>(influencingState)); 
		}

		return wrapped;
	}

	@Override
	public ApiLatticeElement combine(final List<IInfluencingState<ApiLatticeElement, ObjectType>> states)
	{
		return m_lattice.combine(wrap(states));
	}
}
