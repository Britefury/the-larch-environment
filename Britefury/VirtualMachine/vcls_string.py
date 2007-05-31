##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMBuiltinMessage import VMBuiltinMessage
from Britefury.VirtualMachine.VClass import VClass
from Britefury.VirtualMachine import vcls_object


# String class
vcls_string = VClass( 'String' )


# Init object class
vcls_string._class = vcls_object.vcls_class
vcls_string._bases.append( vcls_object.vcls_object )




# String messages

def vstring_init(instance):
	instance._value = ''

def vstringmsg_init(instance, machine, args):
	instance._value = ''

def vstringmsg_print(instance, machine, args):
	print instance._value

def vstringmsg_add(instance, machine, args):
	return pyStrToVString( instance._value + args[0]._value )


vcls_string.setMessage( 'init', VMBuiltinMessage( vstringmsg_init ) )
vcls_string.setMessage( 'print', VMBuiltinMessage( vstringmsg_print ) )
vcls_string.setMessage( 'add', VMBuiltinMessage( vstringmsg_add ) )




def pyStrToVString(pyStr):
	s = vcls_object.vobject_alloc( vcls_string )
	vstring_init( s )
	s._value = pyStr
	return s



