##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.DocModel.RelativeNode import relative, RelativeNode, RelativeList



def _sanitiseInputData(data):
	if isinstance( data, RelativeNode ):
		return _sanitiseInputData( data.node )
	elif isinstance( data, list ):
		return [ _sanitiseInputData( x )   for x in data ]
	elif isinstance( data, tuple ):
		return tuple( [ _sanitiseInputData( x )   for x in data ] )
	else:
		return data


def replace(data, replacement):
	if isinstance( data, RelativeNode ):
		if not isinstance( data.parent, DMListInterface ):
			raise TypeError, '$replace: @data.parent must be a DMListInterface, not %s'  %  ( type( data.parent ), )
		data.parent[data.indexInParent] = _sanitiseInputData( replacement )
		return relative( data.parent[data.indexInParent], data.parent, data.indexInParent )
	else:
		raise TypeError, '$replace: @data must be a RelativeNode'
	
	
	

def append(x, data):
	if isinstance( x, RelativeNode ):
		x.node.append( data )
		return relative( x[-1], x, len( x ) - 1 )
	else:
		raise TypeError, '$append: @x must be a RelativeNode'

def prepend(x, data):
	if isinstance( x, RelativeNode ):
		x.node.insert( 0, data )
		return relative( x[0], x, 0 )
	else:
		raise TypeError, '$prepend: @x must be a RelativeNode'

def insertBefore(x, data):
	if isinstance( x, RelativeNode ):
		if not isinstance( x.parent, DMListInterface ):
			raise TypeError, '$insertBefore: @x.parent must be a DMListInterface, not %s'  %  ( type( x.parent ), )
		index = x.parent.index( x.node )
		x.parent.insert( index, _sanitiseInputData( data ) )
		return relative( x.parent[index], x.parent, index )
	else:
		raise TypeError, '$insertBefore: @x must be a RelativeNode'


def insertAfter(x, data):
	if isinstance( x, RelativeNode ):
		if not isinstance( x.parent, DMListInterface ):
			raise TypeError, '$insertAfter: @x.parent must be a DMListInterface, not %s'  %  ( type( x.parent ), )
		index = x.parent.index( x.node ) + 1
		x.parent.insert( index, _sanitiseInputData( data ) )
		return relative( x.parent[index], x.parent, index )
	else:
		raise TypeError, '$insertAfter: @x must be a RelativeNode'



