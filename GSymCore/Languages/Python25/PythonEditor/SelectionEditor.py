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

from BritefuryJ.GSym.SequentialEditor import SequentialEditHandler, SequentialBuffer, SelectionEditTreeEvent


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations



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



		
class PythonIndentationTreeEvent (object):
	pass

class PythonIndentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonDedentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonSelectionEditTreeEvent (SelectionEditTreeEvent):
	def __init__(self, editHandler, sourceElement):
		super( PythonSelectionEditTreeEvent, self ).__init__( editHandler, sourceElement )

class IndentPythonSelectionTreeEvent (PythonSelectionEditTreeEvent):
	def __init__(self, editHandler, sourceElement):
		super( IndentPythonSelectionTreeEvent, self ).__init__( editHandler, sourceElement )

class DedentPythonSelectionTreeEvent (PythonSelectionEditTreeEvent):
	def __init__(self, editHandler, sourceElement):
		super( DedentPythonSelectionTreeEvent, self ).__init__( editHandler, sourceElement )



class _IndentationStreamValueFn (ElementValueFunction):
	def __init__(self, innerFn, prefix, suffix):
		self._innerFn = innerFn
		self._prefix = prefix
		self._suffix = suffix
		
	def computeElementValue(self, element):
		return element.getValue( self._innerFn )
	
	def addStreamValuePrefixToStream(self, builder, element):
		if self._innerFn is not None:
			self._innerFn.addStreamValuePrefixToStream( builder, element )
		if self._prefix is not None:
			builder.append( self._prefix )
		
	def addStreamValueSuffixToStream(self, builder, element):
		if self._suffix is not None:
			builder.append( self._suffix )
		if self._innerFn is not None:
			self._innerFn.addStreamValueSuffixToStream( builder, element )
			
			
def _addIndentationStreamValueFnToElement(element, prefix, suffix):
	oldFn = element.getValueFunction()
	element.setValueFunction( _IndentationStreamValueFn( oldFn, prefix, suffix ) )
	return oldFn
		
		

class Python25EditHandler (SequentialEditHandler):
	def __init__(self):
		super( Python25EditHandler, self ).__init__( _python25BufferDataFlavor )
		self._grammar = Python25Grammar()
		
		
	def isEditLevelFragmentView(self, fragment):
		return isStmtFragment( fragment )
	
	
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
		oldFn = _addIndentationStreamValueFnToElement( element, Schema.Indent(), Schema.Dedent() )
		bSuccess = element.postTreeEventToParent( PythonIndentTreeEvent() )
		if not bSuccess:
			print 'Python25EditHandler._indentLine(): INDENT LINE FAILED'
			element.setValueFunction( oldFn )
			
	
	
	def _dedentLine(self, element, context, node):
		suite = node.getValidParents()[0]
		suiteParent = suite.getValidParents()[0]
		if not suiteParent.isInstanceOf( Schema.PythonModule ):
			# This statement is not in the root node
			oldFn = _addIndentationStreamValueFnToElement( element, Schema.Dedent(), Schema.Indent() )
			bSuccess = element.postTreeEventToParent( PythonDedentTreeEvent() )
			if not bSuccess:
				print 'Python25EditHandler._dedentLine(): DEDENT LINE FAILED'
				element.setValueFunction( oldFn )
		else:
			print 'Python25EditHandler._dedentLine(): Attempted to dedent line in top-level module'
			
				
				
				
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
		
		oldStartFn = _addIndentationStreamValueFnToElement( startStmtElement, Schema.Indent(), None )
		oldEndFn = _addIndentationStreamValueFnToElement( endStmtElement, None, Schema.Dedent() )
		
		bSuccess = root.getFragmentContentElement().postTreeEvent( IndentPythonSelectionTreeEvent( self, rootElement ) )
		if not bSuccess:
			print 'Python25EditHandler._indentSelection(): INDENT SELECTION FAILED'
			startStmtElement.setValueFunction( oldStartFn )
			endStmtElement.setValueFunction( oldEndFn )
			
				
	
	
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
		
		oldStartFn = _addIndentationStreamValueFnToElement( startStmtElement, Schema.Dedent(), None )
		oldEndFn = _addIndentationStreamValueFnToElement( endStmtElement, None, Schema.Indent() )

		bSuccess = rootElement.postTreeEvent( DedentPythonSelectionTreeEvent( self, rootElement ) )
		if not bSuccess:
			print 'Python25EditHandler._dedentSelection(): DEDENT SELECTION FAILED'
			startStmtElement.setValueFunction( oldStartFn )
			endStmtElement.setValueFunction( oldEndFn )

			
		
			
	def joinStreamsForInsertion(self, subtreeRootFragment, before, insertion, after):
		return joinStreamsForInsertion( subtreeRootFragment, before, insertion, after )
	
	def joinStreamsForDeletion(self, subtreeRootFragment, before, after):
		return joinStreamsAroundDeletionPoint( before, after )
	
	def copyStructuralValue(self, x):
		return x.deepCopy()
	
	
	def createSelectionEditTreeEvent(self, sourceElement):
		return PythonSelectionEditTreeEvent( self, sourceElement )
	
	
	
	def canShareSelectionWith(self, editHandler):
		return False

