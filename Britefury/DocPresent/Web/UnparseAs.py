##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


def unparseAs(ctx, html, text, precedence=None):
	return None



import unittest

class TestCase_unparseAs (unittest.TestCase):
	def test_unparseAs(self):
		self.assert_( unparseAs( 'test', 'test' )  is not  None )
		
