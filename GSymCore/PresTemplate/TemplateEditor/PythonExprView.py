##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakKeyDictionary

from java.awt import Color

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.Cell import Cell

from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.StyleSheet import *

from BritefuryJ.GSym.PresCom import InnerFragment

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Languages.Python25 import Python25

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView

from GSymCore.PresTemplate.TemplateEditor.NodeOperations import NodeRequest


_pythonExprBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_pythonExprHeaderStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.3, 0.6 ) ).withAttr( Primitive.fontSize, 10 )




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


	
	def templateEditorPresent(self, fragment, inheritedState):
		def _onDeleteButton(button, event):
			button.getElement().postTreeEvent( DeleteNodeOperation( node ) )

		deleteButton = Button( Image.systemIcon( 'delete_tiny' ), _onDeleteButton )
		headerBox = _pythonExprHeaderStyle.applyTo( Row( [ Label( 'Py expr' ).alignHLeft(), deleteButton.alignHRight() ] ) )
		
		codeView = Python25.python25EditorPerspective.applyTo( InnerFragment( self.getCode() ) )
		
		box = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( Column( [ headerBox.alignHExpand(), codeView.alignHExpand() ] ) )
		
		p = _pythonExprBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )

		p = p.withFixedValue( self.getModel() )
		p = p.withTreeEventListener( PythonExprNodeEventListener.instance )
		return p




class PythonExprRequest (NodeRequest):
	def applyToParagraphNode(self, paragraph, element):
		return self._insertAfter( paragraph, element )
		
	def applyToPythonExprNode(self, pythonExpr, element):
		return self._insertAfter( pythonExpr, element )
	
	def _createModel(self):
		return PythonExprView.newPythonExprModel()




class PythonExprNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass


	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToPythonExprNode( element.getFragmentContext().getModel(), element )


PythonExprNodeEventListener.instance = PythonExprNodeEventListener()		

