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

from Britefury.PyCodeGen.PyCodeGen import *



class CGBlock (CGNode):
	name = Field( str, '' )
	parent = SemanticGraphSourceField( 'Parent node', 'Parent node' )
	statements = SemanticGraphSinkMultipleSubtreeField( 'Statements', 'Statement list' )



	def getReferenceableNodeByName(self, targetName, sourceNode=None):
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



	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()

		if len( self.statements ) > 0:
			for statementSource in self.statements:
				codeBlock += statementSource.node.generatePyCodeBlock()
		else:
			codeBlock.append( 'pass' )

		return codeBlock


	def generateTexBody(self):
		codeBlock = PyCodeBlock()

		if len( self.statements ) > 0:
			for statementSource in self.statements:
				codeBlock += statementSource.node.generateTex()
		else:
			codeBlock.append( '\\gSymPass' )

		return codeBlock


