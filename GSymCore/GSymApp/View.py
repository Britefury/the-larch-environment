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
from Britefury.gSym.gSymResolveContext import GSymResolveContext
from Britefury.gSym.gSymResolveResult import GSymResolveResult

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym.View import GSymViewContext
from BritefuryJ.GSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout


from GSymCore.Utils.LinkHeader import linkHeaderBar
from GSymCore.Utils.Title import titleBar
from GSymCore.Utils.TabbedBox import tabbedBox

from GSymCore.GSymApp.Styles import *
from GSymCore.GSymApp import NodeClasses as Nodes



class NameTextRepListener (ElementLinearRepresentationListener):
	
	def __init__(self):
		pass

	def textRepresentationModified(self, element, event):
		value = element.getTextRepresentation()
		ctx = element.getContext()
		node = ctx.getDocNode()
		node['name'] = value
		return True

_nameTextRepListener = NameTextRepListener()	



def nameEditor(ctx, node, state, style):
	name = node['name']
	
	text = ctx.text( style, name )
	return ctx.linearRepresentationListener( text, _nameTextRepListener )




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





class AppView (GSymViewObjectNodeDispatch):
	def __init__(self, document, app):
		self._document = document
		self._app = app
		
		
	@ObjectNodeDispatchMethod( Nodes.AppState )
	def AppState(self, ctx, state, node, openDocuments, configuration):
		def _onNew(link, buttonEvent):
			def handleNewDocumentFn(unit):
				world = self._app.getWorld()
				
				name = _newDocumentName( openDocuments )
				
				doc = GSymDocument( world, unit )
				doc.setDocumentName( name )
				location = world.addNewDocument( doc )

				appDoc = Nodes.AppDocument( name=name, location=location )
				openDocuments.append( appDoc )
				
				
			self._app.promptNewDocument( handleNewDocumentFn )
			
			return True
		
			
			
		def _onOpen(link, buttonEvent):
			def handleOpenedDocumentFn(fullPath, document):
				world = self._app.getWorld()
				
				head, documentName = os.path.split( fullPath )
				documentName, ext = os.path.splitext( documentName )
				
				document.setDocumentName( documentName )
				location = world.addNewDocument( document )
				
				appDoc = Nodes.AppDocument( name=documentName, location=location )
				openDocuments.append( appDoc )

				
			self._app.promptOpenDocument( handleOpenedDocumentFn )
			
			return True

			
			
		systemLink = ctx.link( app_linkStyle, 'SYSTEM PAGE', 'system' )
		linkHeader = linkHeaderBar( ctx, [ systemLink ] )
		
		title = titleBar( ctx, 'gSym' )
		
		newLink = ctx.link( app_linkStyle, 'NEW', _onNew )
		openLink = ctx.link( app_linkStyle, 'OPEN', _onOpen )
		controlsBox = ctx.hbox( app_openDocumentsControlsBoxStyle, [ newLink.padX( 5.0 ), openLink.padX( 5.0 ) ] )
		controlsBorder = ctx.border( app_openDocumentsControlsBorder, controlsBox )
		
		openDocumentsSeparatingLine = ctx.line( app_openDocumentsLineStyle )
		
		docListBox = ctx.rgrid( app_openDocumentsGridStyle, ctx.mapViewEval( openDocuments, _AppViewState( '' ) ) )

		openDocumentsContentsBox = ctx.vbox( app_openDocumentsBoxStyle, [ controlsBorder.pad( 2.0, 2.0 ), openDocumentsSeparatingLine.alignHExpand(), docListBox.pad( 10.0, 2.0 ) ] )
		openDocumentsBox = tabbedBox( ctx, 'Documents', openDocumentsContentsBox )
		
		contentBox = ctx.vbox( app_contentBoxStyle, [ linkHeader, title, openDocumentsBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()



	@ObjectNodeDispatchMethod( Nodes.AppDocument )
	def AppDocument(self, ctx, state, node, name, location):
		def _onSave(link, buttonEvent):
			world = self._app.getWorld()
			document = world.getDocument( location )
			
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				self._app.promptSaveDocumentAs( handleSaveDocumentAsFn )
			else:
				document.save()
			
			return True
				
		
		def _onSaveAs(link, buttonEvent):
			world = self._app.getWorld()
			document = world.getDocument( location )
			
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			self._app.promptSaveDocumentAs( handleSaveDocumentAsFn )
			
			return True

			
			
		loc, = state
		
		docLink = ctx.border( app_docLinkBorder, ctx.link( app_docLinkStyle, name, location ) )
		saveLink = ctx.link( app_docLinkStyle, 'SAVE', _onSave )
		saveAsLink = ctx.link( app_docLinkStyle, 'SAVE AS', _onSaveAs )

		return ctx.gridRow( app_docGridRowStyle, [ docLink, saveLink, saveAsLink ] )



	
	


def viewGSymAppDocNodeAsElement(document, docRootNode, resolveContext, location, commandHistory, app):
	viewContext = GSymViewContext( docRootNode, AppView( document, app ), commandHistory )
	return viewContext.getFrame()



def viewGSymAppDocNodeAsPage(document, docRootNode, resolveContext, location, commandHistory, app):
	return GSymViewPage( 'gSym', viewGSymAppDocNodeAsElement( document, docRootNode, resolveContext, location, commandHistory, app ), commandHistory )



def resolveGSymAppLocation(currentUnitClass, document, docRootNode, resolveContext, location, app):
	if location == '':
		return GSymResolveResult( document, docRootNode, currentUnitClass, GSymAppResolveContext( resolveContext, '' ), location )
	else:
		documentLocation, dot, tail = location.partition( '.' )
		
		doc = app.getWorld().getDocument( documentLocation )
		
		if doc is not None:
			return doc.resolveLocation( GSymAppResolveContext( resolveContext, documentLocation + dot ), tail, app )
		else:
			return None


class GSymAppResolveContext (GSymResolveContext):
	pass
