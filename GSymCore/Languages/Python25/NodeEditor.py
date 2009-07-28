##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from weakref import WeakValueDictionary

from java.lang import Object
from java.io import IOException
from java.util import List
from java.awt.event import KeyEvent

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction

from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations


from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Parser import Python25Grammar
from GSymCore.Languages.Python25.Precedence import *
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25.StructureUnparser import Python25StructureUnparser
from GSymCore.Languages.Python25.StructureParser import Python25StructureGrammar
from GSymCore.Languages.Python25.PythonEditOperations import *



_structureParser = Python25StructureGrammar()




class _ReplaceSimpleStatementUnparser (Python25StructureUnparser):
	def __init__(self, target, replacementRange):
		super( _ReplaceSimpleStatementUnparser, self ).__init__()
		self._target = target
		self._replacementRange = replacementRange
		
		
	def _handleHeader(self, node, header):
		if node is self._target:
			return self._replacementRange
		return super( _ReplaceSimpleStatementUnparser, self )._handleHeader( node, header )
	
	

class _ReplaceCompoundHeaderUnparser (Python25StructureUnparser):
	def __init__(self, compoundNode, headerIndex, replacementRange):
		super( _ReplaceCompoundHeaderUnparser, self ).__init__()
		self._compoundNode = compoundNode
		self._headerIndex = headerIndex
		self._replacementRange = replacementRange
		self._headerCount = 0
		
		
	def _handleHeader(self, node, header):
		if node is self._compoundNode:
			if self._headerCount == self._headerIndex:
				return self._replacementRange
			self._headerCount += 1
		return super( _ReplaceCompoundHeaderUnparser, self )._handleHeader( node, header )
	


	
	
def _rebuildSuite(suite, unparser):
	linear = reduce( lambda a, b: a + b, [ unparser( node )   for node in suite ] )
	reparsed = _structureParser.suite().parseListItems( linear )
	
	if reparsed.isValid()  and  reparsed.getEnd() == len( linear ):
		reparsed = reparsed.getValue()
	else:
		raise ValueError, 'NodeEditor:_rebuildSuite(): structure parse failed'
	
	return performSuiteEdits( suite, reparsed )


def _rebuildParentSuite(target, unparser):
	parent = target.getParentTreeNode()
	if parent is None:
		raise TypeError, 'NodeEditor:_rebuildParentSuite(): no parent '
	suite = parent.getNode()

	unparser = _ReplaceSimpleStatementUnparser( target.getNode(), replacement )
	return _rebuildSuite( suite, unparser )
	

def _buildLineEditStructure(statements):
	if len( statements ) > 1  and  isCompoundStmtHeader( statements[0] ):
		return [ statements[0], Nodes.Indent() ]  +  statements[1:]  +  [ Nodes.Dedent() ]
	else:
		return statements


