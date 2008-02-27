##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity

from Britefury.DocPresent.Toolkit.DTContainer import DTContainer
from Britefury.DocPresent.Toolkit.DTBin import DTBin




class DTContainerSequence (DTContainer):
	def __len__(self):
		return len( self._childEntries )


	def __getitem__(self, index):
		entry = self._childEntries[index]
		if isinstance( entry, list ):
			return [ e.child   for e in entry ]
		else:
			return entry.child


	def __setitem__(self, index, item):
		if isinstance( index, slice ):
			oldEntrySet = set( self._childEntries )
			self._childEntries[index] = [ self._p_itemToChildEntry( x )  for x in item ]
			newEntrySet = set( self._childEntries )

			removed = oldEntrySet.difference( newEntrySet )
			added = newEntrySet.difference( oldEntrySet )

			for entry in removed:
				self._o_unregisterChildEntry( entry )

			for entry in added:
				self._o_registerChildEntry( entry )

			self._p_childListModified()
			
			for entry in self._childEntries:
				self._p_linkChildEntryCursorEntity( entry )
			
			self._o_queueResize()
		else:
			newEntry = self._p_itemToChildEntry( item )
			oldEntry = self._childEntries[index]
			self._o_unregisterChildEntry( oldEntry )
			self._childEntries[index] = newEntry
			self._o_registerChildEntry( newEntry )
			self._p_childListModified()
			self._p_linkChildEntryCursorEntity( newEntry )
			self._o_queueResize()




	def __delitem__(self, index):
		entry = self._childEntries[index]
		del self._childEntries[index]
		if isinstance( entry, list ):
			for e in entry:
				self._o_unregisterChildEntry( e )
				DTCursorEntity.remove( e.child.getFirstCursorEntity(), e.child.getLastCursorEntity() )
		else:
			self._o_unregisterChildEntry( entry )
			DTCursorEntity.remove( entry.child.getFirstCursorEntity(), entry.child.getLastCursorEntity() )
		self._p_childListModified()
		self._o_queueResize()

		
		
		
	def _o_appendEntry(self, entry):
		assert not self.hasChild( entry.child ), 'child already present'
		self._childEntries.append( entry )
		self._o_registerChildEntry( entry )
		self._p_childListModified()
		self._p_linkChildEntryCursorEntity( entry )
		self._o_queueResize()
		

	def _o_extendEntries(self, entries):
		for entry in entries:
			assert not self.hasChild( entry.child ), 'child already present'
		self._childEntries.extend( entries )
		for entry in entries:
			self._o_registerChildEntry( entry )
		self._p_childListModified()
		
		for entry in entries:
			self._p_linkChildEntryCursorEntity( entry )
			
		self._o_queueResize()


	def _o_insertEntry(self, index, entry):
		assert not self.hasChild( entry.child ), 'child already present'
		self._childEntries.insert( index, entry )
		self._o_registerChildEntry( entry )
		self._p_childListModified()
		self._p_linkChildEntryCursorEntity( entry )
		self._o_queueResize()



	def _o_remove(self, child):
		assert self.hasChild( child ), 'child not present'
		entry = self._childToEntry[child]
		self._childEntries.remove( entry )
		self._o_unregisterChildEntry( entry )
		self._p_childListModified()
		
		DTCursorEntity.remove( entry.child.getFirstCursorEntity(), entry.child.getLastCursorEntity() )
		
		self._o_queueResize()



	def _f_removeChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		self[index] = DTBin()



	def _p_linkChildEntryCursorEntity(self, childEntry):
		prevCursorEntity = self._f_getPrevCursorEntityBeforeChild( childEntry.child )
		nextCursorEntity = self._f_getNextCursorEntityAfterChild( childEntry.child )
		DTCursorEntity.splice( prevCursorEntity, nextCursorEntity, childEntry.child.getFirstCursorEntity(), childEntry.child.getLastCursorEntity() )





	def _p_childListModified(self):
		pass


	@abstractmethod
	def _p_itemToChildEntry(self, item):
		pass

	
	
	
	
	
	
	
	#
	# CURSOR ENTITY METHODS
	#

	def isOrderReversed(self):
		return False
	
	
	def _o_getFirstCursorEntity(self):
		entries = self._childEntries
		if self.isOrderReversed():
			entries = reversed( entries )

		for entry in entries:
			first = entry.child.getFirstCursorEntity()
			if first is not None:
				return first
		return None


	def _o_getLastCursorEntity(self):
		entries = self._childEntries
		if not self.isOrderReversed():
			entries = reversed( entries )

		for entry in entries:
			last = entry.child.getLastCursorEntity()
			if last is not None:
				return last
		return None



	def _o_getPrevCursorEntityBeforeChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		
		if self.isOrderReversed():
			try:
				entries = self._childEntries[index+1:]
			except IndexError:
				return None
		else:
			entries = reversed( self._childEntries[:index] )
		
		for entry in entries:
			last = entry.child.getLastCursorEntity()
			if last is not None:
				return last
		return None

		
	def _o_getNextCursorEntityAfterChild(self, child):
		entry = self._childToEntry[child]
		index = self._childEntries.index( entry )
		
		if self.isOrderReversed():
			entries = reversed( self._childEntries[:index] )
		else:
			try:
				entries = self._childEntries[index+1:]
			except IndexError:
				return None
		
		for entry in entries:
			last = entry.child.getLastCursorEntity()
			if last is not None:
				return last
		return None



