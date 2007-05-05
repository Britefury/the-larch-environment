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


class LLCTSendMessageExp (LLCTExpression):
	def __init__(self, targetExpression, messageName, argumentExpressions, expandArgExpression=None):
		super( LLCTSendMessageExp, self ).__init__()
		self._targetExpression = targetExpression
		self._messageName = messageName
		self._argumentExpressions = argumentExpressions
		self._expandArgExpression = expandArgExpression

	def generateInstructions(self, instructions, constants, block, registerAllocator, bResultRequired):
		targetReg = self._targetExpression.generateInstructions( instructions, constants, block, registerAllocator, True )
		argRegs = [ argExp.generateInstructions( instructions, constants, block, registerAllocator, True )  for argExp in self._argumentExpressions ]
		expandArgReg = None
		if self._expandArgExpression is not None:
			expandArgReg = self._expandArgExpression.generateInstructions( instructions, constants, block, registerAllocator, True )
		resultRegIndex = None
		instructions.append( SendMessageInstruction( targetReg, constants.addConstant( pyStrToVString( self._messageName ) ), argRegs, expandArgReg ) )
		registerAllocator.freeReg( targetReg )
		for reg in argRegs:
			registerAllocator.freeReg( reg )
		if expandArgReg is not None:
			registerAllocator.freeReg( expandArgReg )
		if bResultRequired:
			resultReg = registerAllocator.allocReg()
			instructions.append( MoveInstruction( resultReg, resultRegister() ) )
			return resultReg
		else:
			return None
