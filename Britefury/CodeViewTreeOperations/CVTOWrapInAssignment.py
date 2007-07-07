##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGAssignment import *
from Britefury.CodeGraph.CGNullExpression import *




def cvto_wrapInAssignment(refTreeNode):
	refCG = refTreeNode.graphNode
	graph = refCG.graph

	parentCGSink = refCG.parent[0]
	parentCG = parentCGSink.node

	if isinstance( parentCG, CGAssignment ):
		assignmentCVT = refTreeNode._tree.buildNode( parentCG )
		return assignmentCVT.assign( refTreeNode )
	else:
		assignment = CGAssignment()
		nullExpression = CGNullExpression()
		graph.nodes.append( assignment )
		graph.nodes.append( nullExpression )
		assignment.value.append( nullExpression.parent )
		parentCGSink.splitLinkWithNode( refCG.parent, assignment.targets, assignment.parent )
		return refTreeNode._tree.buildNode( assignment )
