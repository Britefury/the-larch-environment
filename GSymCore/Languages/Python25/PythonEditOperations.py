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
from GSymCore.Languages.Python25.StructureUnparser import Python25StructureUnparser
from GSymCore.Languages.Python25.StructureParser import Python25StructureGrammar


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

def isCompoundStmtHeader(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.CompountStmtHeader )

def isPythonModule(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.PythonModule )

def isIndentedBlock(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.IndentedBlock )



#
#
# DOM / CONTEXT NAVIGATION
#
#

def getStatementContextFromElement(element):
	context = element.getContext()
	
	assert context is not None
	
	while not isStmt( context.getDocNode() ):
		context = context.getParent()
	return context


def getParentStatementContext(ctx):
	ctx = ctx.getParent()
	while ctx is not None  and  not isStmt( ctx.getDocNode() )  and  not ctx.getDocNode().isInstanceOf( Nodes.PythonModule ):
		ctx = ctx.getParent()
	return ctx

def getStatementContextPath(ctx):
	path = []
	while ctx is not None:
		path.insert( 0, ctx )
		ctx = getParentStatementContext( ctx )
	return path

def getStatementContextPathsFromCommonRoot(ctx0, ctx1):
	path0 = getStatementContextPath( ctx0 )
	path1 = getStatementContextPath( ctx1 )
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


def pyReplaceStatement(ctx, data, replacement, bDontReplaceIfEqual=True):
	if isinstance( data, DocTreeNode ):
		if data == replacement  and  bDontReplaceIfEqual:
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
	
	
	
def pyReplaceStmt(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		if target == replacement  and  bDontReplaceIfEqual:
			# Same content; ignore
			return target
		else:
			return EditOperations.replaceNodeContents( ctx, target, replacement )
	else:
		raise TypeError, 'PythonEditOperations:pyReplaceStmt(): @target must be a DocTreeNode'
			
def indexById(xs, y):
	for i, x in enumerate( xs ):
		if y is x:
			return i
	return -1


def performSuiteEdits(target, modified):
	commonPrefixLen = 0
	for i, (t, m) in enumerate( zip( target, modified ) ):
		if t != m:
			commonPrefixLen = i
			break
	
	commonSuffixLen = 0
	for i, (t, m) in enumerate( zip( reversed( target ), reversed( modified ) ) ):
		if t != m:
			commonSuffixLen = i
			break
		
	minLength = min( len( target ), len( modified ) )
	remaining = minLength - commonPrefixLen
	commonSuffixLen = min( commonSuffixLen, remaining )
	
	target[commonPrefixLen:len(target)-commonSuffixLen] = modified[commonPrefixLen:len(modified)-commonSuffixLen]
	return target[commonPrefixLen:len(target)-commonSuffixLen]
	




def pyReplaceStatementWithRange(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		if len( replacement ) == 1:
			return [ pyReplaceStatement( ctx, data, replacement[0] ) ]
		else:
			xs = EditOperations.insertRangeBefore( ctx, data, replacement[:-1] )
			xs += [ pyReplaceStatement( ctx, data, replacement[-1], False ) ]
			return xs
	else:
		raise TypeError, 'PythonEditOperations:pyReplace(): @data must be a DocTreeNode'



#
#
# PARSE TEXT
#
#

def parseText(parser, text, outerPrecedence=None):
	res = parser.parseStringChars( text )
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




