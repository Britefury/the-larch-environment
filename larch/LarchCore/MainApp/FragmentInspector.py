##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2012.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import *
from BritefuryJ.LSpace import Anchor, ElementPainter, PageController
from BritefuryJ.LSpace.Interactor import HoverElementInteractor, PushElementInteractor
from BritefuryJ.Pres import LazyPres, Pres
from BritefuryJ.Pres.Primitive import *
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
_consoleStyle = StyleSheet.style( Primitive.editable( True ), Primitive.selectable( True ) )
_inspectorStyle = StyleSheet.style( Primitive.editable( False ), Primitive.selectable( False ) )

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
		if self._fragmentElement is not None:
			self._fragmentElement.addPainter( _FragmentContentHighlighter.instance )
			self._fragmentElement.queueFullRedraw()

	def pointerLeave(self, element, event):
		if self._fragmentElement is not None:
			self._fragmentElement.removePainter( _FragmentContentHighlighter.instance )
			self._fragmentElement.queueFullRedraw()


	def buttonPress(self, element, event):
		if event.button == 1:
			if self._fragmentElement is not None:
				self._fragmentElement.removePainter( _FragmentContentHighlighter.instance )
				self._fragmentElement.queueFullRedraw()
				self._fragmentElement = None
			self._selectorEntry._onClick()
			element.closeContainingPopupChain()
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
	def __init__(self, tipFragment):
		self._listeners = None

		self._tipFragment = tipFragment
		self._fragments = []
		f = tipFragment
		while f is not None:
			self._fragments.insert( 0, f )
			f = f.getParent()

		self._entries = [ _FragmentSelectorEntry( self, f )   for f in self._fragments ]


	def _onFragmentSelected(self, fragment):
		inspector = _FragmentInspector( fragment )
		location = fragment.view.browserContext.getLocationForObject( inspector )
		fragment.fragmentElement.rootElement.pageController.openLocation( location, PageController.OpenOperation.OPEN_IN_NEW_WINDOW )


	def __present__(self, fragment, inheritedState):
		self._listeners = PresentationStateListenerList.addListener( self._listeners, fragment )
		xs = []
		first = True
		for e in self._entries:
			if not first:
				xs.append( LineBreak() )
			xs.append( e )
			first = False
		entriesList = Paragraph( xs ).alignHPack()
		return entriesList



class _FragmentInspector (object):
	def __init__(self, fragment):
		self._fragment = fragment

		self._console = Console.Console( '<popup_console>', False )
		self._console.assignVariable( 'm', fragment.model )

		self._explorer = fragment.fragmentElement.treeExplorer()



	def __present__(self, fragment, inheritedState):
		def _explorerPres():
			return Pres.coerce( self._explorer )

		explorer = LazyPres( _explorerPres )

		tabs = [ [ Label( 'Console' ), _consoleStyle( self._console ).alignVTop() ], [ Label( 'Element explorer' ), explorer ] ]
		return TabbedBox( tabs, None ).alignVTop()



def inspectFragment(fragment, sourceElement, triggeringEvent):
	selector = _FragmentSelector( fragment )

	title = SectionHeading1( 'Choose a fragment:' )
	body = Section( title, selector )

	content = _inspectorStyle( SpaceBin( 800.0, 0.0, body ) ).alignHExpand()
	content = DefaultPerspective.instance( content )

	BubblePopup.popupInBubbleAdjacentToMouse( content, sourceElement, Anchor.TOP, True, True )
	return True