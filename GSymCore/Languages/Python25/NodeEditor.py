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




class _CompoundHeaderUnparser (Python25StructureUnparser):
	def __init__(self, compoundNode, headerIndex):
		super( _CompoundHeaderUnparser, self ).__init__()
		self._compoundNode = compoundNode
		self._headerIndex = headerIndex
		self._headerCount = 0
		self.targetHeader = None
		
		
	def _handleHeaderSuite(self, node, header, suite):
		if node is self._compoundNode:
			if self._headerCount == self._headerIndex:
				self.targetHeader = header
			self._headerCount += 1
		return super( _CompoundHeaderUnparser, self )._handleHeaderSuite( node, header, suite )


	def _handleHeader(self, node, header):
		if node is self._compoundNode:
			if self._headerCount == self._headerIndex:
				self.targetHeader = header
			self._headerCount += 1
		return super( _CompoundHeaderUnparser, self )._handleHeader( node, header )
	





def pyReplaceSimpleStmtWithCompoundRange(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		if parent is None:
			raise TypeError, 'NodeEditor:pyReplaceSimpleStmtWithCompoundRange(): no parent '
		suite = parent.getNode()

		unparser = Python25StructureUnparser()
		
		linear = reduce( lambda a, b: a + b, [ unparser( node )   for node in suite ] )
		index = indexById( linear, target.getNode() )
		if index == -1:
			raise ValueError, 'could not replace'
		
		linear[index:index+1] = replacement
		
		reparsed = _structureParser.suite().parseListItems( linear )
		
		if reparsed.isValid()  and  reparsed.getEnd() == len( linear ):
			reparsed = reparsed.getValue()
		else:
			raise ValueError, 'NodeEditor:pyReplaceSimpleStmtWithCompoundRange(): could not parse'
		
		return performSuiteEdits( suite, reparsed )
	else:
		raise TypeError, 'NodeEditor:pyReplaceSimpleStmtWithCompoundRange(): @target must be a DocTreeNode'
	
	
def pyReplaceSimpleStatementWithRange(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		if len( replacement ) == 1:
			if isCompoundStmtHeader( replacement[0] ):
				return pyReplaceSimpleStmtWithCompoundRange( ctx, target, replacement )
			else:
				return [ pyReplaceStmt( ctx, target, replacement[0] ) ]
		else:
			if isCompoundStmtHeader( replacement[0] )  or  isCompoundStmtHeader( replacement[1] ):
				if isCompoundStmtHeader( replacement[0] ):
					r = [ replacement[0], Nodes.Indent(), replacement[1], Nodes.Dedent() ]
				else:
					r = replacement
				return pyReplaceSimpleStmtWithCompoundRange( ctx, target, r )
			else:
				xs = EditOperations.insertRangeBefore( ctx, target, replacement[:-1] )
				xs += [ pyReplaceStatement( ctx, target, replacement[-1], False ) ]
				return xs
	else:
		raise TypeError, 'NodeEditor:pyReplaceSimpleStatementWithRange(): @target must be a DocTreeNode'



	
def pyReplaceCompoundStatementHeaderWithRange(ctx, target, headerIndex, replacement):
	if isinstance( target, DocTreeNode ):
		if len( replacement ) > 1  and  isCompoundStmtHeader( replacement[0] ):
				replacement = [ replacement[0], Nodes.Indent(), replacement[1], Nodes.Dedent() ]
		
				
		parent = target.getParentTreeNode()
		if parent is None:
			raise TypeError, 'NodeEditor:pyReplaceCompoundStatementHeaderWithRange(): no parent '
		suite = parent.getNode()

		unparser = _CompoundHeaderUnparser( target, headerIndex )
		
		linear = reduce( lambda a, b: a + b, [ unparser( node )   for node in suite ] )
		
		index = indexById( linear, unparser.targetHeader )
		if index == -1:
			raise ValueError, 'could not replace'
		
		linear[index:index+1] = replacement
		
		reparsed = _structureParser.suite().parseListItems( linear )
		
		if reparsed.isValid()  and  reparsed.getEnd() == len( linear ):
			reparsed = reparsed.getValue()
		else:
			raise ValueError, 'NodeEditor:pyReplaceCompoundStatementHeaderWithRange(): could not parse'
		
		return performSuiteEdits( suite, reparsed )
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
		
	
	
class ParsedExpressionTextRepresentationListener (ElementTextRepresentationListener):
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
		
		


class _LineTextRepresentationListenerWithParser (ElementTextRepresentationListener):
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
			
			
			
			
class StatementNewLineTextRepresentationListener (_LineTextRepresentationListenerWithParser):
	def __init__(self, parser):
		super( StatementNewLineTextRepresentationListener, self ).__init__( parser )

	
	def _getNextStatementContext(self, statementContext):
		while statementContext is not None:
			nextContext = statementContext.getNextSibling()
			if nextContext is not None:
				return nextContext

			parentContext = getParentStatementContext( statementContext )
			suite = parentContext.getTreeNode()['suite']
			
			statementContext = parentContext
		
		return None
		
		

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
			
			
			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if StatementNewLineTextRepresentationListener._listenerTable is None:
			StatementNewLineTextRepresentationListener._listenerTable = _ListenerTable( StatementNewLineTextRepresentationListener )
		return StatementNewLineTextRepresentationListener._listenerTable.get( parser )



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
	
	
	
