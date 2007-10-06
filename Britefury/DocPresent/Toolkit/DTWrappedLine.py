##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer



class DTWrappedLine (DTContainer):
	class ChildEntry (DTContainer.ChildEntry):
		def __init__(self, child, padding):
			super( DTWrappedLine.ChildEntry, self ).__init__( child )
			self.padding = padding
			self._reqWidth = 0.0
			self._reqHeight = 0.0



	def __init__(self, spacing=0.0, padding=0.0, backgroundColour=None):
		super( DTWrappedLine, self ).__init__( backgroundColour )

		self._spacing = spacing
		self._padding = padding
		self._lineLengths = []



	def getSpacing(self):
		return self._spacing

	def setSpacing(self, spacing):
		self._spacing = spacing
		self._o_queueResize()




	def _p_itemToChildEntry(self, item):
		if isinstance( item, tuple ):
			child, padding = item
			return self.ChildEntry( child, padding )
		else:
			return self.ChildEntry( item, self._padding )



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

			self._o_queueResize()
		else:
			newEntry = self._p_itemToChildEntry( item )
			oldEntry = self._childEntries[index]
			self._o_unregisterChildEntry( oldEntry )
			self._childEntries[index] = newEntry
			self._o_registerChildEntry( newEntry )
			self._o_queueResize()

	def __delitem__(self, index):
		entry = self._childEntries[index]
		del self._childEntries[index]
		if isinstance( entry, list ):
			for e in entry:
				self._o_unregisterChildEntry( entry )
		else:
			self._o_unregisterChildEntry( entry )
		self._o_queueResize()


	def append(self, child, padding=None):
		assert not self.hasChild( child ), 'child already present'
		if padding is None:
			padding = self._padding
		entry = self.ChildEntry( child, padding )
		self._childEntries.append( entry )
		self._o_registerChildEntry( entry )
		self._o_queueResize()

	def extend(self, children):
		entries = [ self.ChildEntry( child, self._padding )   for child in children ]
		self._childEntries.extend( entries )
		for entry in entries:
			self._o_registerChildEntry( entry )
		self._o_queueResize()

	def insert(self, index, child, padding=None):
		assert not self.hasChild( child ), 'child already present'
		if padding is None:
			padding = self._padding
		entry = self.ChildEntry( child, padding )
		self._childEntries.insert( index, entry )
		self._o_registerChildEntry( entry )
		self._o_queueResize()


	def remove(self, child):
		assert self.hasChild( child ), 'child not present'
		entry = self._childToEntry[child]
		self._childEntries.remove( entry )
		self._o_unregisterChildEntry( entry )
		self._o_queueResize()


	def _f_removeChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		self[index] = DTBin()


	def _o_getRequiredWidth(self):
		requisition = 0.0
		for entry in self._childEntries:
			req = entry.child._f_getRequisitionWidth()
			entry._reqWidth = req + entry.padding * 2.0
			requisition += entry._reqWidth
		spacing = 0.0
		if len( self._childEntries ) > 0:
			spacing = self._spacing  *  ( len( self._childEntries ) - 1 )
		return requisition + spacing

	def _o_getRequiredHeight(self):
		requisition = 0.0
		for entry in self._childEntries:
			req = entry.child._f_getRequisitionHeight()
			entry._reqHeight = req
			requisition = max( requisition, req )
		return requisition


	def _o_onAllocateX(self, allocation):
		x = 0.0
		lineLength = 0
		self._lineLengths = []
		for entry in self._childEntries:
			childWidth = entry._reqWidth

			childRight = x + childWidth
			if childRight > allocation:
				if x > 0.0:
					# Terminate current line
					self._lineLengths.append( lineLength )
					# Put this child on a new line
					self._o_allocateChildX( entry.child, 0.0, childWidth )
					x = childWidth + self._spacing
					# Continue on this line: length of this line is 1
					lineLength = 1
				else:
					# First child in this line takes up whole line

					# This line has 1 child
					self._lineLengths.append( 1 )
					# Put it at x=0
					self._o_allocateChildX( entry.child, 0.0, childWidth )
					# Start a new line
					lineLength = 0
					x = 0.0
			else:
				# Continue existing line
				self._o_allocateChildX( entry.child, x, childWidth )
				lineLength += 1
				x += childWidth + self._spacing

		if lineLength > 0:
			self._lineLengths.append( lineLength )


	def _o_onAllocateY(self, allocation):
		i = 0
		lineHeights = []
		for lineLength in self._lineLengths:
			lineHeights.append( max( [ entry._reqHeight   for entry in self._childEntries[i:i+lineLength] ] ) )
			i += lineLength

		totalLineHeights = sum( lineHeights )

		i = 0
		y = ( allocation - totalLineHeights ) * 0.5
		y = max( y, 0.0 )

		for lineLength, lineHeight in zip( self._lineLengths, lineHeights ):
			for entry in self._childEntries[i:i+lineLength]:
				offset = ( lineHeight - entry._reqHeight ) * 0.5
				self._o_allocateChildY( entry.child, y + offset, entry._reqHeight )
			i += lineLength
			y += lineHeight


	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()



	spacing = property( getSpacing, setSpacing )






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
		label1.font = 'Sand bold 20'

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


	labelTexts = [ 'Hello world %d' % ( i, )   for i in xrange( 0, 10 ) ]  +  [ '(', ')', '\'', '"', '.' ]

	labels = [ MyLabel( text )   for text in labelTexts ]

	line = DTWrappedLine()
	for label in labels:
		line.append( label )
	line.spacing = 5.0

	doc.child = line


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
