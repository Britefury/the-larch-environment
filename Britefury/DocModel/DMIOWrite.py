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
	def _testRead(self, source, result):
		x = readSX( source )
		self.assert_( x == result )




	def testWrite(self):
		import cStringIO
		from Britefury.DocModel.DMList import DMList

		h = DMList()
		h.extend( [ 'h', '1', '2L', '3.0' ] )
		g = DMList()
		g.extend( [ 'g', h, 'Hi ' ] )
		f = DMList()
		f.extend( [ 'f', g, ' There' ] )

		stream = cStringIO.StringIO()
		writeSX( stream, f )
		self.assert_( stream.getvalue() == '(f (g (h 1 2L 3.0) \'Hi \') \' There\')' )






if __name__ == '__main__':
	unittest.main()
