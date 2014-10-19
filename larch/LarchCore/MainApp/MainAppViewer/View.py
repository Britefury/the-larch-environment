##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from java.awt.event import KeyEvent

from java.awt.datatransfer import DataFlavor

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Kernel.Document import Document


from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Browser.TestPages import TestsRootPage

from BritefuryJ.LSpace import PageController, Anchor
from BritefuryJ.Graphics import FilledBorder

from BritefuryJ.Controls import Hyperlink, MenuItem, VPopupMenu
from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Border, Row, Column, RGrid, GridRow
from BritefuryJ.Pres.RichText import TitleBar, HSeparator, Head, Body, Page, SplitLinkHeaderBar, NormalText, StrongSpan, EmphSpan
from BritefuryJ.Pres.UI import Section, SectionHeading1
from BritefuryJ.Pres.Help import AttachTooltip, TipBox

from BritefuryJ.Projection import Perspective
from BritefuryJ.DefaultPerspective import DefaultPerspective

from LarchCore.MainApp import Application
from LarchCore.MainApp import DocumentManagement
from LarchCore.Kernel import interpreter_config_page



_appDocRightPadding = 30.0
_controlsPadding = 5.0
_appDocumentControlsStyle = StyleSheet.style( Primitive.rowSpacing( 20.0 ) )
_appDocumentControlsBorder = FilledBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) )
_documentListTableStyle = StyleSheet.style( Primitive.tableColumnSpacing( 15.0 ), Primitive.tableRowSpacing( 5.0 ) )




def _newDocumentName(docs):
	usedNames = set( [ doc.getName()   for doc in docs ] )

	name = 'Untitled'
	if name not in usedNames:
		return name
	
	index = 2
	name = 'Untitled' + str(index)
	while name in usedNames:
		index += 1
		name = 'Untitled' + str(index)

	return name


def _newConsoleIndex(consoles):
	index = 1
	usedIndices = set( [ con.getIndex()   for con in consoles ] )
	while index in usedIndices:
		index += 1
	return index



						
def _contentsList(controls, contentsLists, title):
	controlsBox = _appDocumentControlsStyle.applyTo( Row( [ c.padX( _controlsPadding )   for c in controls ] ) )
	controlsBorder = _appDocumentControlsBorder.surround( controlsBox )

	openDocumentsSeparator = HSeparator()
	
	docListBox = _documentListTableStyle.applyTo( RGrid( contentsLists ) )

	contentsBox = Column( [ controlsBorder.pad( 2.0, 2.0 ), openDocumentsSeparator, docListBox.pad( 12.0, 2.0 ) ] )
	
	heading = SectionHeading1( title )
	
	return Section( heading, contentsBox )
						
						
						
