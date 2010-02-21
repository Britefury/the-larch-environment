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


from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet
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



def nameEditor(ctx, styleSheet, node, state):
	name = node['name']
	
	text = styleSheet.text( name )
	text.setLinearRepresentationListener( _nameTextRepListener )
	return text




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
	def Project(self, ctx, styleSheet, state, node, rootPackage):
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
		
		homeLink = prj_linkStyle.link( 'HOME PAGE', '' )
		linkHeader = prj_linkHeaderStyle.linkHeaderBar( [ homeLink ] )
		
		title = prj_titleStyle.titleBarWithHeader( 'DOCUMENT', name )
		
		
		saveLink = prj_controlsStyle.link( 'SAVE', _onSave )
		saveAsLink = prj_controlsStyle.link( 'SAVE AS', _onSaveAs )
		controlsBox = prj_controlsStyle.hbox( [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = prj_controlsStyle.border( controlsBox )
		
		state = _ProjectViewState( self._resolveContext.location )
		root = ctx.viewEval( rootPackage, styleSheet, state ).alignHExpand()
		indexBox = prj_tabbedBoxStyle.tabbedBox( 'Project Index', root )
		
		contentBox = prj_projectContentBoxStyle.vbox( [ linkHeader, title, controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), indexBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()



	@ObjectNodeDispatchMethod( Nodes.Package )
	def Package(self, ctx, styleSheet, state, node, name, contents):
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
		
		addPageButton = prj_packageButtonStyle.button( _onAddPage, prj_packageButtonStyle.staticText( 'Add page' ) )
		importPageButton = prj_packageButtonStyle.button( _onImportPage, prj_packageButtonStyle.staticText( 'Import page' ) )
		addPackageButton = prj_packageButtonStyle.button( _onAddPackage, prj_packageButtonStyle.staticText( 'Add package' ) )
		
		pageControlsBox = prj_packagePageControlsStyle.hbox( [ addPageButton, importPageButton ] )
		controlsBox = prj_packageControlsStyle.hbox( [ pageControlsBox, addPackageButton ] )

		nameEntry = nameEditor( ctx, prj_packageNameStyle, node, state )
		
		headerBox = prj_packageHeaderStyle.hbox( [ nameEntry, controlsBox ] )
		
		contentsBox = prj_packageContentsStyle.vbox( ctx.mapViewEval( contents, styleSheet, _ProjectViewState( _joinLocation( location, name ) ) ) )

		return prj_packageStyle.vbox( [ headerBox.alignHExpand(), contentsBox.padX( 20.0, 0.0 ).alignHExpand() ] )



	@ObjectNodeDispatchMethod( Nodes.Page )
	def Page(self, ctx, styleSheet, state, node, name, unit):
		location, = state
		
		nameEntry = nameEditor( ctx, prj_pageStyle, node, state )
		
		editLink = prj_pageStyle.link( 'Edit', _joinLocation( location, name ) )

		return prj_pageStyle.hbox( [ nameEntry, editLink ] )

	
	
def viewProjectDocNodeAsElement(document, docRootNode, resolveContext, location, commandHistory, app):
	viewFn = ProjectView( document, app, resolveContext, location )
	viewContext = GSymViewContext( docRootNode, viewFn, PrimitiveStyleSheet.instance, commandHistory )
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

