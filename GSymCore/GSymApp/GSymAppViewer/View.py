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

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch, GSymViewPage
from Britefury.gSym.gSymDocument import GSymDocument

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction

from GSymCore.GSymApp import NodeClasses as Nodes
from GSymCore.GSymApp.GSymAppViewer.GSymAppViewerStyleSheet import GSymAppViewerStyleSheet



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





class AppView (GSymViewObjectNodeDispatch):
	@ObjectNodeDispatchMethod( Nodes.AppState )
	def AppState(self, ctx, styleSheet, state, node, openDocuments, configuration):
		def _onNew():
			def handleNewDocumentFn(unit):
				name = _newDocumentName( openDocuments )
				
				world = ctx.getSubjectContext()['document'].getWorld()
				doc = GSymDocument( world, unit )
				doc.setDocumentName( name )
				location = world.addNewDocument( doc )

				appDoc = Nodes.AppDocument( name=name, location=location )
				openDocuments.append( appDoc )
				
			
			app = ctx.getViewContext().getBrowserContext().app
			app.promptNewDocument( handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpen():
			def handleOpenedDocumentFn(fullPath, document):
				head, documentName = os.path.split( fullPath )
				documentName, ext = os.path.splitext( documentName )
				
				world = ctx.getSubjectContext()['document'].getWorld()
				document.setDocumentName( documentName )
				location = world.addNewDocument( document )
				
				appDoc = Nodes.AppDocument( name=documentName, location=location )
				openDocuments.append( appDoc )

				
			app = ctx.getViewContext().getBrowserContext().app
			app.promptOpenDocument( handleOpenedDocumentFn )
			
			return True

		
		openDocViews = ctx.mapPresentFragment( openDocuments, styleSheet, state.withAttrs( location='' ) )
		
		return styleSheet.appState( openDocViews, _onNew, _onOpen )



	@ObjectNodeDispatchMethod( Nodes.AppDocument )
	def AppDocument(self, ctx, styleSheet, state, node, name, location):
		def _onSave():
			world = ctx.getSubjectContext()['document'].getWorld()
			document = world.getDocument( location )
			
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				app = ctx.getViewContext().getBrowserContext().app
				app.promptSaveDocumentAs( handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs():
			world = ctx.getSubjectContext()['document'].getWorld()
			document = world.getDocument( location )
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			app = ctx.getViewContext().getBrowserContext().app
			app.promptSaveDocumentAs( handleSaveDocumentAsFn )

			
			
		return styleSheet.appDocument( name, Location( location ), _onSave, _onSaveAs )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )

class GSymAppViewerPerspective (GSymPerspective):
	def __init__(self):
		self._viewFn = PyGSymViewFragmentFunction( AppView() )
		
	
	
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject
		else:
			iterAfterDocName = locationIterator.consumeRegex( _docNameRegex )
			if iterAfterDocName is not None:
				documentName = iterAfterDocName.lastToken()
					
				world = enclosingSubject.getSubjectContext()['document'].getWorld()
				doc = world.getDocument( documentName )
				
				if doc is not None:
					subject = GSymSubject( doc, self, enclosingSubject.getSubjectContext().withAttrs( document=doc, location=iterAfterDocName.getPrefix() ) )
					return doc.resolveRelativeLocation( subject, iterAfterDocName )

			return None
	
	
	def getFragmentViewFunction(self):
		return self._viewFn
	
	def getStyleSheet(self):
		return GSymAppViewerStyleSheet.instance
	
	def getEditHandler(self):
		return None
	
