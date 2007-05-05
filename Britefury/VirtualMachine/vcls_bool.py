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
from Britefury.VirtualMachine.vcls_string import pyStrToVString

# Boolean classes
vcls_bool = VClass( 'Bool' )


# Init bool class
vcls_bool._class = vcls_class
vcls_bool._bases.append( vcls_object )




# Bool messages, True and False

# Boolean instances (true and false)
vfalse = vobjectmsg_alloc( vcls_bool, None, [] )
vtrue = vobjectmsg_alloc( vcls_bool, None, [] )

def vfalsemsg_toString(instance, machine, args):
	return pyStrToVString( 'false' )

def vtruemsg_toString(instance, machine, args):
	return pyStrToVString( 'true' )

vfalse.setInstanceMessage( 'toString', VMBuiltinMessage( vfalsemsg_toString ) )
vtrue.setInstanceMessage( 'toString', VMBuiltinMessage( vtruemsg_toString ) )




def pyBoolToVBool(pyBool):
	if pyBool:
		return vtrue
	else:
		return vfalse

