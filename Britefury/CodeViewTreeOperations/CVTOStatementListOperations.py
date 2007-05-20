##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGVar import *
from Britefury.CodeGraph.CGLocalVarDeclaration import *



def cvto_addLocalVarStatement(treeNode, position):
	var = CGVar()
	decl = CGLocalVarDeclaration()
	decl.variable.append( var.declaration )
	treeNode.graphNode.statements.insert( position, decl.parent )
	return treeNode.statementNodes[position ]
