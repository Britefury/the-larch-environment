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



class CGParameters (CGNode):
	name = Field( str, '' )
	parent = SemanticGraphSourceField( 'Parent node', 'Parent node' )
	params = SemanticGraphSinkMultipleSubtreeField( 'Parameters', 'Parameters' )
	expandParam = SemanticGraphSinkSingleSubtreeField( 'Expand parameter', 'Expand parameter' )



	def generatePyCode(self):
		p = ', '.join( [ paramSource.node.generatePyCode()   for paramSource in self.params ] )
		if len( self.expandParam ) > 0:
			p += ', *' + self.expandParam[0].node.generatePyCode()
		return p