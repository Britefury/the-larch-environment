##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Registers import *


class RegisterLevelError (Exception):
	pass



class VMFrame (object):
	def __init__(self, block, outerScope=None):
		super( VMFrame, self ).__init__()
		self.block = block
		self.parent = None
		self.ip = 0
		self.localRegisters = [ None ]  *  self.block.numLocalRegs
		self.tempRegisters = [ None ]  *  self.block.numTempRegs
		self.stack = []
		self.resultRegister = None
		self.selfRegister = None
		self.outerScope = outerScope


	def loadReg(self, reg):
		level, regIndex = reg
		if level >= 0:
			frame = self
			while level > 0:
				frame = frame.outerScope
				level -= 1
			return frame.localRegisters[regIndex]
		elif level == REGLEVEL_TEMP:
			return self.tempRegisters[regIndex]
		elif level == REGLEVEL_CONSTANT:
			return self.block.constants[regIndex]
		elif level == REGLEVEL_SELF:
			return self.selfRegister
		elif level == REGLEVEL_RESULT:
			return self.resultRegister
		else:
			raise RegisterLevelError

	def storeReg(self, reg, value):
		level, regIndex = reg
		if level >= 0:
			frame = self
			while level > 0:
				frame = frame.outerScope
				level -= 1
			frame.localRegisters[regIndex] = value
		elif level == REGLEVEL_TEMP:
			self.tempRegisters[regIndex] = value
		else:
			raise RegisterLevelError

	def push(self, value):
		self.stack.append( value )

	def pop(self):
		return self.stack.pop()


	def depth(self):
		frame = self
		d = 0
		while frame.parent is not None:
			frame = frame.parent
			d += 1
		return d
