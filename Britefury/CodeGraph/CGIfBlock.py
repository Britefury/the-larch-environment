##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *

from Britefury.CodeGraph.CGNode import CGNode

from Britefury.PyCodeGen.PyCodeGen import *





class CGIfBlock (CGNode):
	parent = SemanticGraphSourceField( 'Parent', 'Parent if statement' )
	condition = SemanticGraphSinkSingleSubtreeField( 'Condition', 'Else-if condition' )
	block = SemanticGraphSinkSingleSubtreeField( 'Block', 'Code block' )


	def generatePyCode(self, bElseIf):
		if bElseIf:
			prefix = 'elif'
		else:
			prefix = 'if'
		return prefix + ' ' + self.condition[0].node.generatePyCode() + ':'

	def generatePyCodeBlock(self, bElseIf):
		codeBlock = PyCodeBlock()
		codeBlock.append( self.generatePyCode(bElseIf) )
		innerBlock = self.block[0].node.generatePyCodeBlock()
		innerBlock.indent()
		codeBlock += innerBlock
		return codeBlock



	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		return self.parent[0].node.getReferenceableNodeByName( targetName, self )


	def buildReferenceableNodeTable(self, nodeTable):
		pass