def pyReplaceSimpleStmtWithCompoundRange(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		unparser = _ReplaceSimpleStatementUnparser( target.getNode(), replacement )
		return _rebuildParentSuite( target, unparser )
	else:
		raise TypeError, 'NodeEditor:pyReplaceSimpleStmtWithCompoundRange(): @target must be a DocTreeNode'
	
	
def pyReplaceSimpleStatementWithRange(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		if len( replacement ) == 1:
			if isCompoundStmtHeader( replacement[0] ):
				return pyReplaceSimpleStmtWithCompoundRange( ctx, target, replacement )
			else:
				return [ pyReplaceStmt( ctx, target, replacement[0] ) ]
		elif len( replacement ) == 2:
			if isCompoundStmtHeader( replacement[0] )  or  isCompoundStmtHeader( replacement[1] ):
				replacement = _buildLineEditStructure( replacement )
				return pyReplaceSimpleStmtWithCompoundRange( ctx, target, replacement )
			else:
				xs = EditOperations.insertRangeBefore( ctx, target, replacement[:-1] )
				xs += [ pyReplaceStmt( ctx, target, replacement[-1], False ) ]
				return xs
		else:
			raise ValueError, 'NodeEditor:pyReplaceSimpleStatementWithRange: replacement range must contain 1 or 2 statements'
	else:
		raise TypeError, 'NodeEditor:pyReplaceSimpleStatementWithRange(): @target must be a DocTreeNode'



	
def pyReplaceCompoundStatementHeaderWithRange(ctx, target, headerIndex, replacement):
	if isinstance( target, DocTreeNode ):
		replacement = _buildLineEditStructure( replacement )
		unparser = _ReplaceCompoundHeaderUnparser( target.getNode(), headerIndex, replacement )
		return _rebuildParentSuite( target, unparser )
	else:
		raise TypeError, 'NodeEditor:pyReplaceCompoundStatementHeaderWithRange(): @target must be a DocTreeNode'



	
	
#
#
# EDIT LISTENERS
#
#

class _ListenerTable (object):
	def __init__(self, createFn):
		self._table = WeakValueDictionary()
		self._createFn = createFn
	
		
	def get(self, *args):
		key = args
		try:
			return self._table[key]
		except KeyError:
			listener = self._createFn( *args )
			self._table[key] = listener
			return listener
		
	
	
class ParsedExpressionTextRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser', '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence, node=None):
		#super( ParsedExpressionTextRepresentationListener, self ).__init__()
		self._parser = parser
		self._outerPrecedence = outerPrecedence

	def textRepresentationModified(self, element):
		value = element.getTextRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		if '\n' not in value:
			parsed = parseText( self._parser, value, self._outerPrecedence )
			if parsed is not None:
				if parsed != node:
					pyReplaceExpression( ctx, node, parsed )
			else:
				pyReplaceExpression( ctx, node, Nodes.UNPARSED( value=value ) )
			return True
		else:
			return False
		
	
	_listenerTable = None
		
	@staticmethod
	def newListener(parser, outerPrecedence):
		if ParsedExpressionTextRepresentationListener._listenerTable is None:
			ParsedExpressionTextRepresentationListener._listenerTable = _ListenerTable( ParsedExpressionTextRepresentationListener )
		return ParsedExpressionTextRepresentationListener._listenerTable.get( parser, outerPrecedence )
		
		


class _LineTextRepresentationListenerWithParser (ElementLinearRepresentationListener):
	__slots__ = [ '_parser' ]

	
	def __init__(self, parser):
		self._parser = parser



	def parseLines(self, lineStrings):
		result = []
		# For each line
		for i, line in enumerate( lineStrings ):
			if line.strip() == '':
				# Blank line
				result.append( Nodes.BlankLine() )
			else:
				# Parse
				parsed = parseText( self._parser, line.strip() )
				if parsed is None:
					# Parse failure; unparsed text
					result.append( Nodes.UNPARSED( value=line ) )
				else:
					result.append( parsed )
		return result


	
	
class _StatementTextRepresentationListener (_LineTextRepresentationListenerWithParser):
	def textRepresentationModified(self, element):
		ctx = element.getContext()
		node = ctx.getTreeNode()
		# Get the content
		value = element.getTextRepresentation()
		self.handleContent( ctx, node, value )


	def handleContent(self, ctx, node, value):
		# Split into lines
		lineStrings = value.split( '\n' )
		# Parse
		parsedLines = self.parseLines( lineStrings )
		
		self.handleParsedLines( ctx, node, value, parsedLines )
		
		
	def handleParsedLines(self, ctx, node, value, parsedLines):
		assert False, 'abstract'
			
			
			
			
class SimpleStatementTextRepresentationListener (_StatementTextRepresentationListener):
	def handleParsedLines(self, ctx, node, value, parsedLines):
		if len( parsedLines ) > 2:
			print parsedLines, value.replace( '\n', '\\n' )
		assert len( parsedLines ) == 1  or  len( parsedLines ) == 2, 'Simple statement edit should not yield %d lines'  %  ( len( parsedLines ), )
		if len( parsedLines ) == 2:
			assert not ( isCompoundStmtHeader( parsedLines[0] )  and  isCompoundStmtHeader( parsedLines[1] ) )
		pyReplaceSimpleStatementWithRange( ctx, node, parsedLines )

			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if SimpleStatementTextRepresentationListener._listenerTable is None:
			SimpleStatementTextRepresentationListener._listenerTable = _ListenerTable( SimpleStatementTextRepresentationListener )
		return SimpleStatementTextRepresentationListener._listenerTable.get( parser )
			
			
			
			
class CompoundStatementTextRepresentationListener (_StatementTextRepresentationListener):
	def __init__(self, parser, headerIndex):
		super( CompoundStatementTextRepresentationListener, self ).__init__( parser )
		self._headerIndex = headerIndex

		
	def handleParsedLines(self, ctx, node, value, parsedLines):
		assert len( parsedLines ) == 1  or  len( parsedLines ) == 2
		if len( parsedLines ) == 2:
			assert not ( isCompoundStmtHeader( parsedLines[0] )  and  isCompoundStmtHeader( parsedLines[1] ) )
		pyReplaceCompoundStatementHeaderWithRange( ctx, node, self._headerIndex, parsedLines )

			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser, headerIndex):
		if CompoundStatementTextRepresentationListener._listenerTable is None:
			CompoundStatementTextRepresentationListener._listenerTable = _ListenerTable( CompoundStatementTextRepresentationListener )
		return CompoundStatementTextRepresentationListener._listenerTable.get( parser, headerIndex )
			
			
			
			
