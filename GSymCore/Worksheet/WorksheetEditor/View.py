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

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet, RichTextStyleSheet
from BritefuryJ.DocPresent.Controls import ControlsStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver


from GSymCore.Languages.Python25 import Python25

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema
from GSymCore.Worksheet.WorksheetEditor.WorksheetEditorStyleSheet import WorksheetEditorStyleSheet

from GSymCore.Worksheet.WorksheetEditor.TextStyle import *
from GSymCore.Worksheet.WorksheetEditor.PythonCode import *

from GSymCore.Worksheet.WorksheetEditor.TitleEditor import *
from GSymCore.Worksheet.WorksheetEditor.EmptyEditor import *
from GSymCore.Worksheet.WorksheetEditor.TextNodeEditor import *
from GSymCore.Worksheet.WorksheetEditor.WorksheetNodeEditor import *




class WorkSheetContextMenuFactory (ContextMenuFactory):
	def __init__(self, styleSheet):
		self._styleSheet = styleSheet
	
	
	def buildContextMenu(self, element, menu):
		menuStyle = self._styleSheet['contextMenuStyle']
		controlsStyle = menuStyle['controlsStyle']
		
		
		def makeStyleFn(style):
			def _onLink(link, event):
				caret = rootElement.getCaret()
				if caret.isValid():
					caret.getElement().postTreeEvent( TextStyleOperation( style ) )
			return _onLink
	
		rootElement = element.getRootElement()
		
		normalStyle = controlsStyle.link( 'Normal', makeStyleFn( 'normal' ) ).getElement()
		h1Style = controlsStyle.link( 'H1', makeStyleFn( 'h1' ) ).getElement()
		h2Style = controlsStyle.link( 'H2', makeStyleFn( 'h2' ) ).getElement()
		h3Style = controlsStyle.link( 'H3', makeStyleFn( 'h3' ) ).getElement()
		h4Style = controlsStyle.link( 'H4', makeStyleFn( 'h4' ) ).getElement()
		h5Style = controlsStyle.link( 'H5', makeStyleFn( 'h5' ) ).getElement()
		h6Style = controlsStyle.link( 'H6', makeStyleFn( 'h6' ) ).getElement()
		styles = menuStyle.controlsHBox( [ normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style ] )
		menu.add( menuStyle.sectionWithTitle( 'Style', styles ) )
		
		
		def _onPythonCode(link, event):
			caret = rootElement.getCaret()
			if caret.isValid():
				caret.getElement().postTreeEvent( NewPythonCodeRequest() )
	
		rootElement = element.getRootElement()
		
		newCode = controlsStyle.link( 'Python code', _onPythonCode ).getElement()
		codeControls = menuStyle.controlsHBox( [ newCode ] )
		menu.add( menuStyle.sectionWithTitle( 'Code', codeControls ) )



class WorksheetEditor (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, ctx, styleSheet, inheritedState, node):
		contentViews = ctx.mapPresentFragment( node.getContents(), styleSheet, inheritedState )
		emptyLine = PrimitiveStyleSheet.instance.paragraph( [ PrimitiveStyleSheet.instance.text( '' ) ] )
		emptyLine.addTreeEventListener( EmptyEventListener.instance )
		contentViews += [ emptyLine ]
		
		title = styleSheet.worksheetTitle( node.getTitle() )
		title.addTreeEventListener( TitleEventListener.instance )

		w = styleSheet.worksheet( title, contentViews )
		w.addTreeEventListener( WorksheetNodeEventListener.instance )
		w.addInteractor( WorksheetNodeInteractor.instance )
		w.addContextMenuFactory( instanceCache( WorkSheetContextMenuFactory, styleSheet ) )
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
		p.addTreeEventListener( TextNodeEventListener.instance )
		p.addInteractor( TextNodeInteractor.instance )
		return p


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, ctx, styleSheet, inheritedState, node):
		executionStyle = styleSheet['executionStyle']
		
		def _onSetStyle(style):
			node.setStyle( style )

		codeView = ctx.presentFragmentWithPerspective( node.getCode(), Python25.python25EditorPerspective )
		
		executionResultView = None
		executionResult = node.getResult()
		if executionResult is not None:
			if node.isResultVisible():
				stdout = executionResult.getStdOut()
				result = executionResult.getResult()
				resultView = ctx.presentFragmentWithGenericPerspective( result[0] )   if result is not None   else None
			else:
				stdout = None
				resultView = None
			exc = executionResult.getCaughtException()
			excView = ctx.presentFragmentWithGenericPerspective( exc )   if exc is not None   else None
			executionResultView = executionStyle.executionResult( stdout, executionResult.getStdErr(), excView, resultView )
		
		p = styleSheet.pythonCode( codeView, executionResultView, node.getStyle(), node.isResultVisible(), _onSetStyle )
		return p





class WorksheetEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			view = ViewSchema.WorksheetView( None, enclosingSubject.getFocus() )
			return enclosingSubject.withTitle( 'WS: ' + enclosingSubject.getTitle() ).withFocus( view )
		else:
			return None
	

	
perspective = GSymPerspective( WorksheetEditor(), WorksheetEditorStyleSheet.instance, AttributeTable.instance, None, WorksheetEditorRelativeLocationResolver() )

	