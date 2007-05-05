##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VObject import VObject

class VClass (VObject):
	def __init__(self, name=''):
		super( VClass, self ).__init__()
		self._messages = {}
		self._bases = []
		self._name = name


	def getMessageForInstance(self, messageName):
		try:
			message = self._messages[messageName]
		except KeyError:
			for base in self._bases:
				message = base.getMessageForInstance( messageName )
				if message is not None:
					return message
			return None
		else:
			return message


	def setMessage(self, messageName, message):
		self._messages[messageName] = message


	def isSubclassOf(self, vclass):
		if vclass is self:
			return True
		else:
			for base in self._bases:
				if base.isSubclassOf( vclass ):
					return True
			return False
