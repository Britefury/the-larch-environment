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

from Britefury.gSym.gSymResolveContext import GSymResolveContext
from Britefury.gSym.gSymResolveResult import GSymResolveResult
from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch, GSymViewPage

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym.View import GSymViewContext


from GSymCore.Project.Styles import *
from GSymCore.Project import NodeClasses as Nodes



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




def _ProjectViewState(location):
	return ( location, )




def _joinLocation(*xs):
	loc = ''
	for x in xs:
		if x != '':
			if loc != '':
				loc += '.'
			loc += x
	return loc


class ProjectView (GSymViewObjectNodeDispatch):
	def __init__(self, document, app, resolveContext, location):
		self._document = document
		self._app = app
		self._resolveContext = resolveContext
		self._location = location
		
		
	@ObjectNodeDispatchMethod( Nodes.Project )
	def Project(self, ctx, state, node, rootPackage):
		def _onSave(link, buttonEvent):
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				self._app.promptSaveDocumentAs( handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs(link, buttonEvent):
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			self._app.promptSaveDocumentAs( handleSaveDocumentAsFn )
		
		
		document = self._document
		
		name = document.getDocumentName()
		
		homeLink = ctx.link( prj_linkStyle, 'HOME PAGE', '' )
		linkHeader = linkHeaderBar( ctx, [ homeLink ] )
		
		title = titleBarWithHeader( ctx, 'DOCUMENT', name )
		
		
		saveLink = ctx.link( prj_linkStyle, 'SAVE', _onSave )
		saveAsLink = ctx.link( prj_linkStyle, 'SAVE AS', _onSaveAs )
		controlsBox = ctx.hbox( prj_controlsBoxStyle, [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = ctx.border( prj_controlsBorder, controlsBox )
		
		state = _ProjectViewState( self._resolveContext.location )
		root = ctx.viewEval( rootPackage, state ).alignHExpand()
		indexBox = tabbedBox( ctx, 'Project Index', root )
		
		contentBox = ctx.vbox( prj_projectContentBoxStyle, [ linkHeader, title, controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), indexBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()



	@ObjectNodeDispatchMethod( Nodes.Package )
	def Package(self, ctx, state, node, name, contents):
		location, = state
		
		def _onAddPage(button):
			def _add(pageUnit):
				#contents.append( Nodes.Page( name='New page', unit=pageUnit ) )
				p = Nodes.Page( name='New page', unit=pageUnit )
				contents.append( p )
			self._app.promptNewPage( _add )
		
		def _onImportPage(button):
			def _import(name, pageUnit):
				contents.append( Nodes.Page( name=name, unit=pageUnit ) )
			self._app.promptImportPage( _import )
		
		def _onAddPackage(button):
			contents.append( Nodes.Package( name='New package', contents=[] ) )
		
		addPageButton = ctx.button( prj_packageAddButtonStyle, _onAddPage, ctx.staticText( prj_packageButtonLabelStyle, 'Add page' ) )
		importPageButton = ctx.button( prj_packageAddButtonStyle, _onImportPage, ctx.staticText( prj_packageButtonLabelStyle, 'Import page' ) )
		addPackageButton = ctx.button( prj_packageAddButtonStyle, _onAddPackage, ctx.staticText( prj_packageButtonLabelStyle, 'Add package' ) )
		
		pageControlsBox = ctx.hbox( prj_packagePageControlsBoxStyle, [ addPageButton, importPageButton ] )
		controlsBox = ctx.hbox( prj_packageControlsBoxStyle, [ pageControlsBox, addPackageButton ] )

		nameEntry = nameEditor( ctx, node, state, prj_packageNameStyle )
		
		headerBox = ctx.hbox( prj_packageHeaderBoxStyle, [ nameEntry, controlsBox ] )
		
		contentsBox = ctx.vbox( prj_packageContentsBoxStyle, ctx.mapViewEval( contents, _ProjectViewState( _joinLocation( location, name ) ) ) )

		return ctx.vbox( prj_packageBoxStyle, [ headerBox.alignHExpand(), ctx.indent( 20.0, contentsBox ).alignHExpand() ] )



	@ObjectNodeDispatchMethod( Nodes.Page )
	def Page(self, ctx, state, node, name, unit):
		location, = state
		
		nameEntry = nameEditor( ctx, node, state, prj_pageNameStyle )
		
		editLink = ctx.link( prj_pageEditLinkStyle, 'Edit', _joinLocation( location, name ) )

		return ctx.hbox( prj_pageBoxStyle, [ nameEntry, editLink ] )

	
	
def viewProjectDocNodeAsElement(document, docRootNode, resolveContext, location, commandHistory, app):
	viewFn = ProjectView( document, app, resolveContext, location )
	viewContext = GSymViewContext( docRootNode, viewFn, commandHistory )
	return viewContext.getFrame()



def viewProjectDocNodeAsPage(document, docRootNode, resolveContext, location, commandHistory, app):
	return GSymViewPage( resolveContext.getTitle(), viewProjectDocNodeAsElement( document, docRootNode, resolveContext, location, commandHistory, app ), commandHistory )



def resolveProjectLocation(currentUnitClass, document, docRootNode, resolveContext, location, app):
	if location == '':
		return GSymResolveResult( document, docRootNode, currentUnitClass, ProjectResolveContext( resolveContext, '', document ), location )
	else:
		# Attempt to enter the root package
		rootPackagePrefix = docRootNode['rootPackage']['name'] + '.'
		if location.startswith( rootPackagePrefix ):
			locationPrefix = rootPackagePrefix
			loc = location[len(rootPackagePrefix):]
			package = docRootNode['rootPackage']
		else:
			return None
		
		while loc != '':
			try:
				separatorPos = loc.index( '.' )
				name = loc[:separatorPos]
				loc = loc[separatorPos+1:]
				locationPrefix += name + '.'
			except ValueError:
				separatorPos = len( loc )
				name = loc
				loc = ''
				locationPrefix += name
			node = None
			for n in package['contents']:
				if n['name'] == name:
					node = n
					break
			if node is None:
				return None
			elif isinstance( node, DMObjectInterface ):
				if node.isInstanceOf( Nodes.Package ):
					package = node
				elif n.isInstanceOf( Nodes.Page ):
					return document.resolveUnitLocation( node['unit'], ProjectResolveContext( resolveContext, locationPrefix, document ), loc, app )
				else:
					return None
			else:
				return None
		return None



class ProjectResolveContext (GSymResolveContext):
	def __init__(self, innerContext, location, document):
		super( ProjectResolveContext, self ).__init__( innerContext, location )
		if location == '':
			self._title = document.getDocumentName()
		else:
			self._title = location + ' [' + document.getDocumentName() + ']'
		
	def getTitle(self):
		return self._title

