##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from Britefury.Util.Abstract import abstractmethod

from BritefuryJ.Controls import *

from BritefuryJ.DocPresent import ElementPainter, PageController

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.Pres.Primitive import *
from BritefuryJ.DocPresent.Interactor import *
from BritefuryJ.DocPresent.Input import Modifier


from BritefuryJ.Projection import Subject



class _State (object):
	pass



class _SlidePage (object):
	class _Interactor (PushElementInteractor):
		def __init__(self, page):
			self._page = page
		
		def buttonPress(self, element, event):
			return event.getButton() == 1
		
		def buttonRelease(self, element, event):
			pageController = element.getRootElement().getPageController()
			if ( event.getPointer().getModifiers() & Modifier.CTRL )  !=  0:
				pageController.openLocation( self._page._prevLoc, PageController.OpenOperation.OPEN_IN_CURRENT_TAB )
			else:
				pageController.openLocation( self._page._nextLoc, PageController.OpenOperation.OPEN_IN_CURRENT_TAB )
	
	
	def __init__(self, slide, prevLoc, nextLoc):
		self._slide = slide
		self._prevLoc = prevLoc
		self._nextLoc = nextLoc
		
	def __present__(self, fragment, inheritedState):
		contents = self._slide.build()
		backg = Bin( contents ).withPainter( self._slide._background ).withElementInteractor( self._Interactor( self ) )
		return backg

	
	
class Slide (object):
	def __init__(self, background):
		self._background = background
	
	@abstractmethod
	def build(self):
		pass
	
	


class SlideBackground (ElementPainter):
	@abstractmethod
	def drawBackground(self, element, graphics):
		pass
	
	def draw(self, element, graphics):
		pass




class SlideLayout (object):
	__background__ = None

	@abstractmethod
	def __call__(self, *args, **kwargs):
		pass



class _SlideShowLink (object):
	def __init__(self, slideShow):
		self._slideShow = slideShow
		self._subject = _SlideShowSubject( self._slideShow )
		
	def __present__(self, fragment, inheritedState):
		location = fragment.getLocationForObject( self._subject )
		self._subject._baseLocation = location
		link = Hyperlink( 'Slide show', location + '[0]' )
		return link
	
	
class SlideShow (object):
	def __init__(self, slides):
		self._slides = slides

		
	def show(self):
		return _SlideShowLink( self )
		
	
	

		
class _SlideSubject (Subject):
	def __init__(self, slideShowSubject, slide, index):
		self._slideShowSubject = slideShowSubject
		self._slide = slide
		self._index = index
		
		
	def getTitle(self):
		return 'Slide %d'  %  ( self._index, )
	
	def getFocus(self):
		baseLocation = self._slideShowSubject._baseLocation
		numSlides = len( self._slideShowSubject._slideShow._slides )
		prevLoc = baseLocation + '[%d]' % ( self._index - 1 )   if self._index > 0   else  None
		nextLoc = baseLocation + '[%d]' % ( self._index + 1 )   if self._index < numSlides  else  None
		return _SlidePage( self._slide, prevLoc, nextLoc )
	


class _SlideShowSubject (object):
	def __init__(self, slideShow):
		self._slideShow = slideShow
		self._baseLocation = None
	
	def __getitem__(self, index):
		if index == len( self._slideShow._slides ):
			return _SlideSubject( self, _endSlide, index )
		else:
			return _SlideSubject( self, self._slideShow._slides[index], index )



