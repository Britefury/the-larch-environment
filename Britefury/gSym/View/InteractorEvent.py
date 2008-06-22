##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



class InteractorEvent (object):
	def __init__(self, bUserEvent):
		super( InteractorEvent, self ).__init__()
		self.bUserEvent = bUserEvent



class InteractorEventKey (InteractorEvent):
	def __init__(self, bUserEvent, keyString, keyValue, mods):
		super( InteractorEventKey, self ).__init__( bUserEvent )
		self.keyString = keyString
		self.keyValue = keyValue
		self.mods = mods
		

	@staticmethod
	def fromDTKeyEvent(widget, bUserEvent, event):
		return InteractorEventKey( bUserEvent, event.keyString, event.keyVal, event.state )

	
	def __repr__(self):
		return 'KEY: %s, %d:%d'  %  ( self.keyString, self.keyValue, self.mods )
	
		
	
class InteractorEventText (InteractorEvent):
	def __init__(self, bUserEvent, bChanged, text):
		super( InteractorEventText, self ).__init__( bUserEvent )
		self.bChanged = bChanged
		self.text = text
		

	def __repr__(self):
		return "Text %s"  %  ( self.text, )



class InteractorEventTokenList (InteractorEvent):
	class Token (object):
		def __init__(self, tokenClass, value):
			super( InteractorEventTokenList.Token, self ).__init__()
			self.tokenClass = tokenClass
			self.value = value
		
		def __repr__(self):
			return "Token( %s, '%s' )"  %  ( self.tokenClass, self.value )

	def __init__(self, bUserEvent, bChanged, tokens):
		super( InteractorEventTokenList, self ).__init__( bUserEvent )
		self.bChanged = bChanged
		self.tokens = tokens
		

	def tailEvent(self, fromIndex):
		tok = self.tokens[fromIndex:]
		if len( tok ) == 0:
			return None
		else:
			return InteractorEventTokenList( self.bUserEvent, self.bChanged, tok )
		
		
	def __repr__(self):
		return "Tokens %s"  %  ( self.tokens, )
	


