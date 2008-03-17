##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import bisect

from Britefury.Math.Math import Point2, Vector2
from Britefury.DocPresent.Toolkit.DTCursor import DTCursorLocation
from Britefury.DocPresent.Toolkit.DTContainerSequence import DTContainerSequence
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.Toolkit.DTBin import DTBin



class DTBox (DTContainerSequence):
	ALIGN_LEFT = 0
	ALIGN_TOP = 0
	ALIGN_CENTRE = 1
	ALIGN_RIGHT = 2
	ALIGN_BOTTOM = 2
	ALIGN_EXPAND = 3
	ALIGN_BASELINES = 4
	_ALIGN_TOPLEFT = 0
	_ALIGN_BOTTOMRIGHT = 2


	class ChildEntry (DTContainerSequence.ChildEntry):
		def __init__(self, child, bExpand, bFill, bShrink, alignment, padding):
			super( DTBox.ChildEntry, self ).__init__( child )
			self.bExpand = bExpand
			self.bFill = bFill
			self.bShrink = bShrink
			self.alignment = alignment
			self.padding = padding
			self._reqWidth = 0.0
			self._reqHeight = 0.0
			self._reqBaseline = 0.0



	def __init__(self, direction=DTDirection.LEFT_TO_RIGHT, spacing=0.0, bExpand=False, bFill=False, bShrink=False, alignment=ALIGN_CENTRE, padding=0.0, backgroundColour=None):
		super( DTBox, self ).__init__( backgroundColour )

		self._direction = direction
		self._spacing = spacing
		self._bExpand = bExpand
		self._bFill = bFill
		self._bShrink = bShrink
		self._alignment = alignment
		self._padding = padding
		self._childrenRequisition = Vector2()
		self._childrenBaseline = 0.0
		self._numExpand = 0
		self._numShrink = 0



	def getDirection(self):
		return self._direction

	def setDirection(self, direction):
		self._direction = direction
		self._o_queueResize()

	def getSpacing(self):
		return self._spacing

	def setSpacing(self, spacing):
		self._spacing = spacing
		self._o_queueResize()

	def getBExpand(self):
		return self._bExpand

	def setBExpand(self, bExpand):
		self._bExpand = bExpand
		self._o_queueResize()

	def getBFill(self):
		return self._bFill

	def setBFill(self, bFill):
		self._bFill = bFill
		self._o_queueResize()

	def getBShrink(self):
		return self._bShrink

	def setBShrink(self, bShrink):
		self._bShrink = bShrink
		self._o_queueResize()

	def getAlignment(self):
		return self._alignment

	def setAlignment(self, alignment):
		self._alignment = alignment
		self._o_queueResize()

	def getPadding(self):
		return self._padding

	def setPadding(self, padding):
		self._padding = padding
		self._o_queueResize()



	def remove(self, child):
		self._o_remove( child )



	def _p_itemToChildEntry(self, item):
		if isinstance( item, tuple ):
			child, bExpand, bFill, bShrink, alignment, padding = item
			return self.ChildEntry( child, bExpand, bFill, bShrink, alignment, padding )
		else:
			return self.ChildEntry( item, self._bExpand, self._bFill, self._bShrink, self._alignment, self._padding )


	def _p_buildEntry(self, child, bExpand=None, bFill=None, bShrink=None, alignment=None, padding=None):
		if bExpand is None:
			bExpand = self._bExpand
		if bFill is None:
			bFill = self._bFill
		if bShrink is None:
			bShrink = self._bShrink
		if alignment is None:
			alignment = self._alignment
		if padding is None:
			padding = self._padding
		return self.ChildEntry( child, bExpand, bFill, bShrink, alignment, padding )


	def append(self, child, bExpand=None, bFill=None, bShrink=None, alignment=None, padding=None):
		entry = self._p_buildEntry( child, bExpand, bFill, bShrink, alignment, padding )
		self._o_appendEntry( entry )

	def extend(self, children):
		entries = [ self.ChildEntry( child, self._bExpand, self._bFill, self._bShrink, self._alignment, self._padding )   for child in children ]
		self._o_extendEntries( entries )


	def insert(self, index, child, bExpand=None, bFill=None, bShrink=None, alignment=None, padding=None):
		assert not self.hasChild( child ), 'child already present'
		entry = self._p_buildEntry( child, bExpand, bFill, bShrink, alignment, padding )
		self._o_insertEntry( index, entry )


			
			

		
		
	def getInsertIndex(self, localPos):
		"""Return the index at which an item could be inserted.
		localPos is checked against the contents of the box in order to determine the insert index"""

		if len( self ) == 0:
			return 0

		if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
			pos = localPos.x
		elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
			pos = localPos.y

		if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.TOP_TO_BOTTOM:
			bReversed = False
		elif self._direction == DTDirection.RIGHT_TO_LEFT  or  self._direction == DTDirection.BOTTOM_TO_TOP:
			bReversed = True

		if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
			midPoints  =  [ childEntry._xPos  +  childEntry._width * 0.5   for childEntry in self._childEntries ]
		elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
			midPoints  =  [ childEntry._yPos  +  childEntry._height * 0.5   for childEntry in self._childEntries ]

		if bReversed:
			if pos > midPoints[0]:
				return 0
			elif pos < midPoints[-1]:
				return len( self )
			else:
				for i, ( upper, lower )  in  enumerate( zip( midPoints[:-1], midPoints[1:] ) ):
					if pos >= lower  and  pos  <=  upper:
						return i + 1
		else:
			if pos < midPoints[0]:
				return 0
			elif pos > midPoints[-1]:
				return len( self )
			else:
				for i, ( lower, upper )  in  enumerate( zip( midPoints[:-1], midPoints[1:] ) ):
					if pos >= lower  and  pos  <=  upper:
						return i + 1

		raise ValueError, 'Could not determine insert position'




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

	def _o_getRequiredHeightAndBaseline(self):
		aboveBaseline = 0.0
		baseline = 0.0
		height = 0.0
		for entry in self._childEntries:
			req, bas = entry.child._f_getRequisitionHeightAndBaseline()
			if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
				if entry.alignment == self.ALIGN_BASELINES:
					abv = req - bas
					entry._reqHeight = req
					entry._reqBaseline = bas
					aboveBaseline = max( aboveBaseline, abv )
					baseline = max( baseline, bas )
				else:
					entry._reqHeight = req
					entry._reqBaseline = 0.0
					height = max( height, req )
			elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
				entry._reqHeight = req + entry.padding * 2.0
				entry._reqBaseline = 0.0
				height += entry._reqHeight
		height = max( height, baseline + aboveBaseline )
		requisition = height
		if self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
			if len( self._childEntries ) > 0:
				requisition += self._spacing  *  ( len( self._childEntries ) - 1 )
		self._childrenRequisition.y = requisition
		self._childrenBaseline = baseline
		return requisition, baseline


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
				childX = x + entry.padding
				if entry.bExpand:
					childAlloc += expandPerChild
					if entry.bFill:
						childWidth += expandPerChild
					else:
						childX += expandPerChild * 0.5
				if entry.bShrink:
					childAlloc -= shrinkPerChild
					childWidth -= shrinkPerChild
				self._o_allocateChildX( entry.child, childX, childWidth )
				x += childAlloc + self._spacing
		else:
			for entry in self._childEntries:
				if entry.alignment == self._ALIGN_TOPLEFT:
					self._o_allocateChildX( entry.child, 0.0, entry._reqWidth )
				elif entry.alignment == self.ALIGN_CENTRE:
					self._o_allocateChildX( entry.child, ( allocation - entry._reqWidth )  *  0.5, entry._reqWidth )
				elif entry.alignment == self._ALIGN_BOTTOMRIGHT:
					self._o_allocateChildX( entry.child, allocation - entry._reqWidth, entry._reqWidth )
				elif entry.alignment == self.ALIGN_EXPAND:
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
				childY = y + entry.padding
				if entry.bExpand:
					childAlloc += expandPerChild
					if entry.bFill:
						childHeight += expandPerChild
					else:
						childY += expandPerChild * 0.5
				if entry.bShrink:
					childAlloc -= shrinkPerChild
					childHeight -= shrinkPerChild
				self._o_allocateChildY( entry.child, childY, childHeight )
				y += childAlloc + self._spacing
		else:
			for entry in self._childEntries:
				if entry.alignment == self._ALIGN_TOPLEFT:
					self._o_allocateChildY( entry.child, 0.0, entry._reqHeight )
				elif entry.alignment == self.ALIGN_CENTRE:
					self._o_allocateChildY( entry.child, ( allocation - entry._reqHeight )  *  0.5, entry._reqHeight )
				elif entry.alignment == self._ALIGN_BOTTOMRIGHT:
					self._o_allocateChildY( entry.child, allocation - entry._reqHeight, entry._reqHeight )
				elif entry.alignment == self.ALIGN_EXPAND:
					self._o_allocateChildY( entry.child, 0.0, allocation )
				elif entry.alignment == self.ALIGN_BASELINES:
					y = allocation - self._childrenBaseline + entry._reqBaseline - entry._reqHeight
					self._o_allocateChildY( entry.child, y, entry._reqHeight )


	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()



	def isOrderReversed(self):
		return self._direction == DTDirection.RIGHT_TO_LEFT  or  self._direction == DTDirection.BOTTOM_TO_TOP



	#
	# CURSOR POSITIONING METHODS
	#
	
	def _o_getCursorLocationAtPosition(self, localPos):
		"""Return the index of the child that is closest to @localPos, and a flag which indicates whether @localPos
		is closest to the leading or trailing edge."""
		childEntries = [ entry   for entry in self._childEntries   if not entry.child.isCursorBlocked()  and  entry.child.getFirstCursorEntity() is not None  and  entry.child.getLastCursorEntity() is not None ]
		
		if len( childEntries ) == 0:
			return None
		else:
			if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
				pos = localPos.x
			elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
				pos = localPos.y

			if len( self ) == 1:
				childIndex = 0
			else:		
				class _BoundaryPoints (object):
					def __init__(self, box):
						self._l = len( childEntries ) - 1
						self._bOrderReversed = box._direction == DTDirection.RIGHT_TO_LEFT  or  box._direction == DTDirection.BOTTOM_TO_TOP
						self._bX = box._direction == DTDirection.LEFT_TO_RIGHT  or  box._direction == DTDirectionRIGHT_TO_LEFT
						
					def __len__(self):
						return self._l
					
					def __getitem__(self, i):
						if self._bOrderReversed:
							c0 = childEntries[(self._l - i) - 1]
							c1 = childEntries[self._l - i]
						else:
							c0 = childEntries[i]
							c1 = childEntries[i+1]
							
						if self._bX:
							return  ( c0._xPos + c0._width  +  c1._xPos )  *  0.5
						else:
							return  ( c0._yPos + c0._height  +  c1._yPos )  *  0.5
				
				
				childIndex = bisect.bisect_right( _BoundaryPoints( self ), pos )
	
				
			# Child index computed
			childEntry = childEntries[childInded]
			
			# leading or trailing edge?
			if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.RIGHT_TO_LEFT:
				mid = childEntry._xPos + childEntry._width * 0.5
			elif self._direction == DTDirection.TOP_TO_BOTTOM  or  self._direction == DTDirection.BOTTOM_TO_TOP:
				mid = childEntry._yPos + childEntry._height * 0.5
				
			if pos <= mid:
				return DTCursorLocation( childEntry.child.getFirstCursorEntity(), DTCursorLocation.EDGE_LEADING )
			else:
				return DTCursorLocation( childEntry.child.getLastCursorEntity(), DTCursorLocation.EDGE_TRAILING )

	
	
	#
	# FOCUS NAVIGATION METHODS
	#
	
	def horizontalNavigationList(self):
		if self._direction == DTDirection.LEFT_TO_RIGHT  or  self._direction == DTDirection.TOP_TO_BOTTOM:
			return [ e.child   for e in self._childEntries ]
		else:
			return [ e.child   for e in reversed( self._childEntries ) ]

	def verticalNavigationList(self):
		if elf._direction == DTDirection.TOP_TO_BOTTOM:
			return [ e.child   for e in self._childEntries ]
		elif self._direction == DTDirection.BOTTOM_TO_TOP:
			return [ e.child   for e in reversed( self._childEntries ) ]
		else:
			return []

	
	direction = property( getDirection, setDirection )
	spacing = property( getSpacing, setSpacing )
	bExpand = property( getBExpand, setBExpand )
	bFill = property( getBFill, setBFill )
	bShrink = property( getBShrink, setBShrink )
	alignment = property( getAlignment, setAlignment )
	padding = property( getPadding, setPadding )







if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
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
	hbox.append( label1, False )
	hbox.append( label2, True, True )
	hbox.append( label3, False )
	hbox.spacing = 5.0
	hbox.backgroundColour = Colour3f( 0.6, 0.6, 0.6 )

	vbox = DTBox( DTDirection.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_EXPAND )
	vbox.append( hbox, False )
	vbox.append( label4, False )
	vbox.spacing = 10.0
	vbox.backgroundColour = Colour3f( 0.8, 0.8, 0.8 )

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
