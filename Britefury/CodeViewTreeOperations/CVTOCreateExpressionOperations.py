##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGStringLiteral import *
from Britefury.CodeGraph.CGUnboundRef import *
from Britefury.CodeGraph.CGLambda import *
from Britefury.CodeGraph.CGBlock import *



def cvto_insertStringLiteral(treeNode, treeNodePath):
	strLit = CGStringLiteral()
	treeNode.graph.nodes.append( strLit )
	return treeNode.insertNode( strLit, treeNodePath )



def cvto_insertUnboundRef(treeNode, treeNodePath):
	ref = CGUnboundRef()
	treeNode.graph.nodes.append( ref )
	return treeNode.insertNode( ref, treeNodePath )



def cvto_insertLambda(treeNode, treeNodePath):
	lambdaNode = CGLambda()
	blockNode = CGBlock()
	treeNode.graph.nodes.append( lambdaNode )
	treeNode.graph.nodes.append( blockNode )
	lambdaNode.block.append( blockNode.parent )
	return treeNode.insertNode( lambdaNode, treeNodePath )
