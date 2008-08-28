##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocView.DVNode import DVNode

from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *




def _listViewCoerce(x):
	if isinstance( x, Element ):
		return x
	else:
		return x()


def listViewStrToElementFactory(styleSheet, x):
	return TextElement( styleSheet, x )
	
	


class ListViewLayout (object):
	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		pass
	
	def _getContentElements(self, contents, xs):
		elements = []
		for c in contents:
			if isinstance( c, DVNode ):
				c.refresh()
				elements.append( c.getElement() )
			elif isinstance( c, Element ):
				elements.append( c )
			else:
				raise TypeError, 'ListViewLayout._getContentElements: could not process child of type %s'  %  ( type( c ).__name__, )
		return elements
		
	
	
	def _buildRefreshCell(self, refreshFunction):
		cell = Cell()
		cell.function = refreshFunction
		return cell
	
	
	
	
class ParagraphListViewLayout (ListViewLayout):
	def __init__(self, paragraphStyleSheet, spacingFactory, lineBreakPriority):
		super( ParagraphListViewLayout, self ).__init__()
		self._paragraphStyleSheet = paragraphStyleSheet
		self._spacingFactory = spacingFactory
		self._lineBreakPriority = lineBreakPriority
		
	
	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		def _refresh():
			if len( contents ) > 0:
				contentElements = self._getContentElements( contents, xs )
				childElements = []
				if beginDelim is not None:
					childElements.append( _listViewCoerce( beginDelim ) )
				for c in contentElements[:-1]:
					childElements.append( c )
					if separatorFactory is not None:
						childElements.append( separatorFactory() )
					lineBreak = LineBreakElement( self._lineBreakPriority )
					if self._spacingFactory is not None:
						lineBreak.setChild( self._spacingFactory() )
					childElements.append( lineBreak )
				childElements.append( contentElements[-1] )
				if endDelim is not None:
					childElements.append( _listViewCoerce( endDelim ) )
				paragraphElem.setChildren( childElements )
		refreshCell = self._buildRefreshCell( _refresh )
			
		# Produce contents container
		paragraphElem = ParagraphElement( self._paragraphStyleSheet )
		
		return paragraphElem, refreshCell


		
		
		
class HorizontalListViewLayout (ListViewLayout):
	def __init__(self, hboxStyleSheet, spacingFactory):
		super( HorizontalListViewLayout, self ).__init__()
		self._hboxStyleSheet = hboxStyleSheet
		self._spacingFactory = spacingFactory


	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		def _refresh():
			if len( contents ) > 0:
				contentElements = self._getContentElements( contents, xs )
				childElements = []
				if beginDelim is not None:
					childElements.append( _listViewCoerce( beginDelim ) )
				for c in contentElements[:-1]:
					childElements.append( c )
					if separatorFactory is not None:
						childElements.append( separatorFactory() )
					if self._spacingFactory is not None:
						childElements.append( self._spacingFactory() )
				childElements.append( contentElements[-1] )
				if endDelim is not None:
					childElements.append( _listViewCoerce( endDelim ) )
				hboxElem.setChildren( childElements )
		refreshCell = self._buildRefreshCell( _refresh )
			
		# Produce contents container
		hboxElem = HBoxElement( self._hboxStyleSheet )
		
		return hboxElem, refreshCell


	
class VerticalInlineListViewLayout (ListViewLayout):
	def __init__(self, vboxStyleSheet, lineParagraphStyleSheet, indentation=0.0):
		super( VerticalInlineListViewLayout, self ).__init__()
		self._vboxStyleSheet = vboxStyleSheet
		self._lineParagraphStyleSheet = lineParagraphStyleSheet
		self._borderStyleSheet = BorderStyleSheet( indentation, 0.0, 0.0, 0.0 )   if  indentation != 0.0   else   None
		
		
	def _indent(self, x):
		if self._borderStyleSheet is not None:
			b = BorderElement( self._borderStyleSheet )
			b.setChild( x )
			return b
		else:
			return x


	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		if beginDelim is not None:
			beginDelim = _listViewCoerce( beginDelim )
		
		if endDelim is not None:
			endDelim = _listViewCoerce( endDelim )
			
			
		bin = BinElement( ContainerStyleSheet() )
			
			
		def _refresh():
			if len( contents ) <= 1:
				contentElements = self._getContentElements( contents, xs )
				contentElements = [ beginDelim ] + contentWidgets   if beginDelim is not None   else contentWidgets
				contentElements = contentWidgets + [ endDelim ]   if endDelim is not None   else contentWidgets
				singleLine = ParagraphElement( self._lineParagraphStyleSheet )
				singleLine.setChildren( contentElements )
				bin.setChild( singleLine )
			elif len( contents ) >= 2:
				contentElements = self._getContentElements( contents, xs )
				
				if beginDelim is not None  or  separatorFactory is not None:
					first = ParagraphElement( self._lineParagraphStyleSheet )
					firstChildren = []
					if beginDelim is not None:
						firstChildren.append( beginDelim )
					firstChildren.append( contentElements[0] )
					if separatorFactory is not None:
						firstChildren.append( separatorFactory() )
					first.setChildren( firstChildren )
				else:
					first = contentElements[0]
					
				last = self._indent( contentElements[-1] )
				
				if endDelim is not None:
					end = [ endDelim ]
				else:
					end = []
				
					
				def _middleChild(c):
					if separatorFactory is not None:
						elem = ParagraphElement( self._lineParagraphStyleSheet )
						elem.setChildren( [ c, separatorFactory() ] )
						return elem
					else:
						return c

				middle = [ self._indent( _middleChild( c ) )   for c in contentElements[1:-1] ]
				
				vbox = VBoxElement( self._vboxStyleSheet )
				vbox.setChildren( [ first ]  +  middle  +  [ last ]  +  end )
				
				bin.setChild( vbox )
		refreshCell = self._buildRefreshCell( _refresh )
		
		return bin, refreshCell

	
	
	
class VerticalListViewLayout (ListViewLayout):
	def __init__(self, vboxStyleSheet, lineParagraphStyleSheet, indentation=0.0):
		super( VerticalListViewLayout, self ).__init__()
		self._vboxStyleSheet = vboxStyleSheet
		self._lineParagraphStyleSheet = lineParagraphStyleSheet
		self._borderStyleSheet = BorderStyleSheet( indentation, 0.0, 0.0, 0.0 )   if  indentation != 0.0   else   None
		
		
	def _indent(self, x):
		if self._borderStyleSheet is not None:
			b = BorderElement( self._borderStyleSheet )
			b.setChild( x )
			return b
		else:
			return x
		

	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		def _refresh():
			if len( contents ) > 0:
				contentElements = self._getContentElements( contents, xs )
				childElements = []
				if beginDelim is not None:
					childElements.append( _listViewCoerce( beginDelim ) )
				for c in contentElements[:-1]:
					if separatorFactory is not None:
						x = ParagraphElement( self._lineParagraphStyleSheet )
						x.setChildren( [ c, separatorFactory() ] )
					else:
						x = c
					x = self._indent( x )
					childElements.append( x )
				childElements.append( self._indent( contentElements[-1] ) )
				if endDelim is not None:
					childElements.append( _listViewCoerce( endDelim ) )
				vboxElem.setChildren( childElements )
		refreshCell = self._buildRefreshCell( _refresh )
			
		# Produce contents container
		vboxElem = VBoxElement( self._vboxStyleSheet )
		
		return vboxElem, refreshCell

	
	

	

		

def listView(xs, layout, beginDelim, endDelim, separatorFactory, contents):
	return layout.layoutContents( xs, contents, beginDelim, endDelim, separatorFactory )






