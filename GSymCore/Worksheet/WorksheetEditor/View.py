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

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet, RichTextStyleSheet
from BritefuryJ.DocPresent.Controls import ControlsStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver


from GSymCore.Languages.Python25 import Python25

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema
from GSymCore.Worksheet.WorksheetEditor.WorksheetEditorStyleSheet import WorksheetEditorStyleSheet
from GSymCore.Worksheet.WorksheetEditor.NodeEditor import *



_textInteractor = TextInteractor()
_worksheetInteractor = WorksheetInteractor()




_titleStyle = PrimitiveStyleSheet.instance.withForeground( Color( 0.0, 0.0, 0.5 ) ).withFontBold( True )
_optionsBoxStyle = PrimitiveStyleSheet.instance.withHBoxSpacing( 10.0 )
_controlsStyle = ControlsStyleSheet.instance

def _worksheetContextMenuFactory(element, menu):
	def makeStyleFn(style):
		def _onLink(link, event):
			rootElement.getCaret().getElement().postTreeEvent( ParagraphStyleEvent( style ) )
		return _onLink

	rootElement = element.getRootElement()
	
	styleTitle = _titleStyle.staticText( 'Style' )
	normalStyle = _controlsStyle.link( 'Normal', makeStyleFn( 'normal' ) ).getElement()
	h1Style = _controlsStyle.link( 'H1', makeStyleFn( 'h1' ) ).getElement()
	h2Style = _controlsStyle.link( 'H2', makeStyleFn( 'h2' ) ).getElement()
	h3Style = _controlsStyle.link( 'H3', makeStyleFn( 'h3' ) ).getElement()
	h4Style = _controlsStyle.link( 'H4', makeStyleFn( 'h4' ) ).getElement()
	h5Style = _controlsStyle.link( 'H5', makeStyleFn( 'h5' ) ).getElement()
	h6Style = _controlsStyle.link( 'H6', makeStyleFn( 'h6' ) ).getElement()
	styles = _optionsBoxStyle.hbox( [ normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style ] )
	menu.add( _optionsBoxStyle.vbox( [ styleTitle.alignHCentre(), styles ] ) )



class WorksheetEditor (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, ctx, styleSheet, inheritedState, node):
		contentViews = ctx.mapPresentFragment( node.getContents(), styleSheet, inheritedState )
		emptyLine = PrimitiveStyleSheet.instance.paragraph( [ PrimitiveStyleSheet.instance.text( '' ) ] )
		emptyLine.addTreeEventListener( EmptyTreeEventListener.instance )
		contentViews += [ emptyLine ]
		
		title = styleSheet.worksheetTitle( node.getTitle() )
		title.addTreeEventListener( TitleTextEditor.instance )

		w = styleSheet.worksheet( title, contentViews )
		w.addTreeEventListener( WorksheetTreeEventListener.instance )
		w.addInteractor( _worksheetInteractor )
		w.addContextMenuFactory( _worksheetContextMenuFactory )
		return w
	
	
	@ObjectDispatchMethod( ViewSchema.ParagraphView )
	def Paragraph(self, ctx, styleSheet, inheritedState, node):
		text = node.getText()
		style = node.getStyle()
		if style == 'normal':
			p = styleSheet.paragraph( text )
		elif style == 'h1':
			p = styleSheet.h1( text )
		elif style == 'h2':
			p = styleSheet.h2( text )
		elif style == 'h3':
			p = styleSheet.h3( text )
		elif style == 'h4':
			p = styleSheet.h4( text )
		elif style == 'h5':
			p = styleSheet.h5( text )
		elif style == 'h6':
			p = styleSheet.h6( text )
		p.addTreeEventListener( TextTreeEventListener.instance )
		p.addTreeEventListener( OperationTreeEventListener.instance )
		p.addInteractor( _textInteractor )
		return p


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, ctx, styleSheet, inheritedState, node):
		executionStyle = styleSheet['executionStyle']
		
		def _onShowCode(state):
			node.setShowCode( state )

		def _onCodeEditable(state):
			node.setCodeEditable( state )

		def _onShowResult(state):
			node.setShowResult( state )

		if node.getShowCode():
			codeView = ctx.presentFragmentWithPerspective( node.getCode(), Python25.python25EditorPerspective )
		else:
			codeView = None
		
		executionResultView = None
		executionResult = node.getResult()
		if executionResult is not None:
			if node.getShowResult():
				stdout = executionResult.getStdOut()
				result = executionResult.getResult()
				resultView = ctx.presentFragmentWithGenericPerspective( result[0] )   if result is not None   else None
			else:
				stdout = None
				resultView = None
			exc = executionResult.getCaughtException()
			excView = ctx.presentFragmentWithGenericPerspective( exc )   if exc is not None   else None
			executionResultView = executionStyle.executionResult( stdout, executionResult.getStdErr(), excView, resultView )
		
		p = styleSheet.pythonCode( codeView, executionResultView, node.getShowCode(), node.getCodeEditable(), node.getShowResult(), _onShowCode, _onCodeEditable, _onShowResult )
		return p





class WorksheetEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			view = ViewSchema.WorksheetView( None, enclosingSubject.getFocus() )
			return enclosingSubject.withTitle( 'WS: ' + enclosingSubject.getTitle() ).withFocus( view )
		else:
			return None
	

	
perspective = GSymPerspective( WorksheetEditor(), WorksheetEditorStyleSheet.instance, AttributeTable.instance, None, WorksheetEditorRelativeLocationResolver() )

	