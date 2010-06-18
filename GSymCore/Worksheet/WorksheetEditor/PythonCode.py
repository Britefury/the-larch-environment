##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Worksheet import Schema, ViewSchema




class NewPythonCodeRequest (object):
	pass


class AddPythonCodeOperation (object):
	pass

class PrependPythonCodeOperation (AddPythonCodeOperation):
	def apply(self, worksheet):
		pythonCode = ViewSchema.PythonCodeView.newPythonCodeNode()
		worksheet['contents'].insert( 0, pythonCode )
		return True

	

class AppendPythonCodeOperation (AddPythonCodeOperation):
	def apply(self, worksheet):
		pythonCode = ViewSchema.PythonCodeView.newPythonCodeNode()
		worksheet['contents'].append( pythonCode )
		return True

	

class InsertPythonCodeOperation (AddPythonCodeOperation):
	def __init__(self, node):
		super( InsertPythonCodeOperation, self ).__init__()
		self._node = node
		
		
	def apply(self, worksheet):
		index = worksheet['contents'].indexOf( self._node )
		
		if index != -1:
			pythonCode = ViewSchema.PythonCodeView.newPythonCodeNode()
			worksheet['contents'].insert( index+1, pythonCode )
			return True
		else:
			return False

		
		
