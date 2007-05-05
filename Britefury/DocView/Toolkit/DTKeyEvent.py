##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
class DTKeyEvent (object):
	def __init__(self, gdkEvent):
		self.state = gdkEvent.state
		self.keyVal = gdkEvent.keyval
		self.keyString = gdkEvent.string
		self.hardwareKeycode = gdkEvent.hardware_keycode
		self.group = gdkEvent.group



