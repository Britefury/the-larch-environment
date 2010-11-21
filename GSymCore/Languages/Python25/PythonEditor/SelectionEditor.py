##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocPresent.Clipboard import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.SequentialEditor import SequentialClipboardHandler, SequentialBuffer, SelectionEditTreeEvent


from Britefury.Util.NodeUtil import *



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *




class NotImplementedError (Exception):
	pass


class Python25Buffer (SequentialBuffer):
	pass



_python25BufferDataFlavor = LocalDataFlavor( Python25Buffer )



		
class PythonIndentationTreeEvent (EditEvent):
	pass

class PythonIndentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonDedentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonSelectionEditTreeEvent (SelectionEditTreeEvent):
	def __init__(self, clipboardHandler, sourceElement):
		super( PythonSelectionEditTreeEvent, self ).__init__( clipboardHandler, sourceElement )

class IndentPythonSelectionTreeEvent (PythonSelectionEditTreeEvent):
	def __init__(self, clipboardHandler, sourceElement):
		super( IndentPythonSelectionTreeEvent, self ).__init__( clipboardHandler, sourceElement )

class DedentPythonSelectionTreeEvent (PythonSelectionEditTreeEvent):
	def __init__(self, clipboardHandler, sourceElement):
		super( DedentPythonSelectionTreeEvent, self ).__init__( clipboardHandler, sourceElement )



class Python25ClipboardHandler (SequentialClipboardHandler):
	def __init__(self):
		super( Python25ClipboardHandler, self ).__init__( _python25BufferDataFlavor )
		self._grammar = Python25Grammar()
		
		
	def isEditLevelFragmentView(self, fragment):
		return isStmtFragment( fragment )  or  isTopLevelFragment( fragment )
	
	
	def createSelectionBuffer(self, stream):
		return Python25Buffer( stream )
	
	
	def filterTextForImport(self, text):
		return text.replace( '\t', '' )
		
		
			
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
			print 'Python25ClipboardHandler._indentLine(): INDENT LINE FAILED'
			
	
	
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
				print 'Python25ClipboardHandler._dedentLine(): DEDENT LINE FAILED'
		else:
			print 'Python25ClipboardHandler._dedentLine(): Attempted to dedent line in top-level module'
			
				
				
				
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
				
		startContext.getFragmentContentElement().clearFixedValuesOnPathUpTo( rootElement )
		endContext.getFragmentContentElement().clearFixedValuesOnPathUpTo( rootElement )
		
		event = IndentPythonSelectionTreeEvent( self, rootElement )
		visitor = event.getStreamValueVisitor()
		visitor.setElementPrefix( startStmtElement, Schema.Indent() )
		visitor.setElementSuffix( endStmtElement, Schema.Dedent() )
		
		bSuccess = root.getFragmentContentElement().postTreeEvent( event )
		if not bSuccess:
			print 'Python25ClipboardHandler._indentSelection(): INDENT SELECTION FAILED'
			
				
	
	
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
		visitor.setElementPrefix( startStmtElement, Schema.Dedent() )
		visitor.setElementSuffix( endStmtElement, Schema.Indent() )
		
		bSuccess = rootElement.postTreeEvent( event )
		if not bSuccess:
			print 'Python25ClipboardHandler._dedentSelection(): DEDENT SELECTION FAILED'

			
		
			
	def joinStreamsForInsertion(self, subtreeRootFragment, before, insertion, after):
		return joinStreamsForInsertion( subtreeRootFragment, before, insertion, after )
	
	def joinStreamsForDeletion(self, subtreeRootFragment, before, after):
		return joinStreamsAroundDeletionPoint( before, after )
	
	def copyStructuralValue(self, x):
		return x.deepCopy()
	
	
	def createSelectionEditTreeEvent(self, sourceElement):
		return PythonSelectionEditTreeEvent( self, sourceElement )
	
	
	
	def canShareSelectionWith(self, clipboardHandler):
		return False

