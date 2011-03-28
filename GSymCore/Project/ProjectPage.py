##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.CommandHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from GSymCore.Project.ProjectNode import ProjectNode



class ProjectPage (ProjectNode):
	def __init__(self, name='', data=None):
		super( ProjectPage, self ).__init__()
		self._name = name
		self._data = data
	
	
	def __getstate__(self):
		state = super( ProjectPage, self ).__getstate__()
		state['name'] = self._name
		state['data'] = self._data
		return state
	
	def __setstate__(self, state):
		super( ProjectPage, self ).__setstate__( state )
		self._name = state['name']
		self._data = state['data']
	
	
	def getName(self):
		self._incr.onAccess()
		return self._name
	
	def setName(self, name):
		oldName = self._name
		self._name = name
		self._incr.onChanged()
		if self._commandHistory is not None:
			self._commandHistory.addCommand( lambda: self.setName( name ), lambda: self.setName( oldName ), 'Page set name' )
		
		
	def getData(self):
		self._incr.onAccess()
		return self._data
	
	
	
	name = property( getName, setName )
	data = property( getData )
