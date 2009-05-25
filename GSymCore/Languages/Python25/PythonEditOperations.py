##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations



from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Precedence import *



def _isStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.Stmt )

def _isCompoundStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.CompoundStmt )




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
			if _isCompoundStmt( data ):
				originalSuite = data['suite']
				if _isCompoundStmt( replacement ):
					replacement['suite'].extend( originalSuite )
					return EditOperations.replaceNodeContents( ctx, data, replacement )
				else:
					return EditOperations.replaceWithRange( ctx, data, [ replacement, Nodes.IndentedBlock( suite=originalSuite ) ] )
			else:
				if _isCompoundStmt( replacement ):
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
				if len( suite ) == 0  or  not _isCompoundStmt( suite[-1] ):
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
		if _isCompoundStmt( node ):
			for n in node['suite']:
				self._visit( indent + 1, n )
				
				
	def __str__(self):
		return '\n'.join( [ str( line )   for line in self.lines ] )

		
		
			
		
		
			
	
