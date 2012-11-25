##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMObject, DMList, DMEmbeddedObject, DMEmbeddedIsolatedObject

from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchTools.PythonTools.VisualRegex import Schema



class VisualRegexCodeGeneratorError (Exception):
	pass


class VisualRegexCodeGeneratorUnparsedError (VisualRegexCodeGeneratorError):
	pass



class VisualRegexCodeGenerator (object):
	__dispatch_num_args__ = 0


	# Callable - use document model model method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )



	# Misc
	@DMObjectNodeDispatchMethod( Schema.PythonRegEx )
	def PythonRegEx(self, model, expr):
		return self( expr )


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, model):
		raise VisualRegexCodeGeneratorUnparsedError


	@DMObjectNodeDispatchMethod( Schema.EscapedChar )
	def EscapedChar(self, model, char):
		return '\\' + char

	@DMObjectNodeDispatchMethod( Schema.PythonEscapedChar )
	def PythonEscapedChar(self, model, char):
		return '\\' + char

	@DMObjectNodeDispatchMethod( Schema.LiteralChar )
	def LiteralChar(self, model, char):
		return char


	@DMObjectNodeDispatchMethod( Schema.AnyChar )
	def AnyChar(self, model):
		return '.'

	@DMObjectNodeDispatchMethod( Schema.StartOfLine )
	def StartOfLine(self, model):
		return '^'

	@DMObjectNodeDispatchMethod( Schema.EndOfLine )
	def EndOfLine(self, model):
		return '$'


	@DMObjectNodeDispatchMethod( Schema.CharClass )
	def CharClass(self, model, cls):
		return '\\' + cls


	@DMObjectNodeDispatchMethod( Schema.CharSetChar )
	def CharSetChar(self, model, char):
		return self( char )

	@DMObjectNodeDispatchMethod( Schema.CharSetRange )
	def CharSetRange(self, model, min, max):
		return self( min ) + '-' + self( max )

	@DMObjectNodeDispatchMethod( Schema.CharSet )
	def CharSet(self, model, invert, items):
		return '[' + ( '^'    if invert is not None   else '' ) + ''.join( [ self( item )   for item in items ] ) + ']'


	@DMObjectNodeDispatchMethod( Schema.Group )
	def Group(self, model, subexp, capturing):
		return '(' + ( '?:'    if capturing is None   else '' ) + self( subexp ) + ')'

	@DMObjectNodeDispatchMethod( Schema.DefineNamedGroup )
	def DefineNamedGroup(self, model, subexp, name):
		return '(?P<' + name + '>' + self( subexp ) + ')'

	@DMObjectNodeDispatchMethod( Schema.MatchNamedGroup )
	def MatchNamedGroup(self, model, name):
		return '(?P=' + name + ')'


	@DMObjectNodeDispatchMethod( Schema.MatchNumberedGroup )
	def MatchNumberedGroup(self, model, number):
		return '\\' + number


	@DMObjectNodeDispatchMethod( Schema.Lookahead )
	def Lookahead(self, model, subexp, positive):
		return '(' + ( '?=' if positive is not None   else '?!' ) + self( subexp ) + ')'

	@DMObjectNodeDispatchMethod( Schema.Lookbehind )
	def Lookbehind(self, model, subexp, positive):
		return '(' + ( '?<=' if positive is not None   else '?<!' ) + self( subexp ) + ')'


	@DMObjectNodeDispatchMethod( Schema.SetFlags )
	def SetFlags(self, model, flags):
		return '(?' + flags + ')'

	@DMObjectNodeDispatchMethod( Schema.Comment )
	def Comment(self, model, text):
		return '(?#' + text + ')'


	@DMObjectNodeDispatchMethod( Schema.Repeat )
	def Repeat(self, model, subexp, repetitions):
		return self( subexp ) + '{' + repetitions + '}'

	@DMObjectNodeDispatchMethod( Schema.ZeroOrMore )
	def ZeroOrMore(self, model, subexp, greedy):
		return self( subexp ) + ( '*?'   if greedy   else '*' )

	@DMObjectNodeDispatchMethod( Schema.OneOrMore )
	def OneOrMore(self, model, subexp, greedy):
		return self( subexp ) + ( '+?'   if greedy   else '+' )

	@DMObjectNodeDispatchMethod( Schema.Optional )
	def Optional(self, model, subexp, greedy):
		return self( subexp ) + ( '??'   if greedy   else '?' )

	@DMObjectNodeDispatchMethod( Schema.RepeatRange )
	def RepeatRange(self, model, subexp, min, max):
		return self( subexp ) + '{' + min + ',' + max + '}'


	@DMObjectNodeDispatchMethod( Schema.Sequence )
	def Sequence(self, model, subexps):
		return ''.join( [ self( s )   for s in subexps ] )

	@DMObjectNodeDispatchMethod( Schema.Choice )
	def Choice(self, model, subexps):
		return '|'.join( [ self( s )   for s in subexps ] )













from BritefuryJ.DocModel import DMIOReader
import unittest

