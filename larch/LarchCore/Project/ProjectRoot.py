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
		self._startupExecuted = False


	@property
	def importName(self):
		return self._pythonPackageName   if self._pythonPackageName is not None   else ''


	def __getstate__(self):
		state = super( ProjectRoot, self ).__getstate__()
		state['pythonPackageName'] = self._pythonPackageName
		return state
	
	def __setstate__(self, state):
		super( ProjectRoot, self ).__setstate__( state )
		self._pythonPackageName = state['pythonPackageName']
		self._startupExecuted = False
	
	def __copy__(self):
		return ProjectRoot( self._pythonPackageName, self[:] )
	
	def __deepcopy__(self, memo):
		return ProjectRoot( self._pythonPackageName, [ deepcopy( x, memo )   for x in self ] )
	
	
	def __new_subject__(self, document, enclosingSubject, importName, title):
		"""Used to create the subject that displays the project as a page"""
		projectSubject = ProjectEditor.Subject.ProjectSubject( document, self, enclosingSubject, importName, title )
		if 'index' in self.contentsMap:
			index = self.contentsMap['index']
			if isinstance( index, ProjectPage.ProjectPage ):
				return document.newModelSubject( index.data, projectSubject, index.importName, index.name )
		return projectSubject

		
	def getPythonPackageName(self):
		self._incr.onAccess()
		return self._pythonPackageName
	
	def setPythonPackageName(self, name):
		oldName = self._pythonPackageName
		self._pythonPackageName = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setPythonPackageName( name ), lambda: self.setPythonPackageName( oldName ), 'Project root set python package name' )



	def startup(self):
		if not self._startupExecuted:
			if self._pythonPackageName is not None:
				if '__startup__' in self.contentsMap:
					self._startupExecuted = True
					importName = self._pythonPackageName + '.' + '__startup__'
					__import__( importName )

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



	pythonPackageName = property( getPythonPackageName, setPythonPackageName )
