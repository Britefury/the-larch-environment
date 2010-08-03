##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
from datetime import datetime

from java.lang import Object

from java.awt import Color
from java.awt import BasicStroke

from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *

from BritefuryJ.Cell import LiteralCell

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.GSym.PresCom import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver


from GSymCore.GSymApp import DocumentManagement

from GSymCore.Project import Schema
#from GSymCore.Project.ProjectEditor.ProjectEditorStyleSheet import ProjectEditorStyleSheet



# handleNewPageFn(unit)
def _newPageMenu(world, handleNewPageFn):
	def _make_newPage(newPageFn):
		def newPage(menuItem):
			unit = newPageFn()
			handleNewPageFn( unit )
		return newPage
	items = []
	for newPageFactory in world.newPageFactories:
		items.append( MenuItem.menuItemWithLabel( newPageFactory.menuLabelText, _make_newPage( newPageFactory.newPageFn ) ) )
	return VPopupMenu( items )
	
	
	
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
		items.append( MenuItem.menuItemWithLabel( pageImporter.menuLabelText, _make_importPage( pageImporter.fileType, pageImporter.filePattern, pageImporter.importFn ) ) )
	return VPopupMenu( items )




def _ProjectViewState(location):
	return ( location, )




def _joinLocation(*xs):
	s = '.'.join( [ str( x )   for x in xs ] )
	return Location( s )



class ProjectDrag (Object):
	def __init__(self, source):
		self.source = source
		

def _dragSourceCreateSourceData(element, aspect):
	return ProjectDrag( element.getFragmentContext().getModel() )


_dragSource = ObjectDndHandler.DragSource( ProjectDrag, ObjectDndHandler.ASPECT_NORMAL, _dragSourceCreateSourceData )



def _isChildOf(node, package):
	if node is package:
		return True
	
	if package.isInstanceOf( Schema.Package ):
		for child in package['contents']:
			if _isChildOf( node, child ):
				return True
	return False


def _pageCanDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		model = element.getFragmentContext().getModel()
		return model is not data.source
	return False

def _pageDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPage = element.getFragmentContext().getModel()
		parent = destPage.getValidParents()[0]
		index = parent.indexOfById( destPage )
		if targetPos.y > ( element.getHeight() * 0.5 ):
			index += 1
		
		source = data.source.deepCopy()   if action == ObjectDndHandler.COPY   else data.source
		
		if action == ObjectDndHandler.MOVE:
			sourceParent = data.source.getValidParents()[0]
			indexOfSource = sourceParent.indexOfById( data.source )
			del sourceParent[indexOfSource]
			if parent is sourceParent  and  index > indexOfSource:
				index -= 1

		parent.insert( index, source )
		return True
	return False
			


def _getDestPackageAndIndex(element, targetPos):
	targetPackage = element.getFragmentContext().getModel()
	if targetPos.x > ( element.getWidth() * 0.5 ):
		return targetPackage, len( targetPackage['contents'] )
	else:
		parent1 = targetPackage.getValidParents()[0]
		parent2 = parent1.getValidParents()[0]
		index = parent1.indexOfById( targetPackage )
		if targetPos.y > ( element.getHeight() * 0.5 ):
			index += 1
		return parent2, index
	


def _packageCanDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.COPY  or  not _isChildOf( data.source, destPackage ):
			return True
	return False

def _packageDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.MOVE  and _isChildOf( data.source, destPackage ):
			return False
		
		
		
		#targetPackage = element.getFragmentContext().getModel()
		#if targetPos.x > ( element.getWidth() * 0.5 ):
			#pass
		#else:
			#parent = targetPackage.getValidParents()[0]
			#index = parent.indexOfById( targetPackage )
			#if targetPos.y > ( element.getHeight() * 0.5 ):
				#index += 1
			
			#source = data.source.deepCopy()   if action == ObjectDndHandler.COPY   else data.source
			
			#if action == ObjectDndHandler.MOVE:
				#sourceParent = data.source.getValidParents()[0]
				#indexOfSource = sourceParent.indexOfById( data.source )
				#del sourceParent[indexOfSource]
				#if parent is sourceParent  and  index > indexOfSource:
					#index -= 1

		#parent.insert( index, source )
		#return True
	return False


_pageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _pageCanDrop, _pageDrop )
_packageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _packageCanDrop, _packageDrop )




_controlsStyle = StyleSheet.instance.withAttr( Controls.bClosePopupOnActivate, True )
_projectControlsStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None ) ).withAttr( Primitive.hboxSpacing, 30.0 )
_packageNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) ).withAttr( Primitive.fontBold, True ).withAttr( Primitive.fontSize, 14 )
_itemHoverHighlightStyle = StyleSheet.instance.withAttr( Primitive.hoverBackground, FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) )

