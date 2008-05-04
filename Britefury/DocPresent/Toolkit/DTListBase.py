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

class DTListBase (object):
	layoutClass = None
	
	
	class _ItemBox (object):
		def __init__(self, l, item):
			super( DTListBase._ItemBox, self ).__init__()
			self._l = l
			
			self._bBeginDelim = False
			self._bEndDelim = False
			self._bSep = False
			
			self._item = item
			self._container = None
			self.widget = item
			
			
		def setItem(self, item):
			self._item = item
			if self._container is not None:
				index = 0
				if self._bEndDelim:
					index = 1
				self._container[index] = item
			else:
				self.widget = item
			
		
		
		def setBBeginDelim(self, bBeginDelim):
			if self._l._beginDelimFactory is not None:
				if bBeginDelim != self._bBeginDelim:
					oldState = self._p_changeBegin()
					self._bBeginDelim = bBeginDelim
					self._p_changeEnd( oldState )
					
					if bBeginDelim:
						self._container.insert( 0, self._l._f_makeBeginDelim() )
					else:
						if self._container is not None:
							del self._container[0]
			
		def setBEndDelim(self, bEndDelim):
			if self._l._endDelimFactory is not None:
				if bEndDelim != self._bEndDelim:
					oldState = self._p_changeBegin()
					self._bEndDelim = bEndDelim
					self._p_changeEnd( oldState )
					
					if bEndDelim:
						self._container.append( self._l._f_makeEndDelim() )
					else:
						if self._container is not None:
							del self._container[-1]
			
			
		def setBSep(self, bSep):
			if self._l._separatorFactory is not None:
				if bSep != self._bSep:
					oldState = self._p_changeBegin()
					self._bSep = bSep
					self._p_changeEnd( oldState )
						
					if self._bBeginDelim:
						index = 2
					else:
						index = 1
					if bSep:
						self._container.insert( index, self._l._f_makeSep() )
					else:
						if self._container is not None:
							del self._container[index]

				
				
		def _p_changeBegin(self):
			return self._bBeginDelim  or  self._bEndDelim  or  self._bSep
		
		def _p_changeEnd(self, oldState):
			currentState = self._bBeginDelim  or  self._bEndDelim  or  self._bSep
			if oldState != currentState:
				if currentState:
					self._container = DTBox()
					self._container.append( self._item )
					self.widget = self._container
				else:
					self.widget = self._item
					self._container = None
				
			
		
			
			
		
			
			
	
	def __init__(self, beginDelimFactory='(', endDelimFactory=')', separatorFactory=','):
		super( DTListBase, self ).__init__()

		self._boxes = []
		self._items = []
		self._beginDelimFactory = beginDelimFactory
		self._endDelimFactory = endDelimFactory
		self._separatorFactory = separatorFactory
		


	def __getitem__(self, index):
		return self._items[index]

	def index(self, item):
		return self._items.index( item )

	def __setitem__(self, index, item):
		if isinstance( index, slice ):
			# Create the new boxes
			boxes = [ self._ItemBox( self, child )   for child in item ]

			self._boxes[index] = boxes
			self._items[index] = item

			if len( self._boxes ) > 0:
				self._boxes[0].setBBeginDelim( True )
				self._boxes[-1].setBEndDelim( True )
				self._boxes[-1].setBSep( False )
				for box in self._boxes[:-1]:
					self._boxes.setBSep( True )

			self._list__setitem__( index, [ box.widget   for box in boxes ] )
		else:
			self._boxes[index].setItem( item )
			self._items[index] = item

			self._list__setitem__( index, [ box.widget   for box in boxes ] )

			
	def __delitem__(self, index):
		self._f__delitem__( index )

		del self._boxes[index]
		del self._items[index]
		
		if len( self._boxes ) != 0:
			if index == 0:
				self._boxes[0].setBBeginDelim( True )
			elif index == -1  or  index == len( self ):
				self._boxes[index-1].setBEndDelim( True )
				self._boxes[index-1].setBSep( False )


	def append(self, child):
		if len( self._boxes ) > 0:
			# Add a separator to, and remove the end delimiter from the last box
			self._boxes[-1].setBSep( True )
			self._boxes[-1].setBEndDelim( False )

		# Create and add the new box
		box = self._ItemBox( child )
		self._boxes.append( box )
		# Set delimter state
		box.setBEndDelim( True )
		
		# Add the item to the item list
		self._items.append( child )

		# Add the box to the container
		self._list_append( self._boxes[-1].widget, padding )


	def extend(self, children):
		if len( self._boxes ) > 0:
			# Add a separator to, and remove the end delimiter from the last box
			self._boxes[-1].setBSep( True )
			self._boxes[-1].setBEndDelim( False )

		# Create the new boxes
		boxes = [ self._ItemBox( child )   for child in children ]
		# Set separator and delimiter state
		if len( boxes ) > 0:
			for box in boxes[:-1]:
				box.setBSep( True )
			boxes[-1].setBEndDelim( True )
			
		# Add the boxes and items to the box and item lists
		self._boxes.extend( boxes )
		self._items.extend( children )
			
		# Add the new boxes to the container
		self._list_extend( boxes )


	def insert(self, index, child):
		if index == len( self._items ):
			self.append( item, child )
		else:
			# Make the box
			box = self._ItemBox( child )
			
			# Set the separator and delimiter flags
			box.setBSep( True )
			if index == 0:
				box.setBBeginDelim( True )

			# Perform the insertion
			self._boxes.insert( index, box )
			self._items.insert( index, box )

			self._f_insert( index, box )


	def remove(self, child):
		index = self._items.index( child )
		del self[index]
		
		
		
	def _p_makeSeparator(self):
		if isinstance( self._separatorFactory, str )  or  isinstance( self._separatorFactory, unicode ):
			return DTLabel( self._separatorFactory )
		else:
			return self._separatorFactory()
		
		
	def _p_makeChildBox(self):
		return DTBox( alignment=DTBox.ALIGN_BASELINES )








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
