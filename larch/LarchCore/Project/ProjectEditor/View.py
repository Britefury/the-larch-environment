##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import copy
import os

from java.lang import Object

from java.awt import Color
from java.awt.geom import Rectangle2D
from java.awt import BasicStroke

from java.awt.event import KeyEvent

from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from java.util.regex import Pattern

from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import MethodDispatchView


from BritefuryJ.Live import LiveValue

from BritefuryJ.AttributeTable import *

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Graphics import *
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.LSpace import Anchor
from BritefuryJ.LSpace.Input import ObjectDndHandler
from BritefuryJ.LSpace.Interactor import ClickElementInteractor
from BritefuryJ.Controls import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.Pres.Help import *
from BritefuryJ.Util.Jython import JythonException

from BritefuryJ.Projection import Perspective


from LarchCore.MainApp import DocumentManagement

from LarchCore.Project.ProjectRoot import ProjectRoot
from LarchCore.Project.ProjectPackage import ProjectPackage
from LarchCore.Project.ProjectPage import ProjectPage
from LarchCore.Project.ProjectNode import ProjectNode
from LarchCore.Project import PageData



class ProjectDrag (Object):
	def __init__(self, source):
		self.source = source


def _getProjectModelOfElement(element):
	ctx = element.fragmentContext
	model = ctx.model
	while not isinstance( model, ProjectNode ):
		ctx = ctx.parent
		model = ctx.model
	return model


def _getModelOfPackageOrPageNameElement(element):
	model = _getProjectModelOfElement(element)
	if not isinstance( model, ProjectPackage )  and  not isinstance( model, ProjectPage ):
		raise TypeError, 'model is not a project package or a page, it is a %s' % type( model )
	return model

def _getModelOfProjectNameElement(element):
	model = _getProjectModelOfElement(element)
	if not isinstance( model, ProjectRoot ):
		raise TypeError, 'model is not a project root, it is a %s' % type( model )
	return model


def _dragSourceCreateSourceData(element, aspect):
	return ProjectDrag( _getModelOfPackageOrPageNameElement( element ) )


_dragSource = ObjectDndHandler.DragSource( ProjectDrag, _dragSourceCreateSourceData )



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
	item = copy.deepcopy( data.source )   if action == ObjectDndHandler.COPY   else data.source

	if action == ObjectDndHandler.MOVE:
		# Remove from existing parent
		itemToRemove = data.source
		currentParent = itemToRemove.parent
		indexOfItem = currentParent.indexOfById( itemToRemove )
		del currentParent[indexOfItem]
		if index is not None  and  newParent is currentParent  and  index > indexOfItem:
			index -= 1

	if index is None:
		newParent.append( item )
	else:
		newParent.insert( index, item )
	changeHistory.thaw()




