##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent import *

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from Britefury.Kernel.Abstract import abstractmethod

from GSymCore.PresTemplate import Schema, ViewSchema




class AddNodeOperation (object):
	def __init__(self, nodeFactory):
		self._nodeFactory = nodeFactory

		

class AppendNodeOperation (AddNodeOperation):
	def apply(self, body):
		node = self._nodeFactory()
		body.appendModel( node )
		return True

	

class InsertNodeOperation (AddNodeOperation):
	def __init__(self, nodeFactory, anchor):
		super( InsertNodeOperation, self ).__init__( nodeFactory )
		self._anchor = anchor
		
		
	def apply(self, body):
		node = self._nodeFactory()
		return body.insertModelAfterNode( self._anchor, node )



class NodeRequest (object):
	@abstractmethod
	def applyToParagraphNode(self, paragraph, element):
		pass
	
	@abstractmethod
	def applyToPythonExprNode(self, pythonExpr, element):
		pass
	

	def applyToEmpty(self, body, element):
		return element.postTreeEvent( AppendNodeOperation( self._createModel ) )
	
	
	@abstractmethod
	def _createModel(self):
		pass
	

	def _insertAfter(self, anchor, element):
		return element.postTreeEvent( InsertNodeOperation( self._createModel, anchor ) )
		

	
	
	
	
	
