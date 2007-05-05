##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMBlock import *
from Britefury.VirtualMachine.VMMachine import VMMachine
from Britefury.VirtualMachine.Instructions import *
from Britefury.LowLevelCodeTree.LLCTConstantTable import *
from Britefury.LowLevelCodeTree.LLCTRegisterAllocator import *
from Britefury.LowLevelCodeTree.LLCTReturnExp import LLCTReturnExp


class LLCTBlock (object):
	def __init__(self, name, statements, argNames=[], expandArgName=None):
		super( LLCTBlock, self ).__init__()
		self._name = name
		self._statements = statements
		self._argNames = argNames
		self._expandArgName = expandArgName


	def generateBlockInstructions(self, outerBlock=None):
		instructions = []
		constants = LLCTConstantTable()
		regAlloc = LLCTRegisterAllocator()

		if outerBlock is None:
			outerBlock = VMMachine.baseBlock

		block = VMBlock( self._name, outerBlock, self._argNames, self._expandArgName )

		for statement in self._statements:
			statement.allocateLocalRegisters( block )

		for statement in self._statements[:-1]:
			statement.generateInstructions( instructions, constants, block, regAlloc, False )

		if len( self._statements ) == 0:
			resultReg = llctBuiltin_none( constants )
		else:
			resultReg = self._statements[-1].generateInstructions( instructions, constants, block, regAlloc, True )

		instructions.append( ReturnInstruction( resultReg ) )


		block.initialise( instructions, regAlloc.getNumRequiredRegisters(), constants.getConstants() )

		return block
