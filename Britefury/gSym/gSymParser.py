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

def _ruleAttrName(ruleName):
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
		expr = self._expression.emitPyTree()
		defName = _ruleAttrName( self._ruleName )
		ruleDef = PyDef( defName, [ 'self', '_state', '_input', '_start' ] + self._argNames, fnPre + [ PyReturn( expr ) ], self._dbgSrc )
		return cPre  +  [ ruleDef, ruleMethod ],  None


	
	

class _PDefExpression (object):
	def __init__(self, dbgSrc=None):
		super( _PDefExpression, self ).__init__()
		self._dbgSrc = dbgSrc
	
	def emitPyTree(self):
		return None
	

	def getRulesUsed(self):
		return []



class _PDefLiteral (_PDefExpression):
	def __init__(self, matchString, dbgSrc=None):
		super( _PDefLiteral, self ).__init__( dbgSrc )
		self._matchString = matchString

		
	def emitPyTree(self):
		return PyVar( _literal )( self._matchString ).debug( self._dbgSrc )



class _PDefRegEx (_PDefExpression):
	def __init__(self, pattern, dbgSrc=None):
		super( _PDefRegEx, self ).__init__( dbgSrc )
		self._pattern = pattern

	def emitPyTree(self):
		return PyVar( _regex )( self._pattern ).debug( self._dbgSrc )



class _PDefWord (_PDefExpression):
	def __init__(self, initChars, bodyChars=None, dbgSrc=None):
		super( _PDefWord, self ).__init__( dbgSrc )
		self._initChars = initChars
		self._bodyChars = bodyChars

	def emitPyTree(self):
		return PyVar( _word )( self._initChars, self._bodyChars ).debug( self._dbgSrc )




class _PDefSubexpList (_PDefExpression):
	combinatorName = None

	def __init__(self, subexps, dbgSrc=None):
		super( _PDefSubexpList, self ).__init__( dbgSrc )
		self._subexps = subexps

	def emitPyTree(self):
		return PyVar( self.combinatorName )( PyListLiteral( [ s.emitPyTree()   for s in self._subexps ] ) )

	def getRulesUsed(self):
		return reduce( lambda rules, x: rules + x.getRulesUsed(), self._subexps, [] )



class _PDefSequence (_PDefSubexpList):
	combinatorName = _sequence



class _PDefCombine (_PDefSubexpList):
	combinatorName = _combine



class _PDefChoice (_PDefSubexpList):
	combinatorName = _choice




class _PDefSingleSubexp (_PDefExpression):
	combinatorName = None

	def __init__(self, subexp, dbgSrc=None):
		super( _PDefSingleSubexp, self ).__init__( dbgSrc )
		self._subexp = subexp

	def emitPyTree(self):
		return PyVar( self.combinatorName )( self._subexp.emitPyTree() )

	def getRulesUsed(self):
		return self._subexp.getRulesUsed()



class _PDefOptional (_PDefSingleSubexp):
	combinatorName = _optional



class _PDefZeroOrMore (_PDefSingleSubexp):
	combinatorName = _zeroOrMore



class _PDefOneOrMore (_PDefSingleSubexp):
	combinatorName = _oneOrMore



class _PDefPeek (_PDefSingleSubexp):
	combinatorName = _peek



class _PDefPeekNot (_PDefSingleSubexp):
	combinatorName = _peekNot



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
	
		
	def _testCompileAsExpr(self, pdef, pySrc):
		result = pdef.emitPyTree().compileAsExpr()
		if result != pySrc:
			print 'EXPECTED:'
			print pySrc
			print ''
			print 'RESULT:'
			print result
		self.assert_( result == pySrc )
		
		
	def test_Literal(self):
		self._testCompileAsExpr( _PDefLiteral( 'abc' ), "Literal( 'abc' )" )
		
	def test_Word(self):
		self._testCompileAsExpr( _PDefWord( 'abc' ), "Word( 'abc', None )" )
		self._testCompileAsExpr( _PDefWord( 'abc', 'def' ), "Word( 'abc', 'def' )" )

	def test_RegEx(self):
		self._testCompileAsExpr( _PDefRegEx( 'abc' ), "RegEx( 'abc' )" )
		
	def test_Sequence(self):
		self._testCompileAsExpr( _PDefSequence( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Sequence( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	def test_Sequence(self):
		self._testCompileAsExpr( _PDefSequence( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Sequence( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	def test_Combine(self):
		self._testCompileAsExpr( _PDefCombine( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Combine( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	def test_Choice(self):
		self._testCompileAsExpr( _PDefChoice( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Choice( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	def test_Optional(self):
		self._testCompileAsExpr( _PDefOptional( _PDefLiteral( 'a' ) ), "Optional( Literal( 'a' ) )" )
		
	def test_ZeroOrMore(self):
		self._testCompileAsExpr( _PDefZeroOrMore( _PDefLiteral( 'a' ) ), "ZeroOrMore( Literal( 'a' ) )" )
		
	def test_OneOrMore(self):
		self._testCompileAsExpr( _PDefOneOrMore( _PDefLiteral( 'a' ) ), "OneOrMore( Literal( 'a' ) )" )
		
	def test_Peek(self):
		self._testCompileAsExpr( _PDefPeek( _PDefLiteral( 'a' ) ), "Peek( Literal( 'a' ) )" )
		
	def test_PeekNot(self):
		self._testCompileAsExpr( _PDefPeekNot( _PDefLiteral( 'a' ) ), "PeekNot( Literal( 'a' ) )" )



