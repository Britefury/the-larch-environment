##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
class DTCursorLocation (object):
	EDGE_LEADING = 0
	EDGE_TRAILING = 1
	
	def __init__(self, cursorEntity, edge):
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
		if self._bCurrent  and  self._document is not None:
			self._document._f_cursorLocationNotify( self, True )
			
	def _f_widgetUnrealiseNotify(self):
		self._document._f_cursorUnrealiseNotify( self )
		
		
	location = property( getLocation, setLocation )
		
		
		
		
		
	
