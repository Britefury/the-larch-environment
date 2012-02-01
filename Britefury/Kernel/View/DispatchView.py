##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.IncrementalView import ViewFragmentFunction

from BritefuryJ.Dispatch import PyMethodDispatchViewFragmentFunction
from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod, DMObjectNodeDispatchMethod




class MethodDispatchView (object):
	__dispatch_num_args__ = 2


	def __init__(self):
		self.fragmentViewFunction = PyMethodDispatchViewFragmentFunction( self )


	def _startProfiling(self):
		self.fragmentViewFunction.startProfiling()

	def _stopProfiling(self):
		self.fragmentViewFunction.stopProfiling()

	def _resetProfile(self):
		self.fragmentViewFunction.resetProfile()

	def _getProfileTimings(self):
		return self.fragmentViewFunction.getProfileTimings()

	def _getProfileResults(self):
		return self.fragmentViewFunction.getProfileResults()



