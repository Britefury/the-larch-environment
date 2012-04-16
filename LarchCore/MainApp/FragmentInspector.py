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
from BritefuryJ.LSpace.Interactor import HoverElementInteractor, PushElementInteractor
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.StyleSheet import *
from BritefuryJ.Controls import *
from BritefuryJ.Util import TypeUtils

from BritefuryJ.ObjectPresentation import PresentationStateListenerList

from BritefuryJ.DefaultPerspective import DefaultPerspective

from LarchCore.PythonConsole import Console


_fragSelectorEntryBorder = SolidBorder( 1.0, 3.0, 6.0, 6.0, Color( 0.8, 0.8, 0.8 ), None, Color( 0.5, 0.5, 0.5 ), Color( 0.9, 0.9, 0.9 ) )

_fragContentHighlighterPainter = FilledOutlinePainter( Color( 0.0, 1.0, 0.0, 0.1 ), Color( 0.0, 0.5, 0.0, 0.5 ) )
_objectKindStyleJava = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_objectKindStylePython = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) )
_objectKindStyleDocModel = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.5, 0.5, 0.5 ) ) )
_consoleStyle = StyleSheet.style( Primitive.editable( True ) )

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


class _FragmentSelectorEntryInteractor (HoverElementInteractor, PushElementInteractor):
	def __init__(self, selectorEntry, fragmentElement):
		self._selectorEntry = selectorEntry
		self._fragmentElement = fragmentElement


	def pointerEnter(self, element, event):
		self._fragmentElement.addPainter( _FragmentContentHighlighter.instance )
		self._fragmentElement.queueFullRedraw()

	def pointerLeave(self, element, event):
		self._fragmentElement.removePainter( _FragmentContentHighlighter.instance )
		self._fragmentElement.queueFullRedraw()


	def buttonPress(self, element, event):
		if event.button == 1:
			self._selectorEntry._onClick()
			return True
		return False

	def buttonRelease(self, element, event):
		pass


class _FragmentSelectorEntry (object):
	def __init__(self, selector, fragment):
		self._listeners = None

		self._selector = selector
		self._fragment = fragment
		self._interactor = _FragmentSelectorEntryInteractor( self, fragment.getFragmentElement() )

		model = fragment.model
		modelTypeName = TypeUtils.nameOfTypeOf( model )
		self._name = modelTypeName.rpartition( '.' )[2]
		self._kind = TypeUtils.getKindOfObject( model )


	def _onClick(self):
		self._selector._onFragmentSelected( self._fragment )


	def __present__(self, fragment, inheritedState):
		self._listeners = PresentationStateListenerList.addListener( self._listeners, fragment )
		name = Label( self._name )
		kind = _objectKindMap[self._kind]
		contents = Column( [ name, Spacer( 0.0, 2.0 ), kind.padX( 10.0, 0.0 ) ] )
		return _fragSelectorEntryBorder.surround( contents ).withElementInteractor( self._interactor )




class _FragmentSelector (object):
	def __init__(self, inspector, tipFragment):
		self._listeners = None

		self._inspector = inspector
		self._tipFragment = tipFragment
		self._fragments = []
		f = tipFragment
		while f is not None:
			self._fragments.insert( 0, f )
			f = f.getParent()

		self._entries = [ _FragmentSelectorEntry( self, f )   for f in self._fragments ]


	def _onFragmentSelected(self, fragment):
		self._inspector._onFragmentSelected( fragment )


	def __present__(self, fragment, inheritedState):
		self._listeners = PresentationStateListenerList.addListener( self._listeners, fragment )
		title = Label( 'Please choose a fragment:' )
		xs = []
		first = True
		for e in self._entries:
			if not first:
				xs.append( LineBreak() )
			xs.append( e )
			first = False
		entriesList = Paragraph( xs ).alignHPack()
		return Column( [ title, entriesList ] )




class _FragmentInspectorMain (object):
	def __init__(self, tipFragment):
		self._listeners = None

		self._tipFragment = tipFragment
		self._content = _FragmentSelector( self, self._tipFragment )


	def _onFragmentSelected(self, fragment):
		console = Console.Console( '<popup_console>', False )
		console.assignVariable( 'm', fragment.model )
		self._content = ScrolledViewport( _consoleStyle( console ), 640.0, 480.0, True, True, None ).alignVTop()
		self._listeners = PresentationStateListenerList.onPresentationStateChanged( self._listeners, self )


	def __present__(self, fragment, inheritedState):
		self._listeners = PresentationStateListenerList.addListener( self._listeners, fragment )

		title = Heading3( 'Inspector' )

		body = Column( [ title, SpaceBin( 0.0, 600.0, True, self._content ) ] )

		return SpaceBin( 800.0, 0.0, body ).alignHExpand()


def inspectFragment(fragment, sourceElement, triggeringEvent):
	inspector = _FragmentInspectorMain( fragment )
	inspector = DefaultPerspective.instance( inspector )

	BubblePopup.popupInBubbleAdjacentToMouse( inspector, sourceElement, Anchor.TOP, True, True )
	return True
