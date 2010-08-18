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

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution import Execution

from GSymCore.PresTemplate import Schema





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

	


class TemplateView (NodeView):
	def __init__(self, template, model):
		super( TemplateView, self ).__init__( template, model )
		self._modelToView = WeakKeyDictionary()
		
		
	def getBody(self):
		return _projection( self._model['body'], self )
	
	
	def _viewOf(self, model):
		try:
			return self._modelToView[model]
		except KeyError:
			p = _projection( model, self )
			self._modelToView[model] = p
			return p
		
		


class BodyView (NodeView):
	def __init__(self, template, model):
		super( BodyView, self ).__init__( template, model )
		self._contentsCell = Cell( self._computeContents )
		
		
	def getContents(self):
		return self._contentsCell.getValue()
	
	
	def appendModel(self, node):
		self._model['contents'].append( node )
	
	def insertModelAfterNode(self, node, model):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		self._model['contents'].insert( index + 1, model )
		return True

	def deleteNode(self, node):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True
		
		
		
	def joinConsecutiveTextNodes(self, firstNode):
		assert isinstance( firstNode, ParagraphView )
		contents = self.getContents()
		
		try:
			index = contents.index( firstNode )
		except ValueError:
			return False
		
		if ( index + 1)  <  len( contents ):
			next = contents[index+1]
			if isinstance( next, ParagraphView ):
				firstNode.setText( firstNode.getText() + next.getText() )
				del self._model['contents'][index+1]
				return True
		return False
	
	def splitTextNodes(self, textNode, textLines):
		style = textNode.getStyle()
		textModels = [ Schema.Paragraph( text=t, style=style )   for t in textLines ]
		try:
			index = self.getContents().index( textNode )
		except ValueError:
			return False
		self._model['contents'][index:index+1] = textModels
		return True
		
		
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]
	
		


class ParagraphView (NodeView):
	def __init__(self, template, model):
		super( ParagraphView, self ).__init__( template, model )
	
		
	def getText(self):
		return self._model['text']
	
	def setText(self, text):
		self._model['text'] = text
		
	
	def getStyle(self):
		return self._model['style']
	
	def setStyle(self, style):
		self._model['style'] = style
		
		
	def partialModel(self):
		return Schema.PartialParagraph( style=self._model['style'] )
		
		
	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
		
		
		
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

	
