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

# This BinNavi plugin creates a view that contains the dominator tree
# of an input view. For this purpose the plugin extends the menu of
# graph windows with an additional "Create Dominator Tree" menu.

import sys

from java.lang import Thread
from javax.swing import JMenuItem as JMenuItem
from javax.swing import AbstractAction as AbstractAction

from com.google.security.zynamics.binnavi.API.disassembly import CouldntSaveDataException as CouldntSaveDataException
from com.google.security.zynamics.binnavi.API.disassembly import EdgeType as EdgeType
from com.google.security.zynamics.binnavi.API.helpers import MessageBox as MessageBox
from com.google.security.zynamics.binnavi.API.helpers import GraphAlgorithms as GraphAlgorithms
from com.google.security.zynamics.binnavi.API.plugins import IGraphMenuPlugin as IGraphMenuPlugin

def findRoot(nodes):
	"""Finds the root node of a view. Note that this function is a bit imprecise
	   but it should do the trick for most views."""
	for node in nodes:
		if len(node.parents) == 0:
			return node
	return nodes[0]

def createView(view, tree_node):
	"""Fills a given view with the nodes of a dominator tree"""
	graph_node = view.createNode(tree_node.object)
	
	for child in tree_node.children:
		child_node = createView(view, child)
		view.createEdge(graph_node, child_node, EdgeType.JumpUnconditional)
	
	return graph_node

def create_dominator_view(view):
	"""Takes a view, calculates its dominator tree, and creates a new view
	   that shows that dominator tree."""
	
	if len(view.graph.nodes) == 0:
		MessageBox.showError("Can not create dominator tree of empty views")
		return
	
	# Calculate the dominator tree
	dominator_tree = GraphAlgorithms.getDominatorTree(view.graph, findRoot(view.graph.nodes), None)
	
	try:
		# Create the new view
		tree_view = view.container.createView("Dominator Tree: '%s'" % view.name, "")
		
		# Copy all the nodes from the dominator tree into the new view
		createView(tree_view, dominator_tree.rootNode)
		
		return tree_view
	except CouldntSaveDataException:
		MessageBox.showError("Could not create the dominator tree view")
		return None

class MessageAction(AbstractAction):

	def __init__(self, pi, frame):
		AbstractAction.__init__(self, "Create Dominator Tree")
		self.pi = pi
		self.frame = frame

	def actionPerformed(self, e):
		view = create_dominator_view(self.frame.view2D.view)
		
		if view != None:
			t = WorkaroundThread(self.pi, self.frame.window, view)
			t.start()
#			new_view2d.save()

class DominatorTreePlugin(IGraphMenuPlugin):
	def getName(self):
		return "Dominator Tree Plugin"
	
	def getGuid(self):
		return 945436890432
		
	def getDescription(self):
		return "Creates the dominator tree of a view"
		
	def init(self, pi):
		self.pi = pi
		
	def closed(self, frame):
		pass
		
	def unload(self):
		pass
	
	def extendPluginMenu(self, frame):
		return [ JMenuItem(MessageAction(self.pi, frame)) ]

class WorkaroundThread(Thread):
	def __init__(self, pi, window, view):
		self.pi = pi
		self.window = window
		self.view = view
		
	def run(self):
		new_view2d = self.pi.showInWindow(self.window, self.view)
		new_view2d.doHierarchicalLayout()
		
dominatorTree = DominatorTreePlugin()
navi.getPluginRegistry().addPlugin(dominatorTree)
