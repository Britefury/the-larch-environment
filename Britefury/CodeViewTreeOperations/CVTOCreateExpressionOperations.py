##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGStringLiteral import *
from Britefury.CodeGraph.CGIntLiteral import *
from Britefury.CodeGraph.CGUnboundRef import *
from Britefury.CodeGraph.CGUnaryOperator import CGNegate, CGNot
from Britefury.CodeGraph.CGLambda import *
from Britefury.CodeGraph.CGParameters import *
from Britefury.CodeGraph.CGNullExpression import *
from Britefury.CodeGraph.CGTuple import *
from Britefury.CodeGraph.CGList import *



def cvto_insertStringLiteral(treeNode, position):
	strLit = CGStringLiteral()
	treeNode.graph.nodes.append( strLit )
	return treeNode.insertNode( strLit, position )



def cvto_insertIntLiteral(treeNode, position):
	intLit = CGIntLiteral()
	treeNode.graph.nodes.append( intLit )
	return treeNode.insertNode( intLit, position )



def cvto_insertUnboundRef(treeNode, position):
	ref = CGUnboundRef()
	treeNode.graph.nodes.append( ref )
	return treeNode.insertNode( ref, position )



def cvto_insertNegate(treeNode, position):
	negateNode = CGNegate()
	treeNode.graph.nodes.append( negateNode )

	nullExpression = CGNullExpression()
	treeNode.graph.nodes.append( nullExpression )

	negateNode.expr.append( nullExpression.parent )

	return treeNode.insertNode( negateNode, position )



def cvto_insertNot(treeNode, position):
	notNode = CGNot()
	treeNode.graph.nodes.append( notNode )

	nullExpression = CGNullExpression()
	treeNode.graph.nodes.append( nullExpression )

	notNode.expr.append( nullExpression.parent )

	return treeNode.insertNode( notNode, position )



def cvto_insertLambda(treeNode, position):
	lambdaNode = CGLambda()
	treeNode.graph.nodes.append( lambdaNode )

	paramsNode = CGParameters()
	treeNode.graph.nodes.append( paramsNode )

	nullExpression = CGNullExpression()
	treeNode.graph.nodes.append( nullExpression )

	lambdaNode.parameters.append( paramsNode.parent )
	lambdaNode.valueExpr.append( nullExpression.parent )

	return treeNode.insertNode( lambdaNode, position )




def cvto_insertTuple(treeNode, position):
	tup = CGTuple()
	treeNode.graph.nodes.append( tup )
	return treeNode.insertNode( tup, position )




def cvto_insertList(treeNode, position):
	ls = CGList()
	treeNode.graph.nodes.append( ls )
	return treeNode.insertNode( ls, position )


