##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import copy
import os

from java.lang import Object

from java.io import FileOutputStream

from java.awt import Color, BasicStroke
from java.awt.geom import Rectangle2D
from java.awt.event import KeyEvent

from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from java.util.regex import Pattern

from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Kernel.Document import LinkSubjectDrag


from BritefuryJ.Live import LiveValue

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Graphics import FilledOutlinePainter, SolidBorder
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.LSpace import Anchor
from BritefuryJ.LSpace.Input import ObjectDndHandler
from BritefuryJ.LSpace.Interactor import ClickElementInteractor
from BritefuryJ.Controls import Controls, EditableLabel, MenuItem, Button, TextEntry, Hyperlink
from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Image, Spacer, Bin, Row, Column
from BritefuryJ.Pres.RichText import TitleBar, Body, Head, Page, HSeparator, NormalText, StrongSpan, EmphSpan
from BritefuryJ.Pres.UI import BubblePopup, SectionHeading1, SectionHeading2, Section, Form
from BritefuryJ.Pres.Help import AttachTooltip, TipBox
from BritefuryJ.Util.Jython import JythonException

from BritefuryJ.Projection import Perspective

from BritefuryJ.Editor.List import EditableListController

from Britefury import app_in_jar

from LarchCore.MainApp import DocumentManagement

from LarchCore.Project.ProjectRoot import ProjectRoot
from LarchCore.Project.ProjectPackage import ProjectPackage
from LarchCore.Project.ProjectPage import ProjectPage
from LarchCore.Project.ProjectNode import ProjectNode
from LarchCore.Project import PageData




def _isChildOf(node, package):
	if node is package:
		return True

	if isinstance( package, ProjectPackage ):
		for child in package:
			if _isChildOf( node, child ):
				return True
	return False


class _ProjectTreeController (EditableListController):
	def canMove(self, item, source, index, dest):
		return not _isChildOf( dest, item )


	def reorder(self, item, dest, index):
		changeHistory = dest.__change_history__
		changeHistory.freeze()
		srcIndex = dest.indexOfById( item )
		if srcIndex < index:
			index -= 1
		del dest[srcIndex]
		dest.insert( index, item )
		changeHistory.thaw()
		return True


	def move(self, item, source, index, dest):
		changeHistory = dest.__change_history__
		changeHistory.freeze()
		source.remove( item )
		dest.insert( index, item )
		changeHistory.thaw()
		return True


	def copy(self, item, source, index, dest):
		changeHistory = dest.__change_history__
		changeHistory.freeze()
		dest.insert( index, copy.deepcopy( item ) )
		changeHistory.thaw()
		return True


	def canMoveToEnd(self, item, source, dest):
		return not _isChildOf( dest, item )


	def reorderToEnd(self, item, dest):
		changeHistory = dest.__change_history__
		changeHistory.freeze()
		dest.remove( item )
		dest.append( item )
		changeHistory.thaw()
		return True

	def moveToEnd(self, item, source, dest):
		changeHistory = dest.__change_history__
		changeHistory.freeze()
		source.remove( item )
		dest.append( item )
		changeHistory.thaw()
		return True

	def copyToEnd(self, item, source, dest):
		changeHistory = dest.__change_history__
		changeHistory.freeze()
		dest.append( copy.deepcopy( item ) )
		changeHistory.thaw()
		return True


_ProjectTreeController.instance = _ProjectTreeController()





def _getProjectModelOfElement(element):
	ctx = element.fragmentContext
	model = ctx.model
	while not isinstance( model, ProjectNode ):
		ctx = ctx.parent
		model = ctx.model
	return model


def _getSubjectOfPageNameElement(element):
	model = _getProjectModelOfElement(element)
	if not isinstance( model, ProjectPage ):
		raise TypeError, 'model is not a project page, it is a %s' % type( model )
	fragment = element.fragmentContext
	pageSubject = fragment.subject._pageSubject( model )
	return pageSubject


def _dragSourceCreateLink(element, aspect):
	return LinkSubjectDrag( _getSubjectOfPageNameElement( element ) )


_linkDragSource = ObjectDndHandler.DragSource( LinkSubjectDrag, _dragSourceCreateLink )











_controlsStyle = StyleSheet.style( Controls.bClosePopupOnActivate( True ) )
_projectIndexNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.25, 0.35, 0.5 ) ), Primitive.fontSize( 16 ), Primitive.fontFace( Primitive.lightFontName ) )
_packageNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ), Primitive.fontSize( 14 ), Primitive.fontFace( Primitive.lightFontName ) )
_itemHoverHighlightStyle = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )
_pythonPackageNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) )
_pythonPackageNameNotSetStyle = StyleSheet.style( Primitive.foreground( Color( 0.5, 0.0, 0.0 ) ) )
_pythonPackageNameNotSetCommentStyle = StyleSheet.style( Primitive.foreground( Color( 0.2, 0.2, 0.2 ) ), Primitive.fontItalic( True ) )

