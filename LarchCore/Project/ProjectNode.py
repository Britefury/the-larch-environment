##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.ChangeHistory import Trackable

from BritefuryJ.Incremental import IncrementalValueMonitor



class ProjectNode (object):
	def __init__(self):
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		self._parent = None
	
	
	def __getstate__(self):
		return {}
	
	def __setstate__(self, state):
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		self._parent = None
	
	
	def getParent(self):
		return self._parent

	
	def __get_trackable_contents__(self):
		return None

	
	parent = property( getParent )

