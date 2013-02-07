##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import deepcopy
import os

from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchCore.Project.ProjectContainer import ProjectContainer
from LarchCore.Project import ProjectEditor, ProjectPage




class ProjectRoot (ProjectContainer):
	def __init__(self, packageName=None, contents=None):
		super( ProjectRoot, self ).__init__( contents )
		self._pythonPackageName = packageName
		self.__frontPageId = None
		self.__startupPageId = None

		self.__idToPage = {}
		self.__pageIdCounter = 0

		self._startupExecuted = False


	@property
	def importName(self):
		return self._pythonPackageName   if self._pythonPackageName is not None   else ''


	def __getstate__(self):
		state = super( ProjectRoot, self ).__getstate__()
		state['pythonPackageName'] = self._pythonPackageName
		state['frontPageId'] = self.__frontPageId
		state['startupPageId'] = self.__startupPageId
		return state
	
	def __setstate__(self, state):
		self.__idToPage = {}
		self.__pageIdCounter = 0
		self._startupExecuted = False

		# Need to initialise the ID table before loading contents
		super( ProjectRoot, self ).__setstate__( state )
		self._pythonPackageName = state['pythonPackageName']
		self.__frontPageId = state.get( 'frontPageId' )
		self.__startupPageId = state.get( 'startupPageId' )


	def __copy__(self):
		return ProjectRoot( self._pythonPackageName, self[:] )
	
	def __deepcopy__(self, memo):
		return ProjectRoot( self._pythonPackageName, [ deepcopy( x, memo )   for x in self ] )
	
	
	def __new_subject__(self, document, enclosingSubject, path, importName, title):
		"""Used to create the subject that displays the project as a page"""
		projectSubject = ProjectEditor.Subject.ProjectSubject( document, self, enclosingSubject, path, importName, title )
		frontPage = self.frontPage
		if frontPage is not None:
			return document.newModelSubject( frontPage.data, projectSubject, path, frontPage.importName, frontPage.getName() )
		return projectSubject

		
	def startup(self):
		if not self._startupExecuted:
			if self._pythonPackageName is not None:
				startupPage = self.startupPage
				if startupPage is not None:
					self._startupExecuted = True
					__import__( startupPage.importName )

	def reset(self):
		self._startupExecuted = False



	def export(self, path):
		myPath = path

		if self._pythonPackageName is not None:
			components = self._pythonPackageName.split( '.' )
			for c in components:
				myPath = os.path.join( myPath, c )
				if not os.path.exists( myPath ):
					os.mkdir( myPath )
					initPath = os.path.join( myPath, '__init__.py' )
					f = open( initPath, 'w' )
					f.write( '' )
					f.close()

		self.exportContents( myPath )



	def _registerRoot(self, root, takePriority):
		# No need to register the root package; this is the root package
		pass

	def _unregisterRoot(self, root):
		# No need to unregister the root package; this is the root package
		pass




	@property
	def pythonPackageName(self):
		self._incr.onAccess()
		return self._pythonPackageName

	@pythonPackageName.setter
	def pythonPackageName(self, name):
		oldName = self._pythonPackageName
		self._pythonPackageName = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			def set(name):
				self.pythonPackageName = name
			self.__change_history__.addChange( lambda: set( name ), lambda: set( oldName ), 'Project root set python package name' )



	def _registerPage(self, page, takePriority):
		pageId = page._id

		if pageId is not None  and  pageId in self.__idToPage  and  takePriority:
			# page ID already in use
			# Take it, and make a new one for the page that is currently using it

			# Get the other page that is currently using the page ID
			otherPage = self.__idToPage[pageId]

			# Make new page ID
			self.__pageIdCounter = max( self.__pageIdCounter, len( self.__idToPage) )
			otherPageId = self.__pageIdCounter
			self.__pageIdCounter += 1

			# Re-assign
			self.__idToPage[pageId] = page
			page._id = pageId
			self.__idToPage[otherPageId] = otherPage
			otherPage._id = otherPageId
		elif pageId is None  or  ( pageId in self.__idToPage  and  not takePriority ):
			# Either, no page ID or page ID already in use and not taking priority
			# Create a new one
			self.__pageIdCounter = max( self.__pageIdCounter, len( self.__idToPage) )
			pageId = self.__pageIdCounter
			self.__pageIdCounter += 1
			page._id = pageId

		self.__idToPage[pageId] = page

	def _unregisterPage(self, node):
		nodeId = node._id
		del self.__idToPage[nodeId]


	def getPageById(self, nodeId):
		return self.__idToPage.get( nodeId )



	@property
	def rootNode(self):
		return self


	@property
	def frontPage(self):
		self._incr.onAccess()
		self.startup()
		return self.__idToPage.get( self.__frontPageId )   if self.__frontPageId is not None   else None

	@frontPage.setter
	def frontPage(self, page):
		self.__frontPageId = page._id   if page is not None   else None
		self._incr.onChanged()



	@property
	def startupPage(self):
		self._incr.onAccess()
		return self.__idToPage.get( self.__startupPageId )   if self.__startupPageId is not None   else None

	@startupPage.setter
	def startupPage(self, page):
		self.__startupPageId = page._id   if page is not None   else None
		self._incr.onChanged()


