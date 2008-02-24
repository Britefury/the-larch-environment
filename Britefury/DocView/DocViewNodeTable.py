##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import ref, WeakValueDictionary

from Britefury.DocModel.DMNode import DMNode




class _DocNodeKeyError (Exception):
	pass


class _Key (object):
	def __init__(self, docNode, parent, index, d):
		super( _Key, self ).__init__()
		if isinstance( docNode, DMNode ):
			self._docNode = ref( docNode, self._p_remove )
		else:
			self._docNode = docNode

		if isinstance( parent, DMNode ):
			self._parent = ref( parent, self._p_remove )
		else:
			self._parent = parent

		self._index = index
		self._d = d
		try:
			self._hash = hash( ( docNode, parent, index ) )
		except TypeError:
			print docNode
			print parent
			print index
			raise


	def _p_remove(self, weak):
		if self._d is not None:
			self._d._p_remove( self )


	def __hash__(self):
		return self._hash

	def _p_asTuple(self):
		if isinstance( self._docNode, ref ):
			docNode = self._docNode()
		else:
			docNode = self._docNode

		if isinstance( self._parent, ref ):
			parent = self._parent()
		else:
			parent = self._parent

		return docNode, parent, self._index


	def __cmp__(self, x):
		return cmp( self._p_asTuple(), x._p_asTuple() )

	def __str__(self):
		return '%s:<%s>'  %  ( type( self ), self._p_asTuple() )

	def __repr__(self):
		return '%s:<%s>'  %  ( type( self ), self._p_asTuple() )


	def _p_docNodeKey(self):
		if isinstance( self._docNode, ref ):
			docNode = self._docNode()
		else:
			docNode = self._docNode

		if isinstance( self._parent, ref ):
			parent = self._parent()
		else:
			parent = self._parent

		return DocNodeKey( docNode, parent, self._index )




class DocNodeKey (object):
	def __init__(self, docNode, parentDocNode, index):
		super( DocNodeKey, self ).__init__()
		if isinstance( docNode, DMNode ):
			self._docNode = ref( docNode )
		else:
			self._docNode = docNode

		if isinstance( parentDocNode, DMNode ):
			self._parent = ref( parentDocNode )
		else:
			self._parent = parentDocNode

		self._index = index


	def getDocNode(self):
		if isinstance( self._docNode, ref ):
			return self._docNode()
		else:
			return self._docNode

	def getParentDocNode(self):
		if isinstance( self._parent, ref ):
			return self._parent()
		else:
			return self._parent

	def getIndex(self):
		return self._index


	def _p_key(self, d=None):
		if isinstance( self._docNode, ref ):
			docNode = self._docNode()
			if docNode is None:
				raise _DocNodeKeyError
		else:
			docNode = self._docNode

		if isinstance( self._parent, ref ):
			parent = self._parent()
			if parent is None:
				raise _DocNodeKeyError
		else:
			parent = self._parent

		return _Key( docNode, parent, self._index, d )


	def _p_asTuple(self):
		if isinstance( self._docNode, ref ):
			docNode = self._docNode()
		else:
			docNode = self._docNode

		if isinstance( self._parent, ref ):
			parent = self._parent()
		else:
			parent = self._parent

		return docNode, parent, self._index


	def __cmp__(self, x):
		return cmp( self._p_asTuple(), x._p_asTuple() )


	def __str__(self):
		return '%s:<%s>'  %  ( type( self ), self._p_asTuple() )

	def __repr__(self):
		return '%s:<%s>'  %  ( type( self ), self._p_asTuple() )


	docNode = property( getDocNode )
	parentDocNode = property( getParentDocNode )
	index = property( getIndex )





class DocViewNodeTable (object):
	def __init__(self):
		self._data = WeakValueDictionary()


	def _p_remove(self, _k):
		try:
			del self._data[_k]
		except KeyError:
			pass



	def __getitem__(self, k):
		try:
			return self._data[k._p_key()]
		except _DocNodeKeyError:
			return None

	def __setitem__(self, k, v):
		try:
			self._data[k._p_key(self)] = v
		except _DocNodeKeyError:
			pass

	def __delitem__(self, k):
		try:
			del self._data[k._p_key()]
		except _DocNodeKeyError:
			pass


	def __contains__(self, k):
		try:
			return k._p_key() in self._data
		except _DocNodeKeyError:
			return False

	def __len__(self):
		return len( self._data )


	def __iter__(self):
		return iter( [ k._p_docNodeKey()   for k in self._data ] )

	def keys(self):
		return [ k._p_docNodeKey()   for k in self._data.keys() ]

	def values(self):
		return self._data.values()

	def items(self):
		return [ ( k._p_docNodeKey(), v )   for k, v in self._data.items() ]




import unittest



class TestCase_DocViewNodeTable (unittest.TestCase):
	def testDocNodeKey(self):
		x = DMNode()
		y = DMNode()

		k = DocNodeKey( x, y, 1 )
		self.assert_( k.docNode is x )
		self.assert_( k.parentDocNode is y )
		self.assert_( k.index is 1 )


	def testTable(self):
		import gc


		x = DMNode()
		y = DMNode()
		a = DMNode()

		k = DocNodeKey( x, y, 1 )

		t = DocViewNodeTable()

		self.assert_( k not in t )
		self.assert_( len( t ) == 0 )

		t[k] = a

		self.assert_( k in t )
		self.assert_( len( t ) == 1 )
		self.assert_( t[k] is a )

		self.assert_( t.keys() == [ k ] )
		self.assert_( t.values() == [ a ] )
		self.assert_( t.items() == [ ( k, a ) ] )


		del a

		self.assert_( k not in t )
		self.assert_( len( t ) == 0 )

		a = DMNode()
		t[k] = a

		self.assert_( k in t )
		self.assert_( len( t ) == 1 )
		self.assert_( t[k] is a )

		del x
		gc.collect()

		self.assert_( len( t ) == 0 )

		x = DMNode()
		k = DocNodeKey( x, y, 1 )
		t[k] = a

		self.assert_( k in t )
		self.assert_( len( t ) == 1 )
		self.assert_( t[k] is a )

		del x
		gc.collect()

		self.assert_( len( t ) == 0 )




if __name__ == '__main__':
	unittest.main()