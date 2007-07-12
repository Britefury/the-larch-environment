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




class CGParameters (CGNode):
	parent = SemanticGraphSourceField( 'Parent node', 'Parent node' )
	params = SemanticGraphSinkMultipleSubtreeField( 'Parameters', 'Parameters' )
	expandParam = SemanticGraphSinkSingleSubtreeField( 'Expand parameter', 'Expand parameter' )



	def generatePyCode(self):
		p = ', '.join( [ paramSource.node.generatePyCode()   for paramSource in self.params ] )
		if len( self.expandParam ) > 0:
			p += ', *' + self.expandParam[0].node.generatePyCode()
		return p



	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		for paramSource in self.params:
			if targetName == paramSource.node.name:
				return paramSource.node

		if len( self.expandParam ) > 0:
			if targetName == self.expandParam[0].name:
				return self.expandParam[0].node

		return None




	def generateTexBody(self):
		texBlock = PyCodeBlock()
		for paramSource in self.params:
			paramBlock = paramSource.node.generateTex()
			texBlock += paramBlock
		return texBlock


