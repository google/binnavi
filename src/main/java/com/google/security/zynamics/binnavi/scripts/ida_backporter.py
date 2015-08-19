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
# This BinNavi sample plugin shows how to port names back to IDA.

import sys

from javax.swing import JMenuItem as JMenuItem
from javax.swing import AbstractAction as AbstractAction
from javax.swing import JFileChooser as JFileChooser

from com.google.security.zynamics.binnavi.API.helpers import MessageBox as MessageBox
from com.google.security.zynamics.binnavi.API.plugins import IModuleMenuPlugin as IModuleMenuPlugin

#from com.google.security.zynamics.binnavi.API.disassembly import ViewType as ViewType
#from com.google.security.zynamics.binnavi.API.disassembly import GraphType as GraphType

def createIdcFile(fileName, module):
	""" Takes the information from the module and creates an IDC file that contains all
	the naming information that is backported to IDA Pro. This IDC file must then be run
	by the user from inside IDA Pro.
	"""

	output_file = open(fileName, "w")
	
	output_file.write("#include <idc.idc>\n")
	output_file.write("static main(void) {\n")
	output_file.write("\n")
	
	for function in module.functions:
	
		# Skip functions with default names, otherwise IDA Pro complains
		if function.name.startswith("sub_"):
			continue

		output_file.write("MakeName(0x%s, \"%s\");\n" % (function.address, function.name))
	
	output_file.write("}")
	
	output_file.close()

class MenuAction(AbstractAction):

	def __init__(self, pi, module):
		AbstractAction.__init__(self, "Port names to IDA Pro")
		
		self.pi = pi
		self.module = module

	def actionPerformed(self, e):
	
		fc = JFileChooser()
		
		returnValue = fc.showSaveDialog(self.pi.mainWindow.frame)
		
		if returnValue == JFileChooser.APPROVE_OPTION:
			fileName = fc.selectedFile.absolutePath
			createIdcFile(fileName, self.module)
			MessageBox.showInformation(self.pi.mainWindow.frame, "Module information was successfully written to the selected IDC file. Please run the IDC file in IDA Pro now.")
			
	
class IdaBackporterPlugin(IModuleMenuPlugin):
	def getName(self):
		return "IDA Backporter"
	
	def getGuid(self):
		return 79042346890423L
		
	def getDescription(self):
		return "This plugin creates an IDC file which can then be executed in IDA Pro to port the function names from BinNavi back to an IDA Pro IDB file."
		
	def init(self, pi):
		self.pi = pi
		
	def unload(self):
		pass
	
	def extendModuleMenu(self, modules):
		# We can only export the names of a single module.
		if len(modules) != 1 or not modules[0].isLoaded():
			return [ ]
		else:
			return [ JMenuItem(MenuAction(self.pi, modules[0])) ]

sample = IdaBackporterPlugin()
navi.getPluginRegistry().addPlugin(sample)
