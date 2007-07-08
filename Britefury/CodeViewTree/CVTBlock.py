##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGBlock import CGBlock

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTBlock (CVTNode):
	graphNode = SheetRefField( CGBlock )


	statementNodes = CVTSimpleSinkProductionMultipleField( CGBlock.statements )




	def insertNode(self, graphNodeToInsert, position):
		self.graphNode.statements.insert( position, graphNodeToInsert.parent )
		return self._tree.buildNode( graphNodeToInsert )



	def deleteStatement(self, statement):
		if statement.graphNode.parent in self.graphNode.statements:
			self.graphNode.statements.remove( statement.graphNode.parent )
			statement.graphNode.destroySubtree()



class CVTRuleBlock (CVTRuleSimple):
	graphNodeClass = CGBlock
	cvtNodeClass = CVTBlock

CVTRuleBlock.register()

