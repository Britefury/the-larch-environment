##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Registers import *


class LLCTRegisterAllocator (object):
	def __init__(self):
		super( LLCTRegisterAllocator, self ).__init__()
		self._freeRegisters = []
		self._registerCount = 0

	def allocReg(self):
		if len( self._freeRegisters ) == 0:
			self._p_newRegister()
		reg = self._freeRegisters[0]
		del self._freeRegisters[0]
		return tempRegister( reg )

	def freeReg(self, register):
		if register[0] == REGLEVEL_TEMP:
			self._freeRegisters.append( register[1] )


	def getNumRequiredRegisters(self):
		return self._registerCount


	def _p_newRegister(self):
		self._freeRegisters.append( self._registerCount )
		self._registerCount += 1
