##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer




class DTContainerSequence (DTContainer):
	def isOrderReversed(self):
		return False
	
	
	def getFirstCursorEntity(self):
		entries = self._childEntries
		if self.isOrderReversed():
			entries = reversed( entries )

		for entry in entries:
			first = entry.child.getFirstCursorEntity()
			if first is not None:
				return first
		return None


	def getLastCursorEntity(self):
		entries = self._childEntries
		if not self.isOrderReversed():
			entries = reversed( entries )

		for entry in entries:
			last = entry.child.getLastCursorEntity()
			if last is not None:
				return last
		return None



	def _o_getPrevCursorEntityBeforeChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		
		if self.isOrderReversed():
			try:
				entries = self._childEntries[index+1:]
			except IndexError:
				return None
		else:
			entries = reversed( self._childEntries[:index] )
		
		for entry in entries:
			last = entry.child.getLastCursorEntity()
			if last is not None:
				return last
		return None

		
	def _o_getNextCursorEntityAfterChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		
		if self.isOrderReversed():
			entries = reversed( self._childEntries[:index] )
		else:
			try:
				entries = self._childEntries[index+1:]
			except IndexError:
				return None
		
		for entry in entries:
			last = entry.child.getLastCursorEntity()
			if last is not None:
				return last
		return None



