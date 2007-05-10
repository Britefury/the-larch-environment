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
from Britefury.SheetGraph.SheetGraph import *
from Britefury.LowLevelCodeTree.LLCTBlock import LLCTBlock



class CGBlock (CGNode):
	name = Field( str, '' )
	parent = SheetGraphSourceField( 'Parent node', 'Parent node' )
	params = SheetGraphSinkMultipleField( 'Parameters', 'Parameters' )
	expandParam = SheetGraphSinkSingleField( 'Expand parameter', 'Expand parameter' )
	statements = SheetGraphSinkMultipleField( 'Statements', 'Statement list' )


	def generateLLCT(self, tree):
		llctStatements = [ statementSource.node.generateLLCT( tree )   for statementSource in self.statements ]
		paramNames = [ paramSource.node.generateLLCT( tree )   for paramSource in self.params ]
		if len( self.expandParam ) > 0:
			expandParamName = self.expandParam[0].node.generateLLCT( tree )
		else:
			expandParamName = None
		return LLCTBlock( self.name, llctStatements, paramNames, expandParamName )
