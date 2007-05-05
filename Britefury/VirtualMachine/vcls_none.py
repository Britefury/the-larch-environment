##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMBuiltinMessage import VMBuiltinMessage
from Britefury.VirtualMachine.VClass import VClass
from Britefury.VirtualMachine.vcls_object import vcls_object, vcls_class, vobjectmsg_alloc

# None class
vcls_none = VClass( 'None' )


# Init none class
vcls_none._class = vcls_class
vcls_none._bases.append( vcls_object )



# None messages
def vnonemsg_toString(instance, machine, args):
	return pyStrToVString( 'none' )

vcls_none.setMessage( 'toString', VMBuiltinMessage( vnonemsg_toString ) )



# None instance
vnone = vobjectmsg_alloc( vcls_none, None, [] )




