##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocView.Toolkit.DTContainer import DTContainer
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTBin import DTBin



class DTBox (DTContainer):
	ALIGN_TOPLEFT = 0
	ALIGN_CENTRE = 1
	ALIGN_BOTTOMRIGHT = 2
	ALIGN_EXPAND = 3


	class ChildEntry (DTContainer.ChildEntry):
		def __init__(self, child, bExpand, bFill, bShrink, padding):
			super( DTBox.ChildEntry, self ).__init__( child )
			self.bExpand = bExpand
			self.bFill = bFill
			self.bShrink = bShrink
			self.padding = padding
			self._reqWidth = 0.0
			self._reqHeight = 0.0



	def __init__(self, direction=DTDirection.LEFT_TO_RIGHT, minorDirectionAlignment=ALIGN_CENTRE, spacing=0.0, bExpand=False, bFill=False, bShrink=False, padding=0.0):
		super( DTBox, self ).__init__()

		self._direction = direction
		self._minorDirectionAlignment = minorDirectionAlignment
		self._spacing = spacing
		self._bExpand = bExpand
		self._bFill = bFill
		self._bShrink = bShrink
		self._padding = padding
		self._childrenRequisition = Vector2()
		self._numExpand = 0
		self._numShrink = 0



	def getSpacing(self):
		return self._spacing

	def setSpacing(self, spacing):
		self._spacing = spacing
		self._o_queueResize()




	def _p_itemToChildEntry(self, item):
		if isinstance( item, tuple ):
			child, bExpand, bFill, bShrink, padding = item
			return self.ChildEntry( child, bExpand, bFill, bShrink, padding )
		else:
			return self.ChildEntry( item, self._bExpand, self._bFill, self._bShrink, self._padding )


	def __len__(self):
		return len( self._childEntries )

	def __getitem__(self, index):
		entry = self._childEntries[index]
		if isinstance( entry, list ):
			return [ e.child   for e in entry ]
		else:
			return entry.child

	def __setitem__(self, index, item):
		if isinstance( index, slice ):
			oldEntrySet = set( self._childEntries )
			self._childEntries[index] = [ self._p_itemToChildEntry( x )  for x in item ]
			newEntrySet = set( self._childEntries )

			removed = oldEntrySet.difference( newEntrySet )
			added = newEntrySet.difference( oldEntrySet )

			for entry in removed:
				self._o_unregisterChildEntry( entry )

			for entry in added:
				self._o_registerChildEntry( entry )

			self._p_childListModified()
			self._o_queueResize()
		else:
			newEntry = self._p_itemToChildEntry( item )
			oldEntry = self._childEntries[index]
			self._o_unregisterChildEntry( oldEntry )
			self._childEntries[index] = newEntry
			self._o_registerChildEntry( newEntry )
			self._p_childListModified()
			self._o_queueResize()

	def __delitem__(self, index):
		entry = self._childEntries[index]
		del self._childEntries[index]
		if isinstance( entry, list ):
			for e in entry:
				self._o_unregisterChildEntry( e )
		else:
			self._o_unregisterChildEntry( entry )
		self._p_childListModified()
		self._o_queueResize()


	def append(self, child, bExpand=None, bFill=None, bShrink=None, padding=None):
		assert not self.hasChild( child ), 'child already present'
		if bExpand is None:
			bExpand = self._bExpand
		if bFill is None:
			bFill = self._bFill
		if bShrink is None:
			bShrink = self._bShrink
		if padding is None:
			padding = self._padding
		entry = self.ChildEntry( child, bExpand, bFill, bShrink, padding )
		self._childEntries.append( entry )
		self._o_registerChildEntry( entry )
		self._p_childListModified()
		self._o_queueResize()

	def extend(self, children):
		for child in children:
			self.append( child )


	def insert(self, index, child, bExpand=None, bFill=None, bShrink=None, padding=None):
		assert not self.hasChild( child ), 'child already present'
		if bExpand is None:
			bExpand = self._bExpand
		if bFill is None:
			bFill = self._bFill
		if bShrink is None:
			bShrink = self._bShrink
		if padding is None:
			padding = self._padding
		entry = self.ChildEntry( child, bExpand, bFill, bShrink, padding )
		self._childEntries.insert( index, entry )
		self._o_registerChildEntry( entry )
		self._p_childListModified()
		self._o_queueResize()


	def remove(self, child):
		assert self.hasChild( child ), 'child not present'
		entry = self._childToEntry[child]
		self._childEntries.remove( entry )
		self._o_unregisterChildEntry( entry )
		self._p_childListModified()
		self._o_queueResize()



	def _f_removeChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		self[index] = DTBin()


	def _p_childListModified(self):
		self._numExpand = 0
		self._numShrink = 0

		for entry in self._childEntries:
			if entry.bExpand:
				self._numExpand += 1
			if entry.bShrink:
				self._numShrink += 1




	def _o_getRequiredWidth(self):
		requisition = 0.0
		for entry in self._childEntries:
			req = entry.child._f_getRequisitionWidth()
			if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
				entry._reqWidth = req + entry.padding * 2.0
				requisition += entry._reqWidth
			elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
				entry._reqWidth = req
				requisition = max( requisition, req )
		spacing = 0.0
		if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
			if len( self._childEntries ) > 0:
				spacing = self._spacing  *  ( len( self._childEntries ) - 1 )
		self._childrenRequisition.x = requisition + spacing
		return requisition + spacing

	def _o_getRequiredHeight(self):
		requisition = 0.0
		for entry in self._childEntries:
			req = entry.child._f_getRequisitionHeight()
			if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
				entry._reqHeight = req
				requisition = max( requisition, req )
			elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
				entry._reqHeight = req + entry.padding * 2.0
				requisition += entry._reqHeight
		spacing = 0.0
		if self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
			if len( self._childEntries ) > 0:
				spacing = self._spacing  *  ( len( self._childEntries ) - 1 )
		self._childrenRequisition.y = requisition + spacing
		return requisition + spacing


	def _o_onAllocateX(self, allocation):
		if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
			expandPerChild = 0.0
			shrinkPerChild = 0.0
			if allocation > self._childrenRequisition.x:
				# More space than is required
				if self._numExpand > 0:
					expand = allocation - self._childrenRequisition.x
					expandPerChild = expand / float( self._numExpand )
			elif allocation < self._childrenRequisition.x:
				# Insufficient space
				if self._numShrink > 0:
					shrink = self._childrenRequisition.x - allocation
					shrinkPerChild = shrink / float( self._numShrink )

			if self._direction == DTDirection.LEFT_TO_RIGHT:
				childEntryIter = self._childEntries
			elif self._direction == DTDirection.RIGHT_TO_LEFT:
				childEntryIter = reversed( self._childEntries )

			x = 0.0
			for entry in childEntryIter:
				childAlloc = entry._reqWidth
				childWidth = entry._reqWidth
				if entry.bExpand:
					childAlloc += expandPerChild
					if entry.bFill:
						childWidth += expandPerChild
				if entry.bShrink:
					childAlloc -= shrinkPerChild
					childWidth -= shrinkPerChild
				childX = x
				if not entry.bFill:
					childX += expandPerChild * 0.5
				self._o_allocateChildX( entry.child, childX, childWidth )
				x += childAlloc + self._spacing
		else:
			for entry in self._childEntries:
				if self._minorDirectionAlignment == self.ALIGN_TOPLEFT:
					self._o_allocateChildX( entry.child, 0.0, entry._reqWidth )
				elif self._minorDirectionAlignment == self.ALIGN_CENTRE:
					self._o_allocateChildX( entry.child, ( allocation - entry._reqWidth )  *  0.5, entry._reqWidth )
				elif self._minorDirectionAlignment == self.ALIGN_BOTTOMRIGHT:
					self._o_allocateChildX( entry.child, allocation - entry._reqWidth, entry._reqWidth )
				elif self._minorDirectionAlignment == self.ALIGN_EXPAND:
					self._o_allocateChildX( entry.child, 0.0, allocation )


	def _o_onAllocateY(self, allocation):
		if self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
			expandPerChild = 0.0
			shrinkPerChild = 0.0
			if allocation > self._childrenRequisition.y:
				# More space than is required
				if self._numExpand > 0:
					expand = allocation - self._childrenRequisition.y
					expandPerChild = expand / float( self._numExpand )
			elif allocation < self._childrenRequisition.y:
				# Insufficient space
				if self._numShrink > 0:
					shrink = self._childrenRequisition.y - allocation
					shrinkPerChild = shrink / float( self._numShrink )

			if self._direction == DTDirection.TOP_TO_BOTTOM:
				childEntryIter = self._childEntries
			elif self._direction == DTDirection.BOTTOM_TO_TOP:
				childEntryIter = reversed( self._childEntries )

			y = 0.0
			for entry in childEntryIter:
				childAlloc = entry._reqHeight
				childHeight = entry._reqHeight
				if entry.bExpand:
					childAlloc += expandPerChild
					if entry.bFill:
						childHeight += expandPerChild
				if entry.bShrink:
					childAlloc -= shrinkPerChild
					childHeight -= shrinkPerChild
				childY = y
				if not entry.bFill:
					childY += expandPerChild * 0.5
				self._o_allocateChildY( entry.child, childY, childHeight )
				y += childAlloc + self._spacing
		else:
			for entry in self._childEntries:
				if self._minorDirectionAlignment == self.ALIGN_TOPLEFT:
					self._o_allocateChildY( entry.child, 0.0, entry._reqHeight )
				elif self._minorDirectionAlignment == self.ALIGN_CENTRE:
					self._o_allocateChildY( entry.child, ( allocation - entry._reqHeight )  *  0.5, entry._reqHeight )
				elif self._minorDirectionAlignment == self.ALIGN_BOTTOMRIGHT:
					self._o_allocateChildY( entry.child, allocation - entry._reqHeight, entry._reqHeight )
				elif self._minorDirectionAlignment == self.ALIGN_EXPAND:
					self._o_allocateChildY( entry.child, 0.0, allocation )


	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()



	spacing = property( getSpacing, setSpacing )






