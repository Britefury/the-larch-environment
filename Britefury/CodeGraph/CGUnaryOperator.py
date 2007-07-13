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



class CGUnaryOperator (CGExpression):
	expr = SemanticGraphSinkSingleSubtreeField( 'Expr', 'Sub-expression' )





class CGUnaryOperatorSimple (CGUnaryOperator):
	pyOperatorString = None


	def generatePyCode(self):
		assert self.pyOperatorString is not None
		return self.pyOperatorString + ' ' + self.right[0].node.generatePyCode()



class CGNegate (CGUnaryOperatorSimple):
	pyOperatorString = '-'

class CGNot (CGUnaryOperatorSimple):
	pyOperatorString = 'not'

