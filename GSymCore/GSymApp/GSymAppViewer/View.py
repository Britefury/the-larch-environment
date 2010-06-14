##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

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

from BritefuryJ.GSym import GSymPerspective, GSymRelativeLocationResolver, GSymSubject

from GSymCore.GSymApp import Application
from GSymCore.GSymApp.GSymAppViewer.GSymAppViewerStyleSheet import GSymAppViewerStyleSheet
from GSymCore.GSymApp import DocumentManagement

from GSymCore.PythonConsole import Console



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


						
						
						
						
class AppView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Application.AppState )
	def AppState(self, ctx, styleSheet, state, node):
		def _onNewDoc(element):
			def handleNewDocumentFn(unit):
				name = _newDocumentName( openDocuments )
				
				world = ctx.getSubjectContext()['world']
				doc = GSymDocument( world, unit )
				doc.setDocumentName( name )
				location = world.addNewDocument( doc )

				appDoc = Application.AppDocument( name, location )
				node.addOpenDocument( appDoc )
				
			openDocuments = node.getOpenDocuments()
			DocumentManagement.promptNewDocument( ctx.getSubjectContext()['world'], element, handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpenDoc(element):
			def handleOpenedDocumentFn(fullPath, document):
				head, documentName = os.path.split( fullPath )
				documentName, ext = os.path.splitext( documentName )
				
				world = ctx.getSubjectContext()['world']
				document.setDocumentName( documentName )
				location = world.addNewDocument( document )
				
				appDoc = Application.AppDocument( documentName, location )
				node.addOpenDocument( appDoc )

				
			DocumentManagement.promptOpenDocument( ctx.getSubjectContext()['world'], element.getRootElement().getComponent(), handleOpenedDocumentFn )
			
			return True

		
		def _onNewConsole():
			consoles = node.getConsoles()
			name = _newConsoleName( consoles )
			appConsole = Application.AppConsole( name )
			node.addConsole( appConsole )
			
			return True
		
			
			
		openDocViews = ctx.mapPresentFragment( node.getOpenDocuments(), styleSheet, state.withAttrs( location='' ) )
		consoles = ctx.mapPresentFragment( node.getConsoles(), styleSheet, state.withAttrs( location='' ) )
		
		return styleSheet.appState( openDocViews, consoles, _onNewDoc, _onOpenDoc, _onNewConsole )



	@ObjectDispatchMethod( Application.AppDocument )
	def AppDocument(self, ctx, styleSheet, state, node):
		def _onSave(element):
			world = ctx.getSubjectContext()['world']
			document = world.getDocument( location )
			
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs(element):
			world = ctx.getSubjectContext()['world']
			document = world.getDocument( location )
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			DocumentManagement.promptSaveDocumentAs( world, element.getRootElement().getComponent(), handleSaveDocumentAsFn )

			
		name = node.getName()
		location = node.getLocation()
		return styleSheet.appDocument( name, Location( location ), _onSave, _onSaveAs )



	@ObjectDispatchMethod( Application.AppConsole )
	def AppConsole(self, ctx, styleSheet, state, node):
		name = node.getName()
		return styleSheet.appConsole( name, Location( '$consoles/' + name ) )



	
	
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


perspective = GSymPerspective( AppView(), GSymAppViewerStyleSheet.instance, AttributeTable.instance, None, GSymAppRelativeLocationResolver() )

	
	
