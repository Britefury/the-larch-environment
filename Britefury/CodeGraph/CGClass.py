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





class CGClass (CGStatement):
	declVar = SemanticGraphSinkSingleSubtreeField( 'Variable', 'Name variable' )
	bases = SemanticGraphSinkMultipleSubtreeField( 'Bases', 'Base classes' )
	block = SemanticGraphSinkSingleSubtreeField( 'Block', 'Code block' )



	def generatePyCode(self):
		if len( self.bases ) == 0:
			return 'class ' + self.declVar[0].node.name + ':'
		else:
			return 'class ' + self.declVar[0].node.name + '(' + ', '.join( [ baseSource.node.generatePyCode()   for baseSource in self.bases ] ) + '):'

	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()
		codeBlock.append( self.generatePyCode() )
		innerBlock = self.block[0].node.generatePyCodeBlock()
		innerBlock.indent()
		codeBlock += innerBlock
		return codeBlock




	def buildReferenceableNodeTable(self, nodeTable):
		nodeTable[self.declVar[0].node.name] = self.declVar[0].node



