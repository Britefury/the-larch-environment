##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from java.awt.event import KeyEvent

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch, GSymViewPage
from Britefury.gSym.gSymDocument import GSymDocument

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleSheet import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction

from GSymCore.GSymApp import NodeClasses as Nodes
from GSymCore.GSymApp.GSymAppViewer.GSymAppViewerStyleSheet import GSymAppViewerStyleSheet



def _AppViewState(location):
	return ( location, )




def _joinLocation(x, y):
	if x == '':
		return y
	elif y == '':
		return x
	else:
		return x + '.' + y


	

def _hasDocForName(docs, name):
	for d in docs:
		if name == d['name']:
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





	# handleNewDocumentFn(unit)
	def promptNewDocument(self, handleNewDocumentFn):
		def _make_newDocument(newUnitFn):
			def newDoc():
				unit = newUnitFn()
				handleNewDocumentFn( unit )
			return newDoc
		newDocumentMenu = JPopupMenu( 'New document' )
		for newUnitFactory in self._world.newUnitFactories:
			newDocumentMenu.add( _action( newUnitFactory.menuLabelText, _make_newDocument( newUnitFactory.newDocumentFn ) ) )
		pos = self._frame.getMousePosition( True )
		newDocumentMenu.show( self._frame, pos.x, pos.y )
	
		
		
	# handleOpenedDocumentFn(fullPath, document)
	def promptOpenDocument(self, handleOpenedDocumentFn):
		openDialog = JFileChooser()
		openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
		response = openDialog.showDialog( self._frame, 'Open' )
		if response == JFileChooser.APPROVE_OPTION:
			sf = openDialog.getSelectedFile()
			if sf is not None:
				filename = sf.getPath()
				if filename is not None:
					document = GSymDocument.readFile( self._world, filename )
					if document is not None:
						handleOpenedDocumentFn( filename, document )
	
	
	
	# handleSaveDocumentAsFn(filename)
	def promptSaveDocumentAs(self, handleSaveDocumentAsFn):
		filename = None
		bFinished = False
		while not bFinished:
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
			response = openDialog.showSaveDialog( self._frame )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filenameFromDialog = sf.getPath()
					if filenameFromDialog is not None:
						if os.path.exists( filenameFromDialog ):
							response = JOptionPane.showOptionDialog( self._frame, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
							if response == JFileChooser.APPROVE_OPTION:
								filename = filenameFromDialog
								bFinished = True
							else:
								bFinished = False
						else:
							filename = filenameFromDialog
							bFinished = True
					else:
						bFinished = True
				else:
					bFinished = True
			else:
				bFinished = True

		if filename is not None:
			handleSaveDocumentAsFn( filename )
			return True
		else:
			return False


class AppView (GSymViewObjectNodeDispatch):
	def __init__(self, world):
		self._world = world
		
		
	@ObjectNodeDispatchMethod( Nodes.AppState )
	def AppState(self, ctx, styleSheet, state, node, openDocuments, configuration):
		def _onNew():
			def handleNewDocumentFn(unit):
				name = _newDocumentName( openDocuments )
				
				doc = GSymDocument( self._world, unit )
				doc.setDocumentName( name )
				location = self._world.addNewDocument( doc )

				appDoc = Nodes.AppDocument( name=name, location=location )
				openDocuments.append( appDoc )
				
			
			app = ctx.getViewContext().getBrowserContext().app
			app.promptNewDocument( handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpen():
			def handleOpenedDocumentFn(fullPath, document):
				head, documentName = os.path.split( fullPath )
				documentName, ext = os.path.splitext( documentName )
				
				document.setDocumentName( documentName )
				location = self._world.addNewDocument( document )
				
				appDoc = Nodes.AppDocument( name=documentName, location=location )
				openDocuments.append( appDoc )

				
			app = ctx.getViewContext().getBrowserContext().app
			app.promptOpenDocument( handleOpenedDocumentFn )
			
			return True

		
		openDocViews = ctx.mapPresentFragment( openDocuments, styleSheet, _AppViewState( '' ) )
		
		return styleSheet.appState( openDocViews, _onNew, _onOpen )



	@ObjectNodeDispatchMethod( Nodes.AppDocument )
	def AppDocument(self, ctx, styleSheet, state, node, name, location):
		def _onSave():
			document = self._world.getDocument( location )
			
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				app = ctx.getViewContext().getBrowserContext().app
				app.promptSaveDocumentAs( handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs():
			document = self._world.getDocument( location )
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			app = ctx.getViewContext().getBrowserContext().app
			app.promptSaveDocumentAs( handleSaveDocumentAsFn )

			
			
		loc, = state
		
		return styleSheet.appDocument( name, location, _onSave, _onSaveAs )



	
	


class GSymAppViewerPerspective (GSymPerspective):
	def __init__(self, world):
		self._world = world
		self._viewFn = PyGSymViewFragmentFunction( AppView( world ) )
		
	
	
	def resolveRelativeLocation(self, enclosingSubject, relativeLocation):
		if relativeLocation == '':
			return enclosingSubject
		else:
			documentLocation, dot, tail = relativeLocation.partition( '.' )
			
			doc = self._world.getDocument( documentLocation )
			
			if doc is not None:
				subject = enclosingSubject.enclosedSubjectWithNewDocument( enclosingSubject.getFocus(), self, doc, documentLocation, dot )
				return doc.resolveRelativeLocation( subject, tail )
			else:
				return None
	
	
	def getFragmentViewFunction(self):
		return self._viewFn
	
	def getStyleSheet(self):
		return GSymAppViewerStyleSheet.instance
	
	def getEditHandler(self):
		return None
	
