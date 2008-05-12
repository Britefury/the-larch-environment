##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity


class DTCursorLocation (object):
	EDGE_LEADING = 0
	EDGE_TRAILING = 1
	
	def __init__(self, cursorEntity, edge):
		if edge == self.EDGE_LEADING:
			if ( cursorEntity.edgeFlags & DTCursorEntity.EDGEFLAGS_LEADING )  ==  0:
				prev = cursorEntity.prev
				if prev is not None  and  ( prev.edgeFlags & DTCursorEntity.EDGEFLAGS_TRAILING ) != 0:
					cursorEntity = prev
					edge = self.EDGE_TRAILING
		elif edge == self.EDGE_TRAILING:
			if ( cursorEntity.edgeFlags & DTCursorEntity.EDGEFLAGS_TRAILING )  ==  0:
				next = cursorEntity.next
				if next is not None  and  ( next.edgeFlags & DTCursorEntity.EDGEFLAGS_LEADING ) != 0:
					cursorEntity = next
					edge = self.EDGE_LEADING
		else:
			raise ValueError, 'invalid edge'
		
		self.cursorEntity = cursorEntity
		self.edge = edge



class DTCursor (object):
	def __init__(self, document, location):
		self._document = document
		self._location = location

		if self._location is not None:
			self._location.cursorEntity.widget._f_registerCursor( self )
		
		
	def getLocation(self):
		return self._location
	
	def setLocation(self, location):
		if self._location is not None:
			self._location.cursorEntity.widget._f_unregisterCursor( self )
		self._location = location
		if self._location is not None:
			self._location.cursorEntity.widget._f_registerCursor( self )
		if self._document is not None:
			self._document._f_cursorLocationNotify( self, True )
	
			
	def _f_widgetNotifyOfLocationChange(self, location):
		self._location = location
		if self._document is not None:
			self._document._f_cursorLocationNotify( self, True )
			
	def _f_widgetUnrealiseNotify(self):
		self._document._f_cursorUnrealiseNotify( self )
		
		
	location = property( getLocation, setLocation )
		
		
		
		
		
	
