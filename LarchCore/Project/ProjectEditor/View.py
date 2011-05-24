##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import copy

from java.lang import Object

from java.awt import Color
from java.awt import BasicStroke

from java.awt.event import KeyEvent

from java.util.regex import Pattern

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import ObjectDispatchView


from Britefury.Util.NodeUtil import *

from BritefuryJ.IncrementalUnit import LiteralUnit

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.DocPresent.Interactor import ClickElementInteractor
from BritefuryJ.Controls import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *

from BritefuryJ.Projection import Perspective


from LarchCore.MainApp import DocumentManagement

from LarchCore.Project.ProjectRoot import ProjectRoot
from LarchCore.Project.ProjectPackage import ProjectPackage
from LarchCore.Project.ProjectPage import ProjectPage
from LarchCore.Project.ProjectEditor import ModuleFinder
from LarchCore.Project import PageData






def _ProjectViewState(location):
	return ( location, )




def _joinLocation(*xs):
	s = '.'.join( [ str( x )   for x in xs ] )
	return Location( s )



class ProjectDrag (Object):
	def __init__(self, source):
		self.source = source


def _getModelOfPackageOrPageNameElement(element):
	return element.getFragmentContext().getParent().getModel()

def _getModelOfProjectNameElement(element):
	return element.getFragmentContext().getModel()


def _dragSourceCreateSourceData(element, aspect):
	return ProjectDrag( _getModelOfPackageOrPageNameElement( element ) )


_dragSource = ObjectDndHandler.DragSource( ProjectDrag, ObjectDndHandler.ASPECT_NORMAL, _dragSourceCreateSourceData )



def _isChildOf(node, package):
	if node is package:
		return True

	if isinstance( package, ProjectPackage ):
		for child in package:
			if _isChildOf( node, child ):
				return True
	return False


def _performDrop(data, action, newParent, index):
	changeHistory = newParent.__change_history__
	changeHistory.freeze()
	source = copy.deepcopy( data.source )   if action == ObjectDndHandler.COPY   else data.source

	if action == ObjectDndHandler.MOVE:
		sourceParent = data.source.parent
		indexOfSource = sourceParent.indexOfById( data.source )
		del sourceParent[indexOfSource]
		if index is not None  and  newParent is sourceParent  and  index > indexOfSource:
			index -= 1

	if index is None:
		newParent.append( source )
	else:
		newParent.insert( index, source )
	changeHistory.thaw()




def _pageCanDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		model = _getModelOfPackageOrPageNameElement( element )
		return model is not data.source
	return False

def _pageDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPage = _getModelOfPackageOrPageNameElement( element )
		parent = destPage.parent
		index = parent.indexOfById( destPage )
		if targetPos.y > ( element.getHeight() * 0.5 ):
			index += 1

		_performDrop( data, action, parent, index )
		return True
	return False



def _getDestPackageAndIndex(element, targetPos):
	targetPackage = _getModelOfPackageOrPageNameElement( element )
	if targetPos.x > ( element.getWidth() * 0.5 ):
		return targetPackage, len( targetPackage )
	else:
		parent1 = targetPackage.parent
		parent2 = parent1.parent
		index = parent1.indexOfById( targetPackage )
		if targetPos.y > ( element.getHeight() * 0.5 ):
			index += 1
		return parent2, index


def _packageCanDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.COPY  or  not _isChildOf( data.source, destPackage ):
			return True
	return False

def _packageDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.MOVE  and _isChildOf( data.source, destPackage ):
			return False



		targetPackage = _getModelOfPackageOrPageNameElement( element )
		if targetPos.x > ( element.getWidth() * 0.5 ):
			_performDrop( data, action, targetPackage, None )
			return True
		else:
			parent = targetPackage.parent
			index = parent.indexOfById( targetPackage )
			if targetPos.y > ( element.getHeight() * 0.5 ):
				index += 1

			_performDrop( data, action, parent, index )
			return True
	return False


def _projectIndexDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		project = _getModelOfProjectNameElement( element )
		_performDrop( data, action, project, None )
		return True
	return False


_pageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _pageCanDrop, _pageDrop )
_packageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _packageCanDrop, _packageDrop )
_projectIndexDropDest = ObjectDndHandler.DropDest( ProjectDrag, _projectIndexDrop )




