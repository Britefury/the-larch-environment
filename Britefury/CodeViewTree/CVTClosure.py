##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGClosure import CGClosure

from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTBlockStatements import CVTBlockStatements
from Britefury.CodeViewTree.CVTBlockParameters import CVTBlockParameters



class CVTClosure (CVTExpression):
	graphNodeClass = CGClosure

	graphNode = SheetRefField( CGClosure )


	def _statementsNode(self):
		return self._tree.buildNode( self.graphNode.block[0].node, CVTBlockStatements )

	def _paramsNode(self):
		return self._tree.buildNode( self.graphNode.block[0].node, CVTBlockParameters )


	statementsNode = FunctionRefField( _statementsNode )
	paramsNode = FunctionRefField( _paramsNode )

