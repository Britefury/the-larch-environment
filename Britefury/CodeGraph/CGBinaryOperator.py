##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGExpression import CGExpression
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *



class CGBinaryOperator (CGExpression):
	left = SemanticGraphSinkSingleSubtreeField( 'Left', 'Left sub-expression' )
	right = SemanticGraphSinkSingleSubtreeField( 'Right', 'Right sub-expression' )





class CGBinaryOperatorSimple (CGBinaryOperator):
	pyOperatorString = None


	def generatePyCode(self):
		assert self.pyOperatorString is not None
		return '( ' + self.left[0].node.generatePyCode() + ' ' + self.pyOperatorString + ' ' + self.right[0].node.generatePyCode() + ' )'



class CGBinOpAdd (CGBinaryOperatorSimple):
	pyOperatorString = '+'

class CGBinOpSub (CGBinaryOperatorSimple):
	pyOperatorString = '-'

class CGBinOpMul (CGBinaryOperatorSimple):
	pyOperatorString = '*'

class CGBinOpDiv (CGBinaryOperatorSimple):
	pyOperatorString = '/'

class CGBinOpPow (CGBinaryOperatorSimple):
	pyOperatorString = '**'

class CGBinOpMod (CGBinaryOperatorSimple):
	pyOperatorString = '%'

class CGBinOpBitAnd (CGBinaryOperatorSimple):
	pyOperatorString = '&'

class CGBinOpBitOr (CGBinaryOperatorSimple):
	pyOperatorString = '|'

class CGBinOpBitXor (CGBinaryOperatorSimple):
	pyOperatorString = '^'

class CGBinOpEq (CGBinaryOperatorSimple):
	pyOperatorString = '=='

class CGBinOpNEq (CGBinaryOperatorSimple):
	pyOperatorString = '!='

class CGBinOpLT (CGBinaryOperatorSimple):
	pyOperatorString = '<'

class CGBinOpGT (CGBinaryOperatorSimple):
	pyOperatorString = '>'

class CGBinOpLTE (CGBinaryOperatorSimple):
	pyOperatorString = '<='

class CGBinOpGTE (CGBinaryOperatorSimple):
	pyOperatorString = '>='

class CGBinOpLShift (CGBinaryOperatorSimple):
	pyOperatorString = '<<'

class CGBinOpRShift (CGBinaryOperatorSimple):
	pyOperatorString = '>>'


