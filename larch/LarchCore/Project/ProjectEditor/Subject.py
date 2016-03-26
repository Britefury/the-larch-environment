##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Projection import Subject, SubjectPath, SubjectPathEntry

from BritefuryJ.LSpace.Input import Modifier
from BritefuryJ.Command import CommandName, Command, CommandSet
from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.Controls import Hyperlink
from BritefuryJ.Pres.Primitive import Spacer, Column

from LarchCore.MainApp import DocumentManagement

from LarchCore.Project.ProjectEditor import View




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


def _restart_kernel(subject, pageController):
	subject.project.restart_kernel()

	
	
_saveCommand = Command( CommandName( '&Save' ), _save, Shortcut( 'S', Modifier.CTRL ) )
_saveAsCommand = Command( CommandName( '&Save &as' ), _saveAs )
_restart_kernel_command = Command( CommandName( 'R&estart Kernel' ), _restart_kernel, Shortcut( 'E', Modifier.CTRL ) )
_projectCommands = CommandSet( 'LarchCore.Project', [ _saveCommand, _saveAsCommand, _restart_kernel_command ] )




class ProjectSubject (Subject):
	def __init__(self, document, model, enclosingSubject, path, importName, title):
		super( ProjectSubject, self ).__init__( enclosingSubject, path )
		self._document = document
		self._model = model
		self._title = title


	@property
	def documentSubject(self):
		return self

	@property
	def project(self):
		self._model.startup()
		return self._model


	def getTrailLinkText(self):
		return 'Project'


	def getFocus(self):
		return self._model

	def getPerspective(self):
		return View.perspective

	def getTitle(self):
		return self._title


	def get_kernel(self, kernel_callback):
		self._model.get_kernel(kernel_callback)




	def buildBoundCommandSetList(self, cmdSets):
		cmdSets.add( _projectCommands.bindTo( self ) )
		self.enclosingSubject.buildBoundCommandSetList( cmdSets )


	def _pageSubject(self, page):
		pathEntry = _PageSubjectPathEntry( page )
		relativePath = SubjectPath( pathEntry )
		return relativePath.followFrom( self )



class _PageSubjectPathEntry (SubjectPathEntry):
	def __init__(self, page):
		self.__nodeId = page.nodeId


	def follow(self, outerSubject):
		rootNode = outerSubject.project
		page = rootNode.getPageById( self.__nodeId )
		document = outerSubject._document
		return document.newModelSubject( page.data, outerSubject, outerSubject.path().followedBy( self ), page.importName, page.getName() )


	def __getstate__(self):
		return { 'nodeId' : self.__nodeId }

	def __setstate__(self, state):
		self.__nodeId = state.get( 'nodeId', None )



