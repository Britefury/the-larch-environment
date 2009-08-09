##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt.event import KeyEvent

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch
from Britefury.gSym import gSymDocument

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Browser import Page
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym.View import GSymViewInstance
from BritefuryJ.GSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout


from GSymCore.Project.Styles import *
from GSymCore.Project import NodeClasses as Nodes



class NameTextRepListener (ElementLinearRepresentationListener):
	
	def __init__(self):
		pass

	def textRepresentationModified(self, element, event):
		value = element.getTextRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		node['name'] = value
		return True

_nameTextRepListener = NameTextRepListener()	



def nameEditor(ctx, node, state, style):
	name = node['name']
	
	text = ctx.text( style, name )
	return ctx.linearRepresentationListener( text, _nameTextRepListener )




def _ProjectViewState(location):
	return ( location, )




def _joinLocation(x, y):
	if x == '':
		return y
	elif y == '':
		return x
	else:
		return x + '.' + y


class ProjectView (GSymViewObjectNodeDispatch):
	__dispatch_module__ = Nodes.module
	
	
	def __init__(self):
		pass
		
		
	@ObjectNodeDispatchMethod
	def Project(self, ctx, state, node, rootPackage):
		title = ctx.staticText( prj_projectTitleStyle, 'Project' )
		titleBox = ctx.vbox( prj_projectTitleBoxStyle, [ title ] )
		
		indexHeader = ctx.staticText( prj_projectIndexHeaderStyle, 'Index' )
		indexBox = ctx.vbox( prj_projectIndexBoxStyle, [ indexHeader, ctx.viewEval( rootPackage, _ProjectViewState( '' ) ) ] )
		
		contentBox = ctx.vbox( prj_projectContentBoxStyle, [ titleBox, indexBox ] )
		
		pageBox = ctx.vbox( prj_projectPageBoxStyle, [ contentBox ] )
		
		return pageBox



	@ObjectNodeDispatchMethod
	def Package(self, ctx, state, node, name, contents):
		location, = state
		
		def _onAddPage(button):
			def _add(pageUnit):
				#contents.append( Nodes.Page( name='New page', unit=pageUnit ) )
				p = Nodes.Page( name='New page', unit=pageUnit )
				contents.append( p )
			ctx.getViewContext().getOwner()._app.promptNewPage( _add )
		
		def _onImportPage(button):
			def _import(name, pageUnit):
				contents.append( Nodes.Page( name=name, unit=pageUnit ) )
			ctx.getViewContext().getOwner()._app.promptImportPage( _import )
		
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

		return ctx.vbox( prj_packageBoxStyle, [ headerBox, ctx.indent( 20.0, contentsBox ) ] )



	@ObjectNodeDispatchMethod
	def Page(self, ctx, state, node, name, unit):
		location, = state
		
		nameEntry = nameEditor( ctx, node, state, prj_pageNameStyle )
		
		editLink = ctx.link( prj_pageEditLinkStyle, 'Edit', _joinLocation( location, name ) )

		return ctx.hbox( prj_pageBoxStyle, [ nameEntry, editLink ] )

	
	


class _ProjectViewPage (Page):
	def __init__(self, docRootNode, location, commandHistory, app):
		self._docRootNode = docRootNode
		self._location = location
		self._frame = DPFrame()
		self._viewFn = ProjectView()
		self._app = app
		viewContext = GSymViewInstance( docRootNode, self._viewFn, self._viewRootFn, commandHistory, self )
		self._frame = viewContext.getFrame()
		#self._frame.setEditHandler( Python25EditHandler( viewContext ) )
		
		
	def getContentsElement(self):
		return self._frame
		
		
	def _viewRootFn(self, node, ctx, state):
		return self._viewFn( node, ctx, state )

	
def _viewPage(docRootNode, location, commandHistory, app):
	return gSymDocument.viewUnitLocationAsPage( docRootNode, location, app._world, commandHistory, app )

def viewLocationAsPage(docRootNode, location, commandHistory, app):
	if location == '':
		return _ProjectViewPage( docRootNode, location, commandHistory, app )
	else:
		loc = location
		package = docRootNode['rootPackage']
		while '.' in loc:
			dotPos = loc.index( '.' )
			name = loc[:dotPos]
			loc = loc[dotPos+1:]
			node = None
			for n in package['contents']:
				if n['name'] == name:
					node = n
					break
			if n is None:
				return None
			elif isinstance( n, DMObjectInterface ):
				if n.isInstanceOf( Nodes.Package ):
					package = n
				elif n.isInstanceOf( Nodes.Page ):
					return _viewPage( n['unit'], loc, commandHistory, app )
				else:
					return None
			else:
				return None
		return None


