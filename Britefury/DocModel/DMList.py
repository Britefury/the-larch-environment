##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary
from copy import copy, deepcopy

from Britefury.FileIO.IOXml import ioObjectFactoryRegister, ioReadObjectFromString, ioWriteObjectAsString

from Britefury.Cell.LiteralCell import LiteralRefCell

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel import DMListCommandTracker
from Britefury.DocModel.DMIOWrite import __writesx__












class DMList (DMListInterface):
	__slots__ = [ '_commandTracker_', '_cell' ]

	trackerClass = DMListCommandTracker.DMListCommandTracker



	def __init__(self, value=None, _p_memo={}):
		super( DMList, self ).__init__()


		self._cell = LiteralRefCell()
		if value is None:
			value = []
		else:
			value = [ self._p_coerce( x, _p_memo )   for x in value ]
		self._cell.literalValue = value

		self._commandTracker_ = None


	def __writecontentsx__(self, stream, nodeToIndex):
		stream.write( '(' )
		if len( self ) > 0:
			for v in self[:-1]:
				#v.__writesx__( stream, nodeToIndex )
				__writesx__( stream, nodeToIndex, v )
				stream.write( ' ' )
			#self[-1].__writesx__( stream, nodeToIndex )
			__writesx__( stream, nodeToIndex, self[-1] )
		stream.write( ')' )


	def __str__(self):
		return '(' + ' '.join( [ str( v )  for v in self._cell.literalValue ] ) + ')'


	def _p_coerce(self, x, _p_memo=None):
		if isinstance( x, list )  or  isinstance( x, tuple ):
			if _p_memo is None:
				_p_memo = {}
			else:
				try:
					return _p_memo[id(x)]
				except KeyError:
					pass
			y = DMList( x, _p_memo )
			if _p_memo is not None:
				_p_memo[id(x)] = y
			return y
		else:
			return x


	def append(self, x):
		v = self._cell.literalValue
		x = self._p_coerce( x )
		v.append( x )
		self._cell.literalValue = v
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onAppended( self, x )


	def extend(self, xs):
		memo = {}
		v = self._cell.literalValue
		xs = [ self._p_coerce( x, memo )   for x in xs ]
		v.extend( xs )
		self._cell.literalValue = v
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onExtended( self, xs )

	def insert(self, i, x):
		v = self._cell.literalValue
		x = self._p_coerce( x )
		v.insert( i, x )
		self._cell.literalValue = v
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onInserted( self, i, x )

	def remove(self, x):
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onRemove( self, x )
		v = self._cell.literalValue
		v.remove( x )
		self._cell.literalValue = v

	def __setitem__(self, i, x):
		v = self._cell.literalValue
		oldV = copy( v )
		if isinstance( i, slice ):
			memo = {}
			x = [ self._p_coerce( p, memo )   for p in x ]
		else:
			x = self._p_coerce( x )
		v[i] = x
		self._cell.literalValue = v
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSet( self, oldV, v )

	def __delitem__(self, i):
		v = self._cell.literalValue
		oldV = copy( v )
		del v[i]
		self._cell.literalValue = v
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSet( self, oldV, v )


	def __getitem__(self, i):
		return self._cell.value[i]

	def __contains__(self, x):
		return x in self._cell.value

	def __iter__(self):
		return iter( self._cell.value )

	def __add__(self, xs):
		return self._cell.value + xs

	def __len__(self):
		return len( self._cell.value )

	def index(self, x):
		return self._cell.value.index( x )


	def getDestList(self, layer):
		return layer.getDestList( self )

	def getSrcList(self, layer):
		return layer.getSrcList( self )



	def __copy__(self):
		c = DMList()
		c._cell.literalValue = self._cell.literalValue
		return c

	def __deepcopy__(self, memo):
		c = DMList()
		c._cell.literalValue = deepcopy( self._cell.literalValue, memo )
		return c



ioObjectFactoryRegister( 'DMList', DMList )




import unittest
from Britefury.CommandHistory import CommandHistory



