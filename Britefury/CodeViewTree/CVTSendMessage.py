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

from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTMessageArguments import CVTMessageArguments
from Britefury.CodeViewTree.CVTMessageName import CVTMessageName



class CVTSendMessage (CVTExpression):
	graphNodeClass = CGSendMessage


	graphNode = SheetRefField( CGSendMessage )



	@FunctionRefField
	def targetObjectNode(self):
		if len( self.graphNode.targetObject ) > 0:
			return self._tree.buildNode( self.graphNode.targetObject[0].node )
		else:
			return None


	@FunctionRefField
	def messageNameNode(self):
		return self._tree.buildNode( self.graphNode, CVTMessageName )


	@FunctionRefField
	def argumentsNode(self):
		return self._tree.buildNode( self.graphNode, CVTMessageArguments )




	def unwrapSendMessage(self):
		parentCGSink = self.graphNode.parent[0]
		targetObjectSource = self.graphNode.targetObject[0]

		self.graphNode.targetObject.remove( targetObjectSource )

		parentCGSink.replace( self.graphNode.parent, targetObjectSource )

		self.graphNode.destroy()
