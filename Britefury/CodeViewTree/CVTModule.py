##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGModule import CGModule
from Britefury.CodeGraph.CGLocalVarDeclaration import CGLocalVarDeclaration
from Britefury.CodeGraph.CGVar import CGVar
from Britefury.CodeGraph.CGNull import CGNull

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTModule (CVTNode):
	graphNodeClass = CGModule


	graphNode = SheetRefField( CGModule )


	def _statementNodes(self):
		return [ self._tree.buildNode( statementSource.node )   for statementSource in self.graphNode.statements ]

	statementNodes = FunctionField( _statementNodes )



	def addLocalVarNode(self, position):
		var = CGVar()
		decl = CGLocalVarDeclaration()
		decl.variable.append( var.declaration )
		self.graphNode.statements.insert( position, decl.parent )
		return self.statementNodes[position ]




