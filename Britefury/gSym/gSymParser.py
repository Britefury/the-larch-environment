##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction, GLispCompilerCouldNotCompileSpecial, GLispCompilerInvalidFormLength, GLispCompilerInvalidItem
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyClass, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent

from Britefury.Parser.Parser import Literal, Word, Sequence



_literal = 'Literal'
_word = 'Word'
_regex = 'RegEx'
_sequence = 'Sequence'
_combine = 'Combine'
_choice = 'Choice'
_optional = 'Optional'
_zeroOrMore = 'ZeroOrMore'
_oneOrMore = 'OneOrMore'
_peek = 'Peek'
_peekNot = 'PeekNot'



def _matchCall(pytree):
	return pytree.attr( '_o_match' )( PyVar( '_state' ), PyVar( '_input' ), PyVar( '_start' ), PyVar( '_stop' ) )

def _ruleMethodName(ruleName):
	return '_rule_' + ruleName




class _ParserBase (object):
	def _literal(self, input, start, matchString):
		end = start + len( matchString )
		if input[start:end]  ==  matchString:
			return ( matchString, ), end
		else:
			return None, start
		
		
	def _word(self, input, start, initChars, bodyChars):
		pos = start
		if self._initChars is not None:
			if input[start] not in self._initChars:
				return None, start
			pos = pos + 1
			
		stop = len( input )
		end = stop
		for i in xrange( pos, stop ):
			if input[i] not in self._bodyChars:
				end = i
				break
			
		if end == start:
			return None, start
		else:
			x = input[start:end]
			return ( x, ), end

		
	def _regex(self, input, start, regex):
		m = regex.match( input, start )
		
		if m is not None:
			matchString = m.group()
			if len( matchString ) > 0:
				end = start + len( matchString )
				return ( matchString, ),  end
		return None, start





class _ParserBuilder (object):
	def __init__(self):
		super( _ParserBuilder, self ).__init__()
		self._names = {}
		
	def allocName(self, name):
		count = self._names.get( name, 0 )
		self._names[name] = count + 1
		return '_gsym__parser__%s_%d'  %  ( name, count )

	
	

class _PParser (object):
	def __init__(self, name, rules, dbgSrc=None):
		self._name = name
		self._rules = rules
		self._dbgSrc = dbgSrc
		
		
	def emitPyTree(self):
		builder = _ParserBuilder()
		pre = []
		for rule in self._rules:
			p, x = rule.emitPyTree( builder )
			pre.extend( p )
			assert x is None
		return PyClass( self._name, [ PyVar( '_ParserBase' ) ], pre, self._dbgSrc )



class _PRuleDefinition (object):
	def __init__(self, ruleName, argNames, expression, dbgSrc=None):
		self._ruleName = ruleName
		self._argNames = argNames
		self._expression = expression
		self._dbgSrc = dbgSrc
		
		
	def emitPyTree(self, builder):
		"""
		->  class_pre, expression
		"""
		cPre, fnPre, expr = self._expression.emitPyTree( builder )
		defName = _ruleMethodName( self._ruleName )
		ruleDef = PyDef( defName, [ 'self', '_state', '_input', '_start' ] + self._argNames, fnPre + [ PyReturn( expr ) ], self._dbgSrc )
		return cPre  +  [ ruleDef, ruleMethod ],  None



class _PDefExpression (object):
	def __init__(self, dbgSrc=None):
		super( _PDefExpression, self ).__init__()
		self._dbgSrc = dbgSrc
	
	def emitPyTree(self, builder):
		"""
		-> class_pre,  fn_pre,  expression
		"""
		return [], [], None
	
	def emitAsFnPyTree(self, builder, fnName):
		cPre, fPre, x = self.emitPyTree( builder )
		return cPre, \
		       fPre + [ PyDef( fnName, [ '_input', '_start' ], self._dbgSrc ) ], \
		       PyVar( fnName )


	def getRulesUsed(self):
		return []



class _PDefLiteral (_PDefExpression):
	def __init__(self, matchString):
		super( _PDefLiteral, self ).__init__()
		self._matchString = matchString

		
	def emitPyTree(self, builder):
		return [], \
		       [], \
		       PyVar( 'self' ).attr( '_literal' )( PyVar( '_input' ), PyVar( '_start' ), PyLiteralValue( self._matchString ) )



