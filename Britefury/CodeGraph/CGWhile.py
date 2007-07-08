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





class CGWhile (CGStatement):
	whileExpr = SemanticGraphSinkSingleSubtreeField( 'While exp', 'While expression' )
	block = SemanticGraphSinkSingleSubtreeField( 'Block', 'Code block' )
	elseBlock = SemanticGraphSinkSingleSubtreeField( 'ElseBlock', 'Else block' )



	def generatePyCode(self):
		return 'while ' + self.whileExpr[0].node.generatePyCode() + ':'

	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()
		codeBlock.append( self.generatePyCode() )
		innerBlock = self.block[0].node.generatePyCodeBlock()
		innerBlock.indent()
		codeBlock += innerBlock

		if len( self.elseBlock ) > 0:
			codeBlock.append( 'else:' )
			elseCode = self.elseBlock[0].node.generatePyCodeBlock()
			elseCode.indent()
			codeBlock += elseCode

		return codeBlock

