##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGParameters import CGParameters
from Britefury.CodeGraph.CGVar import CGVar

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTParameters (CVTNode):
	graphNodeClass = CGParameters

	graphNode = SheetRefField( CGParameters )


	def _paramNodes(self):
		return [ self._tree.buildNode( paramSource.node )   for paramSource in self.graphNode.params ]

	def _expandParamNode(self):
		if len( self.graphNode.expandParam ) > 0:
			return self._tree.buildNode( self.graphNode.expandParam[0].node )
		else:
			return None


	paramNodes = FunctionField( _paramNodes )
	expandParamNode = FunctionRefField( _expandParamNode )




	def addParameter(self, name):
		blockParmsCG = self.graphNode
		paramCG = CGVar()
		self.graph.nodes.append( paramCG )
		paramCG.name = name
		blockParmsCG.params.append( paramCG.parent )
		return self._tree.buildNode( paramCG )


	def deleteParameter(self, param):
		self.graphNode.params.remove( param.graphNode.parent )
		param.graphNode.destroySubtree()
