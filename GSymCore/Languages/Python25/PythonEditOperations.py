##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakValueDictionary

import cPickle


from java.lang import Object
from java.io import IOException
from java.util import List
from java.awt.event import KeyEvent
from java.awt.datatransfer import UnsupportedFlavorException, DataFlavor, StringSelection, Transferable

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



class NotImplementedError (Exception):
	pass


#
#
# NODE CLASSIFICATION
#
#

def isStmt(node):
	return isinstance( node, DMObjectInterface )  and  ( node.isInstanceOf( Nodes.Stmt )  or  node.isInstanceOf( Nodes.BlankLine )  or  node.isInstanceOf( Nodes.UNPARSED ) )

def isCompoundStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.CompoundStmt )

def isPythonModule(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.PythonModule )

def isIndentedBlock(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.IndentedBlock )



#
#
# DOM / CONTEXT NAVIGATION
#
#

def _getStatementContextFromElement(element):
	context = element.getContext()
	
	assert context is not None
	
	while not isStmt( context.getDocNode() ):
		context = context.getParent()
	return context


def _getParentStatementContext(ctx):
	ctx = ctx.getParent()
	while ctx is not None  and  not isStmt( ctx.getDocNode() )  and  not ctx.getDocNode().isInstanceOf( Nodes.PythonModule ):
		ctx = ctx.getParent()
	return ctx

def _getStatementContextPath(ctx):
	path = []
	while ctx is not None:
		path.insert( 0, ctx )
		ctx = _getParentStatementContext( ctx )
	return path

def _getStatementContextPathsFromCommonRoot(ctx0, ctx1):
	path0 = _getStatementContextPath( ctx0 )
	path1 = _getStatementContextPath( ctx1 )
	commonLength = min( len( path0 ), len( path1 ) )
	for i, ( p0, p1 ) in enumerate( zip( path0, path1 ) ):
		if p0 is not p1:
			commonLength = i
			break
	return path0[commonLength-1:], path1[commonLength-1:]
		
	
	
	

	


#
#
# LINE LIST
#
#

class PyLine (object):
	def __init__(self, indent):
		self.indent = indent
		
	def getAST(self, parser):
		return None
	
	def withIndent(self, indent):
		pass
	
	
	@abstractmethod
	def copy(self):
		pass
	
	

		
class PyTextLine (PyLine):
	def __init__(self, indent, text):
		super( PyTextLine, self ).__init__( indent )
		self.text = text
		
	def getAST(self, parser):
		return parseText( parser, self.text )
	
	def withIndent(self, indent):
		return _TextLine( indent, self.text )
	
	def __eq__(self, x):
		if isinstance( x, PyTextLine ):
			return self.indent == x.indent  and  self.text == x.text
		else:
			return self.text == x
		
	def __str__(self):
		return '>>' + '\t' * self.indent  +  str( self.text )
	
	
	def copy(self):
		return PyTextLine( self.indent, self.text )
	
	
		
_identity = DefaultIdentityTransformationFunction()
_innerFn = lambda x, f: x

class PyASTLine (PyLine):
	def __init__(self, indent, ast):
		super( PyASTLine, self ).__init__( indent )
		if isinstance( ast, DocTreeNode ):
			ast = ast.getNode()
		self.ast = ast
		
	def getAST(self, parser):
		return self.ast
	
	def withIndent(self, indent):
		return _ASTLine( indent, self.ast )
		
	def __eq__(self, x):
		if isinstance( x, PyASTLine ):
			return self.indent == x.indent  and  self.ast is x.ast
		else:
			return self.ast is x
		
	def __str__(self):
		return '>>' + '\t' * self.indent  +  str( self.ast )
	
	def copy(self):
		ast = _identity( self.ast, _innerFn )
		if isCompoundStmt( ast ):
			ast['suite'] = []
		return PyASTLine( self.indent, ast )

	
		
