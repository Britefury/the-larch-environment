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
from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectDispatch
from Britefury.gSym.gSymDocument import GSymDocument


from Britefury.Util.NodeUtil import *


from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Border import *

from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.PresCom import *

from GSymCore.GSymApp import Application
#from GSymCore.GSymApp.GSymAppViewer.GSymAppViewerCombinators import appState, appDocument, appConsole
from GSymCore.GSymApp import DocumentManagement

from GSymCore.PythonConsole import Console



_appDocRightPadding = 30.0
_controlsPadding = 5.0
_appDocumentControlsStyle = StyleSheet.instance.withAttr( Primitive.rowSpacing, 20.0 ).withAttr( Primitive.border, FilledBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) ) )
_documentListTableStyle = StyleSheet.instance.withAttr( Primitive.tableColumnSpacing, 15.0 ).withAttr( Primitive.tableRowSpacing, 5.0 )



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
	
	heading = Heading3( title )
	
	return Column( [ heading, contentsBox.padX( 5.0, 0.0 ) ] )
						
						
						
class AppView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Application.AppState )
	def AppState(self, fragment, state, node):
		def _onNewDoc(link, event):
			def handleNewDocumentFn(document):
				name = _newDocumentName( openDocuments )
				document.setDocumentName( name )
				
				appDoc = node.registerOpenDocument( document, fragment.getSubjectContext()['location'].getLocationString() + '.documents' )
				
			element = link.getElement()
			openDocuments = node.getOpenDocuments()
			DocumentManagement.promptNewDocument( fragment.getSubjectContext()['world'], element, handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpenDoc(link, event):
			def handleOpenedDocumentFn(fullPath, document):
				head, documentName = os.path.split( fullPath )
				documentName, ext = os.path.splitext( documentName )
				
				document.setDocumentName( documentName )
				appDoc = node.registerOpenDocument( document, fragment.getSubjectContext()['location'].getLocationString() + '.documents' )

				
			element = link.getElement()
			DocumentManagement.promptOpenDocument( fragment.getSubjectContext()['world'], element.getRootElement().getComponent(), handleOpenedDocumentFn )
			
			return True

		
		def _onNewConsole(link, event):
			consoles = node.getConsoles()
			index = _newConsoleIndex( consoles )
			appConsole = Application.AppConsole( index )
			node.addConsole( appConsole )
			
			return True
		
			
			
		openDocViews = InnerFragment.map( node.getOpenDocuments(), state.withAttrs( location='' ) )
		consoles = InnerFragment.map( node.getConsoles(), state.withAttrs( location='' ) )
		
		systemLink = Hyperlink( 'SYSTEM PAGE', Location( 'system' ) )
		configurationLink = Hyperlink( 'CONFIGURATION PAGE', Location( 'config' ) )
		linkHeader = LinkHeaderBar( [ configurationLink, systemLink ] )
		
		title = TitleBar( 'gSym' )
		
		newLink = Hyperlink( 'NEW', _onNewDoc )
		openLink = Hyperlink( 'OPEN', _onOpenDoc )
		openDocumentsBox = _contentsList( [ newLink, openLink ], openDocViews, 'Documents' )
		
		
		newConsoleLink = Hyperlink( 'NEW', _onNewConsole )
		consolesBox = _contentsList( [ newConsoleLink ], consoles, 'Python consoles' )
		
		
		head = Head( [ linkHeader, title ] )
		body = Body( [ openDocumentsBox.pad( 10.0, 10.0 ).alignHLeft(), consolesBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		return StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( Page( [ head, body ] ) )



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
			
			DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )

			
		name = node.getName()
		relLocation = node.getRelativeLocation()
			
		
		location = fragment.getSubjectContext()['location'] + '.documents.' + relLocation
		docLink = Hyperlink( name, location ).padX( 0.0, _appDocRightPadding )
		saveLink = Hyperlink( 'SAVE', _onSave )
		saveAsLink = Hyperlink( 'SAVE AS', _onSaveAs )
	
		return GridRow( [ docLink, saveLink, saveAsLink ] )



	@ObjectDispatchMethod( Application.AppConsole )
	def AppConsole(self, fragment, state, node):
		index = node.getIndex()
		name = 'Console %d'  %  ( index, )
		location = fragment.getSubjectContext()['location'] + '.consoles[%d]'  %  ( index, )
		consoleLink = Hyperlink( name, location ).padX( 0.0, _appDocRightPadding )
	
		return GridRow( [ consoleLink ] )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )




perspective = GSymPerspective( AppView(), None )

