##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMList, DMObject

#from Britefury.DocTree.DocTreeNode import DocTreeNode
from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from Britefury.Util.NodeUtil import *


def _dataForStr(data):
	if isinstance( data, DocTreeNode ):
		return _dataForStr( data.getNode() )
	elif isinstance( data, List ):
		return [ x   for x in data ]
	else:
		return data
	


def replace(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		if parent is None:
			print 'EditOperations:replace(): no parent ', target
		index = parent.indexOfById( target.getNode() )
		if index == -1:
			raise ValueError, 'could not replace'
		parent[index] = replacement
		return parent[index]
	else:
		raise TypeError, 'EditOperations:replace(): @target must be a DocTreeNode'
	
	
def replaceNodeContents(ctx, node, replacement):
	if isinstance( node, DocTreeNode ):
		if isinstance( node, DocTreeObject )  and  ( isinstance( replacement, DocTreeObject )  or  isinstance( replacement, DMObject ) ):
			node.become( replacement )
		elif isinstance( node, DocTreeList )  and  isinstance( replacement, DMListInterface ):
			node[:] = replacement
		else:
			raise TypeError, 'cannot replace contents of a %s with a %s'  %  ( type( node ), type( replacement ) )
	else:
		raise TypeError, 'EditOperations:replace(): @node must be a DocTreeNode'
	
	
def replaceWithRange(ctx, target, replacement):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		if parent is None:
			print 'EditOperations:replaceWithRange(): no parent ', target
		index = parent.indexOfById( target.getNode() )
		if index == -1:
			raise ValueError, 'could not replace with range'
		parent[index:index+1] = replacement
		return parent[index:index+len(replacement)]
	else:
		raise TypeError, 'EditOperations:replaceWithRange(): @target must be a DocTreeNode; it is a %s'  %  ( type( target ), )
	
	
	

def append(ctx, target, data):
	if isinstance( target, DocTreeNode ):
		#target.append( data )
		target.add( data )
		return target[-1]
	else:
		raise TypeError, 'EditOperations:append(): @target must be a DocTreeNode'

def prepend(ctx, target, data):
	if isinstance( target, DocTreeNode ):
		#target.insert( 0, data )
		target.add( 0, data )
		return target[0]
	else:
		raise TypeError, 'EditOperations:prepend(): @target must be a DocTreeNode'

def insertElement(ctx, target, index, data):
	if isinstance( target, DocTreeNode ):
		#parent.insert( index, data )
		target.add( index, data )
		return target[index]
	else:
		raise TypeError, 'EditOperations:insertElement(): @target must be a DocTreeNode'
	
def insertRange(ctx, target, index, data):
	if isinstance( target, DocTreeNode ):
		#parent.insert( index, data )
		target.addAll( index, data )
		return target[index:index+len(data)]
	else:
		raise TypeError, 'EditOperations:insertRange(): @target must be a DocTreeNode'
	
def insertBefore(ctx, target, data):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		index = parent.indexOfById( target.getNode() )
		if index == -1:
			raise ValueError, 'could not insert before'
		#parent.insert( index, data )
		parent.add( index, data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertBefore(): @target must be a DocTreeNode'


def insertRangeBefore(ctx, target, data):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		index = parent.indexOfById( target.getNode() )
		if index == -1:
			raise ValueError, 'could not insert range before'
		parent[index:index] = data
		return parent[index:index+len(data)]
	else:
		raise TypeError, 'EditOperations:insertRangeBefore(): @target must be a DocTreeNode'


def insertAfter(ctx, target, data):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		index = parent.getNode().indexOfById( target.getNode() )
		if index == -1:
			raise ValueError, 'could not insert after'
		index += 1
		#parent.insert( index, data )
		parent.add( index, data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertAfter(): @target must be a DocTreeNode'

	
def insertRangeAfter(ctx, target, data):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		index = parent.getNode().indexOfById( target.getNode() )
		if index == -1:
			raise ValueError, 'could not insert range after'
		index += 1
		parent[index:index] = data
		return parent[index:index+len(data)]
	else:
		raise TypeError, 'EditOperations:insertRangeAfter(): @target must be a DocTreeNode'


def remove(ctx, target):
	if isinstance( target, DocTreeNode ):
		parent = target.getParentTreeNode()
		parent.remove( target.getNode() )
		return None
	else:
		raise TypeError, 'EditOperations:remove(): @target must be a DocTreeNode'

	
	
