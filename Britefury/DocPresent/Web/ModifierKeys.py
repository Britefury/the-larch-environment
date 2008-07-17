##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************


MOD_CTRL = 1
MOD_SHIFT = 2
MOD_ALT = 4


def modifierKeyStringToFlags(x):
	if isinstance( x, str )   or   isinstance( x, unicode ):
		x = [ y.strip()   for y in x.lower().split( ',' ) ]
		mods = 0
		if 'ctrl' in x   or   'control' in x:
			mods |= MOD_CTRL
		if 'shift' in x:
			mods |= MOD_SHIFT
		if 'alt' in x   or   'mod1' in x:
			mods |= MOD_ALT
		return mods
	else:
		return x
	
	
import unittest

class TestCase_modifierKeys (unittest.TestCase):
	def test_modifierKeyStringToFlags(self):
		self.assert_( modifierKeyStringToFlags( 1 ) == 1 )
		self.assert_( modifierKeyStringToFlags( 'ctrl' ) == MOD_CTRL )
		self.assert_( modifierKeyStringToFlags( 'control' ) == MOD_CTRL )
		self.assert_( modifierKeyStringToFlags( 'shift' ) == MOD_SHIFT )
		self.assert_( modifierKeyStringToFlags( 'alt' ) == MOD_ALT )
		self.assert_( modifierKeyStringToFlags( 'mod1' ) == MOD_ALT )
	
		self.assert_( modifierKeyStringToFlags( 'ctrl, shift, alt' ) == MOD_CTRL | MOD_SHIFT | MOD_ALT )


