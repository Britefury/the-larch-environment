##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMList

from Britefury.DocTree.DocTreeNode import DocTreeNode



def _sanitiseInputData(data):
	if isinstance( data, DocTreeNode ):
		return _sanitiseInputData( data.node )
	elif isinstance( data, list ):
		return DMList( [ _sanitiseInputData( x )   for x in data ] )
	elif isinstance( data, tuple ):
		return DMList( [ _sanitiseInputData( x )   for x in data ] )
	else:
		return data


def replace(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		parent = data.parentTreeNode
		if parent is None:
			print 'EditOperations:replace(): no parent ', data
		parent.getNode()[data.indexInParent] = _sanitiseInputData( replacement )
		return parent[data.indexInParent]
	else:
		raise TypeError, 'EditOperations:replace(): @data must be a DocTreeNode'
	
	
def replaceWithRange(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		parent = data.parentTreeNode
		if parent is None:
			print 'EditOperations:replaceWithRange(): no parent ', data
		parent.getNode()[data.indexInParent:data.indexInParent+1] = _sanitiseInputData( replacement )
		return parent[data.indexInParent]
	else:
		raise TypeError, 'EditOperations:replaceWithRange(): @data must be a DocTreeNode'
	
	
	

def append(ctx, xs, data):
	if isinstance( x, DocTreeNode ):
		#xs.append( data )
		xs.add( data )
		return xs[-1]
	else:
		raise TypeError, 'EditOperations:append(): @x must be a DocTreeNode'

def prepend(ctx, xs, data):
	if isinstance( xs, DocTreeNode ):
		#xs.insert( 0, data )
		xs.add( 0, data )
		return xs[0]
	else:
		raise TypeError, 'EditOperations:prepend(): @x must be a DocTreeNode'

def insertBefore(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		index = parent.index( x.node )
		#parent.insert( index, _sanitiseInputData( data ) )
		parent.add( index, _sanitiseInputData( data ) )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertBefore(): @x must be a DocTreeNode'


def insertRangeBefore(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		index = parent.index( x.node )
		parent[index:index] = _sanitiseInputData( data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertRangeBefore(): @x must be a DocTreeNode'


def insertAfter(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		index = parent.node.index( x.node ) + 1
		#parent.insert( index, _sanitiseInputData( data ) )
		parent.add( index, _sanitiseInputData( data ) )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertAfter(): @x must be a DocTreeNode'

	
def insertRangeAfter(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		index = parent.node.index( x.node ) + 1
		parent[index:index] = _sanitiseInputData( data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertRangeAfter(): @x must be a DocTreeNode'


def remove(ctx, x):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		parent.remove( x.node )
		return None
	else:
		raise TypeError, 'EditOperations:remove(): @x must be a DocTreeNode'

	
	
