##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from Britefury.DocPresent.Web.ModifierKeys import modifierKeyStringToFlags



class HighlightHtmlClass (object):
	def __init__(self, className, modifierKeysValue, modifierKeysMask):
		self._className = className + '_hl'
		self._activeClassName = className + '_ahl'
		self._modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
		self._modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
		
	def headerScript(self):
		assert False
		
	def apply(self, ctx, html):
		return '<span class="%s">%s</span>'  %  ( self._className, html )
	
	
	
def highlight(ctx, html, highlightClass):
	return highlightClass.apply( ctx, html )
	
	
	
import unittest


class TestCase_Highlight (unittest.TestCase):
	def testHighlight(self):
		self.assert_( False )
	