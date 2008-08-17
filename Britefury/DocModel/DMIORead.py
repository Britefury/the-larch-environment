##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string
import re

from Britefury.DocModel import DMList


"""
gSym document model IO


Uses basic S-expressions

lists are (...) as normal
tokens inside are:
	atom:   A-Z a-z 0-9 _+-*/%^&|!$@.,<>=[]~
	quoted string / unicode string
	another list
"""


_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )
_whitespace = re.compile( '[%s]+'  %  re.escape( ' \t\n' ), 0 )
_unquotedString = re.compile( '[%s]+'  %  re.escape( _unquotedStringChars ), 0 )
_quotedString = re.compile( r'''(?:"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*")|(?:'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*')''', 0 )


def _match(regexp, source, pos):
	m = regexp.match( source, pos )
	if m is not None:
		matchString = m.group()
		if len( matchString ) > 0:
			return matchString, pos + len( matchString )
	return None, pos


def readSX(source):
	if isinstance( source, file ):
		source = source.read()
	pos = 0
	stack = []
	last = None
	while pos < len( source ):
		if source[pos] == '(':
			# Open paren; start new list
			xs = DMList.DMList()
			# Append the new list to the list that is on the top of the stack; this builds the structure
			if len( stack ) > 0:
				stack[-1].append( xs )
			# Make the top of the stack the new list
			stack.append( xs )
			pos += 1
		elif source[pos] == ')':
			# Close parent; end current list, pop off stack
			last = stack.pop()
			pos += 1
		else:
			# Try looking for:
			
			# White space; skip
			w, pos = _match( _whitespace, source, pos )
			if w is not None:
				continue
			
			# Unicode string
			if source[pos].lower() == 'u':
				pos += 1
				u, pos = _match( _quotedString, source, pos )
				if u is not None:
					u = eval( 'u' + u )
					if len( stack ) > 0:
						stack[-1].append( u )
					else:
						last = u
					continue
					
			# Quoted string
			q, pos = _match( _quotedString, source, pos )
			if q is not None:
				q = eval( q )
				if len( stack ) > 0:
					stack[-1].append( q )
				else:
					last = q
				continue
		
			# Unquoted string / atom
			uq, pos = _match( _unquotedString, source, pos )
			if uq is not None:
				if len( stack ) > 0:
					stack[-1].append( uq )
				else:
					last = uq
				continue
		
			raise ValueError

	return last
				
			
			
		
		
			

import unittest



class TestCase_DMIO (unittest.TestCase):
	def _testRead(self, source, expected):
		x = readSX( source )
		
		if x != expected:
			print 'EXPECTED:'
			print expected, type( expected )
			print 'RESULT:'
			print x, type( x )
		self.assert_( x == expected )




	def testReadUnquotedString(self):
		self._testRead( 'abc', 'abc' )

	def testReadQuotedString(self):
		self._testRead( "'abc 123'", 'abc 123' )

	def testReadUnicodeString(self):
		self._testRead( "u'\\u0107'", u'\u0107' )

	def testReadEmptyString(self):
		self._testRead( '""', '' )

	def testReadList(self):
		source = '(f (g (h 1 2L 3.0) \'Hi \') \' There\' u\'\\u0107\')'
		self._testRead( source,  [ 'f', [ 'g', [ 'h', '1', '2L', '3.0' ], 'Hi ' ], ' There', u'\u0107' ] )




if __name__ == '__main__':
	unittest.main()
