##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing
import string



"""
gSym document model IO


Uses basic S-expressions

lists are (...) as normal
tokens inside are:
	atom:   A-Z a-z 0.9 _+-*/%^&|!$@.,<>=[]~
	quoted string
	double quoted string
	another list
"""



## String IO helpers

def needsQuotes(s):
	return ' ' in s  or  '\t' in s  or  '\n' in s  or  '\r' in s  or  '\x0b' in s  or  '\x0c' in s  or  '(' in s  or  ')' in s  or  '`' in s  or  '{' in s  or  '}' in s  or  '\'' in s






## WRITING FUNCTIONS

def __writesx__(stream, content):
	try:
		w = content.__writesx__
	except AttributeError:
		if isinstance( content, str ):
			if needsQuotes( content ):
				stream.write( repr( content ) )
			else:
				stream.write( content )
		elif isinstance( content, unicode ):
			stream.write( repr( content ) )
	else:
		w( stream )


def writeSX(stream, content):
	__writesx__( stream, content )






import unittest



class TestCase_DMIOWrite (unittest.TestCase):
	def _testWrite(self, data, result):
		import cStringIO
		stream = cStringIO.StringIO()
		writeSX( stream, data )
		self.assert_( stream.getvalue() == result )



	def testUnquotedString(self):
		self._testWrite( 'x', 'x' )
		
	def testQuotedString(self):
		self._testWrite( 'x y', '\'x y\'' )
		
	def testUnicodeString(self):
		from Britefury.DocModel.DMList import DMList
		self._testWrite( u'\u0107', 'u\'\\u0107\'' )
		

	def testWriteList(self):
		from Britefury.DocModel.DMList import DMList
		h = DMList()
		h.extend( [ 'h', '1', '2L', '3.0' ] )
		g = DMList()
		g.extend( [ 'g', h, 'Hi ' ] )
		f = DMList()
		f.extend( [ 'f', g, ' There', u'\u0107' ] )
		
		self._testWrite( f, '(f (g (h 1 2L 3.0) \'Hi \') \' There\' u\'\\u0107\')' )






if __name__ == '__main__':
	unittest.main()
