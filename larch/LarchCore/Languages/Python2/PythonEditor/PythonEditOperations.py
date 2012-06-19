##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import copy


from java.lang import Object
from java.io import IOException
from java.util import List
from java.awt.event import KeyEvent
from java.awt.datatransfer import UnsupportedFlavorException, DataFlavor, StringSelection, Transferable

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface, DMNode



from BritefuryJ.LSpace.StyleParams import *
from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace import *

from BritefuryJ.Util.RichString import RichStringBuilder, RichString



from LarchCore.Languages.Python2 import Schema

from LarchCore.Languages.Python2.PythonEditor.Parser import Python2Grammar
from LarchCore.Languages.Python2.PythonEditor.Precedence import *


class NotImplementedError (Exception):
	pass




#
#
# NODE CLASSIFICATION
#
#

def isExpr(node):
	return isinstance( node, DMObjectInterface )  and  ( node.isInstanceOf( Schema.Expr )  or  node.isInstanceOf( Schema.UNPARSED ) )

def isValidExpr(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.Expr )

def isStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.Stmt )

def isCompoundStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.CompoundStmt )

def isCompoundStmtHeader(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Schema.CompountStmtHeader )

def isCompoundStmtOrCompoundHeader(node):
	return isinstance( node, DMObjectInterface )  and  ( node.isInstanceOf( Schema.CompoundStmt )  or  node.isInstanceOf( Schema.CompountStmtHeader ) )

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
# PRESENTATION TREE / FRAGMENT NAVIGATION
#
#

def getStatementContextFromElement(element):
	fragment = element.getFragmentContext()
	
	assert fragment is not None
	
	while not isStmt( fragment.getModel() ):
		fragment = fragment.getParent()
	return fragment


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


def pyReplaceNode(data, replacement):
	data.become( replacement )

def pyReplaceStatementWithStatementRange(stmt, replacement):
	suite = stmt.getParent()
	if isinstance( suite, DMList ):
		index = suite.indexOfById( stmt )
		suite[index:index+1] = replacement
	else:
		raise TypeError, 'statement must be contained within a suite (a DMList)'
	
def pyReplaceStatementRangeWithStatement(suite, i, j, replacement):
	suite[i:j] = [ replacement ]
	
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
			xs[commonPrefixLen+i] = copy.deepcopy( x )
	
	target[commonPrefixLen:len(target)-commonSuffixLen] = xs[commonPrefixLen:len(modified)-commonSuffixLen]

	


#
#
# RICH STRING OPERATIONS
#
#

def getMinDepthOfRichString(richStr):
	depth = 0
	minDepth = 0
	for item in richStr.getItems():
		if item.isStructural():
			v = item.getValue()
			if v.isInstanceOf( Schema.Indent ):
				depth += 1
				minDepth = min( minDepth, depth )
			elif v.isInstanceOf( Schema.Dedent ):
				depth -= 1
				minDepth = min( minDepth, depth )
	return minDepth


def getDepthOffsetOfRichString(richStr):
	depth = 0
	for item in richStr.getItems():
		if item.isStructural():
			v = item.getValue()
			if v.isInstanceOf( Schema.Indent ):
				depth += 1
			elif v.isInstanceOf( Schema.Dedent ):
				depth -= 1
	return depth


def joinRichStringsAroundDeletionPoint(before, after):
	beforeOffset = getDepthOffsetOfRichString( before )
	afterOffset = getDepthOffsetOfRichString( after )
	builder = RichStringBuilder()
	if ( beforeOffset + afterOffset )  ==  0:
		builder.extend( before )
		builder.extend( after )
	else:
		offset = beforeOffset + afterOffset
		builder.extend( before )
		afterItems = after.getItems()
		if len( afterItems ) > 0:
			firstItem = afterItems[0]
			if isinstance( firstItem, RichString.TextItem ):
				builder.appendRichStringItem( firstItem )
				afterItems = list(afterItems)[1:]
				
		if offset < 0:
			for i in xrange( 0, -offset ):
				builder.appendStructuralValue( Schema.Indent() )
		else:
			for i in xrange( 0, offset ):
				builder.appendStructuralValue( Schema.Dedent() )

		for x in afterItems:
			builder.appendRichStringItem( x )
	return builder.richString()



def _extendRichStringWithoutDedents(builder, richStr, startDepth):
	depth = startDepth
	indentsToSkip = 0
	for item in richStr.getItems():
		if item.isStructural():
			v = item.getValue()
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
			builder.appendRichStringItem( item )
	


def joinRichStringsForInsertion(commonRootCtx, before, insertion, after):
	# Work out how much 'room' we have to play with, in case the inserted data dedents
	rootDepth = getStatementDepth( commonRootCtx )
	
	beforeOffset = getDepthOffsetOfRichString( before )
	insertionOffset = getDepthOffsetOfRichString( insertion )
	insertionMinDepth = getMinDepthOfRichString( insertion )
	afterOffset = getDepthOffsetOfRichString( after )
	
	builder = RichStringBuilder()
	if ( beforeOffset + insertionOffset + afterOffset )  ==  0   and   ( beforeOffset + insertionMinDepth )  >=  0:
		builder.extend( before )
		builder.extend( insertion )
		builder.extend( after )
	else:
		offset = beforeOffset + insertionOffset + afterOffset
		builder.extend( before )
		if ( beforeOffset + insertionMinDepth )  <  0:
			_extendRichStringWithoutDedents( builder, insertion, beforeOffset )
		else:
			builder.extend( insertion )
		
		afterItems = after.getItems()
		if len( afterItems ) > 0:
			firstItem = afterItems[0]
			if isinstance( firstItem, RichString.TextItem ):
				builder.appendRichStringItem( firstItem )
				afterItems = list(afterItems)[1:]
				
		if offset < 0:
			for i in xrange( 0, -offset ):
				builder.appendStructuralValue( Schema.Indent() )
		else:
			for i in xrange( 0, offset ):
				builder.appendStructuralValue( Schema.Dedent() )

		for x in afterItems:
			builder.appendRichStringItem( x )
	return builder.richString()





