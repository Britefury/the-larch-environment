##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNullExpression import CGNullExpression
from Britefury.CodeGraph.CGStringLiteral import CGStringLiteral

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTNullExpression (CVTNode):
	graphNodeClass = CGNullExpression


	graphNode = SheetRefField( CGNullExpression )


	def replaceWithStringLiteral(self):
		parentCGSink = self.graphNode.parent[0]
		n = parentCGSink.index( self.graphNode.parent )
		strLit = CGStringLiteral()
		parentCGSink[n] = strLit.parent
		return self._tree.buildNode( strLit )