class PyLineList (object):
	def __init__(self, nodes):
		self.lines = []
		for node in nodes:
			self._visit( 0, node )
	
			
	def indexOf(self, x):
		for i, line in enumerate( self.lines ):
			if line == x:
				return i
		return None
	
	
	def subList(self, startIndex, endIndex):
		x = PyLineList( [] )
		x.lines = [ line.copy()   for line in self.lines[startIndex:endIndex] ]
		minIndent = min( [ line.indent   for line in x.lines ] )   if len( x.lines ) > 0   else   0
		for line in x.lines:
			line.indent -= minIndent
		return x
	
	
	def replaceRangeWithAST(self, startIndex, endIndex, astLines):
		indent = self.lines[startIndex].indent
		self.lines[startIndex:endIndex] = [ PyASTLine( indent, ast )   for ast in astLines ]
		
		
	def insertLineList(self, index, lineList, indentOffset):
		insertion = [ line.copy()   for line in lineList.lines ]
		for line in insertion:
			line.indent += indentOffset
		self.lines[index:index] = insertion
	
	def insertASTLine(self, index, ast, indentation):
		self.lines.insert( index, PyASTLine( indentation, ast ) )
		
	def deleteLine(self, index):
		del self.lines[index]
	
	def indentRange(self, startIndex, endIndex):
		for line in self.lines[startIndex:endIndex]:
			line.indent += 1
			
	def dedentRange(self, startIndex, endIndex):
		for line in self.lines[startIndex:endIndex]:
			line.indent -= 1
			
	
	def parse(self, lineParser):
		suite = []
		suiteStack = [ suite ]
		
		currentIndent = 0
		for line in self.lines:
			indent = line.indent
			indent = max( indent, 0 )
			
			# Handle change in indentation
			while indent > currentIndent:
				if len( suite ) == 0  or  not isCompoundStmt( suite[-1] ):
					suite.append( Nodes.IndentedBlock( suite=[] ) )
	
				comp = suite[-1]
				suite = comp['suite']
				suiteStack.append( suite )
				currentIndent += 1
			
			while indent < currentIndent:
				del suiteStack[-1]
				suite = suiteStack[-1]
				currentIndent -= 1
				
			
			# Add the node
			ast = line.getAST( lineParser )
			if isCompoundStmt( ast ):
				ast['suite'] = []
			suite.append( ast )
		
		return suiteStack[0]
	
	
	
	def _visit(self, indent, node):
		if not isIndentedBlock( node )   and   not isPythonModule( node ):
			self.lines.append( PyASTLine( indent, node ) )
		if isCompoundStmt( node )  or  isPythonModule( node ):
			for n in node['suite']:
				self._visit( indent + 1, n )
				
				
				
	def copy(self):
		x = PyLineList( [] )
		x.lines = [ line.copy()   for line in self.lines ]
		return x
				
				
	def __str__(self):
		return '\n'.join( [ str( line )   for line in self.lines ] )

		
		
			
		

	
	
#
#
# DOCUMENT EDITING
#
#

def pyReplaceExpression(ctx, data, replacement):
	return EditOperations.replaceNodeContents( ctx, data, replacement )


