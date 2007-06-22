##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGClass import CGClass

from Britefury.CodeViewTree.CVTStatement import CVTStatement
from Britefury.CodeViewTree.CVTClassBases import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTClass (CVTStatement):
	graphNode = SheetRefField( CGClass )


	@FunctionRefField
	def declVarNode(self):
		if len( self.graphNode.declVar ) > 0:
			return self._tree.buildNode( self.graphNode.declVar[0].node )
		else:
			return None

	@FunctionRefField
	def basesNode(self):
		return self._tree.buildNode( self.graphNode, CVTRuleClassBases )

	@FunctionField
	def statementsNode(self):
		return self._tree.buildNode( self.graphNode.block[0].node )



class CVTRuleClass (CVTRuleSimple):
	graphNodeClass = CGClass
	cvtNodeClass = CVTClass

CVTRuleClass.register()

