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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.reil.mono.ILatticeElement;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.IInfluencingState;

/**
 * Wraps API state objects to internal state objects.
 *
 * @param <ApiLatticeElement> Type of the API lattice elements.
 * @param <ObjectType> Type of the additional object of a state.
 */
public final class StateWrapper<ApiLatticeElement extends ILatticeElement<ApiLatticeElement>, ObjectType> implements com.google.security.zynamics.binnavi.API.reil.mono.IInfluencingState<ApiLatticeElement, ObjectType>
{
	/**
	 * Wrapped API lattice element.
	 */
	private final ApiLatticeElement element;

	/**
	 * Wrapped additional object.
	 */
	private final ObjectType object;

	/**
	 * Creates a new wrapper object.
	 *
	 * @param wrappedState The wrapped API state.
	 */
	public StateWrapper(final IInfluencingState<ApiLatticeElement, ObjectType> wrappedState)
	{
		Preconditions.checkNotNull(wrappedState, "IE02086: Wrapped state argument can not be null");

		element = wrappedState.getElement();
		object = wrappedState.getObject();
	}

	@Override
	public ApiLatticeElement getElement()
	{
		return element;
	}

	@Override
	public ObjectType getObject()
	{
		return object;
	}
}