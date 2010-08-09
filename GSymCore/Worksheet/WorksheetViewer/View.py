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

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import StyleSheet
from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Combinators.ContextMenu import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.PresCom import InnerFragment


from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution.ExecutionPresCombinators import executionResultBox, minimalExecutionResultBox

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet import ViewSchema
from GSymCore.Worksheet.WorksheetEditor.View import perspective as editorPerspective, WorksheetEditorSubject



_pythonCodeBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), None ) )
_pythonCodeEditorBorderStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 5.0, 20.0, 20.0, Color( 0.4, 0.5, 0.6 ), None ) )



def _worksheetContextMenuFactory(element, menu):
	def _onRefresh(button, event):
		model.refreshResults()

	model = element.getFragmentContext().getModel()

	refreshButton = Button.buttonWithLabel( 'Refresh', _onRefresh )
	worksheetControls = ControlsHBox( [ refreshButton ] )
	menu.add( SectionVBox( [ SectionTitle( 'Worksheet' ), worksheetControls ] ).alignHExpand() )
	return True




class WorksheetViewer (GSymViewObjectDispatch):
	@ObjectDispatchMethod( ViewSchema.WorksheetView )
	def Worksheet(self, ctx, inheritedState, node):
		bodyView = InnerFragment( node.getBody() )
		
		editLocation = ctx.getSubjectContext()['editLocation']
		
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		editLink = Hyperlink( 'Edit this worksheet', editLocation )
		linkHeader = SplitLinkHeaderBar( [ editLink ], [ homeLink ] )
		
		w = Page( [ linkHeader, bodyView ] )
		w = w.withContextMenuFactory( _worksheetContextMenuFactory )
		return StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( w )


	@ObjectDispatchMethod( ViewSchema.BodyView )
	def Body(self, ctx, inheritedState, node):
		contentViews = InnerFragment.map( [ c    for c in node.getContents()   if c.isVisible() ] )
		return Body( contentViews )
	
	
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
		return p


	
	@ObjectDispatchMethod( ViewSchema.PythonCodeView )
	def PythonCode(self, ctx, inheritedState, node):
		if node.isVisible():
			if node.isCodeVisible():
				codeView = Python25.python25EditorPerspective.applyTo( InnerFragment( node.getCode() ) )
				if node.isCodeEditable():
					codeView = StyleSheet.instance.withAttr( Primitive.editable, True ).applyTo( codeView )
			else:
				codeView = None
			
			executionResultView = None
			executionResult = node.getResult()
			if executionResult is not None:
				if node.isResultVisible():
					stdout = executionResult.getStdOut()
					result = executionResult.getResult()
				else:
					stdout = None
					result = None
				exc = executionResult.getCaughtException()
				if node.isCodeVisible():
					executionResultView = executionResultBox( stdout, executionResult.getStdErr(), exc, result, True, True )
				else:
					executionResultView = minimalExecutionResultBox( stdout, executionResult.getStdErr(), exc, result, True, True )
			
			if node.isResultMinimal():
				return executionResultView.alignHExpand()   if executionResultView is not None   else HiddenContent( '' )
			else:
				boxContents = []
				if node.isCodeVisible():
					boxContents.append( _pythonCodeBorderStyle.applyTo( Border( codeView.alignHExpand() ).alignHExpand() ) )
				if node.isResultVisible()  and  executionResultView is not None:
					boxContents.append( executionResultView.alignHExpand() )
				box = StyleSheet.instance.withAttr( Primitive.vboxSpacing, 5.0 ).applyTo( VBox( boxContents ) )
				
				return _pythonCodeEditorBorderStyle.applyTo( Border( box.alignHExpand() ).alignHExpand() )
		else:
			return HiddenContent( '' )





perspective = GSymPerspective( WorksheetViewer(), StyleSheet.instance, SimpleAttributeTable.instance, None )



class WorksheetViewerSubject (GSymSubject):
	def __init__(self, document, model, enclosingSubject, location):
		self._document = document
		self._modelView = ViewSchema.WorksheetView( None, model )
		self._enclosingSubject = enclosingSubject
		self._location = location
		self._editLocation = self._location + '.edit'
		
		self.edit = WorksheetEditorSubject( document, model, self, self._editLocation )


	def getFocus(self):
		return self._modelView
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Worksheet [view]'
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( location=self._location, editLocation=Location( self._editLocation ), viewLocation=Location( self._location ) )
	
	def getCommandHistory(self):
		return self._document.getCommandHistory()

	