class _PDefWord (_PDefExpression):
	def __init__(self, initChars, bodyChars=None):
		super( _PDefWord, self ).__init__()
		self._initChars = initChars
		self._bodyChars = bodyChars

	def emitPyTree(self, builder):
		if self._bodyChars is None:
			return [], \
			       [], \
			       PyVar( 'self' ).attr( '_word' )( PyVar( '_input' ), PyVar( '_start' ), PyLiteralValue( None ), PyLiteralValue( self._initChars ) )
		else:
			return [], \
			       [], \
			       PyVar( 'self' ).attr( '_word' )( PyVar( '_input' ), PyVar( '_start' ), PyLiteralValue( self._initChars ), PyLiteralValue( self._bodyChars ) )


class _PDefRegEx (_PDefExpression):
	def __init__(self, pattern):
		super( _PDefRegEx, self ).__init__()
		self._pattern = pattern

	def emitPyTree(self, builder):
		name = builder.allocName( 'regex' )
		return [ PyVar( name ).assign_sideEffects( PyVar( '_re' ).attr( 'compile' )( PyLiteralValue( self._pattern ) ) ) ], \
		       [], \
		       PyVar( 'self' ).attr( '_regex' )( PyVar( '_input' ), PyVar( '_start' ), PyVar( 'self' ).attr( name ) )




class _PDefCombinatorExpr (_PDefExpression):
	combinatorFunctionName = None
	combinatorName = None
	combinatorBuilderName = None

	def __init__(self, subexps):
		super( _PDefCombinatorExpr, self ).__init__()
		self._subexps = subexps

	def emitPyTree(self, builder):
		pre = []
		xs = []
		names = []
		for subexp in self._subexps:
			p, x, n = subexp.emitPyTree( builder )
			pre.extend( p )
			xs.append( x )
			names.append( n )
		name = builder.allocName( self.combinatorName )
		return pre + [ PyVar( name ).assign_sideEffects( PyVar( self.combinatorBuilderName )( *[ PyVar( n )  for n in names ] ) ) ], \
		       _matchCall( PyVar( 'self' ).attr( name ) ), \
		       name

	def getRulesUsed(self):
		return reduce( lambda rules, x: rules + x.getRulesUsed(), self._subexps, [] )



class _PDefSequence (_PDefCombinatorExpr):
	combinatorFunctionName = '_sequence'
	combinatorName = 'sequence'
	combinatorBuilderName = _sequence



class _PDefCombine (_PDefCombinatorExpr):
	combinatorFunctionName = '_combine'
	combinatorName = 'combine'
	combinatorBuilderName = _combine



class _PDefChoice (_PDefCombinatorExpr):
	combinatorFunctionName = '_choice'
	combinatorName = 'choice'
	combinatorBuilderName = _choice




class _PDefOptional (_PDefCombinatorExpr):
	combinatorFunctionName = '_optional'
	combinatorName = 'optional'
	combinatorBuilderName = _optional



class _PDefZeroOrMore (_PDefCombinatorExpr):
	combinatorFunctionName = '_zeroOrMore'
	combinatorName = 'zeroOrMore'
	combinatorBuilderName = _zeroOrMore



class _PDefOneOrMore (_PDefCombinatorExpr):
	combinatorFunctionName = '_oneOrMore'
	combinatorName = 'oneOrMore'
	combinatorBuilderName = _oneOrMore



class _PDefPeek (_PDefCombinatorExpr):
	combinatorFunctionName = '_peek'
	combinatorName = '_peek'
	combinatorBuilderName = _peek



class _PDefPeekNot (_PDefCombinatorExpr):
	combinatorFunctionName = '_peekNot'
	combinatorName = 'peekNot'
	combinatorBuilderName = _peekNot



import unittest
import operator



