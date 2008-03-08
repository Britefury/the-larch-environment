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



def wrap(x, parent=None, index=0):
	if isGLispList( x ):
		return WrappedList( x, parent, index )
	elif isinstance( x, unicode ):
		return WrappedUnicode( x, parent, index )
	elif isinstance( x, str ):
		return WrappedString( x, parent, index )
	else:
		return WrappedNode( x, parent, index )


class WrappedNode (object):
	def __init__(self, node, parentNode, indexInParent):
		super( WrappedNode, self ).__init__()
		self._w_node = node
		self._w_parentNode = parentNode
		self._w_indexInParent = indexInParent
		
		
	def __getattr__(self, name):
		return getattr( self._w_node, name )
	
	def getNode(self):
		return self._w_node
	
	def getParent(self):
		return self._w_parentNode
	
	def getIndexInParent(self):
		return self._w_indexInParent
	
	
	def __cmp__(self, x):
		if isinstance( x, WrappedNode ):
			return cmp( self._w_node, x._w_node )
		else:
			return cmp( self._w_node, x )


		
	node = property( getNode )
	parent = property( getParent )
	indexInParent = property( getIndexInParent )
	




class WrappedList (WrappedNode, DMListInterface):
	def __setitem__(self, i, x):
		self._w_node[i] = x

	def __delitem__(self, i):
		del self._w_node[i]

	def __getitem__(self, i):
		if isinstance( i, slice ):
			return [ wrap( x, self, j )   for j, x in zip( i.indices( self._w_node ), self._w_node[i] ) ]
		else:
			return wrap( self._w_node[i], self, i )

	def __contains__(self, x):
		return x in self._w_node

	def __iter__(self):
		for i, x in enumerate( self._w_node ):
			yield wrap( x, self, i )

	def __add__(self, xs):
		if isinstance( xs, WrappedList ):
			return WrappedList( self._w_node + xs._w_node )
		else:
			return self._w_node + xs

	def __len__(self):
		return len( self._w_node )



	def __copy__(self):
		return WrappedList( copy( self._w_node ) )

	def __deepcopy__(self, memo):
		return WrappedList( deepcopy( self._w_node, memo ) )





class WrappedString (WrappedNode):
	def __getitem__(self, i):
		if isinstance( i, slice ):
			return [ wrap( x, self, j )   for j, x in zip( i.indices( self._w_node ), self._w_node[i] ) ]
		else:
			return wrap( self._w_node[i], self, i )

	def __contains__(self, x):
		return x in self._w_node

	def __iter__(self):
		for i, x in enumerate( self._w_node ):
			yield wrap( x, self, i )

	def __add__(self, xs):
		if isinstance( xs, WrappedList ):
			return self._w_node + xs._w_node
		else:
			return self._w_node + xs

	def __mul__(self, i):
		return self._w_node * i

	def __len__(self):
		return len( self._w_node )



	def __copy__(self):
		return WrappedList( copy( self._w_node ) )

	def __deepcopy__(self, memo):
		return WrappedList( deepcopy( self._w_node, memo ) )


		
		
class WrappedUnicode (WrappedString):
	pass



import unittest


class TestCase_WrappedNode (unittest.TestCase):
	def testWrapRange(self):
		a = range( 0, 10 )
		w_a = wrap( a )
		w_5 = w_a[5]
		self.assert_( w_5 == 5 )
		self.assert_( w_5.parent is w_a )
		self.assert_( w_5.indexInParent == 5 )

	def testWrapStrRange(self):
		a = [ str( i )   for i in range( 0, 10 ) ]
		w_a = wrap( a )
		w_5 = w_a[5]
		self.assert_( w_5 == '5' )
		self.assert_( w_5.parent is w_a )
		self.assert_( w_5.indexInParent == 5 )

	def testWrapUnicodeRange(self):
		a = [ unicode( i )   for i in range( 0, 10 ) ]
		w_a = wrap( a )
		w_5 = w_a[5]
		self.assert_( w_5 == u'5' )
		self.assert_( w_5.parent is w_a )
		self.assert_( w_5.indexInParent == 5 )
