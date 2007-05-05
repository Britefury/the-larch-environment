##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Instructions import *
from Britefury.VirtualMachine.vcls_string import *
from Britefury.VirtualMachine.Registers import *
from Britefury.LowLevelCodeTree.LLCTExpression import *
from Britefury.LowLevelCodeTree.LLCTBuiltins import *


class LLCTAssignmentExp (LLCTExpression):
	def __init__(self, varName, srcExpression):
		super( LLCTAssignmentExp, self ).__init__()
		self._varName = varName
		self._srcExpression = srcExpression

	def generateInstructions(self, instructions, constants, block, registerAllocator, bResultRequired):
		srcReg = self._srcExpression.generateInstructions( instructions, constants, block, registerAllocator, True )
		instructions.append( MoveInstruction( block.getLocalReg( self._varName ), srcReg ) )
		registerAllocator.freeReg( srcReg )

		if bResultRequired:
			return llctBuiltin_none( constants )
		else:
			return None