# Special form inserting

class _InsertSpecialFormTreeEvent (TextEditEvent):
	def __init__(self, leaf):
		super( _InsertSpecialFormTreeEvent, self ).__init__( leaf, None, None )


def _insertSpecialFormAtMarker(marker, specialForm):
	element = marker.getElement()
	index = marker.getIndex()
	assert isinstance( element, LSContentLeafEditable )
	
	value = element.getRichString()
	builder = RichStringBuilder()
	builder.append( value[:index] )
	builder.appendStructuralValue( specialForm )
	builder.append( value[index:] )
	modifiedValue = builder.richString()
	
	event = _InsertSpecialFormTreeEvent( element )
	visitor = event.getRichStringVisitor()
	visitor.setElementFixedValue( element, modifiedValue )

	element.postTreeEvent( event )




def insertSpecialFormExpressionAtMarker(marker, specialForm):
	_insertSpecialFormAtMarker( marker, specialForm )


def insertSpecialFormExpressionAtCaret(caret, specialForm):
	return insertSpecialFormExpressionAtMarker( caret.getMarker(), specialForm )



def insertSpecialFormStatementAtMarker(marker, specialForm):
	_insertSpecialFormAtMarker( marker, Schema._temp_SpecialFormStmtWrapper( value=specialForm ) )


def insertSpecialFormStatementAtCaret(caret, specialForm):
	return insertSpecialFormStatementAtMarker( caret.getMarker(), specialForm )





# Embedded object removal

class RemoveEmbeddedObjectTreeEvent (object):
	pass

def requestRemoveEmbeddedObjectContainingElement(element):
	return element.postTreeEvent( RemoveEmbeddedObjectTreeEvent() )





#
#
# SELECTION / TARGET ACQUISITION
#
#


def _getNodeAtMarker(marker, modelTestFn):
	"""
	marker - a Marker
	modelTestFn - function(node) -> True or False
	
	returns
		the node that contains marker, if it passes modelTestFn
		or
		None
	"""
	if marker.isValid():
		element = marker.getElement()
		fragment = element.getFragmentContext()
		model = fragment.model
		if modelTestFn( model ):
			return model
	return None




def _getSelectedNode(selection, modelTestFn):
	"""
	selection - a TextSelection
	modelTestFn - function(node) -> True or False
	
	returns
		the node that contains @selection, if it passes modelTestFn
		or
		None
	"""
	if isinstance( selection, TextSelection ):
		commonRoot = selection.getCommonRoot()
		if commonRoot is not None:
			fragment = commonRoot.getFragmentContext()
			model = fragment.model
			if modelTestFn( model ):
				return model
	return None




def _findNodeAtMarker(marker, modelTestFn):
	"""
	marker - a Marker
	modelTestFn - function(node) -> True or False
	
	returns
		first node containing @marker that passes modelTestFn
		or
		None
	"""
	if marker.isValid():
		element = marker.getElement()
		fragment = element.getFragmentContext()
		
		while fragment is not None:
			model = fragment.model
			if modelTestFn( model ):
				return model
			if isTopLevel( model ):
				return False
			fragment = fragment.getParent()
	return None




def _findSelectedNode(selection, modelTestFn):
	"""
	selection - a TextSelection
	modelTestFn - function(node) -> True or False
	
	returns
		first node containing @selection that passes modelTestFn
		or
		None
	"""
	if isinstance( selection, TextSelection ):
		commonRoot = selection.getCommonRoot()
		if commonRoot is not None:
			fragment = commonRoot.getFragmentContext()
		
			while fragment is not None:
				model = fragment.model
				if modelTestFn( model ):
					return model
				if isTopLevel( model ):
					return False
				fragment = fragment.getParent()
	return None




def getExpressionAtMarker(marker):
	"""
	marker - a Marker
	
	returns
		expression that contains marker
		or
		None
	"""
	return _getNodeAtMarker( marker, isExpr )


def getStatementAtMarker(marker):
	"""
	marker - a Marker
	
	returns
		expression that contains marker
		or
		None
	"""
	return _findNodeAtMarker( marker, isStmt )
	



def getSelectedExpression(selection):
	"""
	selection - a TextSelection
	
	returns
		expression that contains selection
		or
		None
	"""
	return _getSelectedNode( selection, isExpr )


def getSelectedStatement(selection):
	"""
	selection - a TextSelection
	
	returns
		statement that contains selection
		or
		None
	"""
	return _findSelectedNode( selection, isStmt )



def getSelectedStatementRange(selection):
	"""
	selection - a TextSelection
	Note that the statements under the start and end points of the selection must reside within the same suite
	
	returns
		(suite, i, j)
			suite - the suite that contains @selection
			i, j - the start and end indices of the range  -  suite[i:j]
	"""
	if selection.isValid():
		startStmt = getStatementAtMarker( selection.getStartMarker() )
		endStmt = getStatementAtMarker( selection.getEndMarker() )
		suite = startStmt.getParent()
		if suite is endStmt.getParent():
			i = suite.indexOfById( startStmt )
			j = suite.indexOfById( endStmt ) + 1
			return suite, i, j
	return None



	