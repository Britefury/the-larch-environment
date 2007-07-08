##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGWhile import CGWhile

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTStatement import CVTStatement
from Britefury.CodeViewTree.CodeViewTree import *
from Britefury.CodeViewTree.CVTElseBlock import *



class CVTWhile (CVTStatement):
	graphNode = SheetRefField( CGWhile )


	whileExprNode = CVTSimpleSinkProductionSingleField( CGWhile.whileExpr )
	blockNode = CVTSimpleSinkProductionSingleField( CGWhile.block )
	elseBlockNode = CVTSimpleSinkProductionOptionalField( CGWhile.elseBlock, rule=CVTRuleElseBlock )



	def addElse(self):
		if len( self.graphNode.elseBlock ) == 0:
			elseBlockCG = CGBlock()
			self.graph.nodes.append( elseBlockCG )

			self.graphNode.elseBlock.append( elseBlockCG.parent )

			return self._tree.buildNode( elseBlockCG )
		else:
			return self._tree.buildNode( self.graphNode.elseBlock[0].node )


	def removeElse(self):
		if len( self.graphNode.elseBlock ) == 1:
			elseCG = self.graphNode.elseBlock[0].node
			del self.graphNode.elseBlock[0]
			elseCG.destroySubtree()





class CVTRuleWhile (CVTRuleSimple):
	graphNodeClass = CGWhile
	cvtNodeClass = CVTWhile

CVTRuleWhile.register()

