##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from copy import deepcopy

from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace import EditEvent

from BritefuryJ.Util.RichString import RichStringBuilder

from BritefuryJ.Editor.Sequential import SelectionEditTreeEvent

from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingController

from BritefuryJ.ModelAccess.DocModel import ClassAttributeReader, ObjectFieldReader




from LarchCore.Languages.Python2 import Schema

from LarchCore.Languages.Python2.PythonEditor.Precedence import parensRequired, nodePrecedence
from LarchCore.Languages.Python2.PythonEditor.PythonEditOperations import pyReplaceNode, modifySuiteMinimisingChanges, isStmtFragment, isTopLevelFragment, \
	joinRichStringsForInsertion, joinRichStringsAroundDeletionPoint, getStatementContextFromElement, isTopLevel, getStatementContextPathsFromCommonRoot
from LarchCore.Languages.Python2.PythonEditor.PythonEditorCombinators import openParen, closeParen
from LarchCore.Languages.Python2.PythonEditor import Parser




#
#
# Python edit events
#
#


class PythonIndentationTreeEvent (EditEvent):
	pass

class PythonIndentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonDedentTreeEvent (PythonIndentationTreeEvent):
	pass

class IndentPythonSelectionTreeEvent (SelectionEditTreeEvent):
	def __init__(self, sequentialController, sourceElement):
		super( IndentPythonSelectionTreeEvent, self ).__init__( sequentialController, sourceElement )

class DedentPythonSelectionTreeEvent (SelectionEditTreeEvent):
	def __init__(self, sequentialController, sourceElement):
		super( DedentPythonSelectionTreeEvent, self ).__init__( sequentialController, sourceElement )





#
#
# Commit functions
#
#

def _isValidUnparsedStatementValue(value):
	# Unparsed statement is only valid if there is ONE newline, and it is at the end
	i = value.indexOf( '\n' )
	return i != -1   and   i == len( value ) - 1

def _commitUnparsedStatment(model, value):
	withoutNewline = value[:-1]
	unparsed = Schema.UnparsedStmt( value=Schema.UNPARSED( value=withoutNewline.getItemValues() ) )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	pyReplaceNode( model, deepcopy( unparsed ) )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	pyReplaceNode( model, deepcopy( unparsed ) )




def _isValidExprOrTargetOuterUnparsed(value):
	return '\n' not in value


def _commitExprOuterValid(model, parsed):
	expr = model['expr']
	if parsed != expr:
		model['expr'] = parsed

def _commitExprOuterEmpty(model, parsed):
	model['expr'] = Schema.UNPARSED( value=[ '' ] )

def _commitExprOuterUnparsed(model, value):
	values = value.getItemValues()
	if values == []:
		values = [ '' ]
	model['expr'] = Schema.UNPARSED( value=values )


def _commitTargetOuterValid(model, parsed):
	expr = model['target']
	if parsed != expr:
		model['target'] = parsed

def _commitTargetOuterEmpty(model, parsed):
	model['target'] = Schema.UNPARSED( value=[ '' ] )

def _commitTargetOuterUnparsed(model, value):
	values = value.getItemValues()
	if values == []:
		values = [ '' ]
	model['target'] = Schema.UNPARSED( value=values )



def _makeSuiteCommitFn(suite):
	def _commit(model, parsed):
		modifySuiteMinimisingChanges( suite, parsed )
	return _commit





#
# Precedence handler
#

