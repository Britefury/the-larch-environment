##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGStatement import CGStatement
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *
from Britefury.LowLevelCodeTree.LLCTAssignmentExp import LLCTAssignmentExp



class CGLocalAssignment (CGStatement):
	variable = SheetGraphSinkSingleField( 'Variable', 'Target variable' )
	value = SheetGraphSinkSingleField( 'Value', 'Value' )


	def destroyChildren(self):
		# Don't destroy @varaible; its a reference

		for source in self.value:
			source.node.destroy()


	def generateLLCT(self, tree):
		assert len( self.variable ) > 0
		assert len( self.value ) > 0
		return LLCTAssignmentExp( self.variable[0].node.generateLLCT( tree ), self.value[0].node.generateLLCT( tree ) )
