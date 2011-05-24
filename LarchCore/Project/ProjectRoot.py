##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import deepcopy

from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchCore.Project.ProjectContainer import ProjectContainer
from LarchCore.Project import ProjectEditor




class ProjectRoot (ProjectContainer):
	def __init__(self, packageName=None, contents=[]):
		super( ProjectRoot, self ).__init__( contents )
		self._pythonPackageName = packageName
	
	
	def __getstate__(self):
		state = super( ProjectRoot, self ).__getstate__()
		state['pythonPackageName'] = self._pythonPackageName
		return state
	
	def __setstate__(self, state):
		super( ProjectRoot, self ).__setstate__( state )
		self._pythonPackageName = state['pythonPackageName']
	
	def __copy__(self):
		return ProjectRoot( self._pythonPackageName, self[:] )
	
	def __deepcopy__(self, memo):
		return ProjectRoot( self._pythonPackageName, [ deepcopy( x, memo )   for x in self ] )
	
	
	def __new_subject__(self, document, enclosingSubject, location, title):
		return ProjectEditor.Subject.ProjectSubject( document, self, enclosingSubject, location, title )

		
	def getPythonPackageName(self):
		self._incr.onAccess()
		return self._pythonPackageName
	
	def setPythonPackageName(self, name):
		oldName = self._pythonPackageName
		self._pythonPackageName = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setPythonPackageName( name ), lambda: self.setPythonPackageName( oldName ), 'Project root set python package name' )
	
	
	pythonPackageName = property( getPythonPackageName, setPythonPackageName )
