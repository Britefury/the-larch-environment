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


	ifBlockNode = CVTSimpleSinkProductionSingleField( CGIf.ifBlock )
	elseIfBlockNodes = CVTSimpleSinkProductionMultipleField( CGIf.elseIfBlocks, rule=CVTRuleElseIfBlock )
	elseStatementsNode = CVTSimpleSinkProductionOptionalField( CGIf.elseBlock, rule=CVTRuleElseBlock )



	def addElseIf(self, position):
		elseIfBlockCG = CGIfBlock()
		nullExpr = CGNullExpression()
		block = CGBlock()

		self.graph.nodes.append( elseIfBlockCG )
		self.graph.nodes.append( nullExpr )
		self.graph.nodes.append( block )

		elseIfBlockCG.condition.append( nullExpr.parent )
		elseIfBlockCG.block.append( block.parent )
		self.graphNode.elseIfBlocks.insert( position, elseIfBlockCG.ifStatement )

		return self._tree.buildNode( elseIfBlockCG )



	def addElse(self):
		elseBlockCG = CGBlock()
		self.graph.nodes.append( elseBlockCG )

		self.graphNode.elseBlock.append( elseBlockCG.parent )

		return self._tree.buildNode( elseBlockCG )




	def hasElseIfs(self):
		return len( self.graphNode.elseIfBlocks ) > 0



	def removeIf(self):
		# Get the if block
		ifCG = self.graphNode.ifBlock[0].node
		# Get the first else-if block
		elseIf0CG = self.graphNode.elseIfBlocks[0].node

		# Remove the if block
		del self.graphNode.ifBlock[0]
		# Remove the first else-if block
		del self.graphNode.elseIfBlocks[0]
		# Replace the if block with the else-if block
		self.graphNode.ifBlock.append( elseIf0CG.ifStatement )

		ifCG.destroySubtree()



	def removeElseIf(self, elseIfCVT):
		elseIfCG = elseIfCVT.graphNode
		self.graphNode.elseIfBlocks.remove( elseIfCG.ifStatement )
		elseIfCG.destroySubtree()


	def removeElse(self):
		elseCG = self.graphNode.elseBlock[0].node
		del self.graphNode.elseBlock[0]
		elseCG.destroySubtree()





class CVTRuleIf (CVTRuleSimple):
	graphNodeClass = CGIf
	cvtNodeClass = CVTIf

CVTRuleIf.register()

