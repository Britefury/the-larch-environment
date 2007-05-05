##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMBuiltinMessage import VMBuiltinMessage
import Britefury.VirtualMachine.VMMessage
from Britefury.VirtualMachine.VObject import VObject
from Britefury.VirtualMachine.VClass import VClass

# Object class
vcls_object = VClass( 'Object' )

# Class class
vcls_class = VClass( 'Class' )


# Init object class
vcls_object._class = vcls_class


# Init class class
vcls_class._class = vcls_class
vcls_class._bases.append( vcls_object )



# Object messages
def vobject_alloc(instance):
	obj = VObject()
	obj._class = instance
	return obj

def vobjectmsg_alloc(instance, machine, args):
	obj = VObject()
	obj._class = instance
	return obj

def vobjectmsg_init(instance, machine, args):
	pass

def vobjectmsg_getClass(instance, machine, args):
	return instance._class

def vobjectmsg_isInstanceOf(instance, machine, args):
	return pyBoolToVBool( instance.isInstanceOf( args[0] ) )

def vobjectmsg_setInstanceMessage(instance, machine, args):
	instance.setInstanceMessage( args[0]._value, Britefury.VirtualMachine.VMMessage.VMMessage( args[1] ) )

def vobjectmsg_toString(instance, machine, args):
	return pyStrToVStr( 'object class <%d> id<%d>' % ( instance._class._name, id( instance ), ) )


vcls_object.setMessage( 'alloc', VMBuiltinMessage( vobjectmsg_alloc ) )
vcls_object.setMessage( 'init', VMBuiltinMessage( vobjectmsg_init ) )
vcls_object.setMessage( 'getClass', VMBuiltinMessage( vobjectmsg_getClass ) )
vcls_object.setMessage( 'isInstanceOf', VMBuiltinMessage( vobjectmsg_isInstanceOf ) )
vcls_object.setMessage( 'setInstanceMessage', VMBuiltinMessage( vobjectmsg_setInstanceMessage ) )
vcls_object.setMessage( 'toString', VMBuiltinMessage( vobjectmsg_toString ) )






# Class messages

def vclassmsg_alloc(instance, machine, args):
	obj = VClass()
	obj._class = instance
	return obj

def vclassmsg_init(instance, machine, args):
	instance._bases = args[0]

def vclassmsg_isSubclassOf(instance, machine, args):
	return pyBoolToVBool( instance.isSubclassOf( args[0] ) )

def vclassmsg_setMessage(instance, machine, args):
	instance.setMessage( args[0]._value, Britefury.VirtualMachine.VMMessage.VMMessage( args[1] ) )

def vclassmsg_toString(instance, machine, args):
	return pyStrToVStr( 'class <%s> id<%d>' % ( instance._name, id( instance ) ) )


vcls_class.setMessage( 'alloc', VMBuiltinMessage( vclassmsg_alloc ) )
vcls_class.setMessage( 'init', VMBuiltinMessage( vclassmsg_init ) )
vcls_class.setMessage( 'isSubclassOf', VMBuiltinMessage( vclassmsg_isSubclassOf ) )
vcls_class.setMessage( 'setMessage', VMBuiltinMessage( vclassmsg_setMessage ) )
vcls_class.setMessage( 'toString', VMBuiltinMessage( vclassmsg_toString ) )





