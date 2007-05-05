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
from Britefury.VirtualMachine.vcls_string import pyStrToVString

# List class
vcls_list = VClass( 'List' )


# Init object class
vcls_list._class = vcls_object.vcls_class
vcls_list._bases.append( vcls_object.vcls_object )




# List messages

def vlist_init(instance):
	instance._value = []

def vlistmsg_init(instance, machine, args):
	instance._value = []

def vlistmsg_append(instance, machine, args):
	instance._value.append( args[0] )

def vlistmsg_toString(instance, machine, args):
	return pyStrToVStr( 'list of length %d' % ( len( instance._value ), ) )

vcls_list.setMessage( 'init', VMBuiltinMessage( vlistmsg_init ) )
vcls_list.setMessage( 'append', VMBuiltinMessage( vlistmsg_append ) )
vcls_list.setMessage( 'toString', VMBuiltinMessage( vlistmsg_toString ) )



def pyListToVList(pyList):
	l = vcls_object.vobject_alloc( vcls_list )
	vlist_init( l )
	l._value = pyList
	return l

