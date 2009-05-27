##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakValueDictionary

from java.util import List

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent import *


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations




from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Parser import Python25Grammar
from GSymCore.Languages.Python25.Precedence import *



#
#
# NODE CLASSIFICATION
#
#

def isStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.Stmt )

def isCompoundStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.CompoundStmt )





#
#
# DOCUMENT EDITING
#
#

def pyReplaceExpression(ctx, data, replacement):
	return EditOperations.replaceNodeContents( ctx, data, replacement )


def pyReplaceStatement(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		## HACK
		## TODO
		## use Java equals() method for now due to a bug in Jython; should be fixed once the patch for issue 1338 (http://bugs.jython.org) is integrated
		if data.equals( replacement ):
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
						if parent[index+1].isInstanceOf( Nodes.IndentedBlock ):
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
				pyReplaceExpression( ctx, node, Nodes.UNPARSED( value=value ) )
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
				parsed = parseText( self._parser, line )
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
			### HACK
			### TODO
			### use Java equals() method for now due to a bug in Jython; should be fixed once the patch for issue 1338 (http://bugs.jython.org) is integrated
			#if node.equals( parsedLines[0] ):
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
			
			
			
			
class NewLineTextRepresentationListener (LineTextRepresentationListenerWithParser):
	__slots__ = [ '_suite', '_index', '_before', '_after' ]

	
	def __init__(self, parser, suite, index, before, after):
		super( NewLineTextRepresentationListener, self ).__init__( parser )
		self._suite = suite
		self._index = index
		self._before = before
		self._after = after

		
	def textRepresentationModified(self, element):
		# Get the content
		value = element.getTextRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		if value == '':
			# Newline has been deleted
			beforeContent = self._before.getTextRepresentation()
			afterContent = self._after.getTextRepresentation()   if self._after is not None   else   ''
			
			endIndex = self._index + 2   if self._after is not None   else   self._index + 1
			
			value = beforeContent + afterContent
			

			# Split into lines
			lineStrings = value.split( '\n' )
			# Parse
			parsedLines = self.parseLines( lineStrings )
			
			del self._suite[self._index:endIndex]
			EditOperations.insertRange( ctx, self._suite, self._index, parsedLines )
			
			
			



class StatementKeyboardListener (ElementKeyboardListener):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getContext()
			node = context.getTreeNode()
			
			if event.getModifiers() & KeyEvent.SHIFT_MASK  !=  0:
				self._dedent( node )
			else:
				self._indent( node )
			return True
		else:
			return False
		
		
	def onKeyPress(self, element, event):
		return False
	
	def onKeyRelease(self, element, event):
		return False
	
	
	
	def _indent(self, node):
		suite = node.getParentTreeNode()
		index = suite.indexOfById( node )
		if index > 0:
			prev = suite[index-1]
			if isCompoundStmt( prev ):
				prevSuite = prev['suite']
				del suite[index]
				prevSuite.append( node )
	
	
	def _dedent(self, node):
		suite = node.getParentTreeNode()
		index = suite.indexOfById( node )
		if index == len( suite ) - 1:
			compStmt = suite.getParentTreeNode()
			outerSuite = compStmt.getParentTreeNode()
			# If @outerSuite is None, then this suite is from the module node
			if outerSuite is not None:
				outerIndex = outerSuite.indexOfById( compStmt )
				
				del suite[-1]
				outerSuite.insert( outerIndex + 1, node )










class PyLine (object):
	def __init__(self, indent):
		self.indent = indent
		
	def getAST(self, parser):
		return None
	
	def withIndent(self, indent):
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
		return '\t' * self.indent  +  str( self.text )
		
		
		
class PyASTLine (PyLine):
	def __init__(self, indent, ast):
		super( PyASTLine, self ).__init__( indent )
		self.ast = ast
		
	def getAST(self, parser):
		return self.ast
	
	def withIndent(self, indent):
		return _ASTLine( indent, self.ast )
		
	def __eq__(self, x):
		if isinstance( x, PyASTLine ):
			return self.indent == x.indent  and  self.ast == x.ast
		else:
			return self.ast is x
		
	def __str__(self):
		return '\t' * self.indent  +  str( self.ast )

	
		
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
	
	
	def replaceRangeWithAST(self, fromIndex, toIndex, astLines):
		indent = self.lines[fromIndex].indent
		self.lines[fromIndex:toIndex+1] = [ PyASTLine( indent, ast )   for ast in astLines ]
			
	
	def parse(self, lineParser):
		minIndent = min( [ line.indent   for line in self.lines ] )
		
		suite = []
		suiteStack = [ suite ]
		
		currentIndent = minIndent
		for line in self.lines:
			indent = line.indent
			
			# Handle change in indentation
			while indent > currentIndent:
				if len( suite ) == 0  or  not isCompoundStmt( suite[-1] ):
					suite.append( Nodes.IndentedBlock( suite=[] ) )
	
				comp = suite[-1]
				suite = comp['suite']
				suite[:] = []
				suiteStack.append( suite )
				currentIndent += 1
			
			while indent < currentIndent:
				del suiteStack[-1]
				suite = suiteStack[-1]
				currentIndent -= 1
				
			
			# Add the node
			ast = line.getAST( lineParser )
			suite.append( ast )
		
		return suiteStack[0]
	
	
	
	def _visit(self, indent, node):
		self.lines.append( PyASTLine( indent, node ) )
		if isCompoundStmt( node ):
			for n in node['suite']:
				self._visit( indent + 1, n )
				
				
	def __str__(self):
		return '\n'.join( [ str( line )   for line in self.lines ] )

		
		
			
		
def _getStatementContextFromElement(element):
	context = element.getContext()
	
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
		
	
	
	

	
class Python25EditHandler (EditHandler):
	def __init__(self, viewContext):
		self._viewContext = viewContext
		self._grammar = Python25Grammar()
		
		
			
	def deleteSelection(self):
		selection = self._viewContext.getSelection()
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
		
		# Compose a new line of text, and parse it
		line = textBefore + textAfter
		line = line.strip( '\n' )
		
		lineDoc = None
		
		if line.strip() == '':
			# Blank line
			lineDoc = Nodes.BlankLine()
		else:
			# Parse
			parsed = parseText( self._grammar.statement(), line )
			if parsed is None:
				# Parse failure; unparsed text
				lineDoc = Nodes.UNPARSED( value=line )
			else:
				# Parsed
				lineDoc = parsed
				
				
				
		# Now, insert the parsed text into the document		
		if startContext is endContext:
			# Selection is within a single statement
			pyReplaceStatement( startContext, startContext.getTreeNode(), lineDoc )
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
				lineList.replaceRangeWithAST( startIndex, endIndex, [ lineDoc ] )
				
				# Parse to ASTs
				newRootASTs = lineList.parse( self._grammar.statement() )
				
				# Insert into document
				replaceWithRange( commonRoot, commonRoot.getTreeNode(), newRootASTs )
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
				lineList.replaceRangeWithAST( startIndex, endIndex, [ lineDoc ] )
				
				# Parse to ASTs
				newASTs = lineList.parse( self._grammar.statement() )
				
				suite[startStmtIndex:endStmtIndex+1] = newASTs 
				
				
	def replaceSelection(self, replacement):
		pass
	
	def editCopy(self):
		print 'Copying selection:'
		print self._viewContext.getElementTree().getTextRepresentationInSelection( self._viewContext.getSelection() )
	
	def editCut(self):
		pass
	
	def editPaste(self):
		pass




