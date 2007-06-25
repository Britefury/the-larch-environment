##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGNode import *
from Britefury.CodeGraph.CGExpression import *
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *



class CGTuple (CGExpression):
	args = SemanticGraphSinkMultipleSubtreeField( 'Arguments', 'Tuple arguments' )




	def generatePyCode(self):
		if len( self.args ) == 0:
			return '()'
		elif len( self.args ) == 1:
			return '( ' + self.args[0].node.generatePyCode() + ', )'
		else:
			return '( ' + ', '.join( [ argSource.node.generatePyCode()   for argSource in self.args ] ) + ' )'
