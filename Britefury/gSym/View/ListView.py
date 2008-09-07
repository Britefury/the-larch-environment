##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
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
				elements.append( c.getElementNoRefresh() )
			elif isinstance( c, Element ):
				elements.append( c )
			else:
				raise TypeError, 'ListViewLayout._getContentElements: could not process child of type %s'  %  ( type( c ).__name__, )
		return elements
	
	
	
	
class ParagraphListViewLayout (ListViewLayout):
	def __init__(self, paragraphStyleSheet, spacingFactory, lineBreakPriority):
		super( ParagraphListViewLayout, self ).__init__()
		self._paragraphStyleSheet = paragraphStyleSheet
		self._spacingFactory = spacingFactory
		self._lineBreakPriority = lineBreakPriority
		
	
	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		# Produce contents container
		paragraphElem = ParagraphElement( self._paragraphStyleSheet )
		
		childElements = []
		
		if beginDelim is not None:
			childElements.append( _listViewCoerce( beginDelim ) )
			
		if len( contents ) > 0:
			contentElements = self._getContentElements( contents, xs )
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
			
		return paragraphElem


		
		
		
class HorizontalListViewLayout (ListViewLayout):
	def __init__(self, hboxStyleSheet, spacingFactory):
		super( HorizontalListViewLayout, self ).__init__()
		self._hboxStyleSheet = hboxStyleSheet
		self._spacingFactory = spacingFactory


	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		# Produce contents container
		hboxElem = HBoxElement( self._hboxStyleSheet )
		
		childElements = []

		if beginDelim is not None:
			childElements.append( _listViewCoerce( beginDelim ) )

		if len( contents ) > 0:
			contentElements = self._getContentElements( contents, xs )
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
			
		return hboxElem


	
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
			
			
		if len( contents ) <= 1:
			contentElements = self._getContentElements( contents, xs )
			contentElements = [ beginDelim ] + contentWidgets   if beginDelim is not None   else contentWidgets
			contentElements = contentWidgets + [ endDelim ]   if endDelim is not None   else contentWidgets
			singleLine = ParagraphElement( self._lineParagraphStyleSheet )
			singleLine.setChildren( contentElements )
			return singleLine
		elif len( contents ) >= 2:
			contentElements = self._getContentElements( contents, xs )
			
			first = ParagraphElement( self._lineParagraphStyleSheet )
			if beginDelim is not None  or  separatorFactory is not None:
				firstChildren = []
				if beginDelim is not None:
					firstChildren.append( beginDelim )
				firstChildren.append( contentElements[0] )
				if separatorFactory is not None:
					firstChildren.append( separatorFactory() )
				newLine = WhitespaceElement( '\n' )
				firstChildren.append( newLine )
				first.setChildren( firstChildren )
			else:
				firstChildren.append( contentElements[0] )
				firstChildren.append( newLine )
			first.setChildren( firstChildren )
				
				
			def _restChild(c, bLast):
				elem = ParagraphElement( self._lineParagraphStyleSheet )
				newLine = WhitespaceElement( '\n' )
				if separatorFactory is not None  and  not bLast:
					elem.setChildren( [ c, separatorFactory(), newLine ] )
				else:
					elem.setChildren( [ c, newLine ] )
				return elem

			middle = [ self._indent( _restChild( c, False ) )   for c in contentElements[1:-1] ]
			
			last = _restChild( contentElements[-1], True )
			
			if endDelim is not None:
				end = [ endDelim ]
			else:
				end = []
			
			vbox = VBoxElement( self._vboxStyleSheet )
			vbox.setChildren( [ first ]  +  middle  +  [ last ]  +  end )
			
			return vbox

		
	
	
	
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
		# Produce contents container
		vboxElem = VBoxElement( self._vboxStyleSheet )
		
		childElements = []

		if beginDelim is not None:
			childElements.append( _listViewCoerce( beginDelim ) )

		if len( contents ) > 0:
			contentElements = self._getContentElements( contents, xs )
			for c in contentElements[:-1]:
				x = ParagraphElement( self._lineParagraphStyleSheet )
				newLine = WhitespaceElement( '\n' )
				if separatorFactory is not None:
					x.setChildren( [ c, separatorFactory(), newLine ] )
				else:
					x.setChildren( [ c, newLine ] )
				x = self._indent( x )
				childElements.append( x )
			childElements.append( self._indent( contentElements[-1] ) )

		if endDelim is not None:
			childElements.append( _listViewCoerce( endDelim ) )

		vboxElem.setChildren( childElements )
			
		return vboxElem

	
	

	

		

def listView(xs, layout, beginDelim, endDelim, separatorFactory, contents):
	return layout.layoutContents( xs, contents, beginDelim, endDelim, separatorFactory )






