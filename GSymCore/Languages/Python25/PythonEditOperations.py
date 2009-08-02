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

from BritefuryJ.ParserDebugViewer import ParseViewFrame


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

def isCompoundStmtHeader(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.CompountStmtHeader )

def isCompoundStmtOrCompoundHeader(node):
	return isinstance( node, DMObjectInterface )  and  ( node.isInstanceOf( Nodes.CompoundStmt )  or  node.isInstanceOf( Nodes.CompountStmtHeader ) )

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
# DOCUMENT EDITING
#
#

def pyReplaceExpression(ctx, data, replacement):
	return EditOperations.replaceNodeContents( ctx, data, replacement )


	
def pyReplaceStmt(ctx, target, replacement, bDontReplaceIfEqual=True):
	if isinstance( target, DocTreeNode ):
		if target == replacement  and  bDontReplaceIfEqual:
			# Same content; ignore
			return target
		else:
			return EditOperations.replaceNodeContents( ctx, target, replacement )
	else:
		raise TypeError, 'PythonEditOperations:pyReplaceStmt(): @target must be a DocTreeNode'
			
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



#
#
# PARSE STREAM
#
#


def parseStream(parser, input, outerPrecedence=None):
	#f = open( 'parselog.txt', 'a+' )
	res = parser.parseStreamItems( input )
	pos = res.getEnd()
	if res.isValid():
		if pos == len( input ):
			value = res.getValue()
			return removeUnNeededParens( value, outerPrecedence )
		else:
			#f.write( '<INCOMPLETE> %s\n'  %  ( parser.getExpressionName(), ) )
			#f.write( 'FULL TEXT: ' + input.toString() + '\n' )
			#f.write( 'PARSED: ' + input[:pos].toString() + '\n' )
			#f.close()
			return None
	else:
		#f.write( '<FAIL> %s\n'  %  ( parser.getExpressionName(), ) )
		#f.write( 'FULL TEXT:' + input.toString() + '\n' )
		#f.close()
		return None



	
	
#
#
# DEBUG PARSE STREAM
#
#


def debugParseStream(parser, input, outerPrecedence=None):
	#f = open( 'parselog.txt', 'a+' )
	res = parser.debugParseStreamItems( input )
	ParseViewFrame( res )
	pos = res.getEnd()
	if res.isValid():
		if pos == len( input ):
			value = res.getValue()
			return removeUnNeededParens( value, outerPrecedence )
		else:
			#f.write( '<INCOMPLETE> %s\n'  %  ( parser.getExpressionName(), ) )
			#f.write( 'FULL TEXT: ' + input.toString() + '\n' )
			#f.write( 'PARSED: ' + input[:pos].toString() + '\n' )
			#f.close()
			return None
	else:
		#f.write( '<FAIL> %s\n'  %  ( parser.getExpressionName(), ) )
		#f.write( 'FULL TEXT:' + input.toString() + '\n' )
		#f.close()
		return None