_packageContentsIndentation = 20.0




class ProjectView (GSymViewObjectNodeDispatch):
	@DMObjectNodeDispatchMethod( Schema.Project )
	def Project(self, ctx, state, node, rootPackage):
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
		rootView = InnerFragment( rootPackage, state.withAttrs( projectRootPackage=True ) ).alignHExpand()
		

		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		linkHeader = LinkHeaderBar( [ homeLink ] )
		
		title = TitleBarWithSubtitle( 'DOCUMENT', name )
		
		
		saveLink = Hyperlink( 'SAVE', _onSave )
		saveAsLink = Hyperlink( 'SAVE AS', _onSaveAs )
		controlsBox = HBox( [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = _projectControlsStyle.applyTo( Border( controlsBox ) )
		
		indexHeader = Heading3( 'Project Index' )
		projectIndex = VBox( [ indexHeader, rootView.alignHExpand() ] )
		
		head = Head( [ linkHeader, title ] )
		body = Body( [ controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), projectIndex ] )
		
		return StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( Page( [ head, body ] ) )


	@DMObjectNodeDispatchMethod( Schema.Package )
	def Package(self, ctx, state, node, name, contents):
		def _addPackage(menuItem):
			contents.append( Schema.Package( name='NewPackage', contents=[] ) )

		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				node['name'] = text
				
			def onCancel(self, textEntry, originalText):
				nameCell.setLiteralValue( nameBox )
		
		def _onRename(menuItem):
			textEntry = TextEntry( name, _RenameListener(), _nameRegex, 'Please enter a valid identifier' )
			nameCell.setLiteralValue( textEntry )
			# TODO
			# textEntry.grabCaret()
			
		def _addPage(pageUnit):
			#contents.append( Schema.Page( name='New page', unit=pageUnit ) )
			p = Schema.Page( name='NewPage', unit=pageUnit )
			contents.append( p )
		
		def _importPage(name, pageUnit):
			contents.append( Schema.Page( name=name, unit=pageUnit ) )

		def _packageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'New package', _addPackage ) )
			newPageMenu = _newPageMenu( world, _addPage )
			importPageMenu = _importPageMenu( world, element.getRootElement().getComponent(), _importPage )
			menu.add( MenuItem.menuItemWithLabel( 'New page', newPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( MenuItem.menuItemWithLabel( 'Import page', importPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			return True

		location = state['location']
		packageLocation = _joinLocation( location, name )
		
		items = InnerFragment.map( contents, state.withAttrs( location=packageLocation ) )
		
		world = ctx.getSubjectContext()['world']
		
		icon = Image( 'GSymCore/Project/icons/Package.png' )
		nameElement = _packageNameStyle.applyTo( StaticText( name ) )
		nameBox = _itemHoverHighlightStyle.applyTo( HBox( [ icon.padX( 5.0 ).alignVCentre(), nameElement.alignVCentre() ]  ) )
		nameBox = nameBox.withContextMenuFactory( _packageContextMenuFactory )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _packageDropDest )
		
		nameCell = LiteralCell( nameBox )
		
		itemsBox = VBox( items )
		
		return VBox( [ nameCell.genericPerspectiveValuePresInFragment(), itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )
		
		

	@DMObjectNodeDispatchMethod( Schema.Page )
	def Page(self, ctx, state, node, name, unit):
		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				node['name'] = text
				
			def onCancel(self, textEntry, originalText):
				nameCell.setLiteralValue( nameBox )

		def _onRename(menuItem):
			textEntry = TextEntry( name, _RenameListener(), _nameRegex, 'Please enter a valid identifier' )
			nameCell.setLiteralValue( textEntry )
			# TODO
			# textEntry.grabCaret()
			
		def _pageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			return True
			
		
		location = state['location']
		pageLocation = _joinLocation( location, name )

		link = Hyperlink( name, pageLocation )
		link = link.withContextMenuFactory( _pageContextMenuFactory )
		nameBox = _itemHoverHighlightStyle.applyTo( HBox( [ link ] ) )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _pageDropDest )
		
		nameCell = LiteralCell( nameBox )
		
		return nameCell.genericPerspectiveValuePresInFragment()
	


_nameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )

	
class ProjectEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject
		else:
			# Attempt to enter the root package
			docRootNode = enclosingSubject.getFocus()
			package = docRootNode['rootPackage']
			iterAfterRoot = locationIterator.consumeLiteral( '.' + package['name'] )
			if iterAfterRoot is None:
				return None
			else:
				locationIterator = iterAfterRoot
			
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
	
	
	

perspective = GSymPerspective( ProjectView(), StyleSheet.instance, SimpleAttributeTable.instance, None, ProjectEditorRelativeLocationResolver() )
	