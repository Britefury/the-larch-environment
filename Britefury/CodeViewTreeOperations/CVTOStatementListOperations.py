##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGReturn import *
from Britefury.CodeGraph.CGNullExpression import *
from Britefury.CodeGraph.CGVar import *
from Britefury.CodeGraph.CGLocalVarDeclaration import *
from Britefury.CodeGraph.CGDef import *
from Britefury.CodeGraph.CGParameters import *
from Britefury.CodeGraph.CGBlock import *
from Britefury.CodeGraph.CGClass import *



def cvto_addReturnStatement(treeNode, position):
	rtn = CGReturn()
	nullExp = CGNullExpression()
	treeNode.graph.nodes.append( rtn )
	treeNode.graph.nodes.append( nullExp )
	rtn.value.append( nullExp.parent )
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




def cvto_addDefStatement(treeNode, position):
	defStmt = CGDef()
	declVar = CGVar()
	params = CGParameters()
	block = CGBlock()
	treeNode.graph.nodes.append( defStmt )
	treeNode.graph.nodes.append( declVar )
	treeNode.graph.nodes.append( params )
	treeNode.graph.nodes.append( block )
	defStmt.declVar.append( declVar.declaration )
	defStmt.parameters.append( params.parent )
	defStmt.block.append( block.parent )
	treeNode.graphNode.statements.insert( position, defStmt.parent )
	return treeNode.statementNodes[position ]




def cvto_addClassStatement(treeNode, position):
	classStmt = CGClass()
	declVar = CGVar()
	block = CGBlock()
	treeNode.graph.nodes.append( classStmt )
	treeNode.graph.nodes.append( declVar )
	treeNode.graph.nodes.append( block )
	classStmt.declVar.append( declVar.declaration )
	classStmt.block.append( block.parent )
	treeNode.graphNode.statements.insert( position, classStmt.parent )
	return treeNode.statementNodes[position ]
