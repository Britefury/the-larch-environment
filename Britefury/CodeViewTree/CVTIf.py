##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNullExpression import CGNullExpression
from Britefury.CodeGraph.CGBlock import CGBlock
from Britefury.CodeGraph.CGIfBlock import CGIfBlock

from Britefury.CodeGraph.CGIf import CGIf

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTStatement import *
from Britefury.CodeViewTree.CVTElseIfBlock import *
from Britefury.CodeViewTree.CVTElseBlock import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTIf (CVTStatement):
	graphNode = SheetRefField( CGIf )


	@FunctionRefField
	def ifBlockNode(self):
		if len( self.graphNode.ifBlocks ) > 0:
			return self._tree.buildNode( self.graphNode.ifBlocks[0].node )
		else:
			return self._f_makeInvalidNode()


	@FunctionField
	def elseIfBlockNodes(self):
		if len( self.graphNode.ifBlocks ) > 1:
			return [ self._tree.buildNode( ifBlockSource.node, rule=CVTRuleElseIfBlock )   for ifBlockSource in self.graphNode.ifBlocks[1:] ]
		else:
			return []

	elseBlockNode = CVTSimpleSinkProductionOptionalField( CGIf.elseBlock, rule=CVTRuleElseBlock )



	def addElseIf(self, position):
		elseIfBlockCG = CGIfBlock()
		nullExpr = CGNullExpression()
		block = CGBlock()

		self.graph.nodes.append( elseIfBlockCG )
		self.graph.nodes.append( nullExpr )
		self.graph.nodes.append( block )

		elseIfBlockCG.condition.append( nullExpr.parent )
		elseIfBlockCG.block.append( block.parent )
		self.graphNode.ifBlocks.insert( position + 1, elseIfBlockCG.parent )

		return self._tree.buildNode( elseIfBlockCG )



	def addElse(self):
		if len( self.graphNode.elseBlock ) == 0:
			elseBlockCG = CGBlock()
			self.graph.nodes.append( elseBlockCG )

			self.graphNode.elseBlock.append( elseBlockCG.parent )

			return self._tree.buildNode( elseBlockCG )
		else:
			return self._tree.buildNode( self.graphNode.elseBlock[0].node )




	def hasElseIfs(self):
		return len( self.graphNode.ifBlocks ) > 1



	def removeIf(self):
		ifBlockCG = self.graphNode.ifBlocks[0].node
		del self.graphNode.ifBlocks[0]
		ifBlockCG.destroySubtree()



	def removeElseIf(self, elseIfCVT):
		elseIfCG = elseIfCVT.graphNode
		self.graphNode.ifBlocks.remove( elseIfCG.parent )
		elseIfCG.destroySubtree()


	def removeElse(self):
		if len( self.graphNode.elseBlock ) == 1:
			elseCG = self.graphNode.elseBlock[0].node
			del self.graphNode.elseBlock[0]
			elseCG.destroySubtree()





class CVTRuleIf (CVTRuleSimple):
	graphNodeClass = CGIf
	cvtNodeClass = CVTIf

CVTRuleIf.register()

