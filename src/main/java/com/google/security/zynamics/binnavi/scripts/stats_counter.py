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
# This BinNavi sample plugin displays some statistical information about
# the configured databases and their content

import sys

from javax.swing import JMenuItem as JMenuItem
from javax.swing import AbstractAction as AbstractAction

from com.google.security.zynamics.binnavi.API.helpers import MessageBox as MessageBox
from com.google.security.zynamics.binnavi.API.plugins import IMainWindowMenuPlugin as IMainWindowMenuPlugin
from com.google.security.zynamics.binnavi.API.disassembly import ViewType as ViewType
from com.google.security.zynamics.binnavi.API.disassembly import GraphType as GraphType

class MessageAction(AbstractAction):

	def __init__(self, pi):
		AbstractAction.__init__(self, "Project Statistics (Python Sample Plugin)")
		
		self.pi = pi

	def actionPerformed(self, e):
		# Count the configured databases and the loaded databases
		databases = self.pi.databaseManager.databases
		loaded_databases = filter(lambda db : db.isLoaded(), databases)
		
		# Count the modules that can be found in the loaded databases
		modules = sum(map(lambda db : db.modules, loaded_databases), [])
		loaded_modules = filter(lambda m : m.isLoaded(), modules)
		
		# Count the views that can be found in the loaded modules
		views = sum(map(lambda m : m.views, loaded_modules), [])
		loaded_views = filter(lambda v : v.isLoaded(), views)
		
		# Find the functions by filtering the views (this is just an
		# example, in real scripts use module.functions please)
		functions = filter(lambda v : v.type == ViewType.Native and v.graphType == GraphType.Flowgraph, views)
		
		if views == []:
			largest_function_name = "-"
		else:
			functions.sort(lambda f1, f2: f2.nodeCount - f1.nodeCount)
			largest_function = functions[0]
			largest_function_name = "%s (%d nodes / %d edges)" % (largest_function.name, largest_function.nodeCount, largest_function.edgeCount)
		
		db_str = "%d databases configured (%d loaded)." % (len(databases), len(loaded_databases))
		mod_str = "%d modules in the loaded databases (%d loaded)." % (len(modules), len(loaded_modules))
		view_str = "%d views in the loaded modules (%d loaded)." % (len(views), len(loaded_views))
		largest_function_str = "Largest Function: %s" % largest_function_name
		
		MessageBox.showInformation(self.pi.mainWindow.frame, "%s\n%s\n%s\n%s" % (db_str, mod_str, view_str, largest_function_str))
		
class StatisticsSamplePlugin(IMainWindowMenuPlugin):
	def getName(self):
		return "Project Statistics (Python Sample Plugin)"
	
	def getGuid(self):
		return 656257856256;
		
	def getDescription(self):
		return "Displays a few statistics about the configured databases"
		
	def init(self, pi):
		self.pi = pi
		
	def unload(self):
		pass
	
	def extendPluginMenu(self):
		return [ JMenuItem(MessageAction(self.pi)) ]

sample = StatisticsSamplePlugin()
navi.getPluginRegistry().addPlugin(sample)
