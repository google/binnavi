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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.reil.ReilInstruction;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraph;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphEdge;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphNode;


/**
 * Converts an internal InstructionGraph object into an API InstructionGraph object.
 */
public final class CInstructionGraphConverter
{
	/**
	 * You are not supposed to instantiate this class.
	 */
	private CInstructionGraphConverter()
	{
	}

	/**
	 * Converts an internal InstructionGraph object into an API InstructionGraph object.
	 *
	 * @param graph The internal graph object that is converted.
	 *
	 * @return The converted API graph object.
	 */
	public static InstructionGraph convert(final com.google.security.zynamics.reil.algorithms.mono.InstructionGraph graph)
	{
		final List<InstructionGraphNode> nodes = new ArrayList<InstructionGraphNode>();
		final List<InstructionGraphEdge> edges = new ArrayList<InstructionGraphEdge>();

		final Map<com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode, InstructionGraphNode> nodeMap = new HashMap<com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode, InstructionGraphNode>(); 

		for (final com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode node : graph)
		{
			final InstructionGraphNode convertedNode = new InstructionGraphNode(new ReilInstruction(node.getInstruction())); 

			nodeMap.put(node, convertedNode);

			nodes.add(convertedNode);
		}

		for (final com.google.security.zynamics.reil.algorithms.mono.InstructionGraphEdge edge : graph.getEdges())
		{
			final InstructionGraphEdge convertedEdge = new InstructionGraphEdge(nodeMap.get(edge.getSource()), nodeMap.get(edge.getTarget()), EdgeType.convert(edge.getType())); 

			edges.add(convertedEdge);

			InstructionGraphNode.link(nodeMap.get(edge.getSource()), nodeMap.get(edge.getTarget()), convertedEdge);
		}

		return new InstructionGraph(nodes, edges);
	}
}
