"""
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
# This sample plugin shows how to use the monotone framework from Python
# to find out what registers are modified by a function.
#
# If you want to write your own monotone framework plugin you can use
# this file as a skeleton for your own plugin. Just copy and paste the
# code of this plugin to a new file and add your own code there.

import sys

from sets import Set

from javax.swing import JMenuItem as JMenuItem
from javax.swing import AbstractAction as AbstractAction

from com.google.security.zynamics.binnavi.API.helpers import MessageBox as MessageBox
from com.google.security.zynamics.binnavi.API.plugins import IGraphMenuPlugin as IGraphMenuPlugin

from com.google.security.zynamics.binnavi.API.reil.mono import ILattice
from com.google.security.zynamics.binnavi.API.reil.mono import ILatticeElement
from com.google.security.zynamics.binnavi.API.reil.mono import MonotoneSolver
from com.google.security.zynamics.binnavi.API.reil.mono import ITransformationProvider
from com.google.security.zynamics.binnavi.API.reil.mono import DownWalker
from com.google.security.zynamics.binnavi.API.reil.mono import DefaultStateVector
from com.google.security.zynamics.binnavi.API.reil.mono import InstructionGraph

# Determines whether an instruction writes a native register
def writes_native_register(instruction):
	return instruction.thirdOperand.value.isalnum() and \
		instruction.thirdOperand.value.isdigit() == False and \
		instruction.thirdOperand.value[0] != 't' and \
		instruction.mnemonic not in ("jcc", "ldm", "stm")

# This class is used for the elements of the lattice. Each lattice element
# is used to keep track of the known state for a REIL instruction during
# analysis. Since this plugin keeps track of written registers, the kept
# state says what registers are written after this instruction is
# executed.
class SkeletonLatticeElement(ILatticeElement):
	def __init__(self):
		self.written_registers = Set()

	def equals(self, rhs):
		#  This function helps MonoREIL to end the fixed point iteration
		return self.written_registers == rhs.written_registers
		
	def lessThan(self, rhs):
		# This function helps MonoREIL to check the monotonous requirement.
		return self.written_registers < rhs.written_registers

# This class defines the lattice used by the monotone framework. Its only
# purpose is to defined a function that is used to combine a list of states
# into one state.
class SkeletonLattice(ILattice):
	def combine(self, states):
		combined_state = SkeletonLatticeElement()
		
		for state in states:
			combined_state.written_registers = combined_state.written_registers.union(state.element.written_registers)
		
		return combined_state

# This class provides the transformations each instruction has on a state. For
# each instruction of the instruction graph, the current state of the instruction
# and the combined state of the influencing nodes is passed to the function.
# The function returns the state of the instruction while considering the input
# states.
class SkeletonTransformationProvider(ITransformationProvider):
	def transform(self, node, currentState, influencingState):
	
		transformed_state = SkeletonLatticeElement()
		transformed_state.written_registers = transformed_state.written_registers.union(currentState.written_registers)
		transformed_state.written_registers = transformed_state.written_registers.union(influencingState.written_registers)
		
		return transformed_state

# This function creates the initial state of the state vector passed to the
# monotone framework. In the beginning the state of each instruction is defined
# as the register it writes.
def generateStartVector(graph):
	startVector = DefaultStateVector()
	
	for node in graph:
		element = SkeletonLatticeElement()
		
		if writes_native_register(node.instruction):
			element.written_registers.add(node.instruction.thirdOperand.value)
		
		startVector.setState(node, element)
		
	return startVector

class MessageAction(AbstractAction):

	def __init__(self, pi, frame):
		AbstractAction.__init__(self, "Monotone Framework Sample")
		self.pi = pi
		self.frame = frame

	def actionPerformed(self, e):
		# The monotone framework only works on REIL graphs so we have to translate
		# the current view to REIL first.
		reilGraph = self.frame.view2D.view.reilCode
		
		# Generally the monotone framework works on graphs where each node represents
		# a REIL instruction. For this reason there is a helper function that creates
		# this instruction graph from a REIL graph.
		graph = InstructionGraph.create(reilGraph.graph)
		
		# Define the lattice used by the monotone framework.
		lattice = SkeletonLattice()
		
		# Generate the initial state vector.
		startVector = generateStartVector(graph)
		
		# Define the transformations used by the monotone framework.
		transformationProvider = SkeletonTransformationProvider()
		
		# Register tracking starts at the beginning of a function and moves
		# downwards, so we use the default DownWalker class to move through
		# the graph.
		walker = DownWalker()
	
		# Use the monotone framework to find what registers are defined by the current function.
		solver = MonotoneSolver(graph, lattice, startVector, transformationProvider, walker)
		results = solver.solve()
		
		# Process and display the results
		used_register_set = Set()
		
		for node in graph:
			used_register_set = used_register_set.union(results.getState(node).written_registers)
		
		register_list = list(used_register_set)
		register_list.sort()
		joinedString = ", ".join(register_list)
				
		MessageBox.showInformation(self.frame.window.frame, "This function modifies the registers %s." % joinedString)

class MonotoneSkeletonPlugin(IGraphMenuPlugin):
	def getName(self):
		return "Monotone Framework Sample (Register Usage)"
	
	def getGuid(self):
		return 564378237613635
		
	def getDescription(self):
		return "Skeleton for monotone framework plugins (shows what registers are written by a function)"
		
	def init(self, pi):
		self.pi = pi
		
	def closed(self, pi):
		pass
		
	def unload(self):
		pass
	
	def extendPluginMenu(self, frame):
		return [ JMenuItem(MessageAction(self.pi, frame)) ]

skeleton = MonotoneSkeletonPlugin()
navi.getPluginRegistry().addPlugin(skeleton)