_controlsStyle = StyleSheet.instance.withAttr( Controls.bClosePopupOnActivate, True )
_projectControlsStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None ) ).withAttr( Primitive.rowSpacing, 30.0 )
_projectIndexNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.25, 0.5 ) ).withAttr( Primitive.fontBold, True ).withAttr( Primitive.fontSize, 14 )
_packageNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) ).withAttr( Primitive.fontBold, True ).withAttr( Primitive.fontSize, 14 )
_itemHoverHighlightStyle = StyleSheet.instance.withAttr( Primitive.hoverBackground, FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) )
_pythonPackageNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) )
_pythonPackageNameNotSetStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.5, 0.0, 0.0 ) )
_pythonPackageNameNotSetCommentStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.2, 0.2, 0.2 ) ).withAttr( Primitive.fontItalic, True )

_packageContentsIndentation = 20.0


_nameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' )
_pythonPackageNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*' )





class ProjectView (ObjectDispatchView):
	@ObjectDispatchMethod( ProjectRoot )
	def ProjectRoot(self, fragment, inheritedState, project):
		# Save and Save As
		def _onSave(link, buttonEvent):
			if document.hasFilename():
				document.save()
			else:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )

				DocumentManagement.promptSaveDocumentAs( fragment.getSubjectContext()['world'], link.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )


		def _onSaveAs(link, buttonEvent):
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )

			DocumentManagement.promptSaveDocumentAs( fragment.getSubjectContext()['world'], link.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )






		# Python package name
		class _PythonPackageNameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				project.pythonPackageName = text

			def onCancel(self, textEntry, originalText):
				pythonPackageUnit.setLiteralValue( pythonPackageNameLabelBox )


		class _PythonPackageNameInteractor (ClickElementInteractor):
			def testClickEvent(self, element, event):
				return event.getButton() == 1

			def buttonClicked(self, element, event):
				n = project.pythonPackageName
				n = n   if n is not None   else 'Untitled'
				textEntry = TextEntry( n, _PythonPackageNameListener(), _pythonPackageNameRegex, 'Please enter a valid dotted identifier' )
				textEntry.grabCaretOnRealise()
				pythonPackageUnit.setLiteralValue( textEntry )
				return True


		# Project index
		def _addPackage(menuItem):
			project.append( ProjectPackage( 'NewPackage' ) )

		def _addPage(page):
			project.append( page )

		def _projectIndexContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'New package', _addPackage ) )
			newPageMenu = PageData.newPageMenu( _addPage )
			importPageMenu = PageData.importPageMenu( element.getRootElement().getComponent(), _addPage )
			menu.add( MenuItem.menuItemWithLabel( 'New page', newPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( MenuItem.menuItemWithLabel( 'Import page', importPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			return True



		# Get some initial variables
		subjectContext = fragment.getSubjectContext()
		document = subjectContext['document']
		location = subjectContext['location']
		world = subjectContext['world']


		# Set location attribute of inheritedState
		inheritedState = inheritedState.withAttrs( location=location )

		# Link to home page, in link header bar
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		linkHeader = LinkHeaderBar( [ homeLink ] )

		# Title
		title = TitleBarWithSubtitle( 'DOCUMENT', document.getDocumentName() )


		# Controls for 'save' and 'save as'
		saveLink = Hyperlink( 'SAVE', _onSave )
		saveAsLink = Hyperlink( 'SAVE AS', _onSaveAs )
		controlsBox = Row( [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = _projectControlsStyle.applyTo( Border( controlsBox ) )



		# Python package name
		pythonPackageNamePrompt = Label( 'Root Python package name: ' )
		if project.pythonPackageName is None:
			pythonPackageNameLabel = _itemHoverHighlightStyle.applyTo( _pythonPackageNameNotSetStyle.applyTo( Label( '<not set>' ) ) )
			pythonPackageNameLabel = pythonPackageNameLabel.withElementInteractor( _PythonPackageNameInteractor() )
			comment = _pythonPackageNameNotSetCommentStyle.applyTo( Label( '(pages will not be importable until this is set)' ) )
			pythonPackageNameLabelBox = Row( [ pythonPackageNameLabel, Spacer( 25.0, 0.0 ), comment ] )
		else:
			pythonPackageNameLabel = _itemHoverHighlightStyle.applyTo( _pythonPackageNameStyle.applyTo( Label( project.pythonPackageName ) ) )
			pythonPackageNameLabel = pythonPackageNameLabel.withElementInteractor( _PythonPackageNameInteractor() )
			pythonPackageNameLabelBox = Row( [ pythonPackageNameLabel ] )
		pythonPackageUnit = LiteralUnit( pythonPackageNameLabelBox )
		pythonPackageNameBox = Row( [ pythonPackageNamePrompt, pythonPackageUnit.defaultPerspectiveValuePresInFragment() ] )
		
		
		# Clear imported modules
		def _onReset(button, event):
			modules = document.unloadAllImportedModules()
			print 'LarchCore.Project.ProjectEditor.View: unloaded modules:'
			for module in modules:
				print '\t' + module
		resetPrompt = Label( 'Reset (unload project modules): ' )
		resetButton = Button.buttonWithLabel( 'Reset', _onReset )
		reset = Row( [ resetPrompt, resetButton ] )


		# Project index
		indexHeader = Heading3( 'Project Index' )


		# Project contents
		items = InnerFragment.map( project[:], inheritedState )

		nameElement = _projectIndexNameStyle.applyTo( Label( 'Project' ) )
		nameBox = _itemHoverHighlightStyle.applyTo( nameElement.alignVCentre() )
		nameBox = nameBox.withContextMenuInteractor( _projectIndexContextMenuFactory )
		nameBox = nameBox.withDropDest( _projectIndexDropDest )

		itemsBox = Column( items ).alignHExpand()

		contentsView = Column( [ nameBox.alignHExpand(), itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )


		# Project index box
		projectIndex = Column( [ indexHeader, contentsView ] )


		# The page
		head = Head( [ linkHeader, title ] )
		body = Body( [ controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), pythonPackageNameBox, reset, projectIndex ] )

		return StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( Page( [ head, body ] ) )


	@ObjectDispatchMethod( ProjectPackage )
	def Package(self, fragment, inheritedState, package):
		def _addPackage(menuItem):
			package.append( ProjectPackage( 'NewPackage' ) )

		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				package.name = text

			def onCancel(self, textEntry, originalText):
				nameUnit.setLiteralValue( nameBox )

		def _onRename(menuItem):
			textEntry = TextEntry( package.name, _RenameListener(), _nameRegex, 'Please enter a valid identifier' )
			textEntry.grabCaretOnRealise()
			nameUnit.setLiteralValue( textEntry )
		
		def _onDelete(menuItem):
			if package.parent is not None:
				package.parent.remove( package )

		def _addPage(page):
			package.append( page )

		def _packageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'New package', _addPackage ) )
			newPageMenu = PageData.newPageMenu( _addPage )
			importPageMenu = PageData.importPageMenu( element.getRootElement().getComponent(), _addPage )
			menu.add( MenuItem.menuItemWithLabel( 'New page', newPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( MenuItem.menuItemWithLabel( 'Import page', importPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Delete', _onDelete ) )
			return True

		location = inheritedState['location']
		packageLocation = _joinLocation( location, package.name )

		items = InnerFragment.map( package[:], inheritedState.withAttrs( location=packageLocation ) )

		world = fragment.getSubjectContext()['world']

		icon = Image( 'LarchCore/Project/icons/Package.png' )
		nameElement = _packageNameStyle.applyTo( StaticText( package.name ) )
		nameBox = _itemHoverHighlightStyle.applyTo( Row( [ icon.padX( 5.0 ).alignHPack().alignVCentre(), nameElement.alignVCentre() ] ) )
		nameBox = nameBox.withContextMenuInteractor( _packageContextMenuFactory )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _packageDropDest )

		nameUnit = LiteralUnit( nameBox )

		itemsBox = Column( items )

		return Column( [ nameUnit.defaultPerspectiveValuePresInFragment(), itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )



	@ObjectDispatchMethod( ProjectPage )
	def Page(self, fragment, inheritedState, page):
		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				page.name = text

			def onCancel(self, textEntry, originalText):
				nameUnit.setLiteralValue( nameBox )

		def _onRename(menuItem):
			textEntry = TextEntry( page.name, _RenameListener(), _nameRegex, 'Please enter a valid identifier' )
			textEntry.grabCaretOnRealise()
			nameUnit.setLiteralValue( textEntry )

		def _onDelete(menuItem):
			if page.parent is not None:
				page.parent.remove( page )

		def _pageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Delete', _onDelete ) )
			return True


		location = inheritedState['location']
		pageLocation = _joinLocation( location, page.name )

		link = Hyperlink( page.name, pageLocation )
		link = link.withContextMenuInteractor( _pageContextMenuFactory )
		nameBox = _itemHoverHighlightStyle.applyTo( Row( [ link ] ) )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _pageDropDest )

		nameUnit = LiteralUnit( nameBox )

		return nameUnit.defaultPerspectiveValuePresInFragment()





_view = ProjectView()
perspective = Perspective( _view.fragmentViewFunction, None )

