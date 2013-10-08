##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from Britefury.Util.TrackedList import TrackedListProperty

from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Live import LiveFunction

from LarchCore.Project.ProjectNode import ProjectNode



class ProjectContainer (ProjectNode):
	def __init__(self, contents=None):
		super( ProjectContainer, self ).__init__()
		self._contents_ = []
		self._prevContents = []
		self._contentsMapLive = LiveFunction( self._computeContentsMap )
		if contents is not None:
			self[:] = contents
	
	
	@property
	def moduleNames(self):
		names = []
		for c in self._contents:
			names.extend( c.moduleNames )
		return names


	def __getstate__(self):
		state = super( ProjectContainer, self ).__getstate__()
		state['contents'] = self._contents_
		return state
	
	def __setstate__(self, state):
		super( ProjectContainer, self ).__setstate__( state )
		self._contents_ = state['contents']
		self._prevContents = self._contents[:]
		self._contentsMapLive = LiveFunction( self._computeContentsMap )

		for x in self._contents_:
			x._setParent( self, True )
		
	
	def _computeContentsMap(self):
		m = {}
		self._incr.onAccess()
		for x in self._contents:
			m[x.name] = x
		return m
		
		
	def __len__(self):
		self._incr.onAccess()
		return len( self._contents )
	
	def __getitem__(self, index):
		self._incr.onAccess()
		return self._contents[index]
	
	def __iter__(self):
		for x in self._contents:
			self._incr.onAccess()
			yield x
	
	def __contains__(self, x):
		self._incr.onAccess()
		return x in self._contents
	
	def indexOfById(self, x):
		for i, y in enumerate( self._contents ):
			if x is y:
				return i
		return -1
	
	
	
	def __setitem__(self, index, x):
		self._contents[index] = x
	
	def __delitem__(self, index):
		del self._contents[index]
	
	def append(self, x):
		self._contents.append( x )
	
	def insert(self, i, x):
		self._contents.insert( i, x )
	
	def remove(self, x):
		self._contents.remove( x )
		
		

		
	@property
	def contentsMap(self):
		return self._contentsMapLive.getValue()


	def exportContents(self, myPath):
		for x in self._contents:
			x.export( myPath )



	def __get_trackable_contents__(self):
		return self._contents.__get_trackable_contents__()



	def _registerRoot(self, root, takePriority):
		super( ProjectContainer, self )._registerRoot( root, takePriority )
		for x in self._contents_:
			x._registerRoot( root, takePriority )

	def _unregisterRoot(self, root):
		super( ProjectContainer, self )._unregisterRoot( root )
		for x in self._contents_:
			x._unregisterRoot( root )




	@TrackedListProperty
	def _contents(self):
		return self._contents_

	@_contents.changeNotificationMethod
	def _contents_changed(self):
		prev = set(self._prevContents)
		cur = set(self._contents_)
		added = cur - prev
		removed = prev - cur
		for x in removed:
			x._clearParent()
		for x in added:
			x._setParent( self, False )
		self._prevContents = self._contents_[:]
		self._incr.onChanged()

	
	