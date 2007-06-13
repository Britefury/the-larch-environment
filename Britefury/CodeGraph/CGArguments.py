##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGNode import CGNode
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *



class CGArguments (CGNode):
	parent = SemanticGraphSourceField( 'Parent', 'Parent node' )
	args = SemanticGraphSinkMultipleSubtreeField( 'Arguments', 'Argument list' )
	expandArg = SemanticGraphSinkSingleSubtreeField( 'Expand arguments', 'Argument to be expanded' )




	def generatePyCode(self):
		p = ', '.join( [ argSource.node.generatePyCode()   for argSource in self.args ] )
		if len( self.expandArg ) > 0:
			p += ', *(' + self.expandArg[0].node.generatePyCode() + ')'
		return p
