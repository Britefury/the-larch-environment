##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGLocalAssignment import CGLocalAssignment

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTLocalAssignment (CVTNode):
	graphNodeClass = CGLocalAssignment

	graphNode = SheetRefField( CGLocalAssignment )


	@FunctionRefField
	def varRefNode(self):
		return self._tree.buildNode( self.graphNode.varRef[0].node )

	@FunctionRefField
	def valueNode(self):
		return self._tree.buildNode( self.graphNode.value[0].node )

