##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMTag import VMTag

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNode import CGNode
from Britefury.CodeGraph.CGLocalRef import CGLocalRef


class CGVar (CGNode):
	name = Field( str, '', 'Variable name' )
	references = SheetGraphSourceField( 'References', 'All references to this local variable' )
	declaration = SheetGraphSourceField( 'Declaraion', 'The variable declaration' )

	def generateLLCT(self, tree):
		try:
			return tree[self]
		except KeyError:
			tag = VMTag( 'Variable', self.name )
			tree[self] = tag
			return tag



	def createRefNode(self):
		refNode = CGLocalRef()
		refNode.variable.append( self.references )
		return refNode