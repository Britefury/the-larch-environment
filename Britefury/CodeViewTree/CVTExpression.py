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
from Britefury.CodeGraph.CGNot import CGNot
from Britefury.CodeGraph.CGNullExpression import CGNullExpression
from Britefury.CodeGraph.CGTuple import CGTuple
from Britefury.CodeGraph.CGList import CGList

from Britefury.CodeViewTree.CVTStatement import CVTStatement
from Britefury.CodeViewTree.CodeViewTree import *



class CVTExpression (CVTStatement):
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


	def wrapInNot(self):
		parentCGSink = self.graphNode.parent[0]
		notCG = CGNot()
		self.graph.nodes.append( notCG )
		parentCGSink.splitLinkWithNode( self.graphNode.parent, notCG.expr, notCG.parent )
		return self._tree.buildNode( notCG )


	def wrapInTuple(self):
		parentCGSink = self.graphNode.parent[0]
		tupleCG = CGTuple()
		self.graph.nodes.append( tupleCG )
		parentCGSink.splitLinkWithNode( self.graphNode.parent, tupleCG.args, tupleCG.parent )
		return self._tree.buildNode( tupleCG )


	def wrapInList(self):
		parentCGSink = self.graphNode.parent[0]
		listCG = CGList()
		self.graph.nodes.append( listCG )
		parentCGSink.splitLinkWithNode( self.graphNode.parent, listCG.args, listCG.parent )
		return self._tree.buildNode( listCG )


	def replaceWithNullExpression(self):
		nullExpression = CGNullExpression()
		self.graph.nodes.append( nullExpression )
		parentCGSink = self.graphNode.parent[0]
		parentCGSink.replace( self.graphNode.parent, nullExpression.parent )
		self.graphNode.destroySubtree()
		return self._tree.buildNode( nullExpression )



class CVTRuleExpression (CVTRuleSimple):
	graphNodeClass = CGExpression
	cvtNodeClass = CVTExpression

CVTRuleExpression.register()