class AppView (MethodDispatchView):
	@ObjectDispatchMethod( Application.AppState )
	def AppState(self, fragment, state, node):
		def _onNewDoc(link, event):
			def handleNewDocumentFn(document, firstPageSubjectFn):
				name = _newDocumentName( openDocuments )
				document.setDocumentName( name )
				
				node.registerOpenDocument( document )

				subject = document.newSubject( fragment.subject, None, document.getDocumentName() )
				subject = firstPageSubjectFn( subject )

				pageController = link.element.rootElement.pageController
				pageController.openSubject( subject, PageController.OpenOperation.OPEN_IN_CURRENT_TAB )

				
			element = link.getElement()
			openDocuments = node.getOpenDocuments()
			DocumentManagement.promptNewDocument( fragment.subject.world, element, handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpenDoc(link, event):
			def handleOpenedDocumentFn(fullPath, document):
				appDoc = node.registerOpenDocument( document )

				
			element = link.getElement()
			DocumentManagement.promptOpenDocument( fragment.subject.world, element.getRootElement().getComponent(), handleOpenedDocumentFn )
			
			return True
		
		
		
		def _onFileListDrop(element, targetPosition, data, action):
			world = fragment.subject.world
			for filename in data:
				filename = str( filename )
				
				document = Document.readFile( world, filename )
				node.registerOpenDocument( document )
			return True


		def new_console(link, appConsole):
			node.addConsole( appConsole )

			subject = appConsole.subject( fragment.subject )

			pageController = link.element.rootElement.pageController
			pageController.openSubject( subject, PageController.OpenOperation.OPEN_IN_CURRENT_TAB )


		
		def on_new_console(link, event):
			def on_choose_kernel_factory(kernel_factory):
				def on_kernel_started(kernel):
					consoles = node.getConsoles()
					index = _newConsoleIndex( consoles )

					description = kernel_factory.description.human_description

					if kernel.is_in_process():
						full_name = 'In-process console {0} ({1})'.format(index, description)
					else:
						full_name = 'IPython console {0} ({1})'.format(index, description)
					appConsole = Application.AppConsole(kernel, '<console{0}>'.format(index), full_name, index)

					new_console(link, appConsole)

				kernel_factory.create_kernel(on_kernel_started)

			menu = interpreter_config_page.get_interpreter_config().interpreter_chooser_menu(on_choose_kernel_factory)

			menu.popupMenu(link.element, Anchor.TOP_RIGHT, Anchor.TOP_LEFT)

			return True


		openDocViews = Pres.mapCoerce( node.getOpenDocuments() )
		consoles = Pres.mapCoerce( node.getConsoles() )


		systemLink = Hyperlink( 'TEST PAGES', TestsRootPage.instanceSubject )
		configurationLink = Hyperlink( 'CONFIGURATION PAGE', fragment.subject.world.configuration.subject() )
		aboutLink = Hyperlink( 'ABOUT', fragment.subject.aboutPageSubject )
		linkHeader = SplitLinkHeaderBar( [ aboutLink ], [ configurationLink, systemLink ])

		title = TitleBar( 'The Larch Environment' )
		
		newLink = Hyperlink( 'NEW', _onNewDoc )
		openLink = Hyperlink( 'OPEN', _onOpenDoc )
		openDocumentsBox = _contentsList( [ newLink, openLink ], openDocViews, 'Documents' )
		openDocumentsBox = openDocumentsBox.withNonLocalDropDest( DataFlavor.javaFileListFlavor, _onFileListDrop )
		openDocumentsBox = AttachTooltip( openDocumentsBox, 'Click NEW to create a new document. To open from a file, click OPEN, or drag files from a file explorer application.', False )
		
		
		new_console_link = Hyperlink( 'NEW', on_new_console )
		consolesBox = _contentsList( [ new_console_link ], consoles, 'Python consoles' )


		tip = TipBox( [ NormalText( [ StrongSpan( 'Getting started: ' ), 'To get programming quickly, create a new Python console by pressing ', EmphSpan( 'new' ), ', beneath', EmphSpan( ' Python consoles' ), '.' ] ),
				NormalText( [ 'For something more easily modifiable and something you can save, press ', EmphSpan( 'new' ), ' beneath ', EmphSpan( 'Documents' ), ' and choose the ', EmphSpan( 'Quickstart: worksheet' ), ' option.' ] ),
				NormalText( [ StrongSpan( 'Tips: ' ), 'You can highlight items that have help tips by pressing F2. Hover the pointer over them to display the tips. ' + \
							      'Some items do not display their tips unless highlighting is enabled, in order to reduce clutter.' ] ),
				NormalText( [ StrongSpan( 'Command bar: ' ), 'The command bar can be invoked by pressing the ', EmphSpan( 'escape' ), ' key. From there you can type abbreviated commands to invoke them. '
						'Alternatively, type in part of the full name of a command and autocomplete will show a list of commands that match. '
						'Press ', EmphSpan( 'tab' ), ' to switch between the entries in the autocomplete list and press ', EmphSpan( 'enter' ), ' to execute the highlighted command. '
						'Bold letters shown within command names indicate abbreviations, e.g. ', EmphSpan( 's' ), ' for ', EmphSpan( 'save' ), ' and ', EmphSpan( 'sa' ), ' for ', EmphSpan( 'save as' ), '. '
						'You can type these to execute them quickly.' ] )],
			      'larchcore.mainapp.tooltiphighlights' )
		
		
		head = Head( [ linkHeader, title ] )
		body = Body( [ openDocumentsBox.pad( 0.0, 10.0 ).alignHLeft(), consolesBox.pad( 0.0, 10.0 ).alignHLeft(), tip ] )
		return StyleSheet.style( Primitive.editable( False ) ).applyTo( Page( [ head, body ] ) )



	@ObjectDispatchMethod( Application.AppDocument )
	def AppDocument(self, fragment, state, node):
		def _onSave(link, event):
			element = link.getElement()
			world = fragment.subject.world
			document = node.getDocument()
			
			if document.hasFilename():
				document.save()
			else:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )
				
		
		def _onSaveAs(link, event):
			element = link.getElement()
			world = fragment.subject.world
			document = node.getDocument()
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn, document.getFilename() )


		def _onClose(link, event):
			element = link.getElement()
			world = fragment.subject.world
			document = node.getDocument()

			def _performClose():
				document.close()
				node.appState.unregisterDocument( document )


			if document.hasUnsavedData():
				response = JOptionPane.showOptionDialog( element.rootElement.component,
									 'You have not saved your work. Close document anyway?', 'Unsaved data', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Close document', 'Cancel' ], 'Cancel' )
				if response == JOptionPane.YES_OPTION:
					_performClose()
				else:
					return
			else:
				_performClose()


		name = node.getName()

		document = node.getDocument()

		subject = node.getDocument().newSubject( fragment.subject, None, document.getDocumentName() )
		docLink = Hyperlink( name, subject ).padX( 0.0, _appDocRightPadding )
		saveLink = Hyperlink( 'Save', _onSave )
		saveAsLink = Hyperlink( 'Save as', _onSaveAs )
		closeLink = Hyperlink( 'Close', _onClose )

		return GridRow( [ docLink, saveLink, saveAsLink, closeLink ] )



	@ObjectDispatchMethod( Application.AppConsole )
	def AppConsole(self, fragment, state, node):
		subject = node.subject( fragment.subject )
		consoleLink = Hyperlink( node.get_full_name(), subject ).padX( 0.0, _appDocRightPadding )
	
		return GridRow( [ consoleLink ] )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )



_view = AppView()
perspective = Perspective( _view.fragmentViewFunction, None )

