##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy, deepcopy
import sys
import weakref

from Britefury.Kernel.Abstract import abstractmethod
from Britefury.DocModel.DMNode import DMNode
from Britefury.DocModel.DMListInterface import DMListInterface, isDMListCompatible


from Britefury.DocTree.DocTreeNode import DocTreeNode
from Britefury.DocTree.DocTreeList import DocTreeList
from Britefury.DocTree.DocTreeString import DocTreeString
from Britefury.DocTree.DocTreeUnicode import DocTreeUnicode





	
	
class _DocTreeKeyError (Exception):
	pass


class _Key (object):
	__slots__ = [ '_docNode', '_parent', '_index', '_table', '_hash' ]
	
	
	def __init__(self, docNode, parentTreeNode, index, table):
		super( _Key, self ).__init__()
		if isinstance( docNode, DMNode ):
			self._docNode = weakref.ref( docNode, self.__remove )
		else:
			self._docNode = docNode

		if isinstance( parentTreeNode, DocTreeNode ):
			self._parent = weakref.ref( parentTreeNode, self.__remove )
		else:
			self._parent = parentTreeNode

		self._index = index
		self._table = table
		try:
			self._hash = hash( ( docNode, parentTreeNode, index ) )
		except TypeError:
			print docNode
			print parentTreeNode
			print index
			raise


	def __remove(self, weak):
		if self._table is not None:
			self._table._remove( self )


	def __hash__(self):
		return self._hash

	def _asTuple(self):
		if isinstance( self._docNode, weakref.ref ):
			docNode = self._docNode()
		else:
			docNode = self._docNode

		if isinstance( self._parent, weakref.ref ):
			parent = self._parent()
		else:
			parent = self._parent

		return docNode, parent, self._index


	def __cmp__(self, x):
		return cmp( self._asTuple(), x._asTuple() )

	def __str__(self):
		return '%s:<%s>'  %  ( type( self ), self._asTuple() )

	def __repr__(self):
		return '%s:<%s>'  %  ( type( self ), self._asTuple() )


	def _docTreeKey(self):
		if isinstance( self._docNode, weakref.ref ):
			docNode = self._docNode()
		else:
			docNode = self._docNode

		if isinstance( self._parent, weakref.ref ):
			parent = self._parent()
		else:
			parent = self._parent

		return _DocTreeKey( docNode, parent, self._index )




class _DocTreeKey (object):
	__slots__ = [ '_docNode', '_parent', '_index' ]
	
	def __init__(self, docNode, parentTreeNode, index):
		super( _DocTreeKey, self ).__init__()
		if isinstance( docNode, DMNode ):
			self._docNode = weakref.ref( docNode )
		else:
			self._docNode = docNode

		if isinstance( parentTreeNode, DMNode ):
			self._parent = weakref.ref( parentTreeNode )
		else:
			self._parent = parentTreeNode

		self._index = index


	def getDocNode(self):
		if isinstance( self._docNode, weakref.ref ):
			return self._docNode()
		else:
			return self._docNode

	def getParent(self):
		if isinstance( self._parent, weakref.ref ):
			return self._parent()
		else:
			return self._parent

	def getIndex(self):
		return self._index


	def _key(self, d=None):
		if isinstance( self._docNode, weakref.ref ):
			docNode = self._docNode()
			if docNode is None:
				raise _DocTreeKeyError
		else:
			docNode = self._docNode

		if isinstance( self._parent, weakref.ref ):
			parent = self._parent()
			if parent is None:
				raise _DocTreeKeyError
		else:
			parent = self._parent

		return _Key( docNode, parent, self._index, d )


	def _asTuple(self):
		if isinstance( self._docNode, weakref.ref ):
			docNode = self._docNode()
		else:
			docNode = self._docNode

		if isinstance( self._parent, weakref.ref ):
			parent = self._parent()
		else:
			parent = self._parent

		return docNode, parent, self._index
	
	
	def __cmp__(self, x):
		return cmp( self._asTuple(), x._asTuple() )


	def __str__(self):
		return '%s:<%s>'  %  ( type( self ), self._asTuple() )

	def __repr__(self):
		return '%s:<%s>'  %  ( type( self ), self._asTuple() )


	docNode = property( getDocNode )
	parent = property( getParent )
	index = property( getIndex )





class _DocTreeNodeTable (object):
	def __init__(self):
		#self._data = weakref.WeakValueDictionary()
		self._data = {}


	def _remove(self, _k):
		try:
			del self._data[_k]
		except KeyError:
			pass



	def __getitem__(self, k):
		try:
			return self._data[k._key()]
		except _DocTreeKeyError:
			return None

	def __setitem__(self, k, v):
		try:
			self._data[k._key(self)] = v
		except _DocTreeKeyError:
			pass

	def __delitem__(self, k):
		try:
			del self._data[k._key()]
		except _DocTreeKeyError:
			pass


	def __contains__(self, k):
		try:
			return k._key() in self._data
		except _DocTreeKeyError:
			return False

	def __len__(self):
		return len( self._data )


	def __iter__(self):
		return iter( [ k._docTreeKey()   for k in self._data ] )

	def keys(self):
		return [ k._docTreeKey()   for k in self._data.keys() ]

	def values(self):
		return self._data.values()

	def items(self):
		return [ ( k._docTreeKey(), v )   for k, v in self._data.items() ]






