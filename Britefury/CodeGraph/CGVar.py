##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *

from Britefury.CodeGraph.CGNode import CGNode
from Britefury.CodeGraph.CGLocalRef import CGLocalRef


class CGVar (CGNode):
	name = Field( str, '', doc='Variable name' )
	references = SemanticGraphSourceField( 'References', 'All references to this local variable' )
	declaration = SemanticGraphSourceField( 'Declaraion', 'The variable declaration' )


	def generatePyCode(self):
		return self.name


	def createRefNode(self):
		refNode = CGLocalRef()
		refNode.variable.append( self.references )
		return refNode
