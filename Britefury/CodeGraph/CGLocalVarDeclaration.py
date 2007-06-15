##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGStatement import CGStatement
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *
from Britefury.LowLevelCodeTree.LLCTBindExp import LLCTBindExp



class CGLocalVarDeclaration (CGStatement):
	value = SemanticGraphSinkSingleSubtreeField( 'Value', 'Value' )
	variable = SemanticGraphSinkSingleSubtreeField( 'Variable', 'The variable' )


	def generatePyCode(self):
		if len( self.value ) > 0:
			return self.variable[0].node.name + ' = ' + self.value[0].node.generatePyCode()
		else:
			return self.variable[0].node.name + ' = None'



	def buildReferenceableNodeTable(self, nodeTable):
		nodeTable[self.variable[0].node.name] = self.variable[0].node

