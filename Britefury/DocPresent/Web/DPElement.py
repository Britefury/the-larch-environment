##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Web.DPEvent import DPEvent



class ElementContentChangedEvent (DPEvent):
	pass




class DPElement (object):
	MODE_SPAN = 0
	MODE_DIV = 1
	
	
	className = 'element'
	mode = MODE_SPAN
	mouseOverClass = None
	
	
	def __init__(self, id, contentFn):
		super( DPElement, self ).__init__()
		self._view = None
		self._id = id
		self._contentFn = contentFn
		
		
	def _realise(self, view, id):
		self._view = view
		self._id = id
		
		

	def html(self):
		if self.mode == self.MODE_SPAN:
			return '<span class="%s" id="e%d">%s</span>'  %  ( self.className, self._id, self.contentHtml() )
		elif self.mode == self.MODE_DIV:
			return '<div class="%s" id="e%d">%s</div>'  %  ( self.className, self._id, self.contentHtml() )
		else:
			raise ValueError


	def contentHtml(self):
		return self._contentFn()
	
	

	

class DPNode (object):
	MODE_SPAN = 0
	MODE_DIV = 1
	
	
	className = 'element'
	mode = MODE_SPAN
	mouseOverClass = None
	
	
	def __init__(self, id, contentFn):
		super( DPElement, self ).__init__()
		self._view = None
		self._id = id
		self._contentFn = contentFn
		
		
	def _realise(self, view, id):
		self._view = view
		self._id = id
		
		

	def html(self):
		if self.mode == self.MODE_SPAN:
			return '<span class="%s" id="e%d">%s</span>'  %  ( self.className, self._id, self.contentHtml() )
		elif self.mode == self.MODE_DIV:
			return '<div class="%s" id="e%d">%s</div>'  %  ( self.className, self._id, self.contentHtml() )
		else:
			raise ValueError


	def contentHtml(self):
		return self._contentFn()
	
	

	

class DPElementPlaceHolder (object0:
	def __init__(self, id, contentFn):
		super( DPElementPlaceHolder, self ).__init__()
		self._view = None
		self._id = id
		
		
	def _realise(self, view, id):
		self._view = view
		self._id = id
		
		

	def html(self):
		return '<span class="gsym_place_holder" id="e%d">e%d</span>'  %  ( self._id, seld._id )
