##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMBuiltinMessage import VMBuiltinMessage
from Britefury.VirtualMachine.VClass import VClass
from Britefury.VirtualMachine.vcls_object import vcls_object, vcls_class, vobject_alloc

# Block class
vcls_block = VClass( 'Block' )


# Init object class
vcls_block._class = vcls_class
vcls_block._bases.append( vcls_object )




# Block messages

def vblock_init(instance):
	instance._block = None

def vblockmsg_init(instance, machine, args):
	instance._block = None

vcls_block.setMessage( 'init', VMBuiltinMessage( vblockmsg_init ) )




def blockToVBlock(block):
	b = vobject_alloc( vcls_block )
	vblock_init( b )
	b._block = block
	return b
