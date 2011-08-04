##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.IncrementalView import ViewFragmentFunction

from BritefuryJ.Dispatch import DMObjectNodeDispatchViewFragmentFunction, ObjectDispatchViewFragmentFunction

from Britefury.Dispatch.DMObjectNodeMethodDispatch import dmObjectNodeMethodDispatch, dmObjectNodeMethodDispatchAndGetName
from Britefury.Dispatch.ObjectMethodDispatch import objectMethodDispatch, objectMethodDispatchAndGetName

import time


			
		
		
class ObjectNodeDispatchView (object):
	__dispatch_num_args__ = 2
	
	
	def __init__(self):
		self.fragmentViewFunction = DMObjectNodeDispatchViewFragmentFunction( self )


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
		
	
		

	
class ObjectDispatchView (object):
	def __init__(self):
		self.fragmentViewFunction = ObjectDispatchViewFragmentFunction( self )




