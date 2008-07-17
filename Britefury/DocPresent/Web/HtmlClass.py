##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



class ApplyHtmlClass (object):
	def __init__(self, innerHtml, className):
		if isinstance( innerHtml, ApplyHtmlClass ):
			classNames = innerHtml._classNames
			innerHtml = innerHtml._innerHtml
			if className not in classNames:
				classNames += [ className ]
		else:
			classNames = [ className ]
		self._innerHtml = innerHtml
		self._classNames = classNames
		
		
	def html(self):
		return '<span class="%s">%s</span>'  %  ( ' '.join( self._classNames ), self._innerHtml )
	
	
	
import unittest


class TestCase_ApplyHtml (unittest.TestCase):
	def testApply(self):
		self.assert_( ApplyHtmlClass( 'a', 'p' ).html()  ==  '<span class="p">a</span>' )
		self.assert_( ApplyHtmlClass( ApplyHtmlClass( 'a', 'q' ), 'p' ).html()  ==  '<span class="q p">a</span>' )
