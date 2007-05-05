##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMAbstractMessage import VMAbstractMessage
from Britefury.VirtualMachine.VMFrame import VMFrame
from Britefury.VirtualMachine.vcls_list import pyListToVList


class VMMessage (VMAbstractMessage):
	def __init__(self, closure):
		super( VMMessage, self ).__init__()
		self._closure = closure

	def invoke(self, machine, instance, args):
		frame = VMFrame( self._closure._block, self._closure._outerScope )

		for arg, argReg in zip( args, self._closure._block.argRegisters ):
			frame.storeReg( argReg, arg )
		if self._closure._block.expandArgRegister is not None:
			consumed = min( len( self._closure._block.argRegisters ), len( args ) )
			frame.storeReg( self._closure._block.expandArgRegister, pyListToVList( args[consumed:] ) )

		frame.selfRegister = instance
		machine.pushFrame( frame )

