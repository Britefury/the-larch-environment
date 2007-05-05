##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
class CommandTracker (object):
	def __init__(self, commandHistory):
		self._commandHistory = commandHistory


	def track(self, obj):
		assert hasattr( obj, '_commandTracker_' ), 'object to be tracked (%s) has no command tracker attribute'  %  ( obj, )
		assert obj._commandTracker_ is None, 'object (%s) already being tracked'  %  ( obj, )
		obj._commandTracker_ = self

	def stopTracking(self, obj):
		assert obj._commandTracker_ is self, 'object (%s) not being tracked by this tracker (%s)'  %  ( obj, self )
		obj._commandTracker_ = None

