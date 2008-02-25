##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************


"""
Cursor Entity


The API for the Document Presentation Toolkit cursor management system is as follows:

DTWidget.setCursorBlocked(bBlocked)  ::  sets whether the cursor is blocked; if the cursor is blocked, the cursor will skip all cursor entities contained within the range of the widget
DTWidget.isCursorBlocked()  ->  True if the cursor is blocked, False otherwise

DTWidget.getFirstCursorEntity()  ->  the first cursor entity in a widget  (None if cursor is blocked)
DTWidget.getLastCursorEntity()  ->  the last cursor entity in a widget  (None if cursor is blocked)
DTWidget._o_getFirstCursorEntity()  ->  INTERNAL HELPER METHOD;
		the first cursor entity in a widget
DTWidget._o_getLastCursorEntity()  ->  INTERNAL HELPER METHOD;
		the last cursor entity in a widget
DTWidget.getPrevCursorEntity()  ->  the cursor entity before the first cursor entity (comes from outside self)
DTWidget.getNextCursorEntity()  ->  the cursor entity after the last cursor entity (comes from outside self)

DTContainer._f_childCursorBlocked(child)  ->  used to notify a container that a child widget has blocked the cursor
DTContainer._f_childCursorUnblocked(child)  ->  used to notify a container that a child widget has unblocked the cursor

DTContainer._f_getPrevCursorEntityBeforeChild(child)  ->  THIS METHOD MUST RETURN THE CORRECT VALUE EVEN WHEN THE LINKS BETWEEN CURSOR ENTITIES ARE INCORRECT
		the cursor entity before the first cursor entity in child (can come from outside self)
DTContainer._f_getNextCursorEntityAfterChild(child)  ->  THIS METHOD MUST RETURN THE CORRECT VALUE EVEN WHEN THE LINKS BETWEEN CURSOR ENTITIES ARE INCORRECT
		the cursor entity after the last cursor entity in child (can come from outside self)

DTContainer._o_getPrevCursorEntityBeforeChild(child)  ->  INTERNAL HELPER METHOD; THIS METHOD MUST RETURN THE CORRECT VALUE EVEN WHEN THE LINKS BETWEEN CURSOR ENTITIES ARE INCORRECT
		the cursor entity before the first cursor entity in child (only looks in the range of self; returns None if the cursor entity is not in the range of self)
DTContainer._o_getNextCursorEntityAfterChild(child)  ->  INTERNAL HELPER METHOD; THIS METHOD MUST RETURN THE CORRECT VALUE EVEN WHEN THE LINKS BETWEEN CURSOR ENTITIES ARE INCORRECT
		the cursor entity after the last cursor entity in child (only looks in the range of self; returns None if the cursor entity is not in the range of self)
"""



class DTCursorEntity (object):
	__slots__ = [ '_prev', '_next', 'widget' ]
	
	def __init__(self, widget):
		self._prev = None
		self._next = None
		self.widget = widget
		
		

	def _p_getPrev(self):
		return self._prev
	
	def _p_setPrev(self, prev):
		if self._prev is not None:
			self._prev._next = None
		self._prev = prev
		if prev is not None:
			prev._next = self
	
	
	def _p_getNext(self):
		return self._next
	
	def _p_setNext(self, next):
		if self._next is not None:
			self._next._prev = None
		self._next = next
		if next is not None:
			next._prev = self
			
			
	
	@staticmethod
	def insertBefore(node, first, last):
		"""Inserts the range (first,last) before node"""
		if first is not None  and  last is not None:
			first.prev = node.prev
			last.next = node
			
	@staticmethod
	def insertAfter(node, first, last):
		"""Inserts the range first->last after node"""
		if first is not None  and  last is not None:
			last.next = node.next
			first.prev = node
			
	@staticmethod
	def splice(prev, next, first, last):
		"""Replaces the range between (but not including) prev:next  with  first->last"""
		if first is not None  and  last is not None  and  ( prev is not None  or  next is not None ):
			first.prev = prev
			last.next = next
		
			
			
	@staticmethod
	def remove(first, last):
		"""Removes the range first->last"""
		if first is not None  and  last is not None:
			p = first.prev
			n = last.next
			if p is not None:
				p.next = n
			elif n is not None:
				n.prev = p
			
			
	@staticmethod
	def buildListLinks(xs):
		"""Builds the links between an ordered list of entities"""
		if len( xs ) > 0:
			for a, b in zip( xs[:-1], xs[1:] ):
				a.next = b
			return xs[0], xs[-1]
		else:
			return None, None
	
	
	
	prev = property( _p_getPrev, _p_setPrev )
	next = property( _p_getNext, _p_setNext )







