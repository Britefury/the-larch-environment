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

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView
from GSymCore.PresTemplate.TemplateEditor.BodyView import BodyView
from GSymCore.PresTemplate.TemplateEditor.ParagraphView import ParagraphView
from GSymCore.PresTemplate.TemplateEditor.PythonExprView import PythonExprView



class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, template):
		return dmObjectNodeMethodDispatch( self, node, template )

	@DMObjectNodeDispatchMethod( Schema.Template )
	def template(self, template, node):
		return TemplateView( template, node )

	@DMObjectNodeDispatchMethod( Schema.Body )
	def body(self, template, node):
		return BodyView( template, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, template, node):
		return ParagraphView( template, node )
	
	@DMObjectNodeDispatchMethod( Schema.PythonExpr )
	def pythonExpr(self, template, node):
		return PythonExprView( template, node )

_projection = _Projection()




class TemplateView (NodeView):
	def __init__(self, template, model):
		super( TemplateView, self ).__init__( template, model )
		self._modelToView = WeakKeyDictionary()
		
		
	def getBody(self):
		return self._viewOf( self._model['body'] )
	
	
	def _viewOf(self, model):
		try:
			return self._modelToView[model]
		except KeyError:
			p = _projection( model, self )
			self._modelToView[model] = p
			return p
