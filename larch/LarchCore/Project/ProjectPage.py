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
from BritefuryJ.Live import LiveFunction

from LarchCore.Project.ProjectNode import ProjectNode



class ProjectPage (ProjectNode):
	def __init__(self, name='', data=None):
		super( ProjectPage, self ).__init__()
		self._id = None
		self._name = name
		self._data = data
		self.__importable_module = None

		@LiveFunction
		def page_source_code():
			if self._data is not None:
				# Accessing the name
				self._incr.onAccess()
				return self._data.get_source_code(self._name)
			else:
				return ''

		self.__page_source_code_live_fn = page_source_code




	@property
	def importName(self):
		if self._name == '__init__':
			return self.parent.importName
		else:
			return self._join_import_names(self.parent.importName, self._name)


	@property
	def moduleNames(self):
		return [ self.importName ]



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

		
	@property
	def data(self):
		self._incr.onAccess()
		return self._data


	def export(self, path):
		filename = self.name + '.py'
		myPath = os.path.join( path, filename )
		s = self.data.exportAsString( self.name )
		f = open( myPath, 'w' )
		f.write( s )
		f.close()



	def register_importable_modules(self):
		root = self.rootNode
		if root is not None:
			kernel = root.current_kernel
			if kernel is not None:
				self.__importable_module = kernel.new_importable_module()
				self.__importable_module.name = self.importName
				self.__importable_module.set_source(self.__page_source_code_live_fn)

	def unregister_importable_modules(self):
		if self.__importable_module is not None:
			self.__importable_module.destroy()

	def update_importable_modules(self):
		if self.__importable_module is not None:
			self.__importable_module.name = self.importName



	def _registerRoot(self, root, takePriority):
		root._registerPage( self, takePriority )
		if root.current_kernel is not None:
			self.register_importable_modules()

	def _unregisterRoot(self, root):
		if root.current_kernel is not None:
			self.unregister_importable_modules()
		root._unregisterPage( self )



	def __get_trackable_contents__(self):
		return [ self.data ]


	@property
	def nodeId(self):
		return self._id


	name = property( getName, setName )
