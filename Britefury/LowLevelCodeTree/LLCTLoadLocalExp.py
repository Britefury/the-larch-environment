##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Instructions import *
from Britefury.VirtualMachine.vcls_string import *
from Britefury.LowLevelCodeTree.LLCTExpression import *


class LLCTLoadLocalExp (LLCTExpression):
	def __init__(self, varName):
		super( LLCTLoadLocalExp, self ).__init__()
		self._varName = varName

	def generateInstructions(self, instructions, constants, block, registerAllocator, bResultRequired):
		if bResultRequired:
			return block.getLocalReg( self._varName )
		else:
			return None
