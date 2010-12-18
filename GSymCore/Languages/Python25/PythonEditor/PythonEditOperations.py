##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakValueDictionary

import copy

import cPickle


from java.lang import Object
from java.io import IOException
from java.util import List
from java.awt.event import KeyEvent
from java.awt.datatransfer import UnsupportedFlavorException, DataFlavor, StringSelection, Transferable

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface, DMNode



from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder, StreamValue


from Britefury.Util.NodeUtil import *




from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *


class NotImplementedError (Exception):
	pass




#
#
# NODE CLASSIFICATION
#
#

def isStmt(node):
	return isinstance( node, DMObjectInterface )  and  ( node.isInstanceOf( Schema.Stmt )  or  node.isInstanceOf( Schema.BlankLine )  or  node.isInstanceOf( Schema.UNPARSED ) )

def isCompoundStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.CompoundStmt )

def isCompoundStmtHeader(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.CompountStmtHeader )

def isCompoundStmtOrCompoundHeader(node):
	return isinstance( node, DMObjectInterface )  and  ( node.isInstanceOf( Schema.CompoundStmt )  or  node.isInstanceOf( Schema.CompountStmtHeader ) )

def isUnparsed(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.UNPARSED )

def isTopLevel(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.TopLevel )

def isIndentedBlock(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.IndentedBlock )





#
#
# FRAGMENT CLASSIFICATION
#
#

def isStmtFragment(fragment):
	return isStmt( fragment.getModel() )

def isTopLevelFragment(fragment):
	return isTopLevel( fragment.getModel() )



#
#
# DOM / CONTEXT NAVIGATION
#
#

def getStatementContextFromElement(element):
	context = element.getFragmentContext()
	
	assert context is not None
	
	while not isStmt( context.getModel() ):
		context = context.getParent()
	return context


def getParentStatementContext(ctx):
	ctx = ctx.getParent()
	while ctx is not None  and  not isStmt( ctx.getModel() )  and  not isTopLevel( ctx.getModel() ):
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
		
def getStatementDepth(ctx):
	depth = -1
	while ctx is not None:
		depth += 1
		ctx = getParentStatementContext( ctx )
	return depth
	
	

	

			
		

	
	
#
#
# DOCUMENT EDITING
#
#


def pyForceNodeRefresh(data):
	pyReplaceNode( data, data )

def pyReplaceNode(data, replacement):
	data.become( replacement )

def modifySuiteMinimisingChanges(target, modified):
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
	
	xs = modified
	for i, x in enumerate( modified[commonPrefixLen:len(modified)-commonSuffixLen] ):
		if x in target[:commonPrefixLen]  or  x in target[len(target)-commonSuffixLen:]:
			if xs is modified:
				xs = copy.copy( modified )
			xs[commonPrefixLen+i] = x.deepCopy()
	
	target[commonPrefixLen:len(target)-commonSuffixLen] = xs[commonPrefixLen:len(modified)-commonSuffixLen]

	


#
#
# STREAM OPERATIONS
#
#

def getMinDepthOfStream(itemStream):
	depth = 0
	minDepth = 0
	for item in itemStream.getItems():
		if isinstance( item, StreamValue.StructuralItem ):
			v = item.getStructuralValue()
			if v.isInstanceOf( Schema.Indent ):
				depth += 1
				minDepth = min( minDepth, depth )
			elif v.isInstanceOf( Schema.Dedent ):
				depth -= 1
				minDepth = min( minDepth, depth )
	return minDepth


def getDepthOffsetOfStream(itemStream):
	depth = 0
	for item in itemStream.getItems():
		if isinstance( item, StreamValue.StructuralItem ):
			v = item.getStructuralValue()
			if v.isInstanceOf( Schema.Indent ):
				depth += 1
			elif v.isInstanceOf( Schema.Dedent ):
				depth -= 1
	return depth


def joinStreamsAroundDeletionPoint(before, after):
	beforeOffset = getDepthOffsetOfStream( before )
	afterOffset = getDepthOffsetOfStream( after )
	builder = StreamValueBuilder()
	if ( beforeOffset + afterOffset )  ==  0:
		builder.extend( before )
		builder.extend( after )
	else:
		offset = beforeOffset + afterOffset
		builder.extend( before )
		afterItems = after.getItems()
		if len( afterItems ) > 0:
			firstItem = afterItems[0]
			if isinstance( firstItem, StreamValue.TextItem ):
				builder.appendStreamValueItem( firstItem )
				afterItems = list(afterItems)[1:]
				
		if offset < 0:
			for i in xrange( 0, -offset ):
				builder.appendStructuralValue( Schema.Indent() )
		else:
			for i in xrange( 0, offset ):
				builder.appendStructuralValue( Schema.Dedent() )

		for x in afterItems:
			builder.appendStreamValueItem( x )
	return builder.stream()



def _extendStreamWithoutDedents(builder, itemStream, startDepth):
	depth = startDepth
	indentsToSkip = 0
	for item in itemStream.getItems():
		if isinstance( item, StreamValue.StructuralItem ):
			v = item.getStructuralValue()
			if v.isInstanceOf( Schema.Indent ):
				if indentsToSkip > 0:
					indentsToSkip -= 1
					item = None
				else:
					depth += 1
			elif v.isInstanceOf( Schema.Dedent ):
				if depth == 0:
					indentsToSkip += 1
					item = None
				else:
					depth -= 1
		if item is not None:
			builder.appendStreamValueItem( item )
	


def joinStreamsForInsertion(commonRootCtx, before, insertion, after):
	# Work out how much 'room' we have to play with, in case the inserted data dedents
	rootDepth = getStatementDepth( commonRootCtx )
	
	beforeOffset = getDepthOffsetOfStream( before )
	insertionOffset = getDepthOffsetOfStream( insertion )
	insertionMinDepth = getMinDepthOfStream( insertion )
	afterOffset = getDepthOffsetOfStream( after )
	
	builder = StreamValueBuilder()
	if ( beforeOffset + insertionOffset + afterOffset )  ==  0   and   ( beforeOffset + insertionMinDepth )  >=  0:
		builder.extend( before )
		builder.extend( insertion )
		builder.extend( after )
	else:
		offset = beforeOffset + insertionOffset + afterOffset
		builder.extend( before )
		if ( beforeOffset + insertionMinDepth )  <  0:
			_extendStreamWithoutDedents( builder, insertion, beforeOffset )
		else:
			builder.extend( insertion )
		
		afterItems = after.getItems()
		if len( afterItems ) > 0:
			firstItem = afterItems[0]
			if isinstance( firstItem, StreamValue.TextItem ):
				builder.appendStreamValueItem( firstItem )
				afterItems = list(afterItems)[1:]
				
		if offset < 0:
			for i in xrange( 0, -offset ):
				builder.appendStructuralValue( Schema.Indent() )
		else:
			for i in xrange( 0, offset ):
				builder.appendStructuralValue( Schema.Dedent() )

		for x in afterItems:
			builder.appendStreamValueItem( x )
	return builder.stream()
