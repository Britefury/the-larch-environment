##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
class NoMessageError (Exception):
	pass

class VObject (object):
	def __init__(self):
		super( VObject, self ).__init__()
		self._class = None
		self._dict = {}
		self._instanceMessages = {}


	def sendMessage(self, machine, messageName, args):
		message = self.getMessage( messageName )
		if message is None:
			raise NoMessageError, 'object of class \'%s\' has no message \'%s\'' % ( self._class._name, messageName )
		else:
			message.invoke( machine, self, args )


	def getMessage(self, messageName):
		try:
			message = self._instanceMessages[messageName]
		except KeyError:
			if self._class is not None:
				return self._class.getMessageForInstance( messageName )
			else:
				return None
		else:
			return message

	def getMessageForInstance(self, messageName):
		return None


	def setInstanceMessage(self, messageName, message):
		self._instanceMessages[messageName] = message


	def isInstanceOf(self, vclass):
		return self._class.isSubclassOf( vclass )
