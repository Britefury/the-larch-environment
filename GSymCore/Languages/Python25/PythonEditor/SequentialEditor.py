##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent.Clipboard import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.Editor.Sequential import SequentialClipboardHandler, SelectionEditTreeEvent


from Britefury.Util.NodeUtil import *


from BritefuryJ.Editor.Sequential import SequentialEditor

from GSymCore.Languages.Python25 import Schema

from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *





class PythonIndentationTreeEvent (EditEvent):
	pass

class PythonIndentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonDedentTreeEvent (PythonIndentationTreeEvent):
	pass

class IndentPythonSelectionTreeEvent (SelectionEditTreeEvent):
	def __init__(self, sequentialEditor, sourceElement):
		super( IndentPythonSelectionTreeEvent, self ).__init__( sequentialEditor, sourceElement )

class DedentPythonSelectionTreeEvent (SelectionEditTreeEvent):
	def __init__(self, sequentialEditor, sourceElement):
		super( DedentPythonSelectionTreeEvent, self ).__init__( sequentialEditor, sourceElement )

		
		
		
		
class PythonSequentialEditor (SequentialEditor):
	def __init__(self):
		super( PythonSequentialEditor, self ).__init__()
	
	
	def isEditEvent(self, event):
		return isinstance( event, PythonIndentationTreeEvent )
	
	
	def isClipboardEditLevelFragmentView(self, fragment):
		return isStmtFragment( fragment )  or  isTopLevelFragment( fragment )
		
	
	def filterTextForImport(self, text):
		return text.replace( '\t', '' )

	
	def joinStreamsForInsertion(self, subtreeRootFragment, before, insertion, after):
		return joinStreamsForInsertion( subtreeRootFragment, before, insertion, after )
	
	def joinStreamsForDeletion(self, subtreeRootFragment, before, after):
		return joinStreamsAroundDeletionPoint( before, after )
	
	def copyStructuralValue(self, x):
		return x.deepCopy()

	
	
	
	#
	#
	# INDENT AND DEDENT METHODS
	#
	#
	
	def indent(self, element, context, node):
		viewContext = context.getView()
		selection = viewContext.getSelection()
		
		if selection.isEmpty():
			self._indentLine( element, context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( element, context, node )
			else:
				self._indentSelection( selection )
			
			
			
	def dedent(self, element, context, node):
		viewContext = context.getView()
		selection = viewContext.getSelection()
		
		if selection.isEmpty():
			self._dedentLine( element, context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._dedentLine( element, context, node )
			else:
				self._dedentSelection( selection )
				
			
			
	def _indentLine(self, element, context, node):
		event = PythonIndentTreeEvent()
		visitor = event.getStreamValueVisitor()
		visitor.setElementPrefix( element, Schema.Indent() )
		visitor.setElementSuffix( element, Schema.Dedent() )
		bSuccess = element.postTreeEventToParent( event )
		if not bSuccess:
			print 'PythonSequentialEditor._indentLine(): INDENT LINE FAILED'
			
	
	
	def _dedentLine(self, element, context, node):
		suite = node.getValidParents()[0]
		suiteParent = suite.getValidParents()[0]
		if not suiteParent.isInstanceOf( Schema.PythonModule ):
			# This statement is not in the root node
			event = PythonDedentTreeEvent()
			visitor = event.getStreamValueVisitor()
			visitor.setElementPrefix( element, Schema.Dedent() )
			visitor.setElementSuffix( element, Schema.Indent() )
			bSuccess = element.postTreeEventToParent( event )
			if not bSuccess:
				print 'PythonSequentialEditor._dedentLine(): DEDENT LINE FAILED'
		else:
			print 'PythonSequentialEditor._dedentLine(): Attempted to dedent line in top-level module'
			
				
				
				
	def _indentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = getStatementContextFromElement( startMarker.getElement() )
		endContext = getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getFragmentContentElement()
		endStmtElement = endContext.getFragmentContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
		
		# Get the content element, not the fragment itself, otherwise editing operations that involve the module (top level) will trigger events that will NOT be caught
		rootElement = root.getFragmentContentElement()
				
		event = IndentPythonSelectionTreeEvent( self, rootElement )
		visitor = event.getStreamValueVisitor()
		visitor.ignoreElementFixedValuesOnPath( startContext.getFragmentContentElement(), rootElement )
		visitor.ignoreElementFixedValuesOnPath( endContext.getFragmentContentElement(), rootElement )
		visitor.setElementPrefix( startStmtElement, Schema.Indent() )
		visitor.setElementSuffix( endStmtElement, Schema.Dedent() )
		
		bSuccess = root.getFragmentContentElement().postTreeEvent( event )
		if not bSuccess:
			print 'PythonSequentialEditor._indentSelection(): INDENT SELECTION FAILED'
			
				
	
	
	def _dedentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = getStatementContextFromElement( startMarker.getElement() )
		endContext = getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getFragmentContentElement()
		endStmtElement = endContext.getFragmentContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
		
		# Get the content element, not the fragment itself, otherwise editing operations that involve the module (top level) will trigger events that will NOT be caught
		rootElement = root.getFragmentContentElement()
				
		startContext.getFragmentContentElement().clearFixedValuesOnPathUpTo( rootElement )
		endContext.getFragmentContentElement().clearFixedValuesOnPathUpTo( rootElement )
		
		event = DedentPythonSelectionTreeEvent( self, rootElement )
		visitor = event.getStreamValueVisitor()
		visitor.ignoreElementFixedValuesOnPath( startContext.getFragmentContentElement(), rootElement )
		visitor.ignoreElementFixedValuesOnPath( endContext.getFragmentContentElement(), rootElement )
		visitor.setElementPrefix( startStmtElement, Schema.Dedent() )
		visitor.setElementSuffix( endStmtElement, Schema.Indent() )
		
		bSuccess = rootElement.postTreeEvent( event )
		if not bSuccess:
			print 'PythonSequentialEditor._dedentSelection(): DEDENT SELECTION FAILED'
	
	
	
PythonSequentialEditor.instance = PythonSequentialEditor()


