##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction, GLispCompilerCouldNotCompileSpecial, GLispCompilerInvalidFormLength, GLispCompilerInvalidItem
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent

from Britefury.Parser.Parser import Literal, Word, Sequence



_literal = 'Literal'
_word = 'Word'
_sequence = 'Sequence'
_firstOf = 'FirstOf'



class _PDefExpression (object):
	def emitPyTree(self, outerTreeFactory, rulesDefined):
		pass


	def getRulesUsed(self):
		return []



class _PDefLiteral (_PDefExpression):
	def __init__(self, matchString):
		super( _PDefLiteral, self ).__init__()
		self._matchString = matchString

	def emitPyTree(self, rulesDefined):
		return PyVar( _literal )( PyLiteralValue( self._matchString ) )



class _PDefWord (_PDefExpression):
	def __init__(self, initChars, bodyChars=None):
		super( _PDefWord, self ).__init__()
		self._initChars = initChars
		self._bodyChars = bodyChars

	def emitPyTree(self, rulesDefined):
		return PyVar( _word )( PyLiteralValue( self._initChars ), PyLiteralValue( self._bodyChars ) )




class _PDefSubexpList (_PDefExpression):
	parserExpressionName = None

	def __init__(self, subexps):
		super( _PDefSubexpList, self ).__init__()
		self._subexps = subexps

	def emitPyTree(self, rulesDefined):
		return PyVar( self.parserExpressionName )( PyListLiteral( [ x.emitPyTree( rulesDefined )   for x in self._subexps ] ) )

	def getRulesUsed(self):
		return reduce( lambda rules, x: rules + x.getRulesUsed(), self._subexps, [] )



class _PDefSequence (_PDefSubexpList):
	parserExpressionName = _sequence



class _PDefFirstOf (_PDefSubexpList):
	parserExpressionName = _firstOf



import unittest




class Test_gSymParser (unittest.TestCase):
	def _testCompileAsExpr(self, pdef, pySrc):
		result = pdef.emitPyTree( set() ).compileAsExpr()
		if result != pySrc:
			print 'EXPECED:'
			print pySrc
			print ''
			print 'RESULT:'
			print result
		self.assert_( result == pySrc )
	
	def test_Literal(self):
		self._testCompileAsExpr( _PDefLiteral( 'abc' ), "Literal( 'abc' )" )


	def test_Word(self):
		self._testCompileAsExpr( _PDefWord( 'abc' ), "Word( 'abc', None )" )
		self._testCompileAsExpr( _PDefWord( 'abc', 'xyz' ), "Word( 'abc', 'xyz' )" )

	def test_Sequence(self):
		self._testCompileAsExpr( _PDefSequence( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "Sequence( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
	def test_FirstOf(self):
		self._testCompileAsExpr( _PDefFirstOf( [ _PDefLiteral( 'a' ), _PDefLiteral( 'b' ), _PDefLiteral( 'c' ) ] ), "FirstOf( [ Literal( 'a' ), Literal( 'b' ), Literal( 'c' ) ] )" )
		
		
		