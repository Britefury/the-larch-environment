##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGExpression import CGExpression
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *
from Britefury.LowLevelCodeTree.LLCTLoadLocalExp import LLCTLoadLocalExp



class CGUnboundRef (CGExpression):
	targetName = Field( str, '' )


	def generatePyCode(self):
		return self.targetName
