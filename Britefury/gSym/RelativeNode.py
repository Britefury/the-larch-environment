##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy, deepcopy

from Britefury.Kernel.Abstract import abstractmethod
from Britefury.DocModel.DMNode import DMNode
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.GLispInterpreter import isGLispList



def relative(x, parent, index):
	if isGLispList( x ):
		return RelativeList( x, parent, index )
	elif isinstance( x, unicode ):
		return RelativeUnicode( x, parent, index )
	elif isinstance( x, str ):
		return RelativeString( x, parent, index )
	else:
		return RelativeNode( x, parent, index )


class RelativeNode (object):
	def __init__(self, node, parentNode, indexInParent):
		super( RelativeNode, self ).__init__()
		self._rln_node = node
		self._rln_parentNode = parentNode
		self._rln_indexInParent = indexInParent
		
		
	def __getattr__(self, name):
		return getattr( self._rln_node, name )
	
	def getNode(self):
		return self._rln_node
	
	def getParent(self):
		return self._rln_parentNode
	
	def getIndexInParent(self):
		return self._rln_indexInParent
	
	
	def __cmp__(self, x):
		if isinstance( x, RelativeNode ):
			return cmp( self._rln_node, x._rln_node )
		else:
			return cmp( self._rln_node, x )


		
	node = property( getNode )
	parent = property( getParent )
	indexInParent = property( getIndexInParent )
	




class RelativeList (RelativeNode, DMListInterface):
	def __setitem__(self, i, x):
		self._rln_node[i] = x

	def __delitem__(self, i):
		del self._rln_node[i]

	def __getitem__(self, i):
		if isinstance( i, slice ):
			return [ relative( x, self, j )   for j, x in zip( i.indices( self._rln_node ), self._rln_node[i] ) ]
		else:
			return relative( self._rln_node[i], self, i )

	def __contains__(self, x):
		return x in self._rln_node

	def __iter__(self):
		for i, x in enumerate( self._rln_node ):
			yield relative( x, self, i )

	def __add__(self, xs):
		if isinstance( xs, RelativeList ):
			return RelativeList( self._rln_node + xs._rln_node )
		else:
			return self._rln_node + xs

	def __len__(self):
		return len( self._rln_node )



	def __copy__(self):
		return RelativeList( copy( self._rln_node ) )

	def __deepcopy__(self, memo):
		return RelativeList( deepcopy( self._rln_node, memo ) )





class RelativeString (RelativeNode):
	def __getitem__(self, i):
		if isinstance( i, slice ):
			return [ relative( x, self, j )   for j, x in zip( i.indices( self._rln_node ), self._rln_node[i] ) ]
		else:
			return relative( self._rln_node[i], self, i )

	def __contains__(self, x):
		return x in self._rln_node

	def __iter__(self):
		for i, x in enumerate( self._rln_node ):
			yield relative( x, self, i )

	def __add__(self, xs):
		if isinstance( xs, RelativeList ):
			return self._rln_node + xs._rln_node
		else:
			return self._rln_node + xs

	def __mul__(self, i):
		return self._rln_node * i

	def __len__(self):
		return len( self._rln_node )



	def __copy__(self):
		return RelativeList( copy( self._rln_node ) )

	def __deepcopy__(self, memo):
		return RelativeList( deepcopy( self._rln_node, memo ) )


		
		
class RelativeUnicode (RelativeString):
	pass



import unittest


class TestCase_RelativeNode (unittest.TestCase):
	def testWrapRange(self):
		a = range( 0, 10 )
		n_a = relative( a, None, 0 )
		n_5 = n_a[5]
		self.assert_( n_5 == 5 )
		self.assert_( n_5.parent is n_a )
		self.assert_( n_5.indexInParent == 5 )

	def testWrapStrRange(self):
		a = [ str( i )   for i in range( 0, 10 ) ]
		n_a = relative( a, None, 0 )
		n_5 = n_a[5]
		self.assert_( n_5 == '5' )
		self.assert_( n_5.parent is n_a )
		self.assert_( n_5.indexInParent == 5 )

	def testWrapUnicodeRange(self):
		a = [ unicode( i )   for i in range( 0, 10 ) ]
		n_a = relative( a, None, 0 )
		n_5 = n_a[5]
		self.assert_( n_5 == u'5' )
		self.assert_( n_5.parent is n_a )
		self.assert_( n_5.indexInParent == 5 )
