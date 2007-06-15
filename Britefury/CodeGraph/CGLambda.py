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



class CGLambda (CGExpression):
	parameters = SemanticGraphSinkSingleSubtreeField( 'Parameters', 'Parameters' )
	valueExpr = SemanticGraphSinkSingleSubtreeField( 'ValueExpr', 'Value expression' )



	def generatePyCode(self):
		return 'lambda ' + self.parameters[0].node.generatePyCode() + ': ' + self.valueExpr[0].node.generatePyCode()



	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		param = self.parameters[0].node.getReferenceableNodeByName( targetName, sourceNode )
		if param is not None:
			return param
		else:
			return None
