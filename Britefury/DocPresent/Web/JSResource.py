##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************



_jsResourcesAlwaysLoaded = []



class JSResource (object):
	LOADPOLICY_ALWAYS = 0
	LOADPOLICY_ONDEMAND = 1
	
	
	def __init__(self, loadPolicy, dependencies, js):
		self._loadPolicy = loadPolicy
		self._dependencies = dependencies
		self._js = js
		
		if loadPolicy == self.LOADPOLICY_ALWAYS:
			_jsResourcesAlwaysLoaded.append( self )

		
		
		
class JSResourceManager (object):
	def __init__(self):
		self._loadedResources = set()
		self._page = page
		self._resourceQueue = []
		
		for rsc in _jsResourcesAlwaysLoaded:
			self.queueResource( rsc )
			
			
			
	def queueResource(self, rsc):
		if rsc not in self._loadedResources  and  rsc not in self._resourceQueue:
			for dep in rsc._dependencies:
				self.queueResource( dep )
			self._resourceQueue.append( rsc )
	
			
	def getQueuedJS(self):
		return '\n\n\n'.join( [ rsc._js   for rsc in self._resourceQueue ] )
	
	
	def purgeQueue(self):
		self._resourceQueue = []

	
	
