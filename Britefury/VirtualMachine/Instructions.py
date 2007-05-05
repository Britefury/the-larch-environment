##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Registers import *






class Instruction (object):
	def execute(self, machine):
		pass



class MoveInstruction (Instruction):
	def __init__(self, destRegister, srcRegister):
		super( MoveInstruction, self ).__init__()
		self._destRegister = destRegister
		self._srcRegister = srcRegister

	def execute(self, machine):
		machine.frame.storeReg( self._destRegister, machine.frame.loadReg( self._srcRegister ) )

	def disassemble(self, machine):
		return 'MOVE %s, %s'  % ( regName( self._destRegister ), regName( self._srcRegister ) )


class SendMessageInstruction (Instruction):
	def __init__(self, targetRegister, messageNameConstantIndex, argumentRegisters, expandArgumentRegister=None):
		super( SendMessageInstruction, self ).__init__()
		self._targetRegister = targetRegister
		self._messageNameConstantIndex = messageNameConstantIndex
		self._argumentRegisters = argumentRegisters
		self._expandArgumentRegister = expandArgumentRegister

	def execute(self, machine):
		target = machine.frame.loadReg( self._targetRegister )
		args = [ machine.frame.loadReg( reg )   for reg in self._argumentRegisters ]
		if self._expandArgumentRegister is not None:
			args += machine.frame.loadReg( self._expandArgumentRegister )._value
		target.sendMessage( machine, machine.frame.block.constants[self._messageNameConstantIndex]._value, args )

	def disassemble(self, machine):
		if self._expandArgumentRegister is None:
			return 'SENDMSG %s, c%d[%s], %s'  % ( regName( self._targetRegister ), self._messageNameConstantIndex, machine.frame.block.constants[self._messageNameConstantIndex]._value, regListNames( self._argumentRegisters ) )
		else:
			return 'SENDMSG %s, c%d[%s], %s, %s'  % ( regName( self._targetRegister ), self._messageNameConstantIndex, machine.frame.block.constants[self._messageNameConstantIndex]._value, regListNames( self._argumentRegisters ), regName( self._expandArgumentRegister ) )


class ReturnInstruction (Instruction):
	def __init__(self, srcRegister, numFrames=1):
		super( ReturnInstruction, self ).__init__()
		self._srcRegister = srcRegister
		self._numFrames = numFrames

	def execute(self, machine):
		value = machine.frame.loadReg( self._srcRegister )
		for i in xrange( 0, self._numFrames ):
			machine.popFrame()
		if machine.frame is not None:
			machine.frame.resultRegister = value

	def disassemble(self, machine):
		if self._numFrames == 1:
			return 'RETURN %s'  %  ( regName( self._srcRegister ), )
		else:
			return 'RETURN[%d] %s'  %  ( self._numFrames, regName( self._srcRegister ) )
