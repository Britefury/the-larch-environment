##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *

from Britefury.CodeGraph.CGStatement import CGStatement

from Britefury.PyCodeGen.PyCodeGen import *





class CGIf (CGStatement):
	ifBlocks = SemanticGraphSinkMultipleSubtreeField( 'If', 'If block' )
	elseBlock = SemanticGraphSinkSingleSubtreeField( 'ElseBlock', 'Else block' )



	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()

		codeBlock += self.ifBlocks[0].node.generatePyCodeBlock( False )

		for elseIfBlockSource in self.ifBlocks[1:]:
			codeBlock += elseIfBlockSource.node.generatePyCodeBlock( True )

		if len( self.elseBlock ) > 0:
			codeBlock.append( 'else:' )
			elseCode = self.elseBlock[0].node.generatePyCodeBlock()
			elseCode.indent()
			codeBlock += elseCode

		return codeBlock





