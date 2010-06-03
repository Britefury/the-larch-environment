##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
from datetime import datetime

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
from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction


from GSymCore.Languages.Python25 import Python25

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema
from GSymCore.Worksheet.WorksheetEditor.WorksheetEditorStyleSheet import WorksheetEditorStyleSheet
from GSymCore.Worksheet.WorksheetEditor.NodeEditor import *



_textInteractor = TextInteractor()
_worksheetInteractor = WorksheetInteractor()



class WorksheetEditor (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, ctx, styleSheet, inheritedState, node):
		contentViews = ctx.mapPresentFragment( node.getContents(), styleSheet, inheritedState )
		emptyLine = PrimitiveStyleSheet.instance.paragraph( [ PrimitiveStyleSheet.instance.text( '' ) ] )
		emptyLine.addTreeEventListener( TextEditEvent, EmptyTreeEventListener.newListener() )
		contentViews += [ emptyLine ]

		w = styleSheet.worksheet( node.getTitle(), contentViews )
		w.addTreeEventListener( InsertPythonCodeEvent, WorksheetTreeEventListener.newListener() )
		w.addInteractor( _worksheetInteractor )
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
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, ctx, styleSheet, inheritedState, node):
		executionStyle = styleSheet['executionStyle']
		
		def _onShowCode():
			node.setShowCode( not node.getShowCode() )
			return True

		def _onCodeEditable():
			node.setCodeEditable( not node.getCodeEditable() )
			return True

		def _onShowResult():
			node.setShowResult( not node.getShowResult() )
			return True

		if node.getShowCode():
			codeView = ctx.presentFragmentWithPerspective( node.getCode(), Python25.python25EditorPerspective )
		else:
			codeView = None
		
		executionResultView = None
		if node.getShowResult():
			executionResult = node.getResult()
			if executionResult is not None:
				exc = executionResult.getCaughtException()
				result = executionResult.getResult()
				excView = ctx.presentFragmentWithGenericPerspective( exc )   if exc is not None   else None
				resultView = ctx.presentFragmentWithGenericPerspective( result[0] )   if result is not None   else None
				executionResultView = executionStyle.executionResult( executionResult.getStdOut(), executionResult.getStdErr(), excView, resultView )
		
		p = styleSheet.pythonCode( codeView, executionResultView, node.getShowCode(), node.getCodeEditable(), node.getShowResult(), _onShowCode, _onCodeEditable, _onShowResult )
		return p





class WorksheetEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			view = ViewSchema.WorksheetView( None, enclosingSubject.getFocus() )
			return enclosingSubject.withTitle( 'WS: ' + enclosingSubject.getTitle() ).withFocus( view )
		else:
			return None
	

	
_viewFn = PyGSymViewFragmentFunction( WorksheetEditor() )
perspective = GSymPerspective( _viewFn, WorksheetEditorStyleSheet.instance, AttributeTable.instance, None, WorksheetEditorRelativeLocationResolver() )

	