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

from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators.ContextMenu import *

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView
from GSymCore.PresTemplate.TemplateEditor.BodyView import BodyView
from GSymCore.PresTemplate.TemplateEditor.ParagraphView import ParagraphView
from GSymCore.PresTemplate.TemplateEditor.PythonExprView import PythonExprView, PythonExprRequest
from GSymCore.PresTemplate.TemplateEditor.NodeOperations import AddNodeOperation, PargraphRequest



class _Projection (object):
	"""
	_Projection wraps document model nodes in objects that provide methods for modifying and inspecting conents, and presentation
	"""
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




class TemplateNodeEventListener (TreeEventListenerObjectDispatch):
	"""
	Event listener for template node
	"""
	def __init__(self):
		pass

	
	@ObjectDispatchMethod( AddNodeOperation )
	def onAddNode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		
		return event.apply( node.getBody() )


TemplateNodeEventListener.instance = TemplateNodeEventListener()



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
	
		
	def templateEditorPresent(self, fragment, inheritedState):
		bodyView = InnerFragment( self.getBody() )
		
		w = bodyView
		w = w.withTreeEventListener( TemplateNodeEventListener.instance )
		w = w.withContextMenuInteractor( _templateContextMenuFactory )
		return w
		

	
def _templateContextMenuFactory(element, menu):
	rootElement = element.getRootElement()

	
	def makeStyleFn(style):
		def _onLink(link, event):
			caret = rootElement.getCaret()
			if caret.isValid():
				caret.getElement().postTreeEvent( PargraphRequest( style ) )
		return _onLink
	
	normalStyle = Hyperlink( 'Normal', makeStyleFn( 'normal' ) )
	h1Style = Hyperlink( 'H1', makeStyleFn( 'h1' ) )
	h2Style = Hyperlink( 'H2', makeStyleFn( 'h2' ) )
	h3Style = Hyperlink( 'H3', makeStyleFn( 'h3' ) )
	h4Style = Hyperlink( 'H4', makeStyleFn( 'h4' ) )
	h5Style = Hyperlink( 'H5', makeStyleFn( 'h5' ) )
	h6Style = Hyperlink( 'H6', makeStyleFn( 'h6' ) )
	titleStyle = Hyperlink( 'Title', makeStyleFn( 'title' ) )
	styles = ControlsRow( [ normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style, titleStyle ] )
	menu.add( SectionColumn( [ SectionTitle( 'Style' ), styles ] ).alignHExpand() )
	
	
	def _onPythonExpr(link, event):
		caret = rootElement.getCaret()
		if caret.isValid():
			caret.getElement().postTreeEvent( PythonExprRequest() )

	newExpr = Hyperlink( 'Python expression', _onPythonExpr )
	codeControls = ControlsRow( [ newExpr ] )
	menu.add( SectionColumn( [ SectionTitle( 'Code' ), codeControls ] ).alignHExpand() )

	return True
