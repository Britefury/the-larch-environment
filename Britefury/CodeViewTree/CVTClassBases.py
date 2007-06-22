##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGClass import CGClass
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTClassBases (CVTNode):
	graphNode = SheetRefField( CGClass )


	baseNodes = CVTSimpleSinkProductionMultipleField( CGClass.bases )



	def addBase(self):
		basesCG = self.graphNode
		baseCG = CGNullExpression()
		self.graph.nodes.append( baseCG )
		basesCG.bases.append( baseCG.parent )
		return self._tree.buildNode( baseCG )


	def deleteBase(self, baseument):
		if baseument.graphNode.parent in self.graphNode.bases:
			self.graphNode.bases.remove( baseument.graphNode.parent )
			baseument.graphNode.destroySubtree()



	def insertNode(self, graphNodeToInsert, treeNodePath):
		position = len( self.baseNodes )
		if len( treeNodePath ) > 1:
			try:
				n = self.baseNodes.index( treeNodePath[1] )
			except ValueError:
				pass
			else:
				position = n
		self.graphNode.bases.insert( position, graphNodeToInsert.parent )
		return self._tree.buildNode( graphNodeToInsert )



class CVTRuleClassBases (CVTRuleSimple):
	graphNodeClass = CGClass
	cvtNodeClass = CVTClassBases