_frontPageNoteBorder = SolidBorder( 1.0, 1.0, 3.0, 3.0, Color( 0.0, 1.0, 0.0 ), Color( 0.85, 0.95, 0.85 ) )
_frontPageNoteStyle = StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ), Primitive.fontSize( 10 ) )
_startupPageNoteBorder = SolidBorder( 1.0, 1.0, 3.0, 3.0, Color( 0.75, 0.5, 1.0 ), Color( 0.925, 0.9, 0.95 ) )
_startupPageNoteStyle = StyleSheet.style( Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ), Primitive.fontSize( 10 ) )
_notesRowStyle = StyleSheet.style( Primitive.rowSpacing( 10.0 ) )
_notesGap = 15.0

_packageContentsIndentation = 20.0


_packageIcon = Image( Image.getResource( '/LarchCore/Project/images/Package.png' ) )


_nameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' )
_pythonPackageNameRegex = Pattern.compile( '([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)?' )





def _buildProjectJar(element, document):
	component = element.getRootElement().getComponent()

	larchJarURL = app_in_jar.getLarchJarURL()
	chosenJarURL = None
	if larchJarURL is None:
		openDialog = JFileChooser()
		openDialog.setFileFilter( FileNameExtensionFilter( 'Larch executable JAR (*.jar)', [ 'jar' ] ) )
		response = openDialog.showDialog( component, 'Choose Larch JAR' )
		if response == JFileChooser.APPROVE_OPTION:
			sf = openDialog.getSelectedFile()
			if sf is not None:
				chosenJarURL = sf.toURI().toURL()
		else:
			return



	jarFile = None
	bFinished = False
	while not bFinished:
		saveDialog = JFileChooser()
		saveDialog.setFileFilter( FileNameExtensionFilter( 'JAR file (*.jar)', [ 'jar' ] ) )
		response = saveDialog.showSaveDialog( component )
		if response == JFileChooser.APPROVE_OPTION:
			sf = saveDialog.getSelectedFile()
			if sf is not None:
				if sf.exists():
					response = JOptionPane.showOptionDialog( component, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
					if response == JFileChooser.APPROVE_OPTION:
						jarFile = sf
						bFinished = True
					else:
						bFinished = False
				else:
					jarFile = sf
					bFinished = True
			else:
				bFinished = True
		else:
			bFinished = True


	if jarFile is not None:
		outStream = FileOutputStream( jarFile )

		documentBytes = document.writeAsBytes()

		nameBytesPairs = [ ( 'app.larch', documentBytes ) ]

		app_in_jar.buildLarchJar( outStream, nameBytesPairs, larchJarURL=chosenJarURL )

		outStream.close()




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
				if text != '':
					project.pythonPackageName = text
				else:
					project.pythonPackageName = None



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



		#
		# Project Section
		#

		# Python package name
		notSet = _pythonPackageNameNotSetStyle.applyTo( Label( '<not set>' ) )
		pythonPackageNameLabel = EditableLabel( project.pythonPackageName, notSet, _PythonPackageNameListener() ).regexValidated( _pythonPackageNameRegex, 'Please enter a valid dotted identifier' )
		pythonPackageNameLabel = AttachTooltip( pythonPackageNameLabel, 'The root python package name is the name under which the contents of the project can be imported using import statements within the project.\n' + \
			'If this is not set, pages from this project cannot be imported.', False )
		pythonPackageNameRow = Form.Section( 'Root Python package name', 'Pages will not be importable unless this is set', pythonPackageNameLabel )


		# Clear imported modules
		def _onReset(button, event):
			project.reset()
			modules = document.unloadAllImportedModules()
			heading = SectionHeading2( 'Unloaded modules:' )
			modules = Column( [ Label( module )   for module in modules ] )
			report = Section( heading, modules )
			BubblePopup.popupInBubbleAdjacentTo( report, button.getElement(), Anchor.BOTTOM, True, True )
		resetButton = Button.buttonWithLabel( 'Reset', _onReset )
		resetButton = AttachTooltip( resetButton, 'Unloads all modules that were imported from this project from the Python module cache. This way they can be re-imported, allowing modifications to take effect.' )
		resetRow = Form.Section( 'Reset', 'Unload project modules', resetButton )


		projectSection = Form( 'Project', [ pythonPackageNameRow, resetRow ] )


		# Project index
		indexHeader = SectionHeading1( 'Index' )

		nameElement = _projectIndexNameStyle.applyTo( Label( 'Project root' ) )
		nameBox = _itemHoverHighlightStyle.applyTo( nameElement.alignVCentre() )
		nameBox = nameBox.withContextMenuInteractor( _projectIndexContextMenuFactory )
		nameBox = _ProjectTreeController.instance.item( project, nameBox )
		nameBox = AttachTooltip( nameBox, 'Right click to access context menu, from which new pages and packages can be created.\n' + \
			'A page called index at the root will appear instead of the project page. A page called __startup__ will be executed at start time.', False )

		itemsBox = Column( project[:] ).alignHExpand()
		itemsBox = _ProjectTreeController.instance.editableList( project, itemsBox )

		contentsView = Column( [ nameBox.alignHExpand(), itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )


		indexSection = Section( indexHeader, contentsView )


		def _onBuildJar(button, event):
			_buildProjectJar( button.element, document )

		buildJarButton = Button.buttonWithLabel( 'Build JAR', _onBuildJar )
		jarRow = Form.Section( 'Build executable app', 'Export the project as an executable JAR', buildJarButton )

		packagingSection = Form( 'Packaging', [ jarRow ] )


		indexTip = TipBox( [ NormalText( [ StrongSpan( 'Index: ' ), 'Larch projects act like Python programs. Packages act as directories/packages and pages act as Python source files. Pages can import code from one another as if they are modules.' ] ),
				     NormalText( [ 'New pages and packages can be created by right clicking on the entries in the index or on ', EmphSpan( 'Project root' ), ' (they will highlight as you hover over them).' ] ),
				     NormalText( [ StrongSpan( 'Front and startup pages: ' ), 'If a page is set as the front page it will appear instead of the project page. In these cases, the project page can still be reached using the links in the location bar at the top of the window.' ] ),
				     'If a page is set as the startup page, code within it will be executed before all other pages. This can be used for registering editor extensions.',
				     'To set a page as the front page or the startup page, right-click on it to show its context menu and choose the appropriate option.' ],
			      'larchcore.worksheet.worksheeteditor')



		# The page
		head = Head( [ title ] )
		body = Body( [ saveExportSection, projectSection, indexSection, packagingSection, indexTip ] ).alignHPack()

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
		nameBox = Bin( nameBox ).alignHPack()

		nameBox = nameBox.withContextMenuInteractor( _packageContextMenuFactory )
		nameBox = _ProjectTreeController.instance.nestedListItem( package, package, nameBox )
		nameBox = AttachTooltip( nameBox, 'Right click to access context menu.', False )

		nameLive = LiveValue( nameBox )
		itemsBox = Column( package[:] )
		itemsBox = _ProjectTreeController.instance.editableList( package, itemsBox )

		return Column( [ nameLive, itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )



	@ObjectDispatchMethod( ProjectPage )
	def Page(self, fragment, inheritedState, page):
		root = page.rootNode
		if root is None:
			raise RuntimeError, 'No root node'
		isFrontPage = root.frontPage is page
		isStartupPage = root.startupPage is page

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


		def _onClearFrontPage(menuItem):
			root.frontPage = None

		def _onSetAsFrontPage(menuItem):
			root.frontPage = page


		def _onClearStartupPage(menuItem):
			root.startupPage = None

		def _onSetAsStartupPage(menuItem):
			root.startupPage = page


		def _pageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Delete', _onDelete ) )
			menu.add( HSeparator() )

			if isFrontPage:
				menu.add( MenuItem.menuItemWithLabel( 'Clear front page', _onClearFrontPage ) )
			else:
				menu.add( MenuItem.menuItemWithLabel( 'Set as front page', _onSetAsFrontPage ) )

			if isStartupPage:
				menu.add( MenuItem.menuItemWithLabel( 'Clear startup page', _onClearStartupPage ) )
			else:
				menu.add( MenuItem.menuItemWithLabel( 'Set as startup page', _onSetAsStartupPage ) )

			return True


		pageSubject = fragment.subject._pageSubject( page )

		link = Hyperlink( page.name, pageSubject )
		link = link.withContextMenuInteractor( _pageContextMenuFactory )
		nameBox = _itemHoverHighlightStyle.applyTo( Row( [ link ] ) )
		nameBox = _ProjectTreeController.instance.item( page, nameBox )
		nameBox = nameBox.withDragSource( _linkDragSource )
		nameBox = AttachTooltip( nameBox, 'Click to enter page.\nRight click to access context menu.', False )

		nameLive = LiveValue( nameBox )

		if isFrontPage or isStartupPage:
			notes = []
			if isFrontPage:
				notes.append( _frontPageNoteBorder.surround( _frontPageNoteStyle.applyTo( Label( 'Front page' ) ) ) )
			if isStartupPage:
				notes.append( _startupPageNoteBorder.surround( _startupPageNoteStyle.applyTo( Label( 'Startup page' ) ) ) )
			notesPres = _notesRowStyle.applyTo( Row( notes ) )
			pagePres = Row( [ nameLive, notesPres.padX( _notesGap, 0.0 ) ] )
		else:
			pagePres = nameLive

		return pagePres





_view = ProjectView()
perspective = Perspective( _view.fragmentViewFunction, None )

