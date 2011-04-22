##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.ChangeHistory import Trackable

from BritefuryJ.Incremental import IncrementalValueMonitor



class ProjectNode (Trackable):
	def __init__(self):
		self._incr = IncrementalValueMonitor()
		self._changeHistory = None
		self._parent = None
	
	
	# Required due to inheriting Trackable
	def __reduce__(self):
		return self.__class__, (), self.__getstate__()
	
	def __getstate__(self):
		return {}
	
	def __setstate__(self, state):
		self._incr = IncrementalValueMonitor()
		self._changeHistory = None
		self._parent = None
	
	
	def getParent(self):
		return self._parent

	
	def getChangeHistory(self):
		return self._changeHistory
	
	def setChangeHistory(self, changeHistory):
		self._changeHistory = changeHistory
	
	def trackContents(self, history):
		pass
	
	def stopTrackingContents(self, history):
		pass

	
	parent = property( getParent )

