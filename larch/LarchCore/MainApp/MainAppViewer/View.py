##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

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


from BritefuryJ.AttributeTable import *

from BritefuryJ.StyleSheet import *
from BritefuryJ.Browser import Location
from BritefuryJ.LSpace import PageController
from BritefuryJ.Graphics import *

from BritefuryJ.Controls import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.Pres.Help import *

from BritefuryJ.Projection import Perspective, Subject

from LarchCore.MainApp import Application
from LarchCore.MainApp import DocumentManagement

from LarchCore.PythonConsole import Console



_appDocRightPadding = 30.0
_controlsPadding = 5.0
_appDocumentControlsStyle = StyleSheet.style( Primitive.rowSpacing( 20.0 ), Primitive.border( FilledBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) ) ) )
_documentListTableStyle = StyleSheet.style( Primitive.tableColumnSpacing( 15.0 ), Primitive.tableRowSpacing( 5.0 ) )



def _joinLocation(x, y):
	if x == '':
		return y
	elif y == '':
		return x
	else:
		return x + '.' + y


	

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
	controlsBox = Row( [ c.padX( _controlsPadding )   for c in controls ] )
	controlsBorder = _appDocumentControlsStyle.applyTo( Border( controlsBox ) )

	openDocumentsSeparator = HSeparator()
	
	docListBox = _documentListTableStyle.applyTo( RGrid( contentsLists ) )

	contentsBox = Column( [ controlsBorder.pad( 2.0, 2.0 ), openDocumentsSeparator, docListBox.pad( 10.0, 2.0 ) ] )
	
	heading = SectionHeading1( title )
	
	return Section( heading, contentsBox.padX( 5.0, 0.0 ) )
						
						
						
class AppView (MethodDispatchView):
	@ObjectDispatchMethod( Application.AppState )
	def AppState(self, fragment, state, node):
		def _onNewDoc(link, event):
			def handleNewDocumentFn(document):
				name = _newDocumentName( openDocuments )
				document.setDocumentName( name )
				
				appDoc = node.registerOpenDocument( document, fragment.getSubjectContext()['location'] + '.documents' )

				location = fragment.getSubjectContext()['location'] + '.documents.' + appDoc.getRelativeLocation()

				pageController = link.element.rootElement.pageController
				pageController.openLocation( location, PageController.OpenOperation.OPEN_IN_CURRENT_TAB )

				
			element = link.getElement()
			openDocuments = node.getOpenDocuments()
			DocumentManagement.promptNewDocument( fragment.getSubjectContext()['world'], element, handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpenDoc(link, event):
			def handleOpenedDocumentFn(fullPath, document):
				appDoc = node.registerOpenDocument( document, fragment.getSubjectContext()['location'] + '.documents' )

				
			element = link.getElement()
			DocumentManagement.promptOpenDocument( fragment.getSubjectContext()['world'], element.getRootElement().getComponent(), handleOpenedDocumentFn )
			
			return True
		
		
		
		def _onFileListDrop(element, targetPosition, data, action):
			world = fragment.getSubjectContext()['world']
			for filename in data:
				filename = str( filename )
				
				document = Document.readFile( world, filename )
				node.registerOpenDocument( document, fragment.getSubjectContext()['location'] + '.documents' )
			return True

		
		def _onNewConsole(link, event):
			consoles = node.getConsoles()
			index = _newConsoleIndex( consoles )
			appConsole = Application.AppConsole( index )
			node.addConsole( appConsole )

			location = fragment.getSubjectContext()['location'] + '.consoles.c%d'  %  ( index, )

			pageController = link.element.rootElement.pageController
			pageController.openLocation( location, PageController.OpenOperation.OPEN_IN_CURRENT_TAB )

			return True
		
			
			
		openDocViews = InnerFragment.map( node.getOpenDocuments(), state.withAttrs( location='' ) )
		consoles = InnerFragment.map( node.getConsoles(), state.withAttrs( location='' ) )
		
		systemLink = Hyperlink( 'TEST PAGES', Location( 'tests' ) )
		configurationLink = Hyperlink( 'CONFIGURATION PAGE', Location( 'config' ) )
		linkHeader = LinkHeaderBar( [ configurationLink, systemLink ] )
		
		title = TitleBar( 'The Larch Environment' )
		
		newLink = Hyperlink( 'NEW', _onNewDoc )
		openLink = Hyperlink( 'OPEN', _onOpenDoc )
		openDocumentsBox = _contentsList( [ newLink, openLink ], openDocViews, 'Documents' )
		openDocumentsBox = openDocumentsBox.withNonLocalDropDest( DataFlavor.javaFileListFlavor, _onFileListDrop )
		openDocumentsBox = AttachTooltip( openDocumentsBox, 'Click NEW to create a new document. To open, click OPEN, or drag files from a file explorer application.', False )
		
		
		newConsoleLink = Hyperlink( 'NEW', _onNewConsole )
		consolesBox = _contentsList( [ newConsoleLink ], consoles, 'Python consoles' )


		tip = TipBox( 'You can highlight items that have help types by pressing F2. Hover the pointer over them to display the tips. ' + \
			      'Some items have tips that will not appear unless highlighting is enabled.',
			      'larchcore.mainapp.tooltiphighlights' )
		
		
		head = Head( [ linkHeader, title ] )
		body = Body( [ openDocumentsBox.pad( 0.0, 10.0 ).alignHLeft(), consolesBox.pad( 0.0, 10.0 ).alignHLeft(), tip ] )
		return StyleSheet.style( Primitive.editable( False ) ).applyTo( Page( [ head, body ] ) )



	@ObjectDispatchMethod( Application.AppDocument )
	def AppDocument(self, fragment, state, node):
		def _onSave(link, event):
			element = link.getElement()
			world = fragment.getSubjectContext()['world']
			document = node.getDocument()
			
			if document.hasFilename():
				document.save()
			else:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )
				
		
		def _onSaveAs(link, event):
			element = link.getElement()
			world = fragment.getSubjectContext()['world']
			document = node.getDocument()
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn, document.getFilename() )


		def _onClose(link, event):
			element = link.getElement()
			world = fragment.getSubjectContext()['world']
			document = node.getDocument()

			def _performClose():
				document.close()
				node.appState.closeDocument( document )


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
		relLocation = node.getRelativeLocation()
			
		
		location = fragment.getSubjectContext()['location'] + '.documents.' + relLocation
		docLink = Hyperlink( name, location ).padX( 0.0, _appDocRightPadding )
		saveLink = Hyperlink( 'Save', _onSave )
		saveAsLink = Hyperlink( 'Save as', _onSaveAs )
		closeLink = Hyperlink( 'Close', _onClose )

		return GridRow( [ docLink, saveLink, saveAsLink, closeLink ] )



	@ObjectDispatchMethod( Application.AppConsole )
	def AppConsole(self, fragment, state, node):
		index = node.getIndex()
		name = 'Console %d'  %  ( index, )
		location = fragment.getSubjectContext()['location'] + '.consoles.c%d'  %  ( index, )
		consoleLink = Hyperlink( name, location ).padX( 0.0, _appDocRightPadding )
	
		return GridRow( [ consoleLink ] )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )



_view = AppView()
perspective = Perspective( _view.fragmentViewFunction, None )

