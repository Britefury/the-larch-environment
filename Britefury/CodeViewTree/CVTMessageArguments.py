##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGSendMessage import CGSendMessage
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTMessageArguments (CVTNode):
	graphNodeClass = CGSendMessage


	graphNode = SheetRefField( CGSendMessage )



	@FunctionField
	def argNodes(self):
		return [ self._tree.buildNode( argSource.node )   for argSource in self.graphNode.args ]


	@FunctionRefField
	def expandArgNode(self):
		if len( self.graphNode.expandArg ) > 0:
			return self._tree.buildNode( self.graphNode.expandArg[0].node )
		else:
			return None



	def addArgument(self):
		sendMsgCG = self.graphNode
		argCG = CGNullExpression()
		sendMsgCG.args.append( argCG.parent )
		return self._tree.buildNode( argCG )