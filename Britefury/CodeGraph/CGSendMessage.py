##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGExpression import CGExpression
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *
from Britefury.LowLevelCodeTree.LLCTSendMessageExp import LLCTSendMessageExp



class CGSendMessage (CGExpression):
	targetObject = SheetGraphSinkSingleField( 'Target object', 'Expression that evaluates to the object to which the message is to be sent' )
	messageName = Field( str, '', 'The message name' )
	args = SheetGraphSinkMultipleField( 'Arguments', 'Argument list' )
	expandArg = SheetGraphSinkSingleField( 'Expand arguments', 'Argument to be expanded' )



	def generateLLCT(self, tree):
		assert len( self.targetObject ) > 0

		targetObjectLLCT = self.targetObject[0].node.generateLLCT( tree )
		argsLLCT = [ argSource.node.generateLLCT( tree )   for argSource in self.args ]
		expandLLCT = None
		if len( self.expandArg ) > 0:
			expandLLCT = self.expandArg[0].node.generateLLCT( tree )

		return LLCTSendMessageExp( targetObjectLLCT, self.messageName, argsLLCT, expandLLCT )
