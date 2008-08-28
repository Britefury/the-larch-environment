##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.DocTree.DocTreeNode import DocTreeNode



def _sanitiseInputData(data):
	if isinstance( data, DocTreeNode ):
		return _sanitiseInputData( data.node )
	elif isinstance( data, list ):
		return [ _sanitiseInputData( x )   for x in data ]
	elif isinstance( data, tuple ):
		return tuple( [ _sanitiseInputData( x )   for x in data ] )
	else:
		return data


def replace(data, replacement):
	if isinstance( data, DocTreeNode ):
		parent = data.parentTreeNode
		if parent is None:
			print 'NONE: ', data
		parent[data.indexInParent] = _sanitiseInputData( replacement )
		return parent[data.indexInParent]
	else:
		raise TypeError, '$replace: @data must be a DocTreeNode'
	
	
	

def append(x, data):
	if isinstance( x, DocTreeNode ):
		x.append( data )
		return x[-1]
	else:
		raise TypeError, '$append: @x must be a DocTreeNode'

def prepend(x, data):
	if isinstance( x, DocTreeNode ):
		x.insert( 0, data )
		return x[0]
	else:
		raise TypeError, '$prepend: @x must be a DocTreeNode'

def insertBefore(x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		index = parent.index( x.node )
		parent.insert( index, _sanitiseInputData( data ) )
		return parent[index]
	else:
		raise TypeError, '$insertBefore: @x must be a DocTreeNode'


def insertAfter(x, data):
	if isinstance( x, DocTreeNode ):
		parent = x.parentTreeNode
		index = parent.node.index( x.node ) + 1
		parent.insert( index, _sanitiseInputData( data ) )
		return parent[index]
	else:
		raise TypeError, '$insertAfter: @x must be a DocTreeNode'


	
	
