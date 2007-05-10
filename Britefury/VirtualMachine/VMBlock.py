##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.Registers import *


class LocalRegisterNameError (Exception):
	pass


class VMBlock (object):
	def __init__(self, name, outerBlock, argNames=[], expandArgName=None):
		super( VMBlock, self ).__init__()
		self.name = name
		self.instructions = None
		self.numLocalRegs = 0
		self.numTempRegs = None
		self.outerBlock = outerBlock
		self.argNames = argNames
		self.expandArgName = expandArgName
		self.localVarTagToRegIndex = {}
		self.constants = None
		self.argRegisters = [ self.allocLocalReg( arg )   for arg in argNames ]
		if expandArgName is not None:
			self.expandArgRegister = self.allocLocalReg( expandArgName )
		else:
			self.expandArgRegister = None


	def __str__(self):
		return 'block:\n'  +  ''.join( [ '\t%s\n' % ( str( instruction ), )  for instruction in self.instructions ] )


	def initialise(self, instructions, numTempRegs=0, constants=[]):
		self.instructions = instructions
		self.numTempRegs = numTempRegs
		self.constants = constants


	def allocLocalReg(self, varTag):
		try:
			return localRegister( 0, self.localVarTagToRegIndex[varTag] )
		except KeyError:
			regIndex = self.numLocalRegs
			self.localVarTagToRegIndex[varTag] = regIndex
			self.numLocalRegs += 1
			return localRegister( 0, regIndex )


	def getLocalReg(self, varTag, levelOffset=0):
		block = self
		level = levelOffset
		while True:
			try:
				return ( level, block.localVarTagToRegIndex[varTag] )
			except KeyError:
				if block.outerBlock is not None:
					block = block.outerBlock
					level += 1
				else:
					if isinstance( varTag, str ):
						raise LocalRegisterNameError, 'tag: %s'  %  ( varTag )
					else:
						raise LocalRegisterNameError, 'tag: %s (%s)'  %  ( varTag, varTag.userName )