def _canDropOntoPage(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		model = _getModelOfPackageOrPageNameElement( element )
		return model is not data.source
	return False

def _highlightDropOntoPage(element, graphics, targetPos, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		if targetPos.y > ( element.getActualHeight() * 0.5 ):
			# Highlight lower part
			shape = Rectangle2D.Double( 0.0, element.getActualHeight() * 0.75, element.getActualWidth(), element.getActualHeight() * 0.25 )
		else:
			# Highlight upper part
			shape = Rectangle2D.Double( 0.0, 0.0, element.getActualWidth(), element.getActualHeight() * 0.25 )
		ObjectDndHandler.dndHighlightPainter.drawShape( graphics, shape )

def _dropOntoPage(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPage = _getModelOfPackageOrPageNameElement( element )
		parent = destPage.parent
		index = parent.indexOfById( destPage )
		if targetPos.y > ( element.getActualHeight() * 0.5 ):
			index += 1

		_performDrop( data, action, parent, index )
		return True
	return False



def _getDestPackageAndIndex(element, targetPos):
	targetPackage = _getModelOfPackageOrPageNameElement( element )
	assert isinstance( targetPackage, ProjectPackage )
	if targetPos.x > ( element.getActualWidth() * 0.5 ):
		# Drop as child of package
		return targetPackage, len( targetPackage )
	else:
		# Drop as sibling of package
		parent = targetPackage.parent
		index = parent.indexOfById( targetPackage )
		if targetPos.y > ( element.getActualHeight() * 0.5 ):
			index += 1
		return parent, index


def _canDropOntoPackage(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.COPY  or  not _isChildOf( data.source, destPackage ):
			return True
	return False

def _highlightDropOntoPackage(element, graphics, targetPos, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		if targetPos.x > ( element.getActualWidth() * 0.5 ):
			# Highlight right part
			shape = Rectangle2D.Double( element.getActualWidth() * 0.75, 0.0, element.getActualWidth() * 0.25, element.getActualHeight() )
		else:
			if targetPos.y > ( element.getActualHeight() * 0.5 ):
				# Highlight lower part
				shape = Rectangle2D.Double( 0.0, element.getActualHeight() * 0.75, element.getActualWidth(), element.getActualHeight() * 0.25 )
			else:
				# Highlight upper part
				shape = Rectangle2D.Double( 0.0, 0.0, element.getActualWidth(), element.getActualHeight() * 0.25 )
		ObjectDndHandler.dndHighlightPainter.drawShape( graphics, shape )

def _dropOntoPackage(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.MOVE  and _isChildOf( data.source, destPackage ):
			return False



		targetPackage = _getModelOfPackageOrPageNameElement( element )
		if targetPos.x > ( element.getActualWidth() * 0.5 ):
			_performDrop( data, action, targetPackage, None )
			return True
		else:
			parent = targetPackage.parent
			index = parent.indexOfById( targetPackage )
			if targetPos.y > ( element.getActualHeight() * 0.5 ):
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


_pageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _canDropOntoPage, _highlightDropOntoPage, _dropOntoPage )
_packageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _canDropOntoPackage, _highlightDropOntoPackage, _dropOntoPackage )
_projectIndexDropDest = ObjectDndHandler.DropDest( ProjectDrag, _projectIndexDrop )




_controlsStyle = StyleSheet.style( Controls.bClosePopupOnActivate( True ) )
_projectIndexNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.25, 0.35, 0.5 ) ), Primitive.fontSize( 16 ), Primitive.fontFace( Primitive.lightFontName ) )
_packageNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ), Primitive.fontSize( 14 ), Primitive.fontFace( Primitive.lightFontName ) )
_itemHoverHighlightStyle = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )
_pythonPackageNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_pythonPackageNameNotSetStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.0, 0.0 ) ) )
_pythonPackageNameNotSetCommentStyle = StyleSheet.style( Primitive.foreground( Color( 0.2, 0.2, 0.2 ) ), Primitive.fontItalic( True ) )

_packageContentsIndentation = 20.0


_packageIcon = Image( Image.getResource( '/LarchCore/Project/images/Package.png' ) )


_nameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' )
_pythonPackageNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*' )