class _StatementNewLineTextRepresentationListener (_LineTextRepresentationListenerWithParser):
	def _getNextStatementContext(self, statementContext):
		while statementContext is not None:
			nextContext = statementContext.getNextSibling()
			if nextContext is not None:
				return nextContext

			parentContext = getParentStatementContext( statementContext )
			suite = parentContext.getTreeNode()['suite']
			
			statementContext = parentContext
		
		return None


	
	
	
	
	
class SimpleStatementNewLineTextRepresentationListener (_StatementNewLineTextRepresentationListener):
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if SimpleStatementNewLineTextRepresentationListener._listenerTable is None:
			SimpleStatementNewLineTextRepresentationListener._listenerTable = _ListenerTable( SimpleStatementNewLineTextRepresentationListener )
		return SimpleStatementNewLineTextRepresentationListener._listenerTable.get( parser )
	
	
	def textRepresentationModified(self, element):
		# Get the content
		value = element.getTextRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		if value == '':
			# Newline has been deleted
			statementContext = getStatementContextFromElement( element )
			statementTextRep = statementContext.getViewNodeContentElement().getTextRepresentation().strip( '\n' )
			
				
			# Join the statement with the one after
			nextContext = self._getNextStatementContext( statementContext )
			
			if nextContext is not None:
				nextTextRep = nextContext.getViewNodeContentElement().getTextRepresentation().strip( '\n' )
				# Next statement is a compound statement; get the first line of text
				if isCompoundStmt( nextContext.getDocNode() ):
					nextTextRep = nextTextRep.split( '\n' )[0]
				value = statementTextRep + nextTextRep
		
				# Split into lines
				lineStrings = value.split( '\n' )
				assert len( lineStrings ) == 1, 'SimpleStatementNewLineTextRepresentationListener.textRepresentationModified(): combined text should consist of 1 line only'
				
				# Parse
				parsedLines = self.parseLines( lineStrings )

				if getParentStatementContext( statementContext ) is getParentStatementContext( nextContext ):
					# Siblings
					
					if not isCompoundStmt( nextContext.getDocNode() )  and  not isCompoundStmtHeader( parsedLines[0] ):
						# Target node is not compound
						# Next node is not compound
						# Replacement node is not a compound header
						
						pyReplaceStmt( statementContext, statementContext.getTreeNode(), parsedLines[0] )
						EditOperations.remove( nextContext, nextContext.getTreeNode() )
					else:
						# Target node is not compound
						if isCompoundStmt( nextContext.getDocNode() ):
							# Next node is compound
							EditOperations.remove( statementContext, statementContext.getTreeNode() )
							pyReplaceCompoundStatementHeaderWithRange( nextContext, nextContext.getTreeNode(), 0, parsedLines )
						else:
							# Next node is not compound
							EditOperations.remove( statementContext, statementContext.getTreeNode() )
							pyReplaceSimpleStatementWithRange( nextContext, nextContext.getTreeNode(), parsedLines )
				else:
					# Target node is not compound
					
					# TODO
					# TODO
					# TODO
					# TODO
					# Not siblings
					if isCompoundStmt( nextContext.getDocNode() ):
						block = Nodes.IndentedBlock( suite=nextContext.getDocNode()['suite'] )
						EditOperations.replace( nextContext, nextContext.getTreeNode(), block )
					else:
						EditOperations.remove( nextContext, nextContext.getTreeNode() )
					
					EditOperations.replaceWithRange( statementContext, statementContext.getTreeNode(), parsedLines )

					
					


