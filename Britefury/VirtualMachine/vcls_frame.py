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

# Frame class
vcls_frame = VClass( 'Frame' )


# Init object class
vcls_frame._class = vcls_class
vcls_frame._bases.append( vcls_object )




# Frame messages

def vframe_init(instance):
	instance._frame = None

def vframemsg_init(instance, machine, args):
	instance._frame = None

vcls_frame.setMessage( 'init', VMBuiltinMessage( vframemsg_init ) )



def frameToVFrame(frame):
	f = vobject_alloc( vcls_frame )
	vframe_init( f )
	f._frame = frame
	return f

