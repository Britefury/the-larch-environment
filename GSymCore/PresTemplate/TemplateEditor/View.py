##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
from datetime import datetime

from java.awt import Color
from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *
from Britefury.Util.InstanceCache import instanceCache

from BritefuryJ.AttributeTable import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.PresCom import InnerFragment, LocationAsInnerFragment


from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution.ExecutionPresCombinators import executionResultBox, minimalExecutionResultBox

from GSymCore.PresTemplate import Schema
from GSymCore.PresTemplate import ViewSchema

from GSymCore.PresTemplate.TemplateEditor.PythonExpr import *

from GSymCore.PresTemplate.TemplateEditor.TextNodeEditor import *
from GSymCore.PresTemplate.TemplateEditor.BodyNodeEditor import *
from GSymCore.PresTemplate.TemplateEditor.TemplateNodeEditor import *

from GSymCore.PresTemplate.TemplateEditor.SelectionEditor import *

from BritefuryJ.Controls import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Combinators import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Combinators.ContextMenu import *



_pythonExprBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_pythonExprHeaderStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.3, 0.6 ) ).withAttr( Primitive.fontSize, 10 )

_paragraphStyle = StyleSheet.instance.withAttr( RichText.appendNewlineToParagraphs, True )

		
		
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
	styles = ControlsHBox( [ normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style, titleStyle ] )
	menu.add( SectionVBox( [ SectionTitle( 'Style' ), styles ] ).alignHExpand() )
	
	
	def _onPythonExpr(link, event):
		caret = rootElement.getCaret()
		if caret.isValid():
			caret.getElement().postTreeEvent( PythonExprRequest() )

	newExpr = Hyperlink( 'Python expression', _onPythonExpr )
	codeControls = ControlsHBox( [ newExpr ] )
	menu.add( SectionVBox( [ SectionTitle( 'Code' ), codeControls ] ).alignHExpand() )

	return True



class TemplateEditor (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.TemplateView )
	def Template(self, ctx, inheritedState, node):
		bodyView = InnerFragment( node.getBody() )
		
		w = bodyView
		w = w.withTreeEventListener( TemplateNodeEventListener.instance )
		w = w.withContextMenuFactory( _templateContextMenuFactory )
		return w
	
	
	@ObjectDispatchMethod( ViewSchema.BodyView )
	def Body(self, ctx, inheritedState, node):
		emptyLine = Paragraph( [ Text( '' ) ] )
		emptyLine = emptyLine.withTreeEventListener( EmptyEventListener.instance )
		contentViews = list( InnerFragment.map( node.getContents() ) )  +  [ emptyLine ]
		
		w = Body( contentViews )
		w = w.withTreeEventListener( BodyNodeEventListener.instance )
		return w
	
	
	@ObjectDispatchMethod( ViewSchema.ParagraphView )
	def Paragraph(self, ctx, inheritedState, node):
		text = node.getText()
		style = node.getStyle()
		if style == 'normal':
			p = NormalText( text )
		elif style == 'h1':
			p = Heading1( text )
		elif style == 'h2':
			p = Heading2( text )
		elif style == 'h3':
			p = Heading3( text )
		elif style == 'h4':
			p = Heading4( text )
		elif style == 'h5':
			p = Heading5( text )
		elif style == 'h6':
			p = Heading6( text )
		elif style == 'title':
			p = TitleBar( text )
		p = _paragraphStyle.applyTo( p )
		p = withParagraphStreamValueFn( p, node.partialModel() )
		w = Span( [ p ] )
		w = w.withTreeEventListener( TextNodeEventListener.instance )
		w = w.withInteractor( TextNodeInteractor.instance )
		w = w.withFixedValue( node.getModel() )
		return w


	
	@ObjectDispatchMethod( ViewSchema.PythonExprView )
	def PythonExpr(self, ctx, inheritedState, node):
		def _onDeleteButton(button, event):
			button.getElement().postTreeEvent( DeleteNodeOperation( node ) )

		deleteButton = Button( Image.systemIcon( 'delete_tiny' ), _onDeleteButton )
		headerBox = _pythonExprHeaderStyle.applyTo( HBox( [ Label( 'Py expr' ).alignHLeft(), deleteButton.alignHRight() ] ) )
		
		codeView = Python25.python25EditorPerspective.applyTo( InnerFragment( node.getCode() ) )
		
		box = StyleSheet.instance.withAttr( Primitive.vboxSpacing, 5.0 ).applyTo( VBox( [ headerBox.alignHExpand(), codeView.alignHExpand() ] ) )
		
		p = _pythonExprBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )

		p = p.withFixedValue( node.getModel() )
		p = p.withTreeEventListener( PythonExprNodeEventListener.instance )
		return p





perspective = GSymPerspective( TemplateEditor(), TemplateEditHandler() )