class ProjectView (MethodDispatchView):
	@ObjectDispatchMethod( ProjectRoot )
	def ProjectRoot(self, fragment, inheritedState, project):
		# Save and Save As
		def _onSave(control, buttonEvent):
			if document.hasFilename():
				document.save()
			else:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )

				DocumentManagement.promptSaveDocumentAs( world, control.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )


		def _onSaveAs(control, buttonEvent):
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )

			DocumentManagement.promptSaveDocumentAs( world, control.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn, document.getFilename() )


		def _onReload(control, buttonEvent):
			if document.hasFilename():
				document.save()
				document.reload()
				project.reset()
			else:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
					document.reload()
					project.reset()

				DocumentManagement.promptSaveDocumentAs( world, control.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )


		def _onExport(control, event):
			component = control.getElement().getRootElement().getComponent()
			openDialog = JFileChooser()
			openDialog.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY )
			response = openDialog.showDialog( component, 'Export' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None  and  os.path.isdir( filename ):
						response = JOptionPane.showOptionDialog( component, 'Existing content will be overwritten. Proceed?', 'Overwrite existing content',
						                                         JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
						if response == JFileChooser.APPROVE_OPTION:
							exc = None
							try:
								project.export( filename )
							except:
								exc = JythonException.getCurrentException()
							if exc is not None:
								BubblePopup.popupInBubbleAdjacentTo( DefaultPerspective.instance( exc ), control.getElement(), Anchor.BOTTOM, True, True )





		# Python package name
		class _PythonPackageNameListener (EditableLabel.EditableLabelListener):
			def onTextChanged(self, editableLabel, text):
				project.pythonPackageName = text



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
		document = fragment.subject.document
		world = fragment.subject.world


		# Title
		title = TitleBar( document.getDocumentName() )


		# Controls for 'save' and 'save as'
		saveExportHeader = SectionHeading1( 'Save/export' )
		saveButton = Button.buttonWithLabel( 'Save', _onSave )
		saveAsButton = Button.buttonWithLabel( 'Save as', _onSaveAs )
		reloadButton = Button.buttonWithLabel( 'Save and reload', _onReload )
		reloadButton = AttachTooltip( reloadButton, 'Saves and reloads the project from scratch\nCauses all embedded objects to be re-created.' )
		exportButton = Button.buttonWithLabel( 'Export', _onExport )
		exportButton = AttachTooltip( exportButton, 'Exports project contents to text files where possible.' )
		saveBox = Row( [ saveButton.padX( 10.0 ), Spacer( 30.0, 0.0 ), saveAsButton.padX( 10.0 ), Spacer( 30.0, 0.0 ), reloadButton.padX( 10.0 ), Spacer( 50.0, 0.0 ), exportButton.padX( 10.0 ) ] ).alignHLeft()
		saveExportSection = Section( saveExportHeader, saveBox )



		# Project
		projectHeader = SectionHeading1( 'Project' )

		# Python package name
		pythonPackageNamePrompt = Label( 'Root Python package name: ' )
		notSet = _pythonPackageNameNotSetStyle.applyTo( Label( '<not set>' ) )
		pythonPackageNameLabel = EditableLabel( project.pythonPackageName, notSet, _PythonPackageNameListener() ).regexValidated( _pythonPackageNameRegex, 'Please enter a valid dotted identifier' )
		if project.pythonPackageName is None:
			comment = _pythonPackageNameNotSetCommentStyle.applyTo( Label( '(pages will not be importable until this is set)' ) )
			pythonPackageNameBox = Row( [ pythonPackageNamePrompt, pythonPackageNameLabel, Spacer( 25.0, 0.0 ), comment ] )
		else:
			pythonPackageNameBox = Row( [ pythonPackageNamePrompt, pythonPackageNameLabel ] )
		pythonPackageNameBox = AttachTooltip( pythonPackageNameBox, 'The root python package name is the name under which the contents of the project can be imported using import statements within the project.\n' + \
			'If this is not set, pages from this project cannot be imported.', False )
		
		
		# Clear imported modules
		def _onReset(button, event):
			project.reset()
			modules = document.unloadAllImportedModules()
			heading = SectionHeading2( 'Unloaded modules:' )
			modules = Column( [ Label( module )   for module in modules ] )
			report = Section( heading, modules )
			BubblePopup.popupInBubbleAdjacentTo( report, button.getElement(), Anchor.BOTTOM, True, True )
		resetPrompt = Label( 'Reset (unload project modules): ' )
		resetButton = Button.buttonWithLabel( 'Reset', _onReset )
		resetButton = AttachTooltip( resetButton, 'Unloads all modules that were imported from this project from the Python module cache.' )
		reset = Row( [ resetPrompt, resetButton ] )


		projectSection = Section( projectHeader, Body( [ pythonPackageNameBox, reset ] ) )


		# Project index
		indexHeader = SectionHeading1( 'Index' )

		nameElement = _projectIndexNameStyle.applyTo( Label( 'Project root' ) )
		nameBox = _itemHoverHighlightStyle.applyTo( nameElement.alignVCentre() )
		nameBox = nameBox.withContextMenuInteractor( _projectIndexContextMenuFactory )
		nameBox = nameBox.withDropDest( _projectIndexDropDest )
		nameBox = AttachTooltip( nameBox, 'Right click to access context menu, from which new pages and packages can be created.\n' + \
			'A page called index at the root will appear instead of the project page. A page called __startup__ will be executed at start time.', False )

		itemsBox = Column( project[:] ).alignHExpand()

		contentsView = Column( [ nameBox.alignHExpand(), itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )


		indexSection = Section( indexHeader, contentsView )


		tip = TipBox( 'Larch projects act like Python programs. Packages act as directories/packages and pages act as Python source files. Pages can import code from one another as if they are modules.\n' + \
			'If a page called index is present in the root of the project, then it will appear instead of the project page.\n' +\
			'If a page called __startup__ (2 underscores) is present at the root, code within it will be executed before all other pages. This can be used for registering editor extensions.',
			      'larchcore.worksheet.worksheeteditor')


		# The page
		head = Head( [ title ] )
		body = Body( [ saveExportSection, projectSection, indexSection, tip ] ).alignHPack()

		return StyleSheet.style( Primitive.editable( False ) ).applyTo( Page( [ head, body ] ) )


	@ObjectDispatchMethod( ProjectPackage )
	def Package(self, fragment, inheritedState, package):
		def _addPackage(menuItem):
			package.append( ProjectPackage( 'NewPackage' ) )

		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				package.name = text

			def onCancel(self, textEntry, originalText):
				nameLive.setLiteralValue( nameBox )

		def _onRename(menuItem):
			textEntry = TextEntry( package.name, _RenameListener() ).regexValidated( _nameRegex, 'Please enter a valid identifier' )
			textEntry.grabCaretOnRealise()
			nameLive.setLiteralValue( textEntry )
		
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

		nameElement = _packageNameStyle.applyTo( StaticText( package.name ) )
		nameBox = _itemHoverHighlightStyle.applyTo( Row( [ _packageIcon.padX( 5.0 ).alignHPack().alignVCentre(), nameElement.alignVCentre() ] ) )

		# Set drop destination and place in box that is h-packed, otherwise attempting to drop items as children could require the user to
		# drop somewhere off to the right of the package, since the h-expand applied further up the presentation tree will expand it beyond
		# its visible bounds
		nameBox = nameBox.withDropDest( _packageDropDest )
		nameBox = Bin( nameBox ).alignHPack()

		nameBox = nameBox.withContextMenuInteractor( _packageContextMenuFactory )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = AttachTooltip( nameBox, 'Right click to access context menu.', False )

		nameLive = LiveValue( nameBox )

		itemsBox = Column( package[:] )

		return Column( [ nameLive, itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )



	@ObjectDispatchMethod( ProjectPage )
	def Page(self, fragment, inheritedState, page):
		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				page.name = text

			def onCancel(self, textEntry, originalText):
				nameLive.setLiteralValue( nameBox )

		def _onRename(menuItem):
			textEntry = TextEntry( page.name, _RenameListener() ).regexValidated( _nameRegex, 'Please enter a valid identifier' )
			textEntry.grabCaretOnRealise()
			nameLive.setLiteralValue( textEntry )

		def _onDelete(menuItem):
			if page.parent is not None:
				page.parent.remove( page )

		def _pageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Delete', _onDelete ) )
			return True


		document = fragment.subject.document
		pageSubject = document.newModelSubject( page.data, fragment.subject, page.importName, page.name )

		link = Hyperlink( page.name, pageSubject )
		link = link.withContextMenuInteractor( _pageContextMenuFactory )
		nameBox = _itemHoverHighlightStyle.applyTo( Row( [ link ] ) )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _pageDropDest )
		nameBox = AttachTooltip( nameBox, 'Click to enter page.\nRight click to access context menu.', False )

		nameLive = LiveValue( nameBox )

		return nameLive





_view = ProjectView()
perspective = Perspective( _view.fragmentViewFunction, None )

