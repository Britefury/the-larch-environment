##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2012.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import *
from BritefuryJ.LSpace import Anchor, ElementPainter
from BritefuryJ.LSpace.Interactor import HoverElementInteractor
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.StyleSheet import *
from BritefuryJ.Controls import *
from BritefuryJ.Util import TypeUtils

from BritefuryJ.ObjectPresentation import PresentationStateListenerList

from BritefuryJ.DefaultPerspective import DefaultPerspective


_fragSelectorEntryBorder = SolidBorder( 1.0, 3.0, 6.0, 6.0, Color( 0.8, 0.8, 0.8 ), None, Color( 0.5, 0.5, 0.5 ), Color( 0.9, 0.9, 0.9 ) )
_fragSelectorEntrySelectedBorder = SolidBorder( 1.0, 3.0, 6.0, 6.0, Color( 0.8, 0.8, 0.8 ), None, Color( 0.5, 0.5, 0.5 ), Color( 0.9, 0.9, 0.9 ) )

_fragContentHighlighterPainter = FilledOutlinePainter( Color( 0.0, 1.0, 0.0, 0.1 ), Color( 0.0, 0.5, 0.0, 0.5 ) )
_objectKindStyleJava = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_objectKindStylePython = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) )
_objectKindStyleDocModel = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.5, 0.5, 0.5 ) ) )

_objectKindJava = _objectKindStyleJava( Label( 'Java' ) )
_objectKindPython = _objectKindStylePython( Label( 'Python' ) )
_objectKindDocModel = _objectKindStyleDocModel( Label( 'DocModel' ) )

_objectKindMap = {
	TypeUtils.ObjectKind.JAVA : _objectKindJava,
	TypeUtils.ObjectKind.PYTHON : _objectKindPython,
	TypeUtils.ObjectKind.DOCMODEL : _objectKindDocModel,
}



class _FragmentContentHighlighter (ElementPainter):
	def drawBackground(self, element, graphics):
		pass

	def draw(self, element, graphics):
		_fragContentHighlighterPainter.drawShapes( graphics, element.getShapes() )

_FragmentContentHighlighter.instance = _FragmentContentHighlighter()


class _FragmentSelectorEntryInteractor (HoverElementInteractor):
	def __init__(self, fragmentElement):
		self._fragmentElement = fragmentElement


	def pointerEnter(self, element, event):
		self._fragmentElement.addPainter( _FragmentContentHighlighter.instance )
		self._fragmentElement.queueFullRedraw()

	def pointerLeave(self, element, event):
		self._fragmentElement.removePainter( _FragmentContentHighlighter.instance )
		self._fragmentElement.queueFullRedraw()


class _FragmentSelectorEntry (object):
	def __init__(self, fragment):
		self._listeners = None

		self._fragment = fragment
		self._interactor = _FragmentSelectorEntryInteractor( fragment.getFragmentElement() )

		model = fragment.model
		modelTypeName = TypeUtils.nameOfTypeOf( model )
		self._name = modelTypeName.rpartition( '.' )[2]
		self._kind = TypeUtils.getKindOfObject( model )

		self._selected = False


	def select(self):
		self._selected = True
		PresentationStateListenerList.onPresentationStateChanged( self._listeners, self )

	def unselect(self):
		self._selected = False
		PresentationStateListenerList.onPresentationStateChanged( self._listeners, self )


	def __present__(self, fragment, inheritedState):
		self._listeners = PresentationStateListenerList.addListener( self._listeners, fragment )
		border = _fragSelectorEntrySelectedBorder   if self._selected   else _fragSelectorEntryBorder
		name = Label( self._name )
		kind = _objectKindMap[self._kind]
		contents = Column( [ name, Spacer( 0.0, 2.0 ), kind.padX( 10.0, 0.0 ) ] )
		return border.surround( contents ).withElementInteractor( self._interactor )




class _FragmentSelector (object):
	def __init__(self, tipFragment):
		self._listeners = None

		self._tipFragment = tipFragment
		self._fragments = []
		f = tipFragment
		while f is not None:
			self._fragments.insert( 0, f )
			f = f.getParent()

		self._entries = [ _FragmentSelectorEntry( f )   for f in self._fragments ]
		self._selection = 0


	def _setSelection(self, selection):
		self._entries[self._selection].unselect()
		self._selection = selection



	def __present__(self, fragment, inheritedState):
		self._listeners = PresentationStateListenerList.addListener( self._listeners, fragment )
		entriesList = Row( self._entries )
		return ScrolledViewport( entriesList, 640.0, 0.0, True, False, None )




class _FragmentInspector (object):
	def __init__(self, fragment):
		self._fragment = fragment


	def __present__(self, fragment, inheritedState):
		title = Label( 'Inspector' )
		selector = _FragmentSelector( self._fragment )
		return Column( [ title, selector ] )


def inspectFragment(fragment, sourceElement, triggeringEvent):
	inspector = _FragmentInspector( fragment )
	inspector = DefaultPerspective.instance( inspector )

	BubblePopup.popupInBubbleAdjacentToMouse( inspector, sourceElement, Anchor.TOP, True, True )
	return True
