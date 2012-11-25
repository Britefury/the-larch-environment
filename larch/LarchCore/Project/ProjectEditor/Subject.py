##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Projection import Subject

from BritefuryJ.LSpace.Input import Modifier
from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.Controls import Hyperlink
from BritefuryJ.Pres.Pres import *
from BritefuryJ.Pres.Primitive import *

from LarchCore.MainApp import DocumentManagement

from LarchCore.Project.ProjectEditor import View
from LarchCore.Project.ProjectEditor.ModuleFinder import RootFinder, PackageFinder




def _save(subject, pageController):
	document = subject._document
	if document.hasFilename():
		document.save()
	else:
		def handleSaveDocumentAsFn(filename):
			document.saveAs( filename )

		DocumentManagement.promptSaveDocumentAs( subject.world, None, handleSaveDocumentAsFn )


def _saveAs(subject, pageController):
	document = subject._document
	def handleSaveDocumentAsFn(filename):
		document.saveAs( filename )

	DocumentManagement.promptSaveDocumentAs( subject.world, None, handleSaveDocumentAsFn, document.getFilename() )


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



class ProjectSubject (Subject):
	def __init__(self, document, model, enclosingSubject, importName, title):
		super( ProjectSubject, self ).__init__( enclosingSubject )
		self._document = document
		self._model = model
		self._title = title
		packageFinder = PackageFinder( self, model )
		self._rootFinder = RootFinder( self, model.pythonPackageName, packageFinder )


	@property
	def document(self):
		return self._document

	@property
	def documentSubject(self):
		return self


	def getTrailLinkText(self):
		return 'Project'


	def getFocus(self):
		return self._model

	def getPerspective(self):
		return View.perspective

	def getTitle(self):
		return self._title + ' [Prj]'


	def getChangeHistory(self):
		return self._document.getChangeHistory()
	
	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _projectCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )


	def import_resolve(self, name, fullname, path):
		return self._rootFinder.import_resolve( name, fullname, path )







