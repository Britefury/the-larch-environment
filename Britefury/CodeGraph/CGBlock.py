##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGNode import CGNode
from Britefury.CodeGraph.CGVar import CGVar
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *
from Britefury.LowLevelCodeTree.LLCTBlock import LLCTBlock



class CGBlock (CGNode):
	name = Field( str, '' )
	parent = SemanticGraphSourceField( 'Parent node', 'Parent node' )
	params = SemanticGraphSinkMultipleSubtreeField( 'Parameters', 'Parameters' )
	expandParam = SemanticGraphSinkSingleSubtreeField( 'Expand parameter', 'Expand parameter' )
	statements = SemanticGraphSinkMultipleSubtreeField( 'Statements', 'Statement list' )



	def generateLLCT(self, tree):
		llctStatements = [ statementSource.node.generateLLCT( tree )   for statementSource in self.statements ]
		paramNames = [ paramSource.node.generateLLCT( tree )   for paramSource in self.params ]
		if len( self.expandParam ) > 0:
			expandParamName = self.expandParam[0].node.generateLLCT( tree )
		else:
			expandParamName = None
		return LLCTBlock( self.name, llctStatements, paramNames, expandParamName )



	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		for paramSource in self.params:
			if targetName == paramSource.node.name:
				return paramSource.node

		if len( self.expandParam ) > 0:
			if targetName == self.expandParam[0].name:
				return self.expandParam[0].node

		if sourceNode is not None:
			n = self.statements.index( sourceNode.parent )
			nodeTable = {}
			for statementSource in self.statements[:n]:
				statementSource.node.buildReferenceableNodeTable( nodeTable )
			try:
				return nodeTable[targetName]
			except KeyError:
				pass

		return self.parent[0].node.getReferenceableNodeByName( targetName, self )

