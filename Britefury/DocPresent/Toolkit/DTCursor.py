##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref



class DTCursor (object):
	BEFORE = 0
	AFTER = 1
	
	def __init__(self, document, entity, location):
		self._document = document
		self.entity = entity
		self.location = location
		self._bCurrent = False
		
		
	def setPosition(self, entity, location):
		self.entity = entity
		self.location = location
		if self._bCurrent:
			self._document._f_cursorNotify( self, True )
	
			
	def setCurrent(self, bCurrent):
		if bCurrent != self._bCurrent:
			self._bCurrent = bCurrent
			self._document._f_cursorNotify( self, self._bCurrent )
			
			
			
	def makeCurrent(self):
		self._document._f_makeCursorCurrent( self )
		
		
		
		
		
	
