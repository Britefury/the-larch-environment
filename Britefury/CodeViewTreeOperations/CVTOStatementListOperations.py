##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGReturn import *
from Britefury.CodeGraph.CGVar import *
from Britefury.CodeGraph.CGLocalVarDeclaration import *



def cvto_addReturnStatement(treeNode, position):
	rtn = CGReturn()
	treeNode.graph.nodes.append( rtn )
	treeNode.graphNode.statements.insert( position, rtn.parent )
	return treeNode.statementNodes[position ]




def cvto_addLocalVarStatement(treeNode, position):
	var = CGVar()
	decl = CGLocalVarDeclaration()
	treeNode.graph.nodes.append( var )
	treeNode.graph.nodes.append( decl )
	decl.variable.append( var.declaration )
	treeNode.graphNode.statements.insert( position, decl.parent )
	return treeNode.statementNodes[position ]
