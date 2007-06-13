##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGStringLiteral import *
from Britefury.CodeGraph.CGLocalRef import *
from Britefury.CodeGraph.CGLambda import *
from Britefury.CodeGraph.CGParameters import *
from Britefury.CodeGraph.CGNullExpression import *
from Britefury.CodeGraph.CGBlock import *



def cvto_insertStringLiteral(treeNode, treeNodePath):
	strLit = CGStringLiteral()
	treeNode.graph.nodes.append( strLit )
	return treeNode.insertNode( strLit, treeNodePath )



def cvto_insertLocalRef(treeNode, treeNodePath):
	ref = CGLocalRef()
	var = CGVar()
	ref.variable.append( var.parent )
	treeNode.graph.nodes.append( ref )
	treeNode.graph.nodes.append( var )
	return treeNode.insertNode( ref, treeNodePath )



def cvto_insertLambda(treeNode, treeNodePath):
	lambdaNode = CGLambda()
	treeNode.graph.nodes.append( lambdaNode )

	paramsNode = CGParameters()
	treeNode.graph.nodes.append( paramsNode )

	nullExpression = CGNullExpression()
	treeNode.graph.nodes.append( nullExpression )

	lambdaNode.parameters.append( paramsNode.parent )
	lambdaNode.statement.append( nullExpression.parent )

	return treeNode.insertNode( lambdaNode, treeNodePath )
