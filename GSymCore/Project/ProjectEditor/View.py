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

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction


from GSymCore.Project import Schema
from GSymCore.Project.ProjectEditor.ProjectEditorStyleSheet import ProjectEditorStyleSheet



def _ProjectViewState(location):
	return ( location, )




def _joinLocation(*xs):
	s = '.'.join( [ str( x )   for x in xs ] )
	return Location( s )


class ProjectView (GSymViewObjectNodeDispatch):
	@ObjectNodeDispatchMethod( Schema.Project )
	def Project(self, ctx, styleSheet, state, node, rootPackage):
		def _onSave(link, buttonEvent):
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				window = ctx.getViewContext().getBrowserContext().window
				window.promptSaveDocumentAs( handleSaveDocumentAsFn )
			else:
				document.save()
			return True
				
		
		def _onSaveAs(link, buttonEvent):
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			window = ctx.getViewContext().getBrowserContext().window
			window.promptSaveDocumentAs( handleSaveDocumentAsFn )
			return  True
		
		
		subjectContext = ctx.getSubjectContext()
		document = subjectContext['document']
		location = subjectContext['location']
		
		name = document.getDocumentName()
		
		state = state.withAttrs( location=location )
		rootView = ctx.presentFragment( rootPackage, styleSheet, state ).alignHExpand()

		return styleSheet.project( name, rootView, _onSave, _onSaveAs )



	@ObjectNodeDispatchMethod( Schema.Package )
	def Package(self, ctx, styleSheet, state, node, name, contents):
		def _packageRename(newName):
			node['name'] = newName
			
		def _addPage(pageUnit):
			#contents.append( Schema.Page( name='New page', unit=pageUnit ) )
			p = Schema.Page( name='New page', unit=pageUnit )
			contents.append( p )
		
		def _importPage(name, pageUnit):
			contents.append( Schema.Page( name=name, unit=pageUnit ) )

		def _addPackage():
			contents.append( Schema.Package( name='New package', contents=[] ) )

		location = state['location']
		packageLocation = _joinLocation( location, name )
		
		items = ctx.mapPresentFragment( contents, styleSheet, state.withAttrs( location=packageLocation ) )
			
		window = ctx.getViewContext().getBrowserContext().window
		return styleSheet.package( name, packageLocation, items, _packageRename, window, _addPage, _importPage, _addPackage )
	



	@ObjectNodeDispatchMethod( Schema.Page )
	def Page(self, ctx, styleSheet, state, node, name, unit):
		def _pageRename(newName):
			node['name'] = newName
		
		location = state['location']
		pageLocation = _joinLocation( location, name )
		
		return styleSheet.page( name, pageLocation, _pageRename )

	
	


_nameRegex = Pattern.compile( '[a-zA-Z_ ][a-zA-Z0-9_ ]*', 0 )

	
class ProjectEditorPerspective (GSymPerspective):
	def __init__(self):
		self._viewFn = PyGSymViewFragmentFunction( ProjectView() )
		
	
	
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
						subject = GSymSubject( node, self, enclosingSubject.getTitle() + ' ' + name, enclosingSubject.getSubjectContext().withAttrs( location=locationIterator.getPrefix() ) )
						document = enclosingSubject.getSubjectContext()['document']
						return document.resolveUnitRelativeLocation( node['unit'], subject, locationIterator )
					else:
						return None
				else:
					return None
			return None
	
	
	def getFragmentViewFunction(self):
		return self._viewFn
	
	def getStyleSheet(self):
		return ProjectEditorStyleSheet.instance
	
	def getInitialInheritedState(self):
		return AttributeTable.instance
	
	def getEditHandler(self):
		return None
	
