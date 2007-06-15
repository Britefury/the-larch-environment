##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGLocalAssignment import *
from Britefury.CodeGraph.CGNullExpression import *




def cvto_wrapInAssignment(refTreeNode):
	refCG = refTreeNode.graphNode
	graph = refCG.graph
	parentCGSink = refCG.parent[0]
	localAssignment = CGLocalAssignment()
	nullExpression = CGNullExpression()
	graph.nodes.append( localAssignment )
	graph.nodes.append( nullExpression )
	localAssignment.value.append( nullExpression.parent )
	parentCGSink.splitLinkWithNode( refCG.parent, localAssignment.varRef, localAssignment.parent )
	return refTreeNode._tree.buildNode( localAssignment )