class DocTree (object):
	def __init__(self):
		super( DocTree, self ).__init__()
		
		self._table = _DocTreeNodeTable()
		
		
		
	def treeNode(self, x, parentTreeNode=None, indexInParent=-1):
		key = _DocTreeKey( x, parentTreeNode, indexInParent )
		
		try:
			docTreeNode = self._table[key]
		except KeyError:		
			if isDMListCompatible( x ):
				docTreeNode = DocTreeList( self, x, parentTreeNode, indexInParent )
			elif isinstance( x, str ):
				docTreeNode = DocTreeString._build( self, x, parentTreeNode, indexInParent )
			elif isinstance( x, unicode ):
				docTreeNode = DocTreeUnicode._build( self, x, parentTreeNode, indexInParent )
			else:
				docTreeNode = DocTreeNode( self, x, parentTreeNode, indexInParent )
			
			self._table[key] = docTreeNode
		
		return docTreeNode
	
	





import unittest
from Britefury.DocModel.DMList import DMList


class TestCase_DocTreeNodeTable (unittest.TestCase):
	def testDocNodeKey(self):
		x = DMNode()
		y = DMNode()

		kx = _DocTreeKey( x, None, -1 )
		ky = _DocTreeKey( y, kx, 1 )
		self.assert_( ky.docNode is y )
		self.assert_( ky.parent is kx )
		self.assert_( ky.index is 1 )


	def testTable(self):
		import gc
		
		class Value (object):
			pass


		x = DMNode()
		y = DMNode()
		a = Value()

		kx = _DocTreeKey( x, None, -1 )
		ky = _DocTreeKey( y, kx, 1 )

		t = _DocTreeNodeTable()

		self.assert_( ky not in t )
		self.assert_( len( t ) == 0 )

		t[ky] = a

		self.assert_( ky in t )
		self.assert_( len( t ) == 1 )
		self.assert_( t[ky] is a )

		self.assert_( t.keys() == [ ky ] )
		self.assert_( t.values() == [ a ] )
		self.assert_( t.items() == [ ( ky, a ) ] )


		del a

		#self.assert_( ky not in t )
		#self.assert_( len( t ) == 0 )

		a = Value()
		t[ky] = a

		self.assert_( ky in t )
		self.assert_( len( t ) == 1 )
		self.assert_( t[ky] is a )

		del y
		gc.collect()

		self.assert_( len( t ) == 0 )

		y = DMNode()
		ky = _DocTreeKey( y, kx, 1 )
		t[ky] = a

		self.assert_( ky in t )
		self.assert_( len( t ) == 1 )
		self.assert_( t[ky] is a )

		del y
		gc.collect()

		self.assert_( len( t ) == 0 )




class TestCase_DocTree (unittest.TestCase):
	def testWrapStrRange(self):
		tree = DocTree()
		a = DMList( [ str( i )   for i in range( 0, 10 ) ] )
		w_a = tree.treeNode( a, None, 0 )
		w_5 = w_a[5]
		self.assert_( isinstance( w_5, DocTreeString ) )
		self.assert_( w_5 == '5' )
		self.assert_( w_5.parentTreeNode is w_a )
		self.assert_( w_5.indexInParent == 5 )
		self.assert_( ','.join( w_a )  ==  '0,1,2,3,4,5,6,7,8,9' )

	def testWrapUnicodeRange(self):
		tree = DocTree()
		a = DMList( [ unicode( i )   for i in range( 0, 10 ) ] )
		w_a = tree.treeNode( a, None, 0 )
		w_5 = w_a[5]
		self.assert_( isinstance( w_5, DocTreeUnicode ) )
		self.assert_( w_5 == u'5' )
		self.assert_( w_5.parentTreeNode is w_a )
		self.assert_( w_5.indexInParent == 5 )
		self.assert_( u','.join( w_a )  ==  u'0,1,2,3,4,5,6,7,8,9' )

		
		
		
	def _buildDiamond(self):
		dd = DMList( [ 'd' ] )
		dc = DMList( [ dd ] )
		db = DMList( [ dd ] )
		da = DMList( [ db, dc ] )
		return da, db, dc, dd
	
	
	def _buildTree(self):
		da, db, dc, dd = self._buildDiamond()
		tree = DocTree()
		ta = tree.treeNode( da )
		return da, db, dc, dd, tree, ta
		
	
	
	def testDiamond(self):
		da, db, dc, dd = self._buildDiamond()
		
		self.assert_( da[0][0][0] == 'd' )
		self.assert_( da[0][0] is da[1][0] )
		
		
	def testTree(self):
		da, db, dc, dd, tree, ta = self._buildTree()
		
		tb = ta[0]
		tc = ta[1]
		tbd = tb[0]
		tcd = tc[0]
		
		self.assert_( tbd is not tcd )
		self.assert_( tbd.node is tcd.node )
		
		# modify
		de = DMList( [ dd ] )
		dc[0] = de
		
		self.assert_( dc[0][0]  is dd )
		self.assert_( dc[0][0]  is db[0] )
		
		
		tc2 = ta[1]
		te = tc2[0]
		ted = te[0]
		
		
		self.assert_( tbd is not ted )
		self.assert_( tc2 is tc )
		self.assert_( ted.node is tbd.node )
		
