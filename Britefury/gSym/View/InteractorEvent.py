##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



class InteractorEvent (object):
	pass



class InteractorEventKey (InteractorEvent):
	def __init__(self, keyValue, mods):
		super( InteractorEventKey, self ).__init__()
		self.keyValue = keyValue
		self.mods = mods
	
		
class InteractorEventTokenList (InteractorEvent):
	class Token (object):
		def __init__(self, tokenClass, value):
			super( InteractorEventTokens.Token, self ).__init__()
			self.tokenClass = tokenClass
			self.value = value
		
	def __init__(self, tokens=[]):
		super( InteractorEventKey, self ).__init__()
		self.tokens = []
		

	def tailEvent(self, fromIndex):
		tok = self.tokens[fromIndex:]
		if len( tok ) == 0:
			return None
		else:
			return InteractorEventTokenList( tok )
	


