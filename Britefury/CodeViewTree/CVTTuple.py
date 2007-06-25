##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGTuple import CGTuple
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTExpression import *
from Britefury.CodeViewTree.CodeViewTree import *

from Britefury.CodeViewTreeOperations.CVTOWrapInAssignment import cvto_wrapInAssignment



class CVTTuple (CVTExpression):
	graphNode = SheetRefField( CGTuple )



	argNodes = CVTSimpleSinkProductionMultipleField( CGTuple.args )



	def addArgument(self):
		argumentsCG = self.graphNode
		argCG = CGNullExpression()
		self.graph.nodes.append( argCG )
		argumentsCG.args.append( argCG.parent )
		return self._tree.buildNode( argCG )


	def deleteArgument(self, argument):
		if argument.graphNode.parent in self.graphNode.args:
			self.graphNode.args.remove( argument.graphNode.parent )
			argument.graphNode.destroySubtree()



	def insertNode(self, graphNodeToInsert, position):
		self.graphNode.args.insert( position, graphNodeToInsert.parent )
		return self._tree.buildNode( graphNodeToInsert )



	def wrapInAssignment(self):
		return cvto_wrapInAssignment( self )




class CVTRuleTuple (CVTRuleSimple):
	graphNodeClass = CGTuple
	cvtNodeClass = CVTTuple

CVTRuleTuple.register()

