##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os

from copy import deepcopy

from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchCore.Project.ProjectNode import ProjectNode



class ProjectPage (ProjectNode):
	def __init__(self, name='', data=None):
		super( ProjectPage, self ).__init__()
		self._id = None
		self._name = name
		self._data = data


	@property
	def importName(self):
		return self.parent.importName + '.' + self._name


	def __getstate__(self):
		state = super( ProjectPage, self ).__getstate__()
		state['name'] = self._name
		state['data'] = self._data
		state['id'] = self._id
		return state
	
	def __setstate__(self, state):
		super( ProjectPage, self ).__setstate__( state )
		self._name = state['name']
		self._data = state['data']
		self._id = state.get( 'id' )
	
	def __copy__(self):
		return ProjectPage( self._name, self._data )
	
	def __deepcopy__(self, memo):
		return ProjectPage( self._name, deepcopy( self._data, memo ) )
	
	
	def getName(self):
		self._incr.onAccess()
		return self._name
	
	def setName(self, name):
		oldName = self._name
		self._name = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setName( name ), lambda: self.setName( oldName ), 'Page set name' )
		
		
	def getData(self):
		self._incr.onAccess()
		return self._data


	def export(self, path):
		filename = self.name + '.py'
		myPath = os.path.join( path, filename )
		s = self.data.exportAsString( self.name )
		f = open( myPath, 'w' )
		f.write( s )
		f.close()



	def _registerRoot(self, root, takePriority):
		root._registerPage( self, takePriority )

	def _unregisterRoot(self, root):
		root._unregisterPage( self )



	def __get_trackable_contents__(self):
		return [ self.data ]


	@property
	def nodeId(self):
		return self._id


	name = property( getName, setName )
	data = property( getData )
