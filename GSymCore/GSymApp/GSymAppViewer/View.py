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

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Border import *

from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *

from BritefuryJ.GSym import GSymPerspective, GSymRelativeLocationResolver, GSymSubject
from BritefuryJ.GSym.PresCom import *

from GSymCore.GSymApp import Application
#from GSymCore.GSymApp.GSymAppViewer.GSymAppViewerCombinators import appState, appDocument, appConsole
from GSymCore.GSymApp import DocumentManagement

from GSymCore.PythonConsole import Console



_appDocRightPadding = 30.0
_controlsPadding = 5.0
_appDocumentControlsStyle = StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 20.0 ).withAttr( Primitive.border, FilledBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) ) )
_dcumentListTableStyle = StyleSheet2.instance.withAttr( Primitive.tableColumnSpacing, 15.0 ).withAttr( Primitive.tableRowSpacing, 5.0 )



def _joinLocation(x, y):
	if x == '':
		return y
	elif y == '':
		return x
	else:
		return x + '.' + y


	

def _hasDocForName(docs, name):
	for d in docs:
		if name == d.getName():
			return True
	return False

def _newDocumentName(docs):
	name = 'Untitled'
	if not _hasDocForName( docs, name ):
		return name
	
	index = 2
	name = 'Untitled' + str(index)
	while _hasDocForName( docs, name ):
		index += 1
		name = 'Untitled' + str(index)

	return name


def _newConsoleName(docs):
	name = 'Console'
	if not _hasDocForName( docs, name ):
		return name
	
	index = 2
	name = 'Console' + str(index)
	while _hasDocForName( docs, name ):
		index += 1
		name = 'Console' + str(index)

	return name



def _hasDocForLocation(docs, location):
	for d in docs:
		if location == d['location']:
			return True
	return False

def _uniqueDocumentLocation(docs, location):
	if not _hasDocForLocation( docs, location ):
		return location
	
	index = 2
	loc = 'Untitled_' + str(index)
	while _hasDocForLocation( docs, loc ):
		index += 1
		loc = 'Untitled_' + str(index)

	return loc


						
def _contentsList(controls, contentsLists, title):
	controlsBox = HBox( [ c.padX( _controlsPadding )   for c in controls ] )
	controlsBorder = _appDocumentControlsStyle.applyTo( Border( controlsBox ) )

	openDocumentsSeparator = HSeparator()
	
	docListBox = _dcumentListTableStyle.applyTo( RGrid( contentsLists ) )

	contentsBox = VBox( [ controlsBorder.pad( 2.0, 2.0 ), openDocumentsSeparator, docListBox.pad( 10.0, 2.0 ) ] )
	
	heading = Heading3( title )
	
	return VBox( [ heading, contentsBox.padX( 5.0, 0.0 ) ] )
						
						
						