class TestCase_LiteralList (unittest.TestCase):
	def _p_checkListFormat(self, xs):
		self.assert_( not isinstance( xs, list )  and not isinstance( xs, tuple ) )
		if isinstance( xs, DMList ):
			for x in xs:
				self._p_checkListFormat( x )

	def testLiteralListCtor(self):
		x = DMList()



	def testIter(self):
		x = DMList()
		x.extend( [ 1, 2, 3 ] )
		q = [ p   for p in x ]
		self.assert_( q == [ 1, 2, 3 ] )


	def testAppend(self):
		x = DMList()
		x.append( 1 )
		self.assert_( x[0] == 1 )
		x.append( [ 10, 20 ] )
		self.assert_( x[1][:] == [ 10, 20 ] )
		self.assert_( isinstance( x[1], DMListInterface ) )
		x.append( [ [ 40, 50 ], [ 50, [ 60, 70 ] ] ] )
		self.assert_( x[2][0][:] == [ 40, 50 ] )
		self.assert_( x[2][1][0] == 50 )
		self.assert_( x[2][1][1][:] == [ 60, 70 ] )
		self._p_checkListFormat( x )


	def testExtend(self):
		x = DMList()
		x.extend( [ 1, 2, 3 ] )
		self.assert_( x[0] == 1 )
		self.assert_( x[1] == 2 )
		self.assert_( x[2] == 3 )
		self.assert_( x[:] == [ 1, 2, 3 ] )
		x.extend( [ [ 10 ], [ 20 ], [ 30 ] ] )
		self.assert_( x[3][0] == 10 )
		self.assert_( x[4][0] == 20 )
		self.assert_( x[5][0] == 30 )
		self._p_checkListFormat( x )


	def testInsert(self):
		x = DMList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insert( 2, 12 )
		self.assert_( x[:] == [ 1, 2, 12, 3, 4, 5 ] )
		x.insert( 2, [ 13 ] )
		self.assert_( x == [ 1, 2, [ 13 ], 12, 3, 4, 5 ] )
		self._p_checkListFormat( x )



	def testRemove(self):
		x = DMList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.remove( 3 )
		self.assert_( x[:] == [ 1, 2, 4, 5 ] )
		self._p_checkListFormat( x )




	def testSet(self):
		x = DMList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x[4] = 12
		self.assert_( x[:] == [ 1, 2, 3, 4, 12 ] )
		x[1:3] = [ 20, 21, 22 ]
		self.assert_( x[:] == [ 1, 20, 21, 22, 4, 12 ] )
		self._p_checkListFormat( x )


	def testDel(self):
		x = DMList()
		x.extend( range( 0, 10 ) )
		self.assert_( x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		del x[4]
		self.assert_( x[:] == [ 0, 1, 2, 3, 5, 6, 7, 8, 9 ] )
		del x[3:6]
		self.assert_( x[:] == [ 0, 1, 2, 7, 8, 9 ] )
		self._p_checkListFormat( x )





	def testIOXml(self):
		xxa = DMList( [ 'a', 'b', 'c' ] )

		x = DMList( [ 1, 2, 3 ] )
		xx1 = DMList( [ 'plus2', 5, 6, 7 ] )
		xx1.append( xxa )
		x.append( xx1 )
		xx2 = DMList( [ 'times2', 11, 12, 13 ] )
		xx2.append( xxa )
		x.append( xx2 )


		s = ioWriteObjectAsString( x )
		y = ioReadObjectFromString( s )

		self.assert_( y[0:3] == [ 1, 2, 3 ] )
		self.assert_( y[3][:-1] == [ 'plus2', 5, 6, 7 ] )
		self.assert_( y[4][:-1] == [ 'times2', 11, 12, 13 ] )
		self.assert_( y[3][-1] is y[4][-1] )
		self.assert_( y[3][-1][:] == [ 'a', 'b', 'c' ] )
		self.assert_( y[4][-1][:] == [ 'a', 'b', 'c' ] )



	def _testUndo(self, opFunc):
		ch = CommandHistory.CommandHistory()
		dmxs = DMList( range( 0, 10 ) )
		ch.track( dmxs )

		xs = range( 0, 10 )
		xxs = range( 0, 10 )

		opFunc( dmxs )
		opFunc( xs )

		self.assert_( dmxs == xs )

		ch.undo()

		self.assert_( dmxs == xxs )


	def testAppendUndo(self):
		def _append(xs):
			xs.append( 23 )
		self._testUndo( _append )


	def testExtendUndo(self):
		def _extend(xs):
			xs.extend( range( 20, 25 ) )
		self._testUndo( _extend )


	def testInsertUndo(self):
		def _insert(xs):
			xs.insert( 5, 20 )
		self._testUndo( _insert )


	def testRemoveUndo(self):
		def _remove(xs):
			xs.remove( 7 )
		self._testUndo( _remove )


	def testSetUndo(self):
		def _set(xs):
			xs[3:5] = range( 20, 30 )
		self._testUndo( _set )


	def testDelUndo(self):
		def _del(xs):
			del xs[3:5]
		self._testUndo( _del )





	def testDiamondStructure(self):
		sourceA = [ 1, 2, 3 ]
		sourceB = [ 5, 6, sourceA, 7, [ 8, [ sourceA, 9 ], 10 ] ]
		b = DMList( sourceB )
		self.assert_( b[2] is b[4][1][0] )


	def testDiamondStructure2(self):
		sourceA = [ 1, 2, 3 ]
		sourceB = [ 5, 6, sourceA, 7, 8, sourceA, 9, 10 ]
		b = DMList()
		b.extend( sourceB )
		b.append( sourceB )
		self.assert_( b[2] is b[5] )
		self.assert_( b[-1][2] is b[-1][5] )
		b[2:5] = [ sourceA, sourceA ]
		self.assert_( b[2] is b[3] )





if __name__ == '__main__':
	unittest.main()
