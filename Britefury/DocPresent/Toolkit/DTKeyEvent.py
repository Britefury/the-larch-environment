##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

class DTKeyEvent (object):
	def __init__(self, gdkEvent):
		keyval = gdkEvent.keyval
		if keyval <= 255:
			keyval = ord( chr( keyval ).lower() )
		self.state = gdkEvent.state  &  ( gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK | gtk.gdk.MOD1_MASK )
		self.keyVal = keyval
		self.keyString = gdkEvent.string
		self.hardwareKeycode = gdkEvent.hardware_keycode
		self.group = gdkEvent.group
