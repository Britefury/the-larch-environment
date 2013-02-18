##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************

import unittest

from Britefury.Util.LiveList import LiveList

from BritefuryJ.ChangeHistory import ChangeHistory, Trackable

import random



class _Value (object):
	def __init__(self, x):
		self.__change_history__ = None
		self.x = x
	
	
	def __get_trackable_contents__(self):
		return [ self.x ]
		
		
	def isTracked(self):
		return self.__change_history__ is not None
		
		
	def __eq__(self, x):
		return isinstance( x, _Value )  and  self.x == x.x
	
	def __str__(self):
		return 'Value( %s )'  %  str( self.x )
	
	def __repr__(self):
		return 'Value( %s )'  %  str( self.x )
	
	def __cmp__(self, x):
		return cmp( self.x, x.x )


class Test_LiveList (unittest.TestCase):
	def setUp(self):
		self.history = ChangeHistory()
		self.ls = LiveList()
		self.history.track( self.ls )
		self.prevxs = None
		self.newxs = None
		self.ls.changeListener = self._onChanged

	def tearDown(self):
		self.ls.changeListener = None
		self.history.stopTracking( self.ls )
		self.ls = None
		self.history = None
		self.prevxs = None
		self.newxs = None


	def _onChanged(self, oldContents, newContents):
		self.prevxs = oldContents
		self.newxs = newContents


	def _test_changes(self, expectedOldContents, expectedNewContents):
		self.assertEqual(self.prevxs, expectedOldContents)
		self.assertEqual(self.newxs, expectedNewContents)
		self.prevxs = None
		self.newxs = None

	
	def test_setitem(self):
		self.assertEqual( self.ls[:], [] )
		
		_two = _Value( -2 )
		self.ls.append( _two )
		self.assertEqual( self.ls[:], [ _two ] )
		self.assertTrue( _two.isTracked() )
		self._test_changes( [], [ _two ] )
		
		_one = _Value( -1 )
		self.ls[0] = _one
		self.assertEqual( self.ls[:], [ _one ] )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		self._test_changes( [ _two ], [ _one ] )

		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		self.ls[1:] = _rng
		self.assertEqual( self.ls[:], [ _one ] + _rng )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertTrue( x.isTracked() )
		self._test_changes( [ _one ], [ _one ] + _rng )

		
		self.history.undo()
		self.assertEqual( self.ls[:], [ _one ] )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertFalse( x.isTracked() )
		self._test_changes( [ _one ] + _rng, [ _one ] )

		
		self.history.undo()
		self.assertEqual( self.ls[:], [ _two ] )
		self.assertFalse( _one.isTracked() )
		self.assertTrue( _two.isTracked() )
		self._test_changes( [ _one ], [ _two ] )

		
		self.history.redo()
		self.assertEqual( self.ls[:], [ _one ] )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertFalse( x.isTracked() )
		self._test_changes( [ _two ], [ _one ] )

			
		self.history.redo()
		self.assertEqual( self.ls[:], [ _one ] + _rng )
		self.assertFalse( _two.isTracked() )
		self.assertTrue( _one.isTracked() )
		for x in _rng:
			self.assertTrue( x.isTracked() )
		self._test_changes( [ _one ], [ _one ] + _rng )

			
			
		
	def test_delitem(self):
		self.assertEqual( self.ls[:], [] )
		
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		self.ls[:] = _rng
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _rng )

		
		del self.ls[0]
		self.assertEqual( self.ls[:], _rng[1:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, True, True, True ] )
		self._test_changes( _rng, _rng[1:] )

		
		del self.ls[1:3]
		self.assertEqual( self.ls[:], [ _rng[1], _rng[4] ] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, False, False, True ] )
		self._test_changes( _rng[1:], [ _rng[1], _rng[4] ] )

		
		self.history.undo()
		self.assertEqual( self.ls[:], _rng[1:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, True, True, True ] )
		self._test_changes( [ _rng[1], _rng[4] ], _rng[1:] )

		
		self.history.undo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rng[1:], _rng )

		
		self.history.redo()
		self.assertEqual( self.ls[:], _rng[1:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, True, True, True ] )
		self._test_changes( _rng, _rng[1:] )

		
		self.history.redo()
		self.assertEqual( self.ls[:], [ _rng[1], _rng[4] ] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, True, False, False, True ] )
		self._test_changes( _rng[1:], [ _rng[1], _rng[4] ] )

			
		
	def test_append(self):
		self.assertEqual( self.ls[:], [] )
		
		v = _Value( 2 )
		self.ls.append( v )
		self.assertTrue( v.isTracked() )
		
		self.assertEqual( self.ls[:], [ _Value( 2 ) ] )
		self._test_changes( [], [ v ] )

		self.history.undo()
		self.assertEqual( self.ls[:], [] )
		self.assertFalse( v.isTracked() )
		self._test_changes( [ v ], [] )

		self.history.redo()
		self.assertEqual( self.ls[:], [ _Value( 2 ) ] )
		self.assertTrue( v.isTracked() )
		self._test_changes( [], [ v ] )

			
		
	def test_extend(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		self.assertEqual( self.ls[:], [] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, False, False, False, False ] )

		self.ls.extend( _rng )
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _rng )

		self.history.undo()
		self.assertEqual( self.ls[:], [] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ False, False, False, False, False ] )
		self._test_changes( _rng, [] )

		self.history.redo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _rng )

	

	def test_insert(self):
		v = _Value( 20 )
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]

		self.ls[:] = _rng
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertFalse( v.isTracked() )
		self._test_changes( [], _rng )

		self.ls.insert( 2, v )
		self.assertEqual( self.ls[:], _rng[:2] + [ v ] + _rng[2:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertTrue( v.isTracked() )
		self._test_changes( _rng, _rng[:2] + [ v ] + _rng[2:] )

		self.history.undo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertFalse( v.isTracked() )
		self._test_changes( _rng[:2] + [ v ] + _rng[2:], _rng )

		self.history.redo()
		self.assertEqual( self.ls[:], _rng[:2] + [ v ] + _rng[2:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self.assertTrue( v.isTracked() )
		self._test_changes( _rng, _rng[:2] + [ v ] + _rng[2:] )

	

	def test_pop(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]

		self.ls[:] = _rng
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _rng )

		v = self.ls.pop()
		self.assertTrue( v is _rng[-1] )
		self.assertEqual( self.ls[:], _rng[:-1] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, False ] )
		self._test_changes( _rng, _rng[:-1] )

		self.history.undo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rng[:-1], _rng )

		self.history.redo()
		self.assertEqual( self.ls[:], _rng[:-1] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, False ] )
		self._test_changes( _rng, _rng[:-1] )

	

	def test_remove(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]

		self.ls[:] = _rng
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _rng )

		self.ls.remove( _Value( 2 ) )
		self.assertEqual( self.ls[:], _rng[:2] + _rng[3:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, False, True, True ] )
		self._test_changes( _rng, _rng[:2] + _rng[3:] )

		self.history.undo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rng[:2] + _rng[3:], _rng )

		self.history.redo()
		self.assertEqual( self.ls[:], _rng[:2] + _rng[3:] )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, False, True, True ] )
		self._test_changes( _rng, _rng[:2] + _rng[3:] )

	

	def test_reverse(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		_rev = _rng[:]
		_rev.reverse()

		self.ls[:] = _rng
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _rng )

		self.ls.reverse()
		self.assertEqual( self.ls[:], _rev )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rng, _rev )

		self.history.undo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rev, _rng )

		self.history.redo()
		self.assertEqual( self.ls[:], _rev )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rng, _rev )

	

	def test_sort(self):
		_rng = [ _Value( x )   for x in xrange( 0, 5 ) ]
		
		_shuf = _rng[:]
		r = random.Random( 12345 )
		r.shuffle( _shuf )

		self.ls[:] = _shuf
		self.assertEqual( self.ls[:], _shuf )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( [], _shuf )

		self.ls.sort()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _shuf, _rng )

		self.history.undo()
		self.assertEqual( self.ls[:], _shuf )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _rng, _shuf )

		self.history.redo()
		self.assertEqual( self.ls[:], _rng )
		self.assertEqual( [ x.isTracked() for x in _rng ],  [ True, True, True, True, True ] )
		self._test_changes( _shuf, _rng )

			

	def test_eq(self):
		self.ls[:] = range( 0, 5 )

		self.assertTrue( self.ls == range( 0, 5 ) )
		self.assertTrue( self.ls == LiveList( range( 0, 5 ) ) )
		self.assertFalse( self.ls == range( 0, 6 ) )
		self.assertFalse( self.ls == LiveList( range( 0, 6 ) ) )

		self.assertTrue( self.ls != range( 0, 6 ) )
		self.assertTrue( self.ls != LiveList( range( 0, 6 ) ) )
		self.assertFalse( self.ls != range( 0, 5 ) )
		self.assertFalse( self.ls != LiveList( range( 0, 5 ) ) )



	def test_str(self):
		self.ls[:] = range( 0, 5 )

		self.assertEqual( str( self.ls ), str( range( 0, 5 ) ) )


	def test_repr(self):
		self.ls[:] = range( 0, 5 )

		self.assertEqual( repr( self.ls ), 'LiveList( ' + repr( range( 0, 5 ) ) + ' )' )
