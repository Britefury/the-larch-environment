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
	null:		`null`
	quoted string
	double quoted string
	reference: see below
	another list

tokens can be preceeded by a tag, see below


references:
	{identifier}

	identifier can be an index:
	 	an index is used to identify the entity being referenced. Indices are assigned to lists in the order that they appear, with a list taking an index after its contents.
		e.g.
				(a (b c d) e)
				(. (. . .)0 .)1

	identifier can be a name:  [a-zA-Z_] [a-zA-Z0-9]*
	 	the name must match a previously encountered tag



tags:
	{:identifier}

	identifier is a name:  [a-zA-Z_] [a-zA-Z0-9]*
		the token/item following the tag can be referenced using this name
"""



## String IO helpers

def needsQuotes(s):
	return ' ' in s  or  '\t' in s  or  '\n' in s  or  '\r' in s  or  '\x0b' in s  or  '\x0c' in s  or  '(' in s  or  ')' in s  or  '`' in s  or  '{' in s  or  '}' in s  or  '\'' in s






## WRITING FUNCTIONS

def __writesx__(stream, nodeToIndex, content):
	try:
		w = content.__writesx__
	except AttributeError:
		if isinstance( content, str ):
			if needsQuotes( content ):
				stream.write( repr( content ) )
			else:
				stream.write( content )
	else:
		w( stream, nodeToIndex )


def writeSX(stream, content):
	__writesx__( stream, {}, content )






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



	def testWriteDiamond(self):
		import cStringIO
		from Britefury.DocModel.DMList import DMList
		from Britefury.DocModel.DMIORead import readSX

		sourceA = [ '1', '2', '3' ]
		sourceB = [ '5', '6', sourceA, '7', [ '8', [ sourceA, '9' ], '10' ] ]
		b = DMList( sourceB )

		stream = cStringIO.StringIO()
		writeSX( stream, b )
		sourceText = stream.getvalue()
		self.assert_( sourceText =='(5 6 (1 2 3) 7 (8 ({0} 9) 10))' )

		x = readSX( sourceText )
		self.assert_( x[2] is x[4][1][0] )




if __name__ == '__main__':
	unittest.main()