class TestCase_VisualRegexCodeGenerator (unittest.TestCase):
	def _testSX(self, sx, expected):
		sx = '{ vre=LarchTools.PythonTools.VisualRegex<0> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )

		gen = VisualRegexCodeGenerator()
		result = str( gen( data ) )

		if result != expected:
			print 'UNEXPECTED RESULT'
			print 'INPUT:'
			print data
			print 'EXPECTED:'
			print expected.replace( '\n', '\\n' ) + '<<'
			print 'RESULT:'
			print result.replace( '\n', '\\n' ) + '<<'

		self.assert_( result == expected )


	def _testGenSX(self, gen, sx, expected):
		sx = '{ vre=LarchTools.PythonTools.VisualRegex<0> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )

		result = str( gen( data ) )

		if result != expected:
			print 'UNEXPECTED RESULT'
			print 'EXPECTED:'
			print expected.replace( '\n', '\\n' ) + '<<'
			print 'RESULT:'
			print result.replace( '\n', '\\n' ) + '<<'

		self.assert_( result == expected )


	def test_PythonRegEx(self):
		self._testSX( '(vre PythonRegEx expr=(vre LiteralChar char=x))', 'x' )


	def test_UNPARSED(self):
		self.assertRaises( VisualRegexCodeGeneratorUnparsedError, lambda: self._testSX( '(vre UNPARSED value=Test)', '' ) )


	def test_EscapedChar(self):
		self._testSX( '(vre EscapedChar char="(")', '\\(' )

	def test_LiteralChar(self):
		self._testSX( '(vre LiteralChar char=x)', 'x' )


	def test_SpecialChars(self):
		self._testSX( '(vre AnyChar)', '.' )
		self._testSX( '(vre StartOfLine)', '^' )
		self._testSX( '(vre EndOfLine)', '$' )


	def test_CharClass(self):
		self._testSX( '(vre CharClass cls=w)', '\\w' )


	def test_CharSetChar(self):
		self._testSX( '(vre CharSetChar char=(vre LiteralChar char=a))', 'a' )

	def test_CharSetRange(self):
		self._testSX( '(vre CharSetRange min=(vre LiteralChar char=a) max=(vre LiteralChar char=z))', 'a-z' )

	def test_CharSet(self):
		self._testSX( '(vre CharSet items=[(vre CharSetRange min=(vre LiteralChar char=a) max=(vre LiteralChar char=z)) (vre CharSetRange min=(vre LiteralChar char=0) max=(vre LiteralChar char=9))])', '[a-z0-9]' )
		self._testSX( '(vre CharSet invert=1 items=[(vre CharSetRange min=(vre LiteralChar char=a) max=(vre LiteralChar char=z)) (vre CharSetRange min=(vre LiteralChar char=0) max=(vre LiteralChar char=9))])', '[^a-z0-9]' )


	def test_Group(self):
		self._testSX( '(vre Group capturing=1 subexp=(vre LiteralChar char=a))', '(a)' )
		self._testSX( '(vre Group subexp=(vre LiteralChar char=a))', '(?:a)' )

	def test_DefineNamedGroup(self):
		self._testSX( '(vre DefineNamedGroup name=test subexp=(vre LiteralChar char=a))', '(?P<test>a)' )

	def test_MatchNamedGroup(self):
		self._testSX( '(vre MatchNamedGroup name=test)', '(?P=test)' )


	def test_Lookahead(self):
		self._testSX( '(vre Lookahead positive=1 subexp=(vre LiteralChar char=a))', '(?=a)' )
		self._testSX( '(vre Lookahead subexp=(vre LiteralChar char=a))', '(?!a)' )

	def test_Lookbehind(self):
		self._testSX( '(vre Lookbehind positive=1 subexp=(vre LiteralChar char=a))', '(?<=a)' )
		self._testSX( '(vre Lookbehind subexp=(vre LiteralChar char=a))', '(?<!a)' )


	def test_SetFlags(self):
		self._testSX( '(vre SetFlags flags=i)', '(?i)' )

	def test_Comment(self):
		self._testSX( '(vre Comment text="Hello world")', '(?#Hello world)' )


	def test_Repeat(self):
		self._testSX( '(vre Repeat repetitions=3 subexp=(vre LiteralChar char=a))', 'a{3}' )

	def test_ZeroOrMore(self):
		self._testSX( '(vre ZeroOrMore subexp=(vre LiteralChar char=a))', 'a*' )
		self._testSX( '(vre ZeroOrMore greedy=1 subexp=(vre LiteralChar char=a))', 'a*?' )

	def test_OneOrMore(self):
		self._testSX( '(vre OneOrMore subexp=(vre LiteralChar char=a))', 'a+' )
		self._testSX( '(vre OneOrMore greedy=1 subexp=(vre LiteralChar char=a))', 'a+?' )

	def test_Optional(self):
		self._testSX( '(vre Optional subexp=(vre LiteralChar char=a))', 'a?' )
		self._testSX( '(vre Optional greedy=1 subexp=(vre LiteralChar char=a))', 'a??' )

	def test_RepeatRange(self):
		self._testSX( '(vre RepeatRange min=3 max=5 subexp=(vre LiteralChar char=a))', 'a{3,5}' )


	def test_Sequence(self):
		self._testSX( '(vre Sequence subexps=[(vre LiteralChar char=a) (vre LiteralChar char=b) (vre LiteralChar char=c)])', 'abc' )

	def test_Choice(self):
		self._testSX( '(vre Choice subexps=[(vre LiteralChar char=a) (vre LiteralChar char=b) (vre LiteralChar char=c)])', 'a|b|c' )





