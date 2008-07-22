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

MASKS = [ MOD_CTRL, MOD_SHIFT, MOD_ALT ]


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
	

	
	
def testEventModifierKeys_js(eventVarName, modifierKeysValue, modifierKeysMask):
	modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
	modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
	tests = []
	for mask, name in [ ( MOD_CTRL, 'ctrlKey' ),   ( MOD_SHIFT, 'shiftKey' ),   ( MOD_ALT, 'altKey' ) ]:
		if ( modifierKeysMask & mask )  !=  0:
			testValue = 'true'   if ( modifierKeysValue & mask )  !=  0   else   'false'
			tests.append( 'event.%s == %s'  %  ( name, testValue ) )
	result = '  &&  '.join( tests )
	return result   if result != ''   else   'true'
				

"""
    $(document).keydown(
            function(event)
            {
                switch( event.keyCode )
                {
                case 16:
                    $(".shift").addClass( "highlight" )
                    break;
                case 17:
                    $(".ctrl").addClass( "highlight" )
                    break;
                case 18:
                    $(".alt").addClass( "highlight" )
                    break;
                }
            }
        );
    $(document).keyup(
            function(event)
            {
                switch( event.keyCode )
                {
                case 16:
                    $(".shift").removeClass( "highlight" )
                    break;
                case 17:
                    $(".ctrl").removeClass( "highlight" )
                    break;
                case 18:
                    $(".alt").removeClass( "highlight" )
                    break;
                }
            }
        );
"""
	
	
	
	
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
		
		
	def test_testEventModifierKeys_js(self):
		self.assert_( testEventModifierKeys_js( 'event', '', '' )  ==  'true' )
		self.assert_( testEventModifierKeys_js( 'event', 'shift', 'shift' )  ==  'event.shiftKey == true' )
		self.assert_( testEventModifierKeys_js( 'event', 'ctrl', 'ctrl', )  ==  'event.ctrlKey == true' )
		self.assert_( testEventModifierKeys_js( 'event', 'alt', 'alt' )  ==  'event.altKey == true' )
		self.assert_( testEventModifierKeys_js( 'event', '', 'shift' )  ==  'event.shiftKey == false' )
		self.assert_( testEventModifierKeys_js( 'event', '', 'ctrl', )  ==  'event.ctrlKey == false' )
		self.assert_( testEventModifierKeys_js( 'event', '', 'alt' )  ==  'event.altKey == false' )
		self.assert_( testEventModifierKeys_js( 'event', 'alt, ctrl, shift', 'shift, ctrl, alt' )  ==  'event.ctrlKey == true  &&  event.shiftKey == true  &&  event.altKey == true' )
		self.assert_( testEventModifierKeys_js( 'event', 'alt, shift', 'shift, ctrl, alt' )  ==  'event.ctrlKey == false  &&  event.shiftKey == true  &&  event.altKey == true' )


