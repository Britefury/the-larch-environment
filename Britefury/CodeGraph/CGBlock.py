##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGNode import CGNode
from Britefury.CodeGraph.CGVar import CGVar
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *

from Britefury.PyCodeGen.PyCodeGen import *



class CGBlock (CGNode):
	name = Field( str, '' )
	parent = SemanticGraphSourceField( 'Parent node', 'Parent node' )
	statements = SemanticGraphSinkMultipleSubtreeField( 'Statements', 'Statement list' )



	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()
		for statementSource in self.statements:
			codeBlock += statementSource.node.generatePyCodeBlock()
		return codeBlock
