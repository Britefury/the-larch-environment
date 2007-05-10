##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

class LLCTTag (object):
	def __init__(self, systemName, userName):
		self._systemName = systemName
		self._userName = userName


	def getSystemName(self):
		return self._systemName

	def getUserName(self):
		return self._userName

	systemName = property( getSystemName )
	userName = property( getUserName )

