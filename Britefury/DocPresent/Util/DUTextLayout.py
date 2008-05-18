##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )

import cairo
import pango
import pangocairo

from Britefury.Math.Math import Point2, Vector2, Colour3f




class DUTextLayout (object):
	"""
	Text layout
	
	Manages a Pango layout, for rendering text.
	
	A text layout must be initialised (by calling the initialise() method) in order for text to be displayed.
	
	
	Attributes to be set:
	   evRequestResize    -    callback indicating that the layout size has changed
	   evRequestRedraw   -    callback indicating that the layout contents have changed, but size would be the same
	"""
	def __init__(self, text='', bUseMarkup=False, font='Sans 11', colour=None):
		"""
		Constructor
		  text - the text to be displayed by the layout (default: '')
		  bUseMarkup - whether or not Pango markup should be used to display text (default: False)
		  font - the font used to display the text (default: 'Sans 11')
		  colour - the colour of the text (default: None)
		"""
		super( DUTextLayout, self ).__init__()
		
		
		self._text = text
		self._bUseMarkup = bUseMarkup
		
		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		
		self._colour = colour
		
		self._context = None
		self._pangoLayout = None
		
		self._size = None
		self._baseline = None
		
		self.evRequestResize = None
		self.evRequestRedraw = None
		
		
		
	
	def setText(self, text):
		"""
		setText( text )
		  text - the text to display
		
		Sets the text displayed by this layout to @text
		"""
		if text != self._text:
			self._text = text
			self._p_refreshText()
			self._p_requestResize()
	
	def getText(self):
		"""
		getText()  ->  text
		
		Get the text displayed by this layout
		"""
		return self._text
	
	
	def useMarkup(self):
		"""
		useMarkup()
		
		Enable markup; display the text using Pango markup
		"""
		self.setUseMarkup( True )
		
	def usePlaintext(self):
		"""
		usePlaintext()
		
		Disable markup; display the text as plain text
		"""
		self.setUseMarkup( False )
		
	def setUseMarkup(self, bUseMarkup):
		"""
		setUseMarkup( bUseMarkup )
		  bUseMarkup - a flag indicating if markup is enabled
		  
		Sets whether or not Pango markup is used to display the text
		"""
		if bUseMarkup != self._bUseMarkup:
			self._bUseMarkup = bUseMarkup
			self._p_refreshText()
			self._p_requestResize()
				
	def getUseMarkup(self):
		"""
		getUseMarkup()  ->  boolean indicating if markup is enabled
		
		Gets a flag the indicates if markup is enabled
		"""
		return self._bUseMarkup
	
	
	def setFont(self, font):
		"""
		setFont( font )
		  font - a string representing the font description
		
		Sets the font used to display the text
		"""
		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		if self._pangoLayout is not None:
			self._pangoLayout.set_font_description( self._fontDescription )
		self._p_requestResize()
		
	def getFont(self):
		"""
		getFont()  ->  font description string
		
		Gets the font description string
		"""
		return self._fontString
		
			
	def setColour(self, colour):
		"""
		setColour( colour )
		  colour - The colour of the text; either a Colour3f, or None, in which case the colour is not set by draw()
		
		Sets the colour of the text
		"""
		self._colour = colour
		self._p_requestRedraw()
		
	def getColour(self):
		"""
		getColour()  ->  the colour of the text  (a Colour3f)
		
		Gets the colour of the text
		"""
		return self._colour
	
	
	
	
	
	def initialise(self, context):
		"""
		initialise( context )
		  context - the cairo context used to initialise the text layout
		
		Initialises the text layout; this must be done in order for the text to be displayed. @context is a cairo context.
		"""
		if context is not None  and  context is not self._context:
			self._context = context
			self._pangoLayout = context.create_layout()
			self._p_refreshText()
			self._pangoLayout.set_font_description( self._fontDescription )
			
			
	def update(self, context):
		"""
		update( context )
		  context - the cairo context used to update the text layout
		
		Gets information from @context (scale, etc) and updates the text layout
		"""
		context.update_layout( self._pangoLayout )
			
			
	def draw(self, context):
		"""
		draw( context )
		  context - the context on which to draw the text
		
		Draws the text represented by this layout
		"""
		if self._pangoLayout is not None:
			if self._colour is not None:
				context.set_source_rgb( self._colour.r, self._colour.g, self._colour.b )
			context.update_layout( self._pangoLayout )
			context.show_layout( self._pangoLayout )
			
	
	def getSize(self):
		"""
		getSize() -> size (a Vector2)
		
		Gets the size required to display the text (in pixels)
		"""
		if self._size is None:
			if self._pangoLayout is not None:
				s = self._pangoLayout.get_pixel_size()
				self._size = Vector2( s[0], s[1] )
			else:
				self._size = Vector2( 0.0, 0.0 )
		return self._size
		
	
	def getBaseline(self):
		"""
		getBaseline() -> baseline (a float)
		
		Gets the position of the baseline (in pixels) relative to the bottom of the text
		"""
		if self._baseline is None:
			if self._pangoLayout is not None:
				self._baseline = self._pangoLayout.get_iter().get_baseline()  /  float( pango.SCALE )
			else:
				self._baseline = 0.0
		return self._baseline

		
	
	
	def getCharacterIndexAt(self, point):
		"""
		getCharacterIndexAt( point )  ->  character index (int)
		  point - the query position (either a Vector2 or Point2)
		
		Gets the index of the character under @point
		"""
		self._p_refresh()
		if self._pangoLayout is not None:
			index, trailing = self._pangoLayout.xy_to_index( int( point.x * pango.SCALE ), int( point.y * pango.SCALE ) )
			return index
		else:
			raise ValueError, 'not realised'
		
	
	def getCharacterIndexAndSubIndexAt(self, point):
		"""
		getCharacterIndexAndSubIndexAt( point )  ->  character index (int),  index within character (int)
		  point - the query position (either a Vector2 or Point2)
		
		Gets the index and sub-index (index within character) of the character under @point
		The sub-index is 0 at the beginning of the glyph, and non-zero (1) at the end; this can be used to determine which side of the centre-line
		of the character @point lies on.
		"""


	def getCursorIndexAt(self, point):
		"""
		getCursorIndexAt( point )  ->  cursor index (int)
		  point - the query position (either a Vector2 or Point2)
		
		Gets the index of the cursor position nearest to @point
		"""
		self._p_refresh()
		if self._pangoLayout is not None:
			index, trailing = self._pangoLayout.xy_to_index( int( point.x * pango.SCALE ), int( point.y * pango.SCALE ) )
			return index + trailing
		else:
			raise ValueError, 'not realised'
		
		
	def getCharacterRectangle(self, charIndex):
		"""
		getCharacterRectangle( charIndex )  ->  position (Point2), size (Vector2)
		  charIndex - the index of the character
		
		Gets the rectangle that surrounds the character at index @charIndex
		"""
		charRect = self._pangoLayout.index_to_pos( charIndex )
		return Point2( float( charRect[0] ) / pango.SCALE,  float( charRect[1] ) / pango.SCALE ),    Vector2( float( charRect[2] ) / pango.SCALE,  float( charRect[3] ) / pango.SCALE )
		
		

	
	
	def _p_refreshText(self):
		if self._pangoLayout is not None:
			if self._bUseMarkup:
				self._pangoLayout.set_markup( self._text )
			else:
				self._pangoLayout.set_text( self._text )
				
				
	def _p_refresh(self):
		self._p_refreshText()
			
		
		
	def _p_requestResize(self):
		self._size = None
		self._baseline = None
		if self.evRequestResize is not None:
			self.evRequestResize()
		
	def _p_requestRedraw(self):
		if self.evRequestRedraw is not None:
			self.evRequestRedraw()
		
		
