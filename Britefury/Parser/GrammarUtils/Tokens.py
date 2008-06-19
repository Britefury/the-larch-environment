##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase



#
# Regular expression definitions of singleQuotedString, doubleQuotedString, and quotedString taken from pyparsing, which is Copyright (c) 2003-2007  Paul T. McGuire
#

identifier = RegEx( "[A-Za-z_][A-Za-z0-9_]*" )
singleQuotedString = RegEx( r"'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*'" )
doubleQuotedString = RegEx( r'"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*"' )
quotedString = RegEx( r'''(?:"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*")|(?:'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*')''' )
decimalInteger = RegEx( r"[\-]?[0-9]+" )
hexInteger = RegEx( r"0x[0-9A-Fa-f]+" )
integer = decimalInteger  |  hexInteger
floatingPoint = RegEx( r"[\-]?(([0-9]+\.[0-9]*)|(\.[0-9]+))(e[\-]?[0-9]+)?" )





class TestCase_Tokens (ParserTestCase):
	def testIIdentifier(self):
		parser = identifier
		self._matchTest( parser, 'ab', 'ab' )
		self._matchTest( parser, 'ab12', 'ab12' )
		self._matchFailTest( parser, '12ab' )
		self._matchTest( parser, '_ab', '_ab' )
		
		
	def testSingleQuotedString(self):
		parser = singleQuotedString
		self._matchTest( parser, "'abc'", "'abc'" )
		self._matchTest( parser, r"'ab\'c'", r"'ab\'c'" )
		self._matchTest( parser, "'abc'113", "'abc'" )
	
		
	def testDoubleQuotedString(self):
		parser = doubleQuotedString
		self._matchTest( parser, '"abc"', '"abc"' )
		self._matchTest( parser, r'"ab\"c"', r'"ab\"c"' )
		self._matchTest( parser, '"abc"113', '"abc"' )
	
		
	def testQuotedString(self):
		parser = quotedString
		self._matchTest( parser, "'abc'", "'abc'" )
		self._matchTest( parser, r"'ab\'c'", r"'ab\'c'" )
		self._matchTest( parser, "'abc'113", "'abc'" )
		self._matchTest( parser, '"abc"', '"abc"' )
		self._matchTest( parser, r'"ab\"c"', r'"ab\"c"' )
		self._matchTest( parser, '"abc"113', '"abc"' )
	
		
	def testUnicodeString(self):
		parser = unicodeString
		self._matchTest( parser, "u'abc'", "u'abc'" )
		self._matchTest( parser, r"u'ab\'c'", r"u'ab\'c'" )
		self._matchTest( parser, "u'abc'113", "u'abc'" )
		self._matchTest( parser, 'u"abc"', 'u"abc"' )
		self._matchTest( parser, r'u"ab\"c"', r'u"ab\"c"' )
		self._matchTest( parser, 'u"abc"113', 'u"abc"' )
		
		
	def testDecimalInteger(self):
		parser = decimalInteger
		self._matchTest( parser, "123", "123" )
		self._matchTest( parser, "-123", "-123" )
		
		
	def testHexadecimalInteger(self):
		parser = hexInteger
		self._matchTest( parser, "0x123", "0x123" )
		self._matchTest( parser, "0x0123456789abcdef", "0x0123456789abcdef" )
		self._matchTest( parser, "0x0123456789ABCDEF", "0x0123456789ABCDEF" )
		
		

	def testFloatingPoint(self):
		parser = floatingPoint
		self._matchTest( parser, "3.14", "3.14" )
		self._matchTest( parser, "-3.14", "-3.14" )
		self._matchTest( parser, "3.", "3." )
		self._matchTest( parser, "-3.", "-3." )
		self._matchTest( parser, ".14", ".14" )
		self._matchTest( parser, "-.14", "-.14" )

		self._matchTest( parser, "3.14e5", "3.14e5" )
		self._matchTest( parser, "3.14e-5", "3.14e-5" )
		self._matchTest( parser, "-3.14e5", "-3.14e5" )
		self._matchTest( parser, "-3.14e-5", "-3.14e-5" )
		self._matchTest( parser, "3.e5", "3.e5" )
		self._matchTest( parser, "3.e-5", "3.e-5" )
		self._matchTest( parser, "-3.e5", "-3.e5" )
		self._matchTest( parser, "-3.e-5", "-3.e-5" )
		self._matchTest( parser, ".14e5", ".14e5" )
		self._matchTest( parser, ".14e-5", ".14e-5" )
		self._matchTest( parser, "-.14e5", "-.14e5" )
		self._matchTest( parser, "-.14e-5", "-.14e-5" )

		
		