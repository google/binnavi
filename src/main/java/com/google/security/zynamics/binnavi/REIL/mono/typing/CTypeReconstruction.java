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
package com.google.security.zynamics.binnavi.REIL.mono.typing;


//public final class CTypeReconstruction
//{
//	private static StateVector<InstructionGraphNode, TypeReconstructionState> createInitialStateVector(final InstructionGraph graph, final TypeHierarchy typeHierarchy)
//	{
//		final StateVector<InstructionGraphNode, TypeReconstructionState> stateVector = new StateVector<InstructionGraphNode, TypeReconstructionState>();
//
//		for (final InstructionGraphNode node : graph)
//		{
//			final Map<Integer, IType> initialMap = new HashMap<Integer, IType>();
//
//			if (ReilHelpers.usesFirstOperand(node.getInstruction().getMnemonic()))
//			{
//				if (node.getInstruction().getFirstOperand().getType() == OperandType.REGISTER)
//				{
//					initialMap.put(0, typeHierarchy.getTopType(node.getInstruction().getFirstOperand().getSize()));
//				}
//				else if (node.getInstruction().getFirstOperand().getType() == OperandType.INTEGER_LITERAL)
//				{
//					initialMap.put(0, typeHierarchy.getIntegralType(node.getInstruction().getFirstOperand().getSize()));
//				}
//				else
//				{
//					throw new IllegalStateException("IE01207: Not yet implemented");
//				}
//			}
//
//			if (ReilHelpers.usesSecondOperand(node.getInstruction().getMnemonic()))
//			{
//				if (node.getInstruction().getFirstOperand().getType() == OperandType.REGISTER)
//				{
//					initialMap.put(1, typeHierarchy.getTopType(node.getInstruction().getSecondOperand().getSize()));
//				}
//				else if (node.getInstruction().getFirstOperand().getType() == OperandType.INTEGER_LITERAL)
//				{
//					initialMap.put(1, typeHierarchy.getIntegralType(node.getInstruction().getSecondOperand().getSize()));
//				}
//				else
//				{
//					throw new IllegalStateException("IE01208: Not yet implemented");
//				}
//			}
//
//			if (ReilHelpers.usesThirdOperand(node.getInstruction().getMnemonic()))
//			{
//				if (node.getInstruction().getFirstOperand().getType() == OperandType.REGISTER)
//				{
//					initialMap.put(2, typeHierarchy.getTopType(node.getInstruction().getThirdOperand().getSize()));
//				}
//				else if (node.getInstruction().getFirstOperand().getType() == OperandType.INTEGER_LITERAL)
//				{
//					initialMap.put(2, typeHierarchy.getTopType(node.getInstruction().getThirdOperand().getSize()));
//				}
//				else
//				{
//					throw new IllegalStateException("IE01209: Not yet implemented");
//				}
//			}
//
//			stateVector.setState(node, new TypeReconstructionState(node.getInstruction(), initialMap, new HashMap<String, IType>(), new HashMap<String, IType>()));
//		}
//
//		return stateVector;
//	}
//
//	public static IStateVector<InstructionGraphNode, TypeReconstructionState> reconstruct(final INaviView view, final TypeHierarchy typeHierarchy) throws InternalTranslationException
//	{
//		// Translate the given graph to an instruction graph
//		final ReilFunction reilFunction = view.getReilCode();
//		final InstructionGraph instructionGraph = InstructionGraph.create(reilFunction.getGraph());
//
//		final StateVector<InstructionGraphNode, TypeReconstructionState> stateVector = createInitialStateVector(instructionGraph, typeHierarchy);
//
//		final TypeReconstructionSolver tracker = new TypeReconstructionSolver(instructionGraph, stateVector, typeHierarchy);
//
//		return tracker.solve();
//	}
//}
