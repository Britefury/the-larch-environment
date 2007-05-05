##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMFrame import VMFrame
from Britefury.VirtualMachine.VMBuiltinMessage import VMBuiltinMessage
from Britefury.VirtualMachine.VClass import VClass
from Britefury.VirtualMachine.vcls_object import vcls_object, vcls_class
from Britefury.VirtualMachine.vcls_string import pyStrToVString
from Britefury.VirtualMachine.vcls_list import pyListToVList

# Closure class
vcls_closure = VClass( 'Closure' )


# Init object class
vcls_closure._class = vcls_class
vcls_closure._bases.append( vcls_object )




# Closure messages

# Arguments: [ block ]
def vclosuremsg_init(instance, machine, args):
	if len( args ) == 0:
		instance._block = None
		instance._outerScope = None
	else:
		instance._block = args[0]._block
		instance._outerScope = machine.frame

def vclosuremsg_call(instance, machine, args):
	frame = VMFrame( instance._block, instance._outerScope )
	for arg, argReg in zip( args, instance._block.argRegisters ):
		frame.storeReg( argReg, arg )
	if instance._block.expandArgRegister is not None:
		consumed = min( len( instance._block.argRegisters ), len( args ) )
		frame.storeReg( instance._block.expandArgReg, pyListToVList( args[consumed:] ) )
	machine.pushFrame( frame )

def vclosuremsg_toString(instance, machine, args):
	return pyStrToVString( 'closure id<%d>' % ( id( instance ), ) )


vcls_closure.setMessage( 'init', VMBuiltinMessage( vclosuremsg_init ) )
vcls_closure.setMessage( 'call', VMBuiltinMessage( vclosuremsg_call ) )
vcls_closure.setMessage( 'toString', VMBuiltinMessage( vclosuremsg_toString ) )
