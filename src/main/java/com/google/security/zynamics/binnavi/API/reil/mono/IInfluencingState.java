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
package com.google.security.zynamics.binnavi.API.reil.mono;

//! Interface for influencing states.
/**
 * Interface that must be implemented by all classes to be used as influencing
 * states in the context of MonoREIL.
 *
 * @param <LatticeElement> Type of the elements in the lattice.
 * @param <ObjectType> Type of the additional object.
 */
public interface IInfluencingState<LatticeElement extends ILatticeElement<LatticeElement>, ObjectType> extends com.google.security.zynamics.reil.algorithms.mono.interfaces.IInfluencingState<LatticeElement, ObjectType>
{
	//! The lattice element of the influencing state.
	/**
	 * Returns the lattice element that belongs to the influencing state.
	 *
	 * @return The lattice element that belongs to the influencing state.
	 */
	@Override
	LatticeElement getElement();

	//! The additional object associated with the influencing state.
	/**
	 * Returns the additional object that is associated with the influencing state.
	 *
	 * @return The additional object that is associated with the influencing state.
	 */
	@Override
	ObjectType getObject();
}
