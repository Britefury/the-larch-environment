##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Instructions import *
from Britefury.VirtualMachine.vcls_string import *
from Britefury.VirtualMachine.vcls_block import *
from Britefury.VirtualMachine.Registers import *
from Britefury.VirtualMachine.VMMachine import VMMachine
from Britefury.LowLevelCodeTree.LLCTExpression import *


class LLCTClosureExp (LLCTExpression):
	def __init__(self, block):
		super( LLCTClosureExp, self ).__init__()
		self._block = block


	def generateInstructions(self, instructions, constants, block, registerAllocator, bResultRequired):
		if bResultRequired:
			newConstant = constants.addConstant( pyStrToVString( 'new' ) )
			blockConstant = constants.addConstant( blockToVBlock( self._block.generateBlockInstructions( block ) ) )

			closureClassReg = block.getLocalReg( VMMachine.tag_Closure )

			currentFrameReg = registerAllocator.allocReg()
			closureReg = registerAllocator.allocReg()

			instructions.append( SendMessageInstruction( closureClassReg, newConstant, [ constRegister( blockConstant ) ] ) )
			instructions.append( MoveInstruction( closureReg, resultRegister() ) )

			registerAllocator.freeReg( closureClassReg )
			registerAllocator.freeReg( currentFrameReg )

			return closureReg
