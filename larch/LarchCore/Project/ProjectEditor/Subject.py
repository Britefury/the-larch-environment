##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.Browser import Location

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Projection import Subject

from BritefuryJ.LSpace.Input import Modifier
from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.Controls import Hyperlink
from BritefuryJ.Pres.Primitive import Spacer, Column

from LarchCore.MainApp import DocumentManagement
from LarchCore.MainApp import AppLocationPath

from LarchCore.Project.ProjectPage import ProjectPage
from LarchCore.Project.ProjectPackage import ProjectPackage
from LarchCore.Project.ProjectEditor import View
from LarchCore.Project.ProjectEditor.ModuleFinder import RootFinder, PackageFinder




def _save(subject, pageController):
	document = subject._document
	if document.hasFilename():
		document.save()
	else:
		def handleSaveDocumentAsFn(filename):
			document.saveAs( filename )

		DocumentManagement.promptSaveDocumentAs( subject.getSubjectContext()['world'], None, handleSaveDocumentAsFn )


def _saveAs(subject, pageController):
	document = subject._document
	def handleSaveDocumentAsFn(filename):
		document.saveAs( filename )

	DocumentManagement.promptSaveDocumentAs( subject.getSubjectContext()['world'], None, handleSaveDocumentAsFn, document.getFilename() )


def _reset(subject, pageController):
	document = subject._document
	modules = document.unloadAllImportedModules()
	print 'LarchCore.Project.ProjectEditor.Subject: unloaded modules:'
	for module in modules:
		print '\t' + module

	
	
_saveCommand = Command( CommandName( '&Save' ), _save, Shortcut( 'S', Modifier.CTRL ) )
_saveAsCommand = Command( CommandName( '&Save &as' ), _saveAs )
_resetCommand = Command( CommandName( 'R&eset' ), _reset, Shortcut( 'E', Modifier.CTRL ) )
_projectCommands = CommandSet( 'LarchCore.Project', [ _saveCommand, _saveAsCommand, _resetCommand ] )
	


class _PackageSubject (object):
	def __init__(self, projectSubject, model, location, importName):
		self._projectSubject = projectSubject
		self._model = model
		self._location = location
		self._importName = importName
	
	def __resolve__(self, name):
		item = self._model.contentsMap.get( name )
		if item is not None:
			itemLocation = self._location + '.' + name
			importName = self._importName + '.' + name   if self._importName is not None   else None
			if isinstance( item, ProjectPackage ):
				return _PackageSubject( self._projectSubject, item, itemLocation, importName )
			elif isinstance( item, ProjectPage ):
				return self._projectSubject._document.newModelSubject( item.data, self._projectSubject, itemLocation, importName, name )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )



class _IndexPage (object):
	def __init__(self, model, perspective, projectPageLoc):
		assert isinstance( projectPageLoc, Location )
		self._model = model
		self._perspective = perspective
		self._projectPageLoc = projectPageLoc
	
	def __present__(self, fragment, inherited_state):
		projectLink = Hyperlink( 'Go to project page', self._projectPageLoc )
		return Column( [ self._perspective( self._model ).alignHExpand().alignVExpand(), Spacer( 0.0, 10.0 ).alignVTop(), projectLink.alignHLeft().alignVTop() ] ).alignHExpand()


class _ProjectIndexSubject (Subject):
	def __init__(self, indexSubject, enclosingSubject, projectLoc):
		super( _ProjectIndexSubject, self ).__init__( enclosingSubject )
		assert isinstance( projectLoc, Location )
		self._indexSubject = indexSubject
		self._page = _IndexPage( indexSubject.getFocus(), indexSubject.getPerspective(), projectLoc )
	
	
	def getFocus(self):
		return self._page

	def getPerspective(self):
		return DefaultPerspective.instance

	def getTitle(self):
		return self._indexSubject.getTitle()

	def getSubjectContext(self):
		return self._indexSubject.getSubjectContext()

	def getChangeHistory(self):
		return self._indexSubject.getChangeHistory()

	def buildBoundCommandSetList(self, cmdSets):
		self._indexSubject.buildBoundCommandSetList( cmdSets )

	



class _RootSubject (Subject):
	def __init__(self, document, model, enclosingSubject, location, importName, title):
		super( _RootSubject, self ).__init__( enclosingSubject )
		assert isinstance( location, Location )
		self._document = document
		self._model = model
		self._location = location
		self._title = title
		
		

	def getFocus(self):
		return self._model

	def getPerspective(self):
		return View.perspective

	def getTitle(self):
		return self._title + ' [Prj]'

	def getSubjectContext(self):
		return self.enclosingSubject.getSubjectContext().withAttrs( document=self._document, docLocation=self._location, location=self._location )


	def getChangeHistory(self):
		return self._document.getChangeHistory()
	
	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _projectCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )


	def __resolve__(self, name):
		item = self._model.contentsMap.get( name )
		if item is not None:
			itemLocation = self._location + '.' + name
			importName = self._model.getPythonPackageName()
			importName = importName + '.' + name   if importName is not None   else None
			if isinstance( item, ProjectPackage ):
				return _PackageSubject( self, item, itemLocation, importName )
			elif isinstance( item, ProjectPage ):
				return self._document.newModelSubject( item.data, self, itemLocation, importName, name )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )



class ProjectSubject (_RootSubject):
	def __init__(self, document, model, enclosingSubject, location, importName, title):
		super( ProjectSubject, self ).__init__( document, model, enclosingSubject, location, importName, title )
		assert isinstance( location, Location )
		self._packageFinder = PackageFinder( self, model, location )
		self._rootFinder = RootFinder( self, model.pythonPackageName )
		self._rootSubject = _RootSubject( document, model, enclosingSubject, location, importName, title )



	def getSubjectContext(self):
		t = super( ProjectSubject, self ).getSubjectContext()
		index = self._model.contentsMap.get( 'index' )
		if index is not None  and  isinstance( index, ProjectPage ):
			location = self._location + '.___project___'
		else:
			location = self._location
		return AppLocationPath.addLocationPathEntry( t, 'Project', location )


	def redirect(self):
		index = self._model.contentsMap.get( 'index' )
		if index is not None  and  isinstance( index, ProjectPage ):
			return _ProjectIndexSubject( self.__resolve__( 'index' ), self.enclosingSubject, self._location + '.___project___' )
		else:
			return None
	

	def __resolve__(self, name):
		self._model.startup()
		if name == '___project___':
			return self._rootSubject
		else:
			return super( ProjectSubject, self ).__resolve__( name )



	def import_resolve(self, name, fullname, path):
		return self._rootFinder.import_resolve( name, fullname, path )
