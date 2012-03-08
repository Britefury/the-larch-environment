##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import deepcopy

from BritefuryJ.DocModel import DMObject, DMList, DMEmbeddedObject, DMEmbeddedIsolatedObject

from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchTools.PythonTools.SWYN import Schema



class SWYNCodeGeneratorError (Exception):
	pass


class SWYNCodeGeneratorUnparsedError (SWYNCodeGeneratorError):
	pass



class SWYNCodeGenerator (object):
	__dispatch_num_args__ = 0


	# Callable - use document model model method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )



	# Misc
	@DMObjectNodeDispatchMethod( Schema.SWYNRegEx )
	def SWYNRegEx(self, model, expr):
		return self( expr )


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, model):
		raise SWYNCodeGeneratorUnparsedError


	@DMObjectNodeDispatchMethod( Schema.EscapedChar )
	def EscapedChar(self, model, char):
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

class TestCase_SWYNCodeGenerator (unittest.TestCase):
	def _testSX(self, sx, expected):
		sx = '{ swyn=LarchTools.PythonTools.SWYN<0> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )

		gen = SWYNCodeGenerator( '<test>' )
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
		sx = '{ swyn=LarchTools.PythonTools.SWYN<0> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )

		result = str( gen( data ) )

		if result != expected:
			print 'UNEXPECTED RESULT'
			print 'EXPECTED:'
			print expected.replace( '\n', '\\n' ) + '<<'
			print 'RESULT:'
			print result.replace( '\n', '\\n' ) + '<<'

		self.assert_( result == expected )


	def test_SWYNRegEx(self):
		self._testSX( '(swyn SWYNRegEx expr=(swyn LiteralChar char=x))', 'x' )


	def test_UNPARSED(self):
		self.assertRaises( SWYNCodeGeneratorUnparsedError, lambda: self._testSX( '(swyn UNPARSED value=Test)', '' ) )


	def test_EscapedChar(self):
		self._testSX( '(swyn EscapedChar char="(")', '\\(' )

	def test_LiteralChar(self):
		self._testSX( '(swyn LiteralChar char=x)', 'x' )


	def test_SpecialChars(self):
		self._testSX( '(swyn AnyChar)', '.' )
		self._testSX( '(swyn StartOfLine)', '^' )
		self._testSX( '(swyn EndOfLine)', '$' )


	def test_CharClass(self):
		self._testSX( '(swyn CharClass cls=w)', '\\w' )


	def test_CharSetChar(self):
		self._testSX( '(swyn CharSetChar char=(swyn LiteralChar char=a))', 'a' )

	def test_CharSetRange(self):
		self._testSX( '(swyn CharSetRange min=(swyn LiteralChar char=a) max=(swyn LiteralChar char=z))', 'a-z' )

	def test_CharSet(self):
		self._testSX( '(swyn CharSet items=[(swyn CharSetRange min=(swyn LiteralChar char=a) max=(swyn LiteralChar char=z)) (swyn CharSetRange min=(swyn LiteralChar char=0) max=(swyn LiteralChar char=9))])', '[a-z0-9]' )
		self._testSX( '(swyn CharSet invert=1 items=[(swyn CharSetRange min=(swyn LiteralChar char=a) max=(swyn LiteralChar char=z)) (swyn CharSetRange min=(swyn LiteralChar char=0) max=(swyn LiteralChar char=9))])', '[^a-z0-9]' )


	def test_Group(self):
		self._testSX( '(swyn Group capturing=1 subexp=(swyn LiteralChar char=a))', '(a)' )
		self._testSX( '(swyn Group subexp=(swyn LiteralChar char=a))', '(?:a)' )

	def test_DefineNamedGroup(self):
		self._testSX( '(swyn DefineNamedGroup name=test subexp=(swyn LiteralChar char=a))', '(?P<test>a)' )

	def test_MatchNamedGroup(self):
		self._testSX( '(swyn MatchNamedGroup name=test)', '(?P=test)' )


	def test_Lookahead(self):
		self._testSX( '(swyn Lookahead positive=1 subexp=(swyn LiteralChar char=a))', '(?=a)' )
		self._testSX( '(swyn Lookahead subexp=(swyn LiteralChar char=a))', '(?!a)' )

	def test_Lookbehind(self):
		self._testSX( '(swyn Lookbehind positive=1 subexp=(swyn LiteralChar char=a))', '(?<=a)' )
		self._testSX( '(swyn Lookbehind subexp=(swyn LiteralChar char=a))', '(?<!a)' )


	def test_SetFlags(self):
		self._testSX( '(swyn SetFlags flags=i)', '(?i)' )

	def test_Comment(self):
		self._testSX( '(swyn Comment text="Hello world")', '(?#Hello world)' )


	def test_Repeat(self):
		self._testSX( '(swyn Repeat repetitions=3 subexp=(swyn LiteralChar char=a))', 'a{3}' )

	def test_ZeroOrMore(self):
		self._testSX( '(swyn ZeroOrMore subexp=(swyn LiteralChar char=a))', 'a*' )
		self._testSX( '(swyn ZeroOrMore greedy=1 subexp=(swyn LiteralChar char=a))', 'a*?' )

	def test_OneOrMore(self):
		self._testSX( '(swyn OneOrMore subexp=(swyn LiteralChar char=a))', 'a+' )
		self._testSX( '(swyn OneOrMore greedy=1 subexp=(swyn LiteralChar char=a))', 'a+?' )

	def test_Optional(self):
		self._testSX( '(swyn Optional subexp=(swyn LiteralChar char=a))', 'a?' )
		self._testSX( '(swyn Optional greedy=1 subexp=(swyn LiteralChar char=a))', 'a??' )

	def test_RepeatRange(self):
		self._testSX( '(swyn RepeatRange min=3 max=5 subexp=(swyn LiteralChar char=a))', 'a{3,5}' )


	def test_Sequence(self):
		self._testSX( '(swyn Sequence subexps=[(swyn LiteralChar char=a) (swyn LiteralChar char=b) (swyn LiteralChar char=c)])', 'abc' )

	def test_Choice(self):
		self._testSX( '(swyn Choice subexps=[(swyn LiteralChar char=a) (swyn LiteralChar char=b) (swyn LiteralChar char=c)])', 'a|b|c' )