if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocView.Toolkit.DTLabel import DTLabel
	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	import cairo
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	def onChangeText(widget, data=None):
		label1.text = 'Something else'

	def onChangeFont(widget, data=None):
		label1.font = 'Sans bold 20'

	def onChangeColour(widget, data=None):
		label1.colour = Colour3f( 1.0, 0.0, 0.0 )


	def makeButton(text, response):
		button = gtk.Button( text )
		button.connect( 'clicked', response )
		button.show()
		return button


	class MyLabel (DTLabel):
		def _o_onEnter(self, localPos):
			super( MyLabel, self )._o_onEnter( localPos )
			self._savedColour = self.colour
			self.colour = Colour3f( 1.0, 0.5, 0.0 )

		def _o_onLeave(self, localPos):
			super( MyLabel, self )._o_onLeave( localPos )
			self.colour = self._savedColour


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	label1 = MyLabel( 'Hello world' )
	label2 = MyLabel( 'Hello world 2' )
	label3 = MyLabel( 'Hello world 3' )
	label4 = MyLabel( 'Hello world 4' )
	label4.font = 'Sans 30'

	hbox = DTBox( DTDirection.LEFT_TO_RIGHT )
	hbox.append( label1, True )
	hbox.append( label2, True )
	hbox.append( label3, True )
	hbox.spacing = 5.0

	vbox = DTBox( DTDirection.TOP_TO_BOTTOM )
	vbox.append( hbox, True )
	vbox.append( label4, True )
	vbox.spacing = 10.0

	doc.child = vbox


	textButton = makeButton( 'Change text', onChangeText )
	fontButton = makeButton( 'Change font', onChangeFont )
	colourButton = makeButton( 'Change colour', onChangeColour )


	buttonBox = gtk.HBox( True )
	buttonBox.pack_start( textButton, False, False, 20 )
	buttonBox.pack_start( fontButton, False, False, 20 )
	buttonBox.pack_start( colourButton, False, False, 20 )
	buttonBox.show_all()

	box = gtk.VBox()
	box.pack_start( doc )
	box.pack_start( gtk.HSeparator(), False, False, 10 )
	box.pack_start( buttonBox, False, False, 10 )
	box.show_all()

	window.add( box )
	window.show()

	gtk.main()
