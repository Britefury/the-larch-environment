##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************




class DVStyleSheetClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		super( DVStyleSheetClass, cls ).__init__( clsName, clsBases, clsDict )
		cls.singleton = cls()


class DVStyleSheet (object):
	__metaclass__ = DVStyleSheetClass
