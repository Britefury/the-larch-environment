##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocPresent.Toolkit.DTContainerSequence import DTContainerSequence



class DTWrappedHBox (DTContainerSequence):
	class ChildEntry (DTContainerSequence.ChildEntry):
		def __init__(self, child, padding):
			super( DTWrappedHBox.ChildEntry, self ).__init__( child )
			self.padding = padding
			self._reqWidth = 0.0
			self._reqHeight = 0.0
			self._baseline = 0.0



	def __init__(self, spacing=0.0, padding=0.0, indentation=0.0, backgroundColour=None):
		super( DTWrappedHBox, self ).__init__( backgroundColour )

		self._spacing = spacing
		self._padding = padding
		self._indentation = indentation
		self._lines = []



	def getSpacing(self):
		return self._spacing

	def setSpacing(self, spacing):
		self._spacing = spacing
		self._o_queueResize()
		
		
		
	def getPadding(self):
		return self._padding

	def setPadding(self, padding):
		self._padding = padding
		self._o_queueResize()
		
		
		
	def getIndentation(self):
		return self._indentation

	def setIndentation(self, indentation):
		self._indentation = indentation
		self._o_queueResize()
		
		
		
	def remove(self, child):
		self._o_remove( child )




	def _p_itemToChildEntry(self, item):
		if isinstance( item, tuple ):
			child, padding = item
			return self.ChildEntry( child, padding )
		else:
			return self.ChildEntry( item, self._padding )



	def _p_buildEntry(self, child, padding=None):
		if padding is None:
			padding = self._padding
		return self.ChildEntry( child, padding )

	def append(self, child, padding=None):
		entry = self._p_buildEntry( child, padding )
		self._o_appendEntry( entry )

	def extend(self, children):
		entries = [ self.ChildEntry( child, self._padding )   for child in children ]
		self._o_extendEntries( entries )

	def insert(self, index, child, padding=None):
		assert not self.hasChild( child ), 'child already present'
		entry = self._p_buildEntry( child, padding )
		self._o_insertEntry( index, entry )


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

	def _o_getRequiredHeightAndBaseline(self):
		aboveBaseline = 0.0
		baseline = 0.0
		for entry in self._childEntries:
			req, bas = entry.child._f_getRequisitionHeightAndBaseline()
			abv = req - bas
			entry._reqHeight = req
			entry._baseline = bas
			aboveBaseline = max( aboveBaseline, abv )
			baseline = max( baseline, bas )
		requisition = aboveBaseline + baseline
		return requisition, baseline


	def _o_onAllocateX(self, allocation):
		x = 0.0
		self._lines = []
		currentLine = []
		bFirstChildInLine = True
		bFirstLine = True
		for entry in self._childEntries:
			childWidth = entry._reqWidth

			childRight = x + childWidth
			if childRight > allocation:
				if bFirstChildInLine:
					# First child in this line takes up whole line

					# This line has 1 child
					self._lines.append( [ entry ] )
					# Put it at the start
					if bFirstLine:
						x = 0.0
					else:
						x = self._indentation
					# Allocate
					self._o_allocateChildX( entry.child, x + entry.padding, childWidth )
					
					# Start a new line
					# Nothing in the current line
					currentLine = []
					# Start @x at indentation
					x = self._indentation
					# Not the first line in the box
					bFirstLine = False
					# Next child will be the first child in the new line
					bFirstChildInLine = True
				else:
					# Terminate the current line, and put @entry on the next line
					
					# Terminate current line
					self._lines.append( currentLine )
					
					# Start a new line
					# Start with @entry in the new line
					currentLine = [ entry ]
					# Start @x at indentation
					x = self._indentation
					# Not the first line in the box
					bFirstLine = False
					# Next child will be the second child in the new line
					bFirstChildInLine = False
					
					# Allocate
					self._o_allocateChildX( entry.child, x, childWidth )
					
					# Move @x on
					x += childWidth + self._spacing
			else:
				# Continue existing line
				
				# Allocate
				self._o_allocateChildX( entry.child, x, childWidth )

				# Add @entry to the current line
				currentLine.append( entry )
				# Move @x on
				x += childWidth + self._spacing
				# Next child will not be the first child in this line
				bFirstChildInLine = False
				

		if len( currentLine )  >  0:
			self._lines.append( currentLine )


	def _o_onAllocateY(self, allocation):
		lineHeights = []
		for line in self._lines:
			lineHeights.append( max( [ entry._reqHeight   for entry in line ] ) )

		totalLineHeights = sum( lineHeights )

		y = ( allocation - totalLineHeights ) * 0.5
		y = max( y, 0.0 )

		for line, lineHeight in zip( self._lines, lineHeights ):
			for entry in line:
				offset = ( lineHeight - entry._reqHeight ) * 0.5
				self._o_allocateChildY( entry.child, y + offset, entry._reqHeight )
			y += lineHeight


	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()



	#
	# FOCUS NAVIGATION METHODS
	#
	
	def horizontalNavigationList(self):
		return [ e.child   for e in self._childEntries ]

	def verticalNavigationList(self):
		return []

	
	
	spacing = property( getSpacing, setSpacing )
	padding = property( getPadding, setPadding )
	indentation = property( getIndentation, setIndentation )






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
	doc.getGtkWidget().show()


	labelTexts = [ 'Hello world %d' % ( i, )   for i in xrange( 0, 10 ) ]  +  [ '(', ')', '\'', '"', '.' ]

	labels = [ MyLabel( text )   for text in labelTexts ]

	line = DTWrappedHBox( indentation=30.0 )
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
	box.pack_start( doc.getGtkWidget() )
	box.pack_start( gtk.HSeparator(), False, False, 10 )
	box.pack_start( buttonBox, False, False, 10 )
	box.show_all()

	window.add( box )
	window.show()

	gtk.main()