class AppView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Application.AppState )
	def AppState(self, ctx, state, node):
		def _onNewDoc(link, event):
			def handleNewDocumentFn(unit):
				name = _newDocumentName( openDocuments )
				
				world = ctx.getSubjectContext()['world']
				doc = GSymDocument( world, unit )
				doc.setDocumentName( name )
				location = world.addNewDocument( doc )

				appDoc = Application.AppDocument( name, location )
				node.addOpenDocument( appDoc )
				
			element = link.getElement()
			openDocuments = node.getOpenDocuments()
			DocumentManagement.promptNewDocument( ctx.getSubjectContext()['world'], element, handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpenDoc(link, event):
			def handleOpenedDocumentFn(fullPath, document):
				head, documentName = os.path.split( fullPath )
				documentName, ext = os.path.splitext( documentName )
				
				world = ctx.getSubjectContext()['world']
				document.setDocumentName( documentName )
				location = world.addNewDocument( document )
				
				appDoc = Application.AppDocument( documentName, location )
				node.addOpenDocument( appDoc )

				
			element = link.getElement()
			DocumentManagement.promptOpenDocument( ctx.getSubjectContext()['world'], element.getRootElement().getComponent(), handleOpenedDocumentFn )
			
			return True

		
		def _onNewConsole(link, event):
			consoles = node.getConsoles()
			name = _newConsoleName( consoles )
			appConsole = Application.AppConsole( name )
			node.addConsole( appConsole )
			
			return True
		
			
			
		openDocViews = InnerFragment.map( node.getOpenDocuments(), state.withAttrs( location='' ) )
		consoles = InnerFragment.map( node.getConsoles(), state.withAttrs( location='' ) )
		
		systemLink = Hyperlink( 'SYSTEM PAGE', Location( 'system' ) )
		linkHeader = LinkHeaderBar( [ systemLink ] )
		
		title = TitleBar( 'gSym' )
		
		newLink = Hyperlink( 'NEW', _onNewDoc )
		openLink = Hyperlink( 'OPEN', _onOpenDoc )
		openDocumentsBox = _contentsList( [ newLink, openLink ], openDocViews, 'Documents' )
		
		
		newConsoleLink = Hyperlink( 'NEW', _onNewConsole )
		consolesBox = _contentsList( [ newConsoleLink ], consoles, 'Python consoles' )
		
		
		head = Head( [ linkHeader, title ] )
		body = Body( [ openDocumentsBox.pad( 10.0, 10.0 ).alignHLeft(), consolesBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		return Page( [ head, body ] )



	@ObjectDispatchMethod( Application.AppDocument )
	def AppDocument(self, ctx, state, node):
		def _onSave(link, event):
			element = link.getElement()
			world = ctx.getSubjectContext()['world']
			document = world.getDocument( location )
			
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs(link, event):
			element = link.getElement()
			world = ctx.getSubjectContext()['world']
			document = world.getDocument( location )
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )

			
		name = node.getName()
		location = node.getLocation()
			
		
		docLink = Hyperlink( name, Location( location ) ).padX( 0.0, _appDocRightPadding )
		saveLink = Hyperlink( 'SAVE', _onSave )
		saveAsLink = Hyperlink( 'SAVE AS', _onSaveAs )
	
		return GridRow( [ docLink, saveLink, saveAsLink ] )



	@ObjectDispatchMethod( Application.AppConsole )
	def AppConsole(self, ctx, state, node):
		name = node.getName()
		location = Location( '$consoles/' + name )
		consoleLink = Hyperlink( name, location ).padX( 0.0, _appDocRightPadding )
	
		return GridRow( [ consoleLink ] )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )



class GSymAppRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject.withTitle( 'gSym' )
		
		consolesIterator = locationIterator.consumeLiteral( '$consoles/' )
		if consolesIterator is not None:
			consoleName = consolesIterator.getSuffix()
			for console in enclosingSubject.getFocus().getConsoles():
				if consoleName == console.getName():
					return enclosingSubject.withFocus( console.getConsole() ).withPerspective( Console.consoleViewerPerspective ).withTitle( consoleName ).withSubjectContext( 
					        enclosingSubject.getSubjectContext().withAttrs( location=locationIterator.getLocation().getLocationString() ) )
			
			return None
		else:
			iterAfterDocName = locationIterator.consumeRegex( _docNameRegex )
			if iterAfterDocName is not None:
				documentName = iterAfterDocName.lastToken()
					
				world = enclosingSubject.getSubjectContext()['world']
				doc = world.getDocument( documentName )
				
				if doc is not None:
					subject = enclosingSubject.withFocus( doc ).withTitle( enclosingSubject.getTitle() + ' [' + documentName + ']' )
					subject = subject.withSubjectContext( enclosingSubject.getSubjectContext().withAttrs( document=doc, location=iterAfterDocName.getPrefix() ) )
					subject = subject.withCommandHistory( doc.getCommandHistory() )
					return doc.resolveRelativeLocation( subject, iterAfterDocName )

			return None


perspective = GSymPerspective( AppView(), StyleSheet2.instance, AttributeTable.instance, None, GSymAppRelativeLocationResolver() )

	
	
