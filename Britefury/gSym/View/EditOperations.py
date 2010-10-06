##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMList, DMObject, DMNode



from Britefury.Util.NodeUtil import *


def _dataForStr(data):
	if isinstance( data, List ):
		return [ x   for x in data ]
	else:
		return data
	


def replace(ctx, target, replacement):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		if parent is None:
			print 'EditOperations:replace(): no parent ', target
		index = parent.indexOfById( target )
		if index == -1:
			raise ValueError, 'could not replace'
		parent[index] = replacement
		return parent[index]
	else:
		raise TypeError, 'EditOperations:replace(): @target must be a DMNode'
	
	
def replaceNodeContents(ctx, node, replacement):
	if isinstance( node, DMNode ):
		if isinstance( node, DMObject )  and  isinstance( replacement, DMObject ):
			node.become( replacement.deepCopy() )
		elif isinstance( node, DMList )  and  isinstance( replacement, DMListInterface ):
			node[:] = replacement
		else:
			raise TypeError, 'cannot replace contents of a %s with a %s'  %  ( type( node ), type( replacement ) )
	else:
		raise TypeError, 'EditOperations:replace(): @node must be a DMNode'
	
	
def replaceWithRange(ctx, target, replacement):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		if parent is None:
			print 'EditOperations:replaceWithRange(): no parent ', target
		index = parent.indexOfById( target )
		if index == -1:
			raise ValueError, 'could not replace with range'
		parent[index:index+1] = replacement
		return parent[index:index+len(replacement)]
	else:
		raise TypeError, 'EditOperations:replaceWithRange(): @target must be a DMNode; it is a %s'  %  ( type( target ), )
	
	
	

def append(ctx, target, data):
	if isinstance( target, DMNode ):
		#target.append( data )
		target.add( data )
		return target[-1]
	else:
		raise TypeError, 'EditOperations:append(): @target must be a DMNode'

def prepend(ctx, target, data):
	if isinstance( target, DMNode ):
		#target.insert( 0, data )
		target.add( 0, data )
		return target[0]
	else:
		raise TypeError, 'EditOperations:prepend(): @target must be a DMNode'

def insertElement(ctx, target, index, data):
	if isinstance( target, DMNode ):
		#parent.insert( index, data )
		target.add( index, data )
		return target[index]
	else:
		raise TypeError, 'EditOperations:insertElement(): @target must be a DMNode'
	
def insertRange(ctx, target, index, data):
	if isinstance( target, DMNode ):
		#parent.insert( index, data )
		target.addAll( index, data )
		return target[index:index+len(data)]
	else:
		raise TypeError, 'EditOperations:insertRange(): @target must be a DMNode'
	
def insertBefore(ctx, target, data):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		index = parent.indexOfById( target )
		if index == -1:
			raise ValueError, 'could not insert before'
		#parent.insert( index, data )
		parent.add( index, data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertBefore(): @target must be a DMNode'


def insertRangeBefore(ctx, target, data):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		index = parent.indexOfById( target )
		if index == -1:
			raise ValueError, 'could not insert range before'
		parent[index:index] = data
		return parent[index:index+len(data)]
	else:
		raise TypeError, 'EditOperations:insertRangeBefore(): @target must be a DMNode'


def insertAfter(ctx, target, data):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		index = parent.indexOfById( target )
		if index == -1:
			raise ValueError, 'could not insert after'
		index += 1
		#parent.insert( index, data )
		parent.add( index, data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertAfter(): @target must be a DMNode'

	
def insertRangeAfter(ctx, target, data):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		index = parent.indexOfById( target )
		if index == -1:
			raise ValueError, 'could not insert range after'
		index += 1
		parent[index:index] = data
		return parent[index:index+len(data)]
	else:
		raise TypeError, 'EditOperations:insertRangeAfter(): @target must be a DMNode'


def remove(ctx, target):
	if isinstance( target, DMNode ):
		parent = target.getValidParents()[0]
		parent.remove( target )
		return None
	else:
		raise TypeError, 'EditOperations:remove(): @target must be a DMNode'

	
	