import unittest

class DTCursorEntityTest (unittest.TestCase):
	def makeEntityList(self, start, end):
		l = [ DTCursorEntity( x )   for x in xrange( start, end ) ]
		DTCursorEntity.buildListLinks( l )
		return l
	
	def getContents(self, start, end):
		c = []
		while start is not end:
			c.append( start.widget )
			start = start.next
		return c
		
	
	def testCTor(self):
		e = DTCursorEntity( 1 )
		self.assert_( e.widget is 1 )
		self.assert_( e.prev is None )
		self.assert_( e.next is None )



	def testSetPrev(self):
		ea = DTCursorEntity( None )
		eb = DTCursorEntity( None )
		
		eb.prev = ea
		
		self.assert_( ea.prev is None )
		self.assert_( ea.next is eb )
		self.assert_( eb.prev is ea )
		self.assert_( eb.next is None )
		
		eb.prev = None

		self.assert_( ea.prev is None )
		self.assert_( ea.next is None )
		self.assert_( eb.prev is None )
		self.assert_( eb.next is None )



	def testSetNext(self):
		ea = DTCursorEntity( None )
		eb = DTCursorEntity( None )
		
		ea.next = eb
		
		self.assert_( ea.prev is None )
		self.assert_( ea.next is eb )
		self.assert_( eb.prev is ea )
		self.assert_( eb.next is None )

		ea.next = None

		self.assert_( ea.prev is None )
		self.assert_( ea.next is None )
		self.assert_( eb.prev is None )
		self.assert_( eb.next is None )
		
		
		
	def testInsertBefore(self):
		l = self.makeEntityList( 0, 10 )
		l2 = self.makeEntityList( 20, 25 )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 10 ) )
		
		DTCursorEntity.insertBefore( l[5], l2[0], l2[-1] )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 5 ) + range( 20, 25 ) + range( 5, 10 ) )
		

	def testInsertAfter(self):
		l = self.makeEntityList( 0, 10 )
		l2 = self.makeEntityList( 20, 25 )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 10 ) )
		
		DTCursorEntity.insertAfter( l[5], l2[0], l2[-1] )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 6 ) + range( 20, 25 ) + range( 6, 10 ) )


	def testSplice(self):
		l = self.makeEntityList( 0, 10 )
		l2 = self.makeEntityList( 20, 25 )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 10 ) )
		
		DTCursorEntity.splice( l[3], l[7], l2[0], l2[-1] )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 4 ) + range( 20, 25 ) + range( 7, 10 ) )


	def testSplice2(self):
		l = self.makeEntityList( 0, 10 )
		l2 = self.makeEntityList( 20, 25 )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 10 ) )
		
		DTCursorEntity.splice( l[4], l[5], l2[0], l2[-1] )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 5 ) + range( 20, 25 ) + range( 5, 10 ) )


	def testRemove(self):
		l = self.makeEntityList( 0, 10 )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 10 ) )
		
		DTCursorEntity.remove( l[4], l[8] )
		
		self.assert_( self.getContents( l[0], None )  ==  range( 0, 4 ) + range( 9, 10 ) )
		
		
	def testBuildListLinks(self):
		l = [ DTCursorEntity( x )   for x in xrange( 0, 10 ) ]
		s, e = DTCursorEntity.buildListLinks( l )
		self.assert_( s is l[0] )
		self.assert_( e is l[-1] )



