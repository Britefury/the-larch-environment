##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMList

#from Britefury.DocTree.DocTreeNode import DocTreeNode
from BritefuryJ.DocTree import DocTreeNode



def _sanitiseInputData(data):
	if isinstance( data, DocTreeNode ):
		return _sanitiseInputData( data.getNode() )
	elif isinstance( data, list ):
		return DMList( [ _sanitiseInputData( x )   for x in data ] )
	elif isinstance( data, tuple ):
		return DMList( [ _sanitiseInputData( x )   for x in data ] )
	elif isinstance( data, List ):
		return DMList( [ _sanitiseInputData( x )   for x in data ] )
	else:
		return data
	
	
def _dataForStr(data):
	if isinstance( data, DocTreeNode ):
		return _dataForStr( data.getNode() )
	elif isinstance( data, List ):
		return [ _sanitiseInputData( x )   for x in data ]
	else:
		return data
	


def replace(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		parent = data.getParentTreeNode()
		if parent is None:
			print 'EditOperations:replace(): no parent ', data
		index = parent.indexOfById( data.getNode() )
		parent[index] = _sanitiseInputData( replacement )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:replace(): @data must be a DocTreeNode'
	
	
def replaceNodeContents(ctx, node, replacement):
	if isinstance( node, DocTreeNode ):
		node[:] = _sanitiseInputData( replacement )
		return node
	else:
		raise TypeError, 'EditOperations:replace(): @node must be a DocTreeNode'
	
	
def replaceWithRange(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		parent = data.getParentTreeNode()
		if parent is None:
			print 'EditOperations:replaceWithRange(): no parent ', data
		index = parent.indexOfById( data.getNode() )
		parent[index:index+1] = _sanitiseInputData( replacement )
		return parent[index:index+len(replacement)]
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

def insertElement(ctx, xs, index, data):
	if isinstance( xs, DocTreeNode ):
		#parent.insert( index, _sanitiseInputData( data ) )
		xs.add( index, _sanitiseInputData( data ) )
		return xs[index]
	else:
		raise TypeError, 'EditOperations:insertElement(): @xs must be a DocTreeNode'
	
def insertRange(ctx, xs, index, data):
	if isinstance( xs, DocTreeNode ):
		#parent.insert( index, _sanitiseInputData( data ) )
		xs.addAll( index, _sanitiseInputData( data ) )
		return xs[index]
	else:
		raise TypeError, 'EditOperations:insertRange(): @xs must be a DocTreeNode'
	
def insertBefore(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.getParentTreeNode()
		index = parent.indexOfById( x.getNode() )
		#parent.insert( index, _sanitiseInputData( data ) )
		parent.add( index, _sanitiseInputData( data ) )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertBefore(): @x must be a DocTreeNode'


def insertRangeBefore(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.getParentTreeNode()
		index = parent.indexOfById( x.getNode() )
		parent[index:index] = _sanitiseInputData( data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertRangeBefore(): @x must be a DocTreeNode'


def insertAfter(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.getParentTreeNode()
		index = parent.getNode().indexOfById( x.getNode() ) + 1
		#parent.insert( index, _sanitiseInputData( data ) )
		parent.add( index, _sanitiseInputData( data ) )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertAfter(): @x must be a DocTreeNode'

	
def insertRangeAfter(ctx, x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.getParentTreeNode()
		index = parent.getNode().indexOfById( x.getNode() ) + 1
		parent[index:index] = _sanitiseInputData( data )
		return parent[index]
	else:
		raise TypeError, 'EditOperations:insertRangeAfter(): @x must be a DocTreeNode'


def remove(ctx, x):
	if isinstance( x, DocTreeNode ):
		parent = x.getParentTreeNode()
		parent.remove( x.getNode() )
		return None
	else:
		raise TypeError, 'EditOperations:remove(): @x must be a DocTreeNode'

	
	
