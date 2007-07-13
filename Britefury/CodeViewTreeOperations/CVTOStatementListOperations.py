##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGReturn import *
from Britefury.CodeGraph.CGImport import *
from Britefury.CodeGraph.CGBreak import *
from Britefury.CodeGraph.CGComment import *
from Britefury.CodeGraph.CGContinue import *
from Britefury.CodeGraph.CGNullExpression import *
from Britefury.CodeGraph.CGVar import *
from Britefury.CodeGraph.CGLocalVarDeclaration import *
from Britefury.CodeGraph.CGIf import *
from Britefury.CodeGraph.CGIfBlock import *
from Britefury.CodeGraph.CGWhile import *
from Britefury.CodeGraph.CGDef import *
from Britefury.CodeGraph.CGParameters import *
from Britefury.CodeGraph.CGBlock import *
from Britefury.CodeGraph.CGClass import *




def cvto_createCommentSubgraph(treeNode):
	comment = CGComment()
	treeNode.graph.nodes.append( comment )
	return comment

def cvto_addComment(treeNode, position):
	subgraph = cvto_createCommentSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createReturnSubgraph(treeNode):
	rtn = CGReturn()
	nullExp = CGNullExpression()
	treeNode.graph.nodes.append( rtn )
	treeNode.graph.nodes.append( nullExp )
	rtn.value.append( nullExp.parent )
	return rtn

def cvto_addReturnStatement(treeNode, position):
	subgraph = cvto_createReturnSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createLocalVarSubgraph(treeNode):
	decl = CGLocalVarDeclaration()
	var = CGVar()
	treeNode.graph.nodes.append( decl )
	treeNode.graph.nodes.append( var )
	decl.variable.append( var.declaration )
	return decl

def cvto_addLocalVarStatement(treeNode, position):
	subgraph = cvto_createLocalVarSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createIfSubgraph(treeNode):
	ifStmt = CGIf()
	ifBlock = CGIfBlock()
	nullExp = CGNullExpression()
	block = CGBlock()
	treeNode.graph.nodes.append( ifStmt )
	treeNode.graph.nodes.append( ifBlock )
	treeNode.graph.nodes.append( nullExp )
	treeNode.graph.nodes.append( block )

	ifBlock.condition.append( nullExp.parent )
	ifBlock.block.append( block.parent )
	ifStmt.ifBlocks.append( ifBlock.parent )
	return ifStmt

def cvto_addIfStatement(treeNode, position):
	subgraph = cvto_createIfSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createWhileSubgraph(treeNode):
	whileStmt = CGWhile()
	nullExp = CGNullExpression()
	block = CGBlock()
	treeNode.graph.nodes.append( whileStmt )
	treeNode.graph.nodes.append( nullExp )
	treeNode.graph.nodes.append( block )
	whileStmt.whileExpr.append( nullExp.parent )
	whileStmt.block.append( block.parent )
	return whileStmt

def cvto_addWhileStatement(treeNode, position):
	subgraph = cvto_createWhileSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createBreakSubgraph(treeNode):
	brk = CGBreak()
	treeNode.graph.nodes.append( brk )
	return brk

def cvto_addBreakStatement(treeNode, position):
	subgraph = cvto_createBreakSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createContinueSubgraph(treeNode):
	cont = CGContinue()
	treeNode.graph.nodes.append( cont )
	return cont

def cvto_addContinueStatement(treeNode, position):
	subgraph = cvto_createContinueSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createDefSubgraph(treeNode):
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
	return defStmt

def cvto_addDefStatement(treeNode, position):
	subgraph = cvto_createDefSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createClassSubgraph(treeNode):
	classStmt = CGClass()
	declVar = CGVar()
	block = CGBlock()
	treeNode.graph.nodes.append( classStmt )
	treeNode.graph.nodes.append( declVar )
	treeNode.graph.nodes.append( block )
	classStmt.declVar.append( declVar.declaration )
	classStmt.block.append( block.parent )
	return classStmt

def cvto_addClassStatement(treeNode, position):
	subgraph = cvto_createClassSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




def cvto_createImportSubgraph(treeNode):
	imp = CGImport()
	treeNode.graph.nodes.append( imp )
	return imp

def cvto_addImportStatement(treeNode, position):
	subgraph = cvto_createImportSubgraph( treeNode )
	treeNode.graphNode.statements.insert( position, subgraph.parent )
	return treeNode.statementNodes[position]




