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
from Britefury.Util.InstanceCache import instanceCache

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import StyleSheet2, PrimitiveStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver


from GSymCore.Languages.Python25 import Python25

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema
from GSymCore.Worksheet.WorksheetViewer.WorksheetViewerStyleSheet import WorksheetViewerStyleSheet
from GSymCore.Worksheet.WorksheetEditor.View import perspective as editorPerspective



class WorkSheetContextMenuFactory (ContextMenuFactory):
	def __init__(self, styleSheet):
		self._styleSheet = styleSheet
	
	
	def buildContextMenu(self, element, menu):
		menuStyle = self._styleSheet['contextMenuStyle']
		controlsStyle = menuStyle['controlsStyle']
		
		
		def _onRefresh(button, event):
			model.refreshResults()
	
		model = element.getFragmentContext().getModel()

		refreshButton = controlsStyle.buttonWithLabel( 'Refresh', _onRefresh ).getElement()
		worksheetControls = menuStyle.controlsHBox( [ refreshButton ] )
		menu.add( menuStyle.sectionWithTitle( 'Worksheet', worksheetControls ).alignHExpand() )




class WorksheetViewer (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, ctx, styleSheet, inheritedState, node):
		bodyView = ctx.presentFragment( node.getBody(), styleSheet )
		
		w = styleSheet.worksheet( bodyView, ctx.getSubjectContext()['editLocation'] )
		w.addContextMenuFactory( instanceCache( WorkSheetContextMenuFactory, styleSheet ) )
		return w
	


	@ObjectDispatchMethod( ViewSchema.BodyView )
	def Body(self, ctx, styleSheet, inheritedState, node):
		contentViews = ctx.mapPresentFragment( [ c    for c in node.getContents()   if c.isVisible() ], styleSheet, inheritedState )
		
		return styleSheet.body( contentViews )
	
	
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
		elif style == 'title':
			p = styleSheet.title( text )
		return p


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, ctx, styleSheet, inheritedState, node):
		if node.isVisible():
			executionStyle = styleSheet['executionStyle']
			
			if node.isCodeVisible():
				if node.isCodeEditable():
					codeView = ctx.presentFragmentWithPerspectiveAndStyleSheet( node.getCode(), Python25.python25EditorPerspective, styleSheet['pythonStyle'] )
				else:
					codeView = ctx.presentFragmentWithPerspectiveAndStyleSheet( node.getCode(), Python25.python25EditorPerspective, styleSheet['staticPythonStyle'] )
					
			else:
				codeView = None
			
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
				if node.isCodeVisible():
					executionResultView = executionStyle.executionResult( stdout, executionResult.getStdErr(), excView, resultView )
				else:
					executionResultView = executionStyle.minimalExecutionResult( stdout, executionResult.getStdErr(), excView, resultView )
			
			if node.isResultMinimal():
				p = styleSheet.minimalPythonCodeResult( executionResultView )
			else:
				p = styleSheet.pythonCode( codeView, executionResultView, node.isCodeVisible(), node.isResultVisible() )
		
			return p
		else:
			return PrimitiveStyleSheet.instance.hiddenContent( '' )





class WorksheetViewerRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		editIterator = locationIterator.consumeLiteral( ':edit' )
		if editIterator is None  and  locationIterator.getSuffix() == '':
			view = ViewSchema.WorksheetView( None, enclosingSubject.getFocus() )
			subjectContext = enclosingSubject.getSubjectContext()
			editLocation = Location( locationIterator.getLocation().getLocationString() + ':edit' )
			return enclosingSubject.withTitle( 'WS: ' + enclosingSubject.getTitle() ).withFocus( view ).withSubjectContext( subjectContext.withAttrs( editLocation=editLocation ) )
		elif editIterator is not None:
			subjectContext = enclosingSubject.getSubjectContext()
			viewLocation = Location( locationIterator.getPrefix() )
			subject = enclosingSubject.withPerspective( editorPerspective ).withSubjectContext( subjectContext.withAttrs( viewLocation=viewLocation ) )
			return editorPerspective.resolveRelativeLocation( subject, editIterator )
	

	
perspective = GSymPerspective( WorksheetViewer(), StyleSheet2.instance, AttributeTable.instance, None, WorksheetViewerRelativeLocationResolver() )

	