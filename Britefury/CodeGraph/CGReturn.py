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
from Britefury.LowLevelCodeTree.LLCTReturnExp import LLCTReturnExp



class CGReturn (CGExpression):
	value = SemanticGraphSinkSingleSubtreeField( 'Value', 'Value' )
	rootBlock = SemanticGraphSinkSingleField( 'Root node', 'Root node' )





	def generateLLCT(self, tree):
		assert len( self.value ) > 0
		assert len( self.parent ) > 0

		if len( self.root ) > 0:
			numFrames = 1
			rootNode = self.root[0].node
			node = self
			while node is not rootBlock:
				if isinstance( node, CGBlock ):
					numFrames += 1
				node = node.parent[0].node
		else:
			numFrames = 1

		return LLCTReturnExp( self.value[0].node.generateLLCT( tree ), numFrames )
