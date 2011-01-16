##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


gSymVersion = '0.1.5-alpha'



def _stringVersionToNumericVersion(v):
	if not isinstance( v, str )  and not isinstance( v, unicode ):
		raise TypeError, 'Version must be a string'
	return tuple( [ int( x )   for x in v.split( '-' )[0].split( '.' ) ] )


def compareVersions(a, b):
	return cmp( _stringVersionToNumericVersion( a ), _stringVersionToNumericVersion( b ) )




import unittest

class TestCase_gSymVersion (unittest.TestCase):
	def test_stringVersionToNumericVersion(self):
		self.assert_( _stringVersionToNumericVersion( '0.1' )  ==  ( 0, 1 ) )
		self.assert_( _stringVersionToNumericVersion( u'0.1' )  ==  ( 0, 1 ) )
		self.assertRaises( TypeError, lambda: _stringVersionToNumericVersion( [] ) )
		self.assertRaises( ValueError, lambda: _stringVersionToNumericVersion( '0a.1' ) )
		
	def test_compareVersions(self):
		self.assert_( compareVersions( '0.1', '0.2' )  ==  -1 )
		self.assert_( compareVersions( '0.2', '0.1' )  ==  1 )
		self.assert_( compareVersions( '0.1', '0.1' )  ==  0 )
		self.assert_( compareVersions( '0.1', '0.1.1' )  ==  -1 )
	
