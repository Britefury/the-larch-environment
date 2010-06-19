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
	def apply(self, body):
		pythonCode = ViewSchema.PythonCodeView.newPythonCodeModel()
		body.prependModel( pythonCode )
		return True

	

class AppendPythonCodeOperation (AddPythonCodeOperation):
	def apply(self, body):
		pythonCode = ViewSchema.PythonCodeView.newPythonCodeModel()
		body.appendModel( pythonCode )
		return True

	

class InsertPythonCodeOperation (AddPythonCodeOperation):
	def __init__(self, node):
		super( InsertPythonCodeOperation, self ).__init__()
		self._node = node
		
		
	def apply(self, body):
		pythonCode = ViewSchema.PythonCodeView.newPythonCodeModel()
		return body.insertModelBeforeNode( self._node, pythonCode )


	


		
