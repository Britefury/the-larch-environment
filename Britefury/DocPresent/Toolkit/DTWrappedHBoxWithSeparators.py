##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocPresent.Toolkit.DTWrappedHBox import DTWrappedHBox
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel

import traceback

class DTWrappedHBoxWithSeparators (DTWrappedHBox):
	def __init__(self, separatorFactory=',', spacing=0.0, padding=0.0):
		super( DTWrappedHBoxWithSeparators, self ).__init__( spacing, padding )

		self._boxes = []
		self._items = []
		self._separatorFactory = separatorFactory


	def __getitem__(self, index):
		return self._items[index]

	def index(self, item):
		return self._items.index( item )

	def __setitem__(self, index, item):
		if isinstance( index, slice ):
			# Create the new boxes
			boxes = [ DTBox()   for child in item ]

			# Add the items to the new boxes
			for child, box in zip( item, boxes ):
				box.append( child )

			self._boxes[index] = boxes
			self._items[index] = item

			for box in self._boxes[:-1]:
				if len( box ) < 2:
					box.append( self._p_makeSeparator() )
			if len( self._boxes ) > 0:
				if len( self._boxes[-1] ) > 1:
					del self._boxes[-1][1]

			super( DTWrappedHBoxWithSeparators, self ).__setitem__( index, boxes )
		else:
			self._boxes[index][0] = item
			self._items[index] = item


	def __delitem__(self, index):
		super( DTWrappedHBoxWithSeparators, self ).__delitem__( index )

		del self._boxes[index]
		del self._items[index]

		if len( self._boxes ) > 0:
			if len( self._boxes[-1] ) > 1:
				del self._boxes[-1][1]


	def append(self, child, padding=None):
		if len( self._boxes ) > 0:
			# Add a separator to the last box
			self._boxes[-1].append( self._p_makeSeparator() )

		# Create and add the new box
		self._boxes.append( DTBox() )
		# Add the item to the new box
		self._boxes[-1].append( child )
		# Add the item to the item list
		self._items.append( child )

		# Add the box to the container
		super( DTWrappedHBoxWithSeparators, self ).append( self._boxes[-1], padding )


	def extend(self, children):
		if len( self._boxes ) > 0:
			# Add a separator to the last box
			self._boxes[-1].append( self._p_makeSeparator() )

		# Create the new boxes
		boxes = [ DTBox()   for child in children ]
		# Add the items to the new boxes
		for child, box in zip( children, boxes ):
			box.append( child )
		# Add separators too all but the last
		for box in boxes[:-1]:
			box.append( self._p_makeSeparator() )
		# Add the boxes and items to the box and item lists
		self._boxes.extend( boxes )
		self._items.extend( children )

		# Add the new boxes to the container
		super( DTWrappedHBoxWithSeparators, self ).extend( boxes )


	def insert(self, index, child, padding=None):
		if index == len( self._items ):
			self.append( item, padding )
		else:
			box = DTBox()
			box.append( child )
			box.append( self._p_makeSeparator() )

			self._boxes.insert( index, box )
			self._items.insert( index, box )

			super( DTWrappedHBoxWithSeparators, self ).insert( index, box )


	def remove(self, child):
		index = self._items.index( child )
		del self[index]
		
		
		
	def _p_makeSeparator(self):
		if isinstance( self._separatorFactory, str )  or  isinstance( self._separatorFactory, unicode ):
			return DTLabel( self._separatorFactory )
		else:
			return self._separatorFactory()








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

	line = DTWrappedHBoxWithSeparators()
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
