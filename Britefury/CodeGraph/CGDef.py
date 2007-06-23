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

from Britefury.PyCodeGen.PyCodeGen import *





class CGDef (CGStatement):
	declVar = SemanticGraphSinkSingleSubtreeField( 'Variable', 'Name variable' )
	parameters = SemanticGraphSinkSingleSubtreeField( 'Parameters', 'Parameters' )
	block = SemanticGraphSinkSingleSubtreeField( 'Block', 'Code block' )
	functionDoc = Field( str, '', doc='Function documentation' )


	@FunctionField
	def functionName(self):
		if len( self.declVar ) > 0:
			return self.declVar[0].node.name
		else:
			return ''



	def generatePyCode(self):
		return 'def ' + self.declVar[0].node.name + '(' + self.parameters[0].node.generatePyCode() + '):'

	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()
		codeBlock.append( self.generatePyCode() )
		innerBlock = self.block[0].node.generatePyCodeBlock()
		innerBlock.indent()
		codeBlock += innerBlock
		return codeBlock




	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		param = self.parameters[0].node.getReferenceableNodeByName( targetName, sourceNode )
		if param is not None:
			return param
		else:
			return None



	def buildReferenceableNodeTable(self, nodeTable):
		nodeTable[self.declVar[0].node.name] = self.declVar[0].node