def pyReplaceStatement(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		if data == replacement:
			# Same data; ignore
			return data
		else:
			if isCompoundStmt( data ):
				originalSuite = data['suite']
				if isCompoundStmt( replacement ):
					replacement['suite'].extend( originalSuite )
					return EditOperations.replaceNodeContents( ctx, data, replacement )
				else:
					return EditOperations.replaceWithRange( ctx, data, [ replacement, Nodes.IndentedBlock( suite=originalSuite ) ] )
			else:
				if isCompoundStmt( replacement ):
					parent = data.getParentTreeNode()
					if parent is None:
						raise TypeError, 'PythonEditOperations:pyReplace(): no parent '
					index = parent.indexOfById( data.getNode() )
					if index == -1:
						raise ValueError, 'could not replace'
					if len( parent )  > ( index + 1 ):
						if isIndentedBlock( parent[index+1] ):
							# Join the indented block
							indentedBlock = parent[index+1]
							originalSuite = indentedBlock['suite']
							replacement['suite'].extend( originalSuite )
							del parent[index+1]
							return EditOperations.replaceNodeContents( ctx, data, replacement )
					return EditOperations.replaceNodeContents( ctx, data, replacement )
				else:
					return EditOperations.replaceNodeContents( ctx, data, replacement )
	else:
		raise TypeError, 'PythonEditOperations:pyReplace(): @data must be a DocTreeNode'
	
	
def pyReplaceStatementWithRange(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		if len( replacement ) == 1:
			return [ pyReplaceStatement( ctx, data, replacement[0] ) ]
		else:
			xs = EditOperations.insertRangeBefore( ctx, data, replacement[:-1] )
			xs += [ pyReplaceStatement( ctx, data, replacement[-1] ) ]
			return xs
	else:
		raise TypeError, 'PythonEditOperations:pyReplace(): @data must be a DocTreeNode'





#
#
# PARSE TEXT
#
#

def parseText(parser, text, outerPrecedence=None):
	res = parser.parseString( text )
	pos = res.getEnd()
	if res.isValid():
		if pos == len( text ):
			value = res.getValue()
			return removeUnNeededParens( value, outerPrecedence )
		else:
			print '<INCOMPLETE>'
			print 'FULL TEXT:', text
			print 'PARSED:', text[:pos]
			return None
	else:
		print 'FULL TEXT:', text
		print '<FAIL>'
		return None




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
				#if parsed != node:
					#replace( ctx, node, parsed )
				#replaceNodeContents( ctx, node, Nodes.UNPARSED( value=value ) )
				if parsed != node:
					pyReplaceExpression( ctx, node, parsed )
			else:
				#if node != Nodes.UNPARSED( value=value ):
					#replace( ctx, node, Nodes.UNPARSED( value=value ) )
				#replaceNodeContents( ctx, node, Nodes.UNPARSED( value=value ) )
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
		
		




class LineTextRepresentationListenerWithParser (ElementTextRepresentationListener):
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
					# Parsed
					if not isCompoundStmt( parsed ):
						# Normal statement (non-compount)
						result.append( parsed )
					else:
						lineParsed = parsed
						lineParsed['suite'] = self.parseLines( lineStrings[i+1:] )
						result.append( lineParsed )
						break
		return result


	
	
class ParsedLineTextRepresentationListener (LineTextRepresentationListenerWithParser):
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
		
		pyReplaceStatementWithRange( ctx, node, parsedLines )

		#if isCompoundStmt( node ):
			#originalContents = node['suite']
			#if isCompoundStmt( parsedLines[-1] ):
				#parsedLines[-1]['suite'].extend( originalContents )
			#else:
				#parsedLines.extend( originalContents )
				
		#if len( parsedLines ) == 1:
			#if node == parsedLines[0]:
				## Same data; ignore
				#pass
			#else:
				##replace( ctx, node, parsedLines[0] )
				#replaceNodeContents( ctx, node, parsedLines[0] )
		#else:
			#replaceWithRange( ctx, node, parsedLines )

			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if ParsedLineTextRepresentationListener._listenerTable is None:
			ParsedLineTextRepresentationListener._listenerTable = _ListenerTable( ParsedLineTextRepresentationListener )
		return ParsedLineTextRepresentationListener._listenerTable.get( parser )
			
			
			
			
class StatementNewLineTextRepresentationListener (LineTextRepresentationListenerWithParser):
	def __init__(self, parser):
		super( StatementNewLineTextRepresentationListener, self ).__init__( parser )

	
	def _getNextStatementContext(self, statementContext):
		while statementContext is not None:
			nextContext = statementContext.getNextSibling()
			if nextContext is not None:
				return nextContext

			parentContext = _getParentStatementContext( statementContext )
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
			statementContext = _getStatementContextFromElement( element )
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

				if _getParentStatementContext( statementContext ) is _getParentStatementContext( nextContext ):
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
	
	
	
	
	
	
class Python25BufferFlavor (DataFlavor):
	def __init__(self):
		super( Python25BufferFlavor, self ).__init__( Object, DataFlavor.javaJVMLocalObjectMimeType )


_python25BufferDataFlavor = Python25BufferFlavor()
	


class Python25Transferable (Transferable):
	def __init__(self, data):
		self.data = data

	def getTransferData(self, flavor):
		if flavor is _python25BufferDataFlavor:
			return self.data
		else:
			raise UnsupportedFlavorException
	
	def getTransferDataFlavors(self):
		return [ _python25BufferDataFlavor ]
	
	def isDataFlavorSupported(self, flavor):
		return flavor is _python25BufferDataFlavor
	


	
class Python25Buffer (object):
	pass


class Python25BufferString (Python25Buffer):
	def __init__(self, text):
		self.text = text
		
		
class Python25BufferLinePair (Python25Buffer):
	def __init__(self, line1, line2):
		self.line1 = line1
		self.line2 = line2
		
		
class Python25BufferSubtree (Python25Buffer):
	def __init__(self, prefix, lineList, suffix):
		self.prefix = prefix
		self.lineList = lineList
		self.suffix = suffix
	
	



class Python25EditHandler (EditHandler):
	def __init__(self, viewContext):
		self._viewContext = viewContext
		self._grammar = Python25Grammar()
		
		
			
	def indent(self, context, node):
		selection = self._viewContext.getSelection()
		
		if selection.isEmpty():
			self._indentLine( context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = _getStatementContextFromElement( startMarker.getElement() )
			endContext = _getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( context, node )
			else:
				self._indentSelection( selection )
			
			
			
	def dedent(self, context, node):
		selection = self._viewContext.getSelection()
		
		if selection.isEmpty():
			self._dedentLine( context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = _getStatementContextFromElement( startMarker.getElement() )
			endContext = _getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( context, node )
			else:
				self._dedentSelection( selection )
				
			
			
	def _indentLine(self, context, node):
		# If @node comes after a compunt statement, move it to the end of that statement
		suite = node.getParentTreeNode()
		index = suite.indexOfById( node )
		next = suite[index+1]   if index < len( suite ) - 1   else None
		if index > 0:
			prev = suite[index-1]
			if isCompoundStmt( prev ):
				prevSuite = prev['suite']
				
				nodes = [ node ]
				
				# If @node is followed by an indented block, join the contents of the indented block onto @prevSuite
				if next is not None   and   isIndentedBlock( next ):
					nodes.extend( next['suite'] )
					EditOperations.remove( context, next )
						
				del suite[index]
				prevSuite.extend( nodes )
				
				# Now, 
				return
			

		if next is not None   and   isIndentedBlock( next ):
			# If @node is followed by an indented block, insert into the indented block
			EditOperations.remove( context, node )
			next['suite'].insert( 0, node )
		else:
			# Else, move @node into its own indented block
			indentedNode = Nodes.IndentedBlock( suite=[ node ] )
			EditOperations.replace( context, node, indentedNode )
			
	
	
	def _dedentLine(self, context, node):
		suite = node.getParentTreeNode()
		compStmt = suite.getParentTreeNode()
		outerSuite = compStmt.getParentTreeNode()   if compStmt is not None   else None
		outerIndex = outerSuite.indexOfById( compStmt )   if outerSuite is not None   else None

		# If @outerSuite is None, then this suite is from the module node, in which case we cannot dedent
		if outerSuite is not None:
			index = suite.indexOfById( node )
			if index == len( suite ) - 1:
				# If @node is at the end of a suite, move it into the parent suite
				outerIndex = outerSuite.indexOfById( compStmt )
				
				del suite[-1]
				outerSuite.insert( outerIndex + 1, node )
				
				# If @node was inside an indented block, which will now be empty: remove the indented block
				if len( suite ) == 0   and   isIndentedBlock( compStmt ):
					EditOperations.remove( context, compStmt )
				return
			elif index == 0  and  isIndentedBlock( compStmt ):
				# @node is the first node in an indented block; move it out.
				# The indented block has 2 or more child statements, otherwise the previous if-statment
				# would have been taken
				EditOperations.remove( context, node )
				outerSuite.insert( outerIndex, node )
			else:
				# Remove @node and all subsequent nodes from @suite.
				# Place @node into @outerSuite
				# Place all subsequent nodes into an indented block
				subsequentNodes = suite[index+1:]
				del suite[index:]
				outerSuite.insert( outerIndex + 1, Nodes.IndentedBlock( suite=subsequentNodes ) )
				outerSuite.insert( outerIndex + 1, node )
				
				
				
				
	def _indentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = _getStatementContextFromElement( startMarker.getElement() )
		endContext = _getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getViewNodeContentElement()
		endStmtElement = endContext.getViewNodeContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = _getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
				
		if len( path0 ) == 1:
			# Start of selection is in the header of a compound statement; we need to work from one node up
			r = _getParentStatementContext( root )
			if r is not None:
				root = r
			
		rootDoc = root.getDocNode()
		
		# Convert to a line list
		lineList = PyLineList( rootDoc['suite'] )
		
		# Replace the lines in the range startIndex->endIndex with the new parsed line
		startIndex = lineList.indexOf( startContext.getDocNode() )
		endIndex = lineList.indexOf( endContext.getDocNode() )
		lineList.indentRange( startIndex, endIndex + 1 )
		
		# Parse to ASTs
		newRootASTs = lineList.parse( self._grammar.statement() )
		
		# Insert into document
		rootDoc['suite'] = newRootASTs
			
				
	
	
	def _dedentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = _getStatementContextFromElement( startMarker.getElement() )
		endContext = _getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getViewNodeContentElement()
		endStmtElement = endContext.getViewNodeContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = _getStatementContextPathsFromCommonRoot( startContext, endContext )
		commonRoot = path0[0]
		
		if len( path0 ) == 1:
			# Start marker is in the header of a compound statement
			# Move up one
			commonRoot = _getParentStatementContext( commonRoot )
			if commonRoot is None:
				return
		
		rootParent = _getParentStatementContext( commonRoot )
		
		if rootParent is not None:
			rootParentDoc = rootParent.getDocNode()
			
			# Convert to a line list
			lineList = PyLineList( rootParentDoc['suite'] )
			
			# Replace the lines in the range startIndex->endIndex with the new parsed line
			startIndex = lineList.indexOf( startContext.getDocNode() )
			endIndex = lineList.indexOf( endContext.getDocNode() )
			lineList.dedentRange( startIndex, endIndex + 1 )
			
			# Parse to ASTs
			newRootASTs = lineList.parse( self._grammar.statement() )
			
			# Insert into document
			rootParentDoc['suite'] = newRootASTs

			
			
			
	def deleteSelection(self):
		self.replaceSelection( None )
	
	
	def replaceSelection(self, replacement):
		selection = self._viewContext.getSelection()
		
		if not selection.isEmpty():
			if replacement is not None   and   isinstance( replacement, Python25BufferSubtree ):
				raise NotImplementedError
			else:
				startMarker = selection.getStartMarker()
				endMarker = selection.getEndMarker()
				
				# Get the statements that contain the start and end markers
				startContext = _getStatementContextFromElement( startMarker.getElement() )
				endContext = _getStatementContextFromElement( endMarker.getElement() )
				# Get the statement elements
				startStmtElement = startContext.getViewNodeContentElement()
				endStmtElement = endContext.getViewNodeContentElement()
				# Get the text before and after the selection
				textBefore = startStmtElement.getTextRepresentationFromStartToMarker( startMarker )
				textAfter = endStmtElement.getTextRepresentationFromMarkerToEnd( endMarker )
				
				if replacement is None:
					# Compose a new line of text, and parse it
					line = textBefore + textAfter
				
					lineDocs = [ self._parseLine( line.strip( '\n' ) ) ]
				elif isinstance( replacement, str )   or   isinstance( replacement, unicode ):
					# Compose a new line of text, and parse it
					line = textBefore + replacement + textAfter
				
					lineDocs = [ self._parseLine( line.strip( '\n' ) ) ]
				elif isinstance( replacement, Python25BufferString ):
					# Compose a new line of text, and parse it
					line = textBefore + replacement.text + textAfter
				
					lineDocs = [ self._parseLine( line.strip( '\n' ) ) ]
				elif isinstance( replacement, Python25BufferLinePair ):
					# Compose two new lines of text, and parse
					line1 = textBefore + replacement.line2
					line2 = replacement.line2 + textAfter
				
					lineDocs = [ self._parseLine( line1.strip( '\n' ) ),  self._parseLine( line2.strip( '\n' ) ) ]
					
					
						
						
						
				# Now, insert the parsed text into the document		
				if startContext is endContext:
					# Selection is within a single statement
					pyReplaceStatementWithRange( startContext, startContext.getTreeNode(), lineDocs )
					selection.clear()
				else:
					# Get paths to start and end nodes, from the common root statement
					path0, path1 = _getStatementContextPathsFromCommonRoot( startContext, endContext )
					commonRoot = path0[0]
					selection.clear()
					
					if len( path0 ) == 1:
						# The path to the start node has only 1 entry; this means that the only statement
						# on the path is the common root.
						# The only way this can happen is if the start marker is within the bounds of the header
						# of a compound statement
						rootDoc = commonRoot.getDocNode()
						
						# Convert to a line list
						lineList = PyLineList( [ rootDoc ] )
						
						# Replace the lines in the range startIndex->endIndex with the new parsed line
						startIndex = lineList.indexOf( startContext.getDocNode() )
						endIndex = lineList.indexOf( endContext.getDocNode() )
						lineList.replaceRangeWithAST( startIndex, endIndex + 1, lineDocs )
						
						# Parse to ASTs
						newRootASTs = lineList.parse( self._grammar.statement() )
						
						# Insert into document
						EditOperations.replaceWithRange( commonRoot, commonRoot.getTreeNode(), newRootASTs )
					else:
						# Get the suite from the common root statement
						suite = commonRoot.getDocNode()['suite']
						
						# Get the indices of the child statements that contain the start and end markers respectively
						startStmtIndex = suite.indexOfById( path0[1].getDocNode() )
						endStmtIndex = suite.indexOfById( path1[1].getDocNode() )
						assert startStmtIndex != -1  and  endStmtIndex != -1
						
						# Convert to a line list
						lineList = PyLineList( suite[startStmtIndex:endStmtIndex+1] )
						
						# Replace the lines in the range startIndex->endIndex with the new parsed line
						startIndex = lineList.indexOf( startContext.getDocNode() )
						endIndex = lineList.indexOf( endContext.getDocNode() )
						lineList.replaceRangeWithAST( startIndex, endIndex + 1, lineDocs )
						
						# Parse to ASTs
						newASTs = lineList.parse( self._grammar.statement() )
						
						suite[startStmtIndex:endStmtIndex+1] = newASTs 
				
				
	
	
	
	
	def _insertBufferAtMarker(self, marker, b):
		markerStmtContext = _getStatementContextFromElement( marker.getElement() )
		markerStmtElement = markerStmtContext.getViewNodeContentElement()
		textBefore = markerStmtElement.getTextRepresentationFromStartToMarker( marker )
		textAfter = markerStmtElement.getTextRepresentationFromMarkerToEnd( marker )
		if isinstance( b, Python25BufferString )  or  isinstance( b, Python25BufferLinePair )  or  isinstance( b, str )  or  isinstance( b, unicode ):
			if isinstance( b, str )  or  isinstance( b, unicode ):
				line = textBefore + b + textAfter
				lineDoc = self._parseLine( line.strip() )
				pyReplaceStatement( markerStmtContext, markerStmtContext.getTreeNode(), lineDoc )
			elif isinstance( b, Python25BufferString ):
				line = textBefore + b.text + textAfter
				lineDoc = self._parseLine( line.strip() )
				pyReplaceStatement( markerStmtContext, markerStmtContext.getTreeNode(), lineDoc )
			elif isinstance( b, Python25BufferLinePair ):
				# Even if the second line has indentation, its not enough to determine what to apply, so don't
				line1, line2 = b.line1, b.line2
				if not line2.startswith( line2.strip() ):
					stripped = line2.strip()
					contentIndex = line2.index( stripped )
					line2 = line2[contentIndex:]
				
				line1 = textBefore + line1
				line2 = line2 + textAfter
				lineDocs = [ self._parseLine( line1.strip() ), self._parseLine( line2.strip() ) ]
				pyReplaceStatementWithRange( markerStmtContext, markerStmtContext.getTreeNode(), lineDocs )
		elif isinstance( b, Python25BufferSubtree ):
			# Compute the text and ASTs for the lines that surround the marker / insertion point
			preLine = ( textBefore + b.prefix ).strip( '\n' )
			postLine = ( b.suffix + textAfter ).strip( '\n' )
			preDoc = self._parseLine( preLine )   if preLine != ''   else None
			postDoc = self._parseLine( postLine )   if postLine != ''   else None

			# Get the indentation of the first line of the source data
			destIndent = b.lineList.lines[0].indent   if len( b.lineList.lines ) > 0   else 0

			# Recurse up the destination document from the caret statement, the number of levels
			# necessary to fit the source block, so that indentation matches
			rootContext = markerStmtContext
			for i in xrange( 0, destIndent+1 ):
				parentContext = _getParentStatementContext( rootContext )
				if parentContext is None:
					break
				rootContext = parentContext
			rootDoc = rootContext.getDocNode()
			
			# Convert the destination data to a line list, and get the position of the caret
			lineList = PyLineList( rootDoc['suite'] )
			index = lineList.indexOf( markerStmtContext.getDocNode() )
			indentAtCaret = lineList.lines[index].indent
			
			# Compute the indentation offset that should be applied to the source data so that
			# the first line matches the caret in the destination
			indentOffset = indentAtCaret - destIndent
			
			lineList.deleteLine( index )
			if postDoc is not None:
				postIndent = b.lineList.lines[-1].indent + indentOffset   if len( b.lineList.lines ) > 0   else indentAtCaret
				lineList.insertASTLine( index, postDoc, postIndent )
			lineList.insertLineList( index, b.lineList, indentOffset )
			if preDoc is not None:
				lineList.insertASTLine( index, preDoc, indentAtCaret )
			
			# Parse to ASTs
			newASTs = lineList.parse( self._grammar.statement() )
			
			rootDoc['suite'] = newASTs
			

			
			
	
	
	
	
	
	def getSourceActions(self):					# -> int
		return self.COPY_OR_MOVE
	
	
	
	def createTransferable(self):					# -> Transferable
		selection = self._viewContext.getSelection()
		
		if not selection.isEmpty():
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = _getStatementContextFromElement( startMarker.getElement() )
			endContext = _getStatementContextFromElement( endMarker.getElement() )
	
			if startContext is endContext:
				# Selection within a single statement
				text = self._viewContext.getElementTree().getTextRepresentationInSelection( selection )
				return Python25Transferable( Python25BufferString( text ) )
			else:
				# Get the statement elements
				startStmtElement = startContext.getViewNodeContentElement()
				endStmtElement = endContext.getViewNodeContentElement()
				# Get the text between the selection start and the end of the start line, and the start of the end line and the selection end
				textInFirstLine = startStmtElement.getTextRepresentationFromMarkerToEnd( startMarker )
				textInLastLine = endStmtElement.getTextRepresentationFromStartToMarker( endMarker )
				
				path0, path1 = _getStatementContextPathsFromCommonRoot( startContext, endContext )
				
				if len( path0 ) == 2  and  len( path1 ) == 2:
					# Both statements fromt the same suite
					commonRootCtx = path0[0]
					suite = commonRootCtx.getDocNode()['suite']
					startIndex = suite.indexOfById( startContext.getDocNode() )
					endIndex = suite.indexOfById( endContext.getDocNode() )
					if endIndex == startIndex + 1:
						return Python25Transferable( Python25BufferLinePair( textInFirstLine, textInLastLine ) )


				#
				# Handle the subtree case
				#
				path0, path1 = _getStatementContextPathsFromCommonRoot( startContext, endContext )
				commonRoot = path0[0]
				
				rootDoc = commonRoot.getDocNode()
				
				# Convert to a line list
				lineList = PyLineList( [ rootDoc ] )

				# Compute the range of the line list
				startIndex = lineList.indexOf( startContext.getDocNode() )
				endIndex = lineList.indexOf( endContext.getDocNode() )
				
				# Determine if the start marker is at the start of the start statement
				if textInFirstLine == startStmtElement.getTextRepresentation():
					textInFirstLine = ''
				else:
					startIndex += 1
					
				if textInLastLine == endStmtElement.getTextRepresentation():
					textInLastLine = ''
				else:
					endIndex -= 1
					
				subList = lineList.subList( startIndex, endIndex + 1 )
				
				return Python25Transferable( Python25BufferSubtree( textInFirstLine, subList, textInLastLine ) )
					

		return None

				
				
				
	def exportDone(self, data, action):				# -> None,   data <- Transferable, action <- int
		if action == self.MOVE:
			self.deleteSelection()
		
			
			

	def canImport(self, support):					# -> bool,   support <- TransferHandler.TransferSupport
		if support.isDataFlavorSupported( _python25BufferDataFlavor ):
			return True
		else:
			return False

		
		
	def importData(self, info):					# -> bool,   info <- TransferHandler.TransferSupport
		if not self.canImport( info ):
			return False
		try:
			data = info.getTransferable().getTransferData( _python25BufferDataFlavor )
		except UnsupportedFlavorException:
			return False
		except IOException:
			return False
		
		if info.isDrop():
			# Drop
			return False
		else:
			# Paste
			selection = self._viewContext.getSelection()
			if not selection.isEmpty():
				self.replaceSelection( data )
				return True
			else:
				caret = self._viewContext.getCaret()
				caretMarker = caret.getMarker()
				if caretMarker.isValid():
					self._insertBufferAtMarker( caretMarker, data )
					return True
				return False
	

	def _parseLine(self, line):
		if line.strip() == '':
			# Blank line
			return Nodes.BlankLine()
		else:
			# Parse
			parsed = parseText( self._grammar.statement(), line )
			if parsed is None:
				# Parse failure; unparsed text
				return Nodes.UNPARSED( value=line )
			else:
				# Parsed
				return parsed
		