class CompoundStatementNewLineTextRepresentationListener (_StatementNewLineTextRepresentationListener):
	def __init__(self, parser, headerIndex, suiteElement):
		super( CompoundStatementNewLineTextRepresentationListener, self ).__init__( parser )
		self._headerIndex = headerIndex
		self._suiteElement = suiteElement
	
	
	def textRepresentationModified(self, element):
		# Get the content
		value = element.getTextRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		if value == '':
			# Newline has been deleted
			statementContext = getStatementContextFromElement( element )
			statementTextRep = statementContext.getViewNodeContentElement().getTextRepresentation().strip( '\n' )
			
			if isCompoundStmt( statementContext.getDocNode() ):
				# Edited new-line that is from the header of a compound statement, join the first statement
				if len( statementContext.getDocNode()['suite'] ) > 0:
					# The statement is a compound statement, and has at least 1 child; join with first child
					# The new line between the header and the first child statement will have been removed by the delete operation, so the first line will be the combined header/child
					value = statementTextRep.split( '\n' )[0]
			
					# Split into lines
					lineStrings = value.split( '\n' )
					# Parse
					parsedLines = self.parseLines( lineStrings )
					
					nextDocNode = statementContext.getDocNode()['suite'][0]
					nextTreeNode = statementContext.getTreeNode()['suite'][0]

					if isCompoundStmt( nextTreeNode ):
						block = Nodes.IndentedBlock( suite=nextDocNode['suite'] )
						EditOperations.replace( statementContext, nextTreeNode, block )
					else:
						EditOperations.remove( statementContext, nextTreeNode )
					
					if isCompoundStmt( parsedLines[-1] ):
						comp = parsedLines[-1]
						comp['suite'] = statementContext.getDocNode()['suite']
					else:
						if len( statementContext.getDocNode()['suite'] ) > 0:
							comp = Nodes.IndentedBlock()
							parsedLines.append( comp )
							comp['suite'] = statementContext.getDocNode()['suite']

					EditOperations.replaceWithRange( statementContext, statementContext.getTreeNode(), parsedLines )
					return
				

				# The statement is a compound statement and has no children; join with next statement; fall through
				
				
			# Join the statement with the one after
			nextContext = self._getNextStatementContext( statementContext )
			
			if nextContext is not None:
				nextTextRep = nextContext.getViewNodeContentElement().getTextRepresentation().strip( '\n' )
				if isCompoundStmt( nextContext.getDocNode() ):
					nextTextRep = nextTextRep.split( '\n' )[0]
				value = statementTextRep + nextTextRep
		
				# Split into lines
				lineStrings = value.split( '\n' )
				# Parse
				parsedLines = self.parseLines( lineStrings )

				if getParentStatementContext( statementContext ) is _getParentStatementContext( nextContext ):
					# Siblings
					if isCompoundStmt( nextContext.getDocNode() ):
						if isCompoundStmt( parsedLines[-1] ):
							comp = parsedLines[-1]
						else:
							comp = Nodes.IndentedBlock()
							parsedLines.append( comp )
						
						comp['suite'] = nextContext.getDocNode()['suite']
					
					EditOperations.replaceWithRange( statementContext, statementContext.getTreeNode(), parsedLines )
					EditOperations.remove( nextContext, nextContext.getTreeNode() )
				else:
					# Not siblings
					if isCompoundStmt( nextContext.getDocNode() ):
						block = Nodes.IndentedBlock( suite=nextContext.getDocNode()['suite'] )
						EditOperations.replace( nextContext, nextContext.getTreeNode(), block )
					else:
						EditOperations.remove( nextContext, nextContext.getTreeNode() )
					
					EditOperations.replaceWithRange( statementContext, statementContext.getTreeNode(), parsedLines )

					
					
	

class StatementKeyboardListener (ElementKeyboardListener):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getContext()
			node = context.getTreeNode()
			
			if event.getModifiers() & KeyEvent.SHIFT_MASK  !=  0:
				context.getViewContext().getEditHandler().dedent( context, node )
			else:
				context.getViewContext().getEditHandler().indent( context, node )
			return True
		else:
			return False
		
		
	def onKeyPress(self, element, event):
		return False
	
	def onKeyRelease(self, element, event):
		return False
	
	
	
