##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
from datetime import datetime

from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet, RichTextStyleSheet
from BritefuryJ.DocPresent.Controls import ControlsStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction


from GSymCore.GSymApp import DocumentManagement

from GSymCore.Project import Schema
from GSymCore.Project.ProjectEditor.ProjectEditorStyleSheet import ProjectEditorStyleSheet



_controlsStyle = ControlsStyleSheet.instance.withClosePopupOnActivate()

# handleNewPageFn(unit)
def _newPageMenu(world, handleNewPageFn):
	def _make_newPage(newPageFn):
		def newPage(menuItem):
			unit = newPageFn()
			handleNewPageFn( unit )
		return newPage
	items = []
	for newPageFactory in world.newPageFactories:
		items.append( _controlsStyle.menuItemWithLabel( newPageFactory.menuLabelText, _make_newPage( newPageFactory.newPageFn ) ).getElement() )
	return _controlsStyle.vpopupMenu( items )
	
	
	
# handleImportedPageFn(name, unit)
def _importPageMenu(world, component, handleImportedPageFn):
	def _make_importPage(fileType, filePattern, importUnitFn):
		def _import(actionEvent):
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( fileType, [ filePattern ] ) )
			response = openDialog.showDialog( component, 'Import' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						t1 = datetime.now()
						unit = importUnitFn( filename )
						t2 = datetime.now()
						if unit is not None:
							unitName = os.path.splitext( filename )[0]
							unitName = os.path.split( unitName )[1]
							print 'ProjectEditor.View: IMPORT TIME = %s'  %  ( t2 - t1, )
							handleImportedPageFn( unitName, unit )
		return _import

	items = []
	for pageImporter in world.pageImporters:
		items.append( _controlsStyle.menuItemWithLabel( pageImporter.menuLabelText, _make_importPage( pageImporter.fileType, pageImporter.filePattern, pageImporter.importFn ) ).getElement() )
	return _controlsStyle.vpopupMenu( items )




def _ProjectViewState(location):
	return ( location, )




def _joinLocation(*xs):
	s = '.'.join( [ str( x )   for x in xs ] )
	return Location( s )


class ProjectView (GSymViewObjectNodeDispatch):
	@DMObjectNodeDispatchMethod( Schema.Project )
	def Project(self, ctx, styleSheet, state, node, rootPackage):
		def _onSave(link, buttonEvent):
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				DocumentManagement.promptSaveDocumentAs( ctx.getSubjectContext()['world'], link.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs(link, buttonEvent):
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			DocumentManagement.promptSaveDocumentAs( ctx.getSubjectContext()['world'], link.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )
		
		
		subjectContext = ctx.getSubjectContext()
		document = subjectContext['document']
		location = subjectContext['location']
		
		name = document.getDocumentName()
		
		state = state.withAttrs( location=location )
		rootView = ctx.presentFragment( rootPackage, styleSheet, state ).alignHExpand()

		return styleSheet.project( name, rootView, _onSave, _onSaveAs )



	@DMObjectNodeDispatchMethod( Schema.Package )
	def Package(self, ctx, styleSheet, state, node, name, contents):
		def _addPackage(menuItem):
			contents.append( Schema.Package( name='New package', contents=[] ) )

		def _onRenameAccept(textEntry, text):
			node['name'] = text
			
		def _onRenameCancel(textEntry, originalText):
			nameBox.setChildren( [ nameElement ] )
		
		def _onRename(menuItem):
			textEntry = styleSheet.renameEntry( name, _onRenameAccept, _onRenameCancel )
			nameBox.setChildren( [ textEntry.getElement() ] )
			textEntry.grabCaret()
			
		def _addPage(pageUnit):
			#contents.append( Schema.Page( name='New page', unit=pageUnit ) )
			p = Schema.Page( name='New page', unit=pageUnit )
			contents.append( p )
		
		def _importPage(name, pageUnit):
			contents.append( Schema.Page( name=name, unit=pageUnit ) )

		def _packageContextMenuFactory(element, menu):
			menu.add( _controlsStyle.menuItemWithLabel( 'New package', _addPackage ).getElement() )
			newPageMenu = _newPageMenu( world, _addPage )
			importPageMenu = _importPageMenu( world, element.getRootElement().getComponent(), _importPage )
			menu.add( _controlsStyle.subMenuItemRightWithLabel( 'New page', newPageMenu ).getElement() )
			menu.add( _controlsStyle.subMenuItemRightWithLabel( 'Import page', importPageMenu ).getElement() )
			menu.add( RichTextStyleSheet.instance.hseparator() )
			menu.add( _controlsStyle.menuItemWithLabel( 'Rename', _onRename ).getElement() )
			return True

		location = state['location']
		packageLocation = _joinLocation( location, name )
		
		items = ctx.mapPresentFragment( contents, styleSheet, state.withAttrs( location=packageLocation ) )
			
		world = ctx.getSubjectContext()['world']
		packageView, nameBox, nameElement = styleSheet.package( name, packageLocation, items, _packageContextMenuFactory )
		return packageView
	



	@DMObjectNodeDispatchMethod( Schema.Page )
	def Page(self, ctx, styleSheet, state, node, name, unit):
		def _onRenameAccept(textEntry, text):
			node['name'] = text
			
		def _onRenameCancel(textEntry, originalText):
			nameBox.setChildren( [ nameElement ] )
		
		def _onRename(menuItem):
			textEntry = styleSheet.renameEntry( name, _onRenameAccept, _onRenameCancel )
			nameBox.setChildren( [ textEntry.getElement() ] )
			textEntry.grabCaret()
		
		def _pageContextMenuFactory(element, menu):
			menu.add( _controlsStyle.menuItemWithLabel( 'Rename', _onRename ).getElement() )
			return True

		location = state['location']
		pageLocation = _joinLocation( location, name )
		
		pageView, nameBox, nameElement = styleSheet.page( name, pageLocation, _pageContextMenuFactory )
		return pageView

	
	


_nameRegex = Pattern.compile( '[a-zA-Z_ ][a-zA-Z0-9_ ]*', 0 )

	
class ProjectEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject
		else:
			# Attempt to enter the root package
			docRootNode = enclosingSubject.getFocus()
			rootPackagePrefix = docRootNode['rootPackage']['name']
			iterAfterPackagePrefix = locationIterator.consumeLiteral( '.' + rootPackagePrefix )
			if iterAfterPackagePrefix is not None:
				locationIterator = iterAfterPackagePrefix
				package = docRootNode['rootPackage']
			else:
				print 'No root package'
				return None
			
			while locationIterator.getSuffix() != '':
				iterAfterDot = locationIterator.consumeLiteral( '.' )
				name = None
				if iterAfterDot is not None:
					iterAfterName = iterAfterDot.consumeRegex( _nameRegex )
					if iterAfterName is not None:
						name = iterAfterName.lastToken()
						locationIterator = iterAfterName

				node = None
				if name is not None:
					for n in package['contents']:
						if n['name'] == name:
							node = n
							break
				if node is None:
					return None
				elif isinstance( node, DMObjectInterface ):
					if node.isInstanceOf( Schema.Package ):
						package = node
					elif node.isInstanceOf( Schema.Page ):
						subject = enclosingSubject.withFocus( node ).withTitle( enclosingSubject.getTitle() + ' ' + name )
						subject = subject.withSubjectContext( enclosingSubject.getSubjectContext().withAttrs( location=locationIterator.getPrefix() ) )
						document = enclosingSubject.getSubjectContext()['document']
						return document.resolveUnitRelativeLocation( node['unit'], subject, locationIterator )
					else:
						return None
				else:
					return None
			return None
	
	
	

perspective = GSymPerspective( PyGSymViewFragmentFunction( ProjectView() ), ProjectEditorStyleSheet.instance, AttributeTable.instance, None, ProjectEditorRelativeLocationResolver() )
	