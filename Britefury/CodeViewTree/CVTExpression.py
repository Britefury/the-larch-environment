##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGExpression import CGExpression
from Britefury.CodeGraph.CGCall import CGCall
from Britefury.CodeGraph.CGArguments import CGArguments
from Britefury.CodeGraph.CGGetAttr import CGGetAttr
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTStatement import CVTStatement



class CVTExpression (CVTStatement):
	graphNodeClass = CGExpression


	graphNode = SheetRefField( CGExpression )


	def wrapInCall(self):
		parentCGSink = self.graphNode.parent[0]
		callCG = CGCall()
		argsCG = CGArguments()
		self.graph.nodes.append( callCG )
		self.graph.nodes.append( argsCG )
		callCG.arguments.append( argsCG.parent )
		parentCGSink.splitLinkWithNode( self.graphNode.parent, callCG.targetObject, callCG.parent )
		return self._tree.buildNode( callCG )


	def wrapInGetAttr(self):
		parentCGSink = self.graphNode.parent[0]
		getAttrCG = CGGetAttr()
		self.graph.nodes.append( getAttrCG )
		parentCGSink.splitLinkWithNode( self.graphNode.parent, getAttrCG.targetObject, getAttrCG.parent )
		return self._tree.buildNode( getAttrCG )


	def replaceWithNullExpression(self):
		nullExpression = CGNullExpression()
		self.graph.nodes.append( nullExpression )
		parentCGSink = self.graphNode.parent[0]
		parentCGSink.replace( self.graphNode.parent, nullExpression.parent )
		self.graphNode.destroySubtree()
		return self._tree.buildNode( nullExpression )