_pythonPrecedenceHandler = PrecedenceHandler( ClassAttributeReader( parensRequired ), ObjectFieldReader( 'parens' ).stringToInteger( -1 ), ClassAttributeReader( nodePrecedence ), openParen, closeParen )




		
class PythonSyntaxRecognizingController (SyntaxRecognizingController):
	def __init__(self, name='Py2Edit'):
		super( PythonSyntaxRecognizingController, self ).__init__( name )

		self._grammar = Parser.Python2Grammar()


		self._expr = self.parsingEditFilter( 'Expression', self._grammar.expression(), pyReplaceNode )
		self._stmt = self.parsingEditFilter( 'Statement', self._grammar.simpleSingleLineStatementValid(), pyReplaceNode )
		self._compHdr = self.partialParsingEditFilter( 'Compound header', self._grammar.compoundStmtHeader() )
		self._stmtUnparsed = self.unparsedEditFilter( 'Unparsed statement', _isValidUnparsedStatementValue, _commitUnparsedStatment, _commitInnerUnparsed )
		self._topLevel = self.topLevelEditFilter()
		self._exprOuterValid = self.parsingEditFilter( 'Expression-outer-valid', self._grammar.tupleOrExpression(), _commitExprOuterValid, _commitExprOuterEmpty )
		self._exprOuterInvalid = self.unparsedEditFilter( 'Expression-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitExprOuterUnparsed )
		self._targetOuterValid = self.parsingEditFilter( 'Target-outer-valid', self._grammar.targetListOrTargetItem(), _commitTargetOuterValid, _commitTargetOuterEmpty )
		self._targetOuterInvalid = self.unparsedEditFilter( 'Target-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitTargetOuterUnparsed )

		self._expressionEditRule = self.editRule( _pythonPrecedenceHandler, [ self._expr ] )
		self._structuralExpressionEditRule = self.softStructuralEditRule( _pythonPrecedenceHandler, [ self._expr ] )
		self._unparsedEditRule = self.editRule( [ self._expr ] )
		self._statementEditRule = self.softStructuralEditRule( [ self._stmt, self._compHdr, self._stmtUnparsed ] )
		self._unparsedStatementEditRule = self.editRule( [ self._stmt, self._compHdr, self._stmtUnparsed ] )
		self._compoundStatementHeaderEditRule = self.softStructuralEditRule( [ self._compHdr, self._stmtUnparsed ] )
		self._specialFormStatementEditRule = self.softStructuralEditRule( [ self._stmt, self._compHdr, self._stmtUnparsed ] )
		self._targetTopLevelEditRule = self.softStructuralEditRule( _pythonPrecedenceHandler, [ self._targetOuterValid, self._targetOuterInvalid, self._topLevel ] )
		self._expressionTopLevelEditRule = self.softStructuralEditRule( _pythonPrecedenceHandler, [ self._exprOuterValid, self._exprOuterInvalid, self._topLevel ] )





	def _makeSuiteEditFilter(self, suite):
		return self.parsingEditFilter( 'Suite', self._grammar.suite(), _makeSuiteCommitFn( suite ) )

	def _makeCompoundSuiteEditFilter(self, suite):
		return self.parsingEditFilter( 'Suite', self._grammar.compoundSuite(), _makeSuiteCommitFn( suite ) )










	def isEditEvent(self, event):
		return isinstance( event, PythonIndentationTreeEvent )
	
	
	def isClipboardEditLevelFragmentView(self, fragment):
		return isStmtFragment( fragment )  or  isTopLevelFragment( fragment )
		
	
	def textToSequentialForImport(self, text):
		text = text.replace( '\t', '' )
		return RichStringBuilder( text ).richString()

	
	def joinRichStringsForInsertion(self, subtreeRootFragment, before, insertion, after):
		return joinRichStringsForInsertion( subtreeRootFragment, before, insertion, after )
	
	def joinRichStringsForDeletion(self, subtreeRootFragment, before, after):
		return joinRichStringsAroundDeletionPoint( before, after )

	
	
	
	#
	#
	# INDENT AND DEDENT METHODS
	#
	#
	
	def indent(self, element, fragment, node):
		viewContext = fragment.getView()
		selection = viewContext.getSelection()
		
		if selection is None  or  not isinstance( selection, TextSelection )  or  not selection.isValid():
			self._indentLine( element, fragment, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( element, fragment, node )
			else:
				self._indentSelection( selection )
			
			
			
	def dedent(self, element, fragment, node):
		viewContext = fragment.getView()
		selection = viewContext.getSelection()
		
		if selection is None  or  not isinstance( selection, TextSelection )  or  not selection.isValid():
			self._dedentLine( element, fragment, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._dedentLine( element, fragment, node )
			else:
				self._dedentSelection( selection )
				
			
			
	def _indentLine(self, element, fragment, node):
		event = PythonIndentTreeEvent()
		visitor = event.getRichStringVisitor()
		visitor.setElementPrefix( element, Schema.Indent() )
		visitor.setElementSuffix( element, Schema.Dedent() )
		bSuccess = element.postTreeEventToParent( event )
		if not bSuccess:
			print 'PythonSyntaxRecognizingController._indentLine(): INDENT LINE FAILED'
			
	
	
	def _dedentLine(self, element, fragment, node):
		suite = node.getParent()
		suiteParent = suite.getParent()
		if not isTopLevel( suiteParent ):
			# This statement is not within a top-level node
			event = PythonDedentTreeEvent()
			visitor = event.getRichStringVisitor()
			visitor.setElementPrefix( element, Schema.Dedent() )
			visitor.setElementSuffix( element, Schema.Indent() )
			bSuccess = element.postTreeEventToParent( event )
			if not bSuccess:
				print 'PythonSyntaxRecognizingController._dedentLine(): DEDENT LINE FAILED'
		else:
			print 'PythonSyntaxRecognizingController._dedentLine(): Attempted to dedent line in top-level module'
			
				
				
				
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
		visitor = event.getRichStringVisitor()
		visitor.ignoreElementFixedValuesOnPath( startContext.getFragmentContentElement(), rootElement )
		visitor.ignoreElementFixedValuesOnPath( endContext.getFragmentContentElement(), rootElement )
		visitor.setElementPrefix( startStmtElement, Schema.Indent() )
		visitor.setElementSuffix( endStmtElement, Schema.Dedent() )
		
		bSuccess = root.getFragmentContentElement().postTreeEvent( event )
		if not bSuccess:
			print 'PythonSyntaxRecognizingController._indentSelection(): INDENT SELECTION FAILED'
			
				
	
	
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
		visitor = event.getRichStringVisitor()
		visitor.ignoreElementFixedValuesOnPath( startContext.getFragmentContentElement(), rootElement )
		visitor.ignoreElementFixedValuesOnPath( endContext.getFragmentContentElement(), rootElement )
		visitor.setElementPrefix( startStmtElement, Schema.Dedent() )
		visitor.setElementSuffix( endStmtElement, Schema.Indent() )
		
		bSuccess = rootElement.postTreeEvent( event )
		if not bSuccess:
			print 'PythonSyntaxRecognizingController._dedentSelection(): DEDENT SELECTION FAILED'
	
	
	
PythonSyntaxRecognizingController.instance = PythonSyntaxRecognizingController( 'Py2Edit' )


