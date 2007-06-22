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

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTParameters (CVTNode):
	graphNode = SheetRefField( CGParameters )


	paramNodes = CVTSimpleSinkProductionMultipleField( CGParameters.params )
	expandParamNode = CVTSimpleSinkProductionOptionalField( CGParameters.expandParam )




	def addParameter(self, name):
		blockParmsCG = self.graphNode
		paramCG = CGVar()
		self.graph.nodes.append( paramCG )
		paramCG.name = name
		blockParmsCG.params.append( paramCG.declaration )
		return self._tree.buildNode( paramCG )


	def deleteParameter(self, param):
		if param.graphNode.declaration in self.graphNode.params:
			self.graphNode.params.remove( param.graphNode.declaration )
			param.graphNode.destroySubtree()




class CVTRuleParameters (CVTRuleSimple):
	graphNodeClass = CGParameters
	cvtNodeClass = CVTParameters

CVTRuleParameters.register()

