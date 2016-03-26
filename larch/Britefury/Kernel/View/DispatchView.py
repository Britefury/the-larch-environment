##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.IncrementalView import ViewFragmentFunction

from BritefuryJ.Dispatch import PyMethodDispatchViewFragmentFunction




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



