##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from copy import deepcopy
import os

from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchCore.Project.ProjectContainer import ProjectContainer




class ProjectPackage (ProjectContainer):
	def __init__(self, name='', contents=None):
		super( ProjectPackage, self ).__init__( contents )
		self._name = name


	@property
	def importName(self):
		return self.parent.importName + '.' + self._name


	def __getstate__(self):
		state = super( ProjectPackage, self ).__getstate__()
		state['name'] = self._name
		return state
	
	def __setstate__(self, state):
		super( ProjectPackage, self ).__setstate__( state )
		self._name = state['name']
	
	def __copy__(self):
		return ProjectPackage( self._name, self[:] )
	
	def __deepcopy__(self, memo):
		return ProjectPackage( self._name, [ deepcopy( x, memo )   for x in self ] )

	
	def getName(self):
		self._incr.onAccess()
		return self._name
	
	def setName(self, name):
		oldName = self._name
		self._name = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setName( name ), lambda: self.setName( oldName ), 'Package set name' )



	def export(self, path):
		myPath = os.path.join( path, self.name )
		if not os.path.exists( myPath ):
			os.mkdir( myPath )

		# Create an empty init module if none is present
		if '__init__' not in self.contentsMap:
			initPath = os.path.join( myPath, '__init__.py' )
			f = open( initPath, 'w' )
			f.write( '' )
			f.close()

		self.exportContents( myPath )



	name = property( getName, setName )
