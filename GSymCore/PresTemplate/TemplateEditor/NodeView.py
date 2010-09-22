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

from GSymCore.PresTemplate import Schema


class NodeView (object):
	def __init__(self, template, model):
		self._template = template
		self._model = model
		
	def getModel(self):
		return self._model
	
	def isVisible(self):
		return True
	
	def __present__(self, fragment, inheritedState):
		return InnerFragment( self._model )
	
	def _viewOf(self, model):
		return self._template._viewOf( model )
