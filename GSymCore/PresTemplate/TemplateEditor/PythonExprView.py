##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakKeyDictionary

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.Cell import Cell

from BritefuryJ.GSym.PresCom import InnerFragment

from GSymCore.Languages.Python25 import Python25

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView


class PythonExprView (IncrementalOwner, NodeView):
	def __init__(self, template, model):
		NodeView.__init__( self, template, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getCode(self):
		return self._model['code']
	
	def setCode(self, code):
		self._model['code'] = code
		
		
		
	@staticmethod
	def newPythonExprModel():
		return Schema.PythonExpr( code=Python25.py25NewExpr() )