class Test_gSymParser (unittest.TestCase):
	def _testCompileAsParser(self, pdef, ruleName, className, pySrc):
		ruleDef = _PRuleDefinition( ruleName, [], pdef )
		parserDef = _PParser( className, [ ruleDef ] )
		result = parserDef.emitPyTree().compileAsStmt()
		if result != pySrc:
			print 'EXPECTED:'
			print '\n'.join( pySrc )
			print ''
			print 'RESULT:'
			print '\n'.join( result )
		self.assert_( result == pySrc )
	
		
		
	def test_Literal(self):
		pySrcParser = [
			"class LP (_ParserBase):",
			"  def _rule_literal(self, _state, _input, _start):",
			"    return self._literal( _input, _start, 'abc' )",
			"  def literal(self, _input, _start):",
			"    return self._rule_literal( ParserState(), _input, _start )"
			]
		self._testCompileAsParser( _PDefLiteral( 'abc' ), 'literal', 'LP', pySrcParser )

		
		
	def test_Word(self):
		pySrcParser = [
			"class WP (_ParserBase):",
			"  def _rule_word(self, _state, _input, _start):",
			"    return self._word( _input, _start, 'abc', 'xyz' )",
			"  def word(self, _input, _start):",
			"    return self._rule_word( ParserState(), _input, _start )"
			]
		self._testCompileAsParser( _PDefWord( 'abc', 'xyz' ), 'word', 'WP', pySrcParser )

		
		
	def test_RegEx(self):
		pySrcParser = [
			"class RP (_ParserBase):",
			"  _gsym__parser__regex_0 = _re.compile( 'abc' )",
			"  def _rule_regex(self, _state, _input, _start):",
			"    return self._regex( _input, _start, self._gsym__parser__regex_0 )",
			"  def regex(self, _input, _start):",
			"    return self._rule_regex( ParserState(), _input, _start )"
			]
		self._testCompileAsParser( _PDefRegEx( 'abc' ), 'regex', 'RP', pySrcParser )


		
	#def test_Sequence(self):
		#pySrc = [
			#"_gsym__parser__literal_0 = Literal( 'a' )",
			#"_gsym__parser__literal_1 = Literal( 'b' )",
			#"_gsym__parser__literal_2 = Literal( 'c' )",
			#"_gsym__parser__sequence_0 = Sequence( _gsym__parser__literal_0, _gsym__parser__literal_1, _gsym__parser__literal_2 )",
			#"self._gsym__parser__sequence_0._o_match( _state, _input, _start, _stop )"
			#]
		#self._testCompileAsExpr( _PDefSequence( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), pySrc )

		#pySrcRule = [
			#"_gsym__parser__literal_0 = Literal( 'a' )",
			#"_gsym__parser__literal_1 = Literal( 'b' )",
			#"_gsym__parser__literal_2 = Literal( 'c' )",
			#"_gsym__parser__sequence_0 = Sequence( _gsym__parser__literal_0, _gsym__parser__literal_1, _gsym__parser__literal_2 )",
			#"def _rule_sequence(self, _state, _input, _start, _stop):",
			#"  return self._gsym__parser__sequence_0._o_match( _state, _input, _start, _stop )",
			#"def sequence(self, _input, _start, _stop):",
			#"  return self._rule_sequence( ParserState(), _input, _start, _stop )"
			#]
		#self._testCompileAsRule( _PDefSequence( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), 'sequence', pySrcRule )
		
	#def test_Combine(self):
		#self._testCompileAsExpr( _PDefCombine( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Combine( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	#def test_Choice(self):
		#self._testCompileAsExpr( _PDefChoice( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Choice( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	#def test_Optional(self):
		#self._testCompileAsExpr( _PDefOptional( _PDefLiteral( 'a' ) ), "Optional( Literal( 'a' ) )" )
		
	#def test_ZeroOrMore(self):
		#self._testCompileAsExpr( _PDefZeroOrMore( _PDefLiteral( 'a' ) ), "ZeroOrMore( Literal( 'a' ) )" )
		
	#def test_OneOrMore(self):
		#self._testCompileAsExpr( _PDefOneOrMore( _PDefLiteral( 'a' ) ), "OneOrMore( Literal( 'a' ) )" )
		
	#def test_Peek(self):
		#self._testCompileAsExpr( _PDefPeek( _PDefLiteral( 'a' ) ), "Peek( Literal( 'a' ) )" )
		
	#def test_PeekNot(self):
		#self._testCompileAsExpr( _PDefPeekNot( _PDefLiteral( 'a' ) ), "PeekNot( Literal( 'a' ) )" )
		
		
		