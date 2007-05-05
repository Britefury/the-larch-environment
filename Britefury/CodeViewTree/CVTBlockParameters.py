##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGBlock import CGBlock

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTBlockParameters (CVTNode):
	graphNode = SheetRefField( CGBlock )


	def _paramNodes(self):
		return [ self._tree.buildNode( paramSource.node )   for paramSource in self.graphNode.params ]

	def _expandParamNode(self):
		if len( self.graphNode.expandParam ) > 0:
			return self._tree.buildNode( self.graphNode.expandParam[0].node )
		else:
			return None


	paramNodes = FunctionField( _paramNodes )
	expandParamNode = FunctionRefField( _expandParamNode )



