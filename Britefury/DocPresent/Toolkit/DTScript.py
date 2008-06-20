##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer


_superOffset = 0.333
_subOffset = 0.333

_superOffsetFraction = _superOffset  /  ( _superOffset + _subOffset )
_subOffsetFraction = _subOffset  /  ( _superOffset + _subOffset )


class DTScript (DTContainer):
	LEFTSUPER = 0
	LEFTSUB = 1
	MAIN = 2
	RIGHTSUPER = 3
	RIGHTSUB = 4
	childSlots = [ LEFTSUPER, LEFTSUB, MAIN, RIGHTSUPER, RIGHTSUB ]

	def __init__(self, spacing=1.0, scriptSpacing=1.0, backgroundColour=None):
		super( DTScript, self ).__init__( backgroundColour )

		self._children = [ None ] * 5
		self._childRequisitions = [ Vector2()  for i in xrange( 0, 5 ) ]
		self._childAboveBaselines = [ 0.0   for i in xrange( 0, 5 ) ]
		self._leftRequisitionWidth = 0.0
		self._mainRequisitionWidth = 0.0
		self._rightRequisitionWidth = 0.0
		self._Ma = 0.0
		self._Mb = 0.0
		self._Mh = 0.0
		self._Pa = 0.0
		self._Pb = 0.0
		self._Ph = 0.0
		self._Ba = 0.0
		self._Bb = 0.0
		self._Bh = 0.0
		self._superBaseFromTop = 0.0
		self._mainBaseFromTop = 0.0
		self._subBaseFromTop = 0.0

		self._spacing = spacing
		self._scriptSpacing = scriptSpacing

		self._childScale = 0.7



	def getChild(self, slot):
		return self._children[slot]
	
	def setChild(self, slot, child):
		existingChild = self._children[slot]
		if child is not existingChild:
			if child is not None:
				child._f_unparent()
			if existingChild is not None:
				entry = self._childToEntry[existingChild]
				self._childEntries.remove( entry )
				self._o_unregisterChildEntry( entry )
				DTCursorEntity.remove( existingChild.getFirstCursorEntity(), existingChild.getLastCursorEntity() )
			self._children[slot] = child
			if child is not None:
				entry = self.ChildEntry( child)
				self._childEntries.append( entry )
				self._o_registerChildEntry( entry )
				if slot != self.MAIN:
					child._f_setScale( self._childScale, self._rootScale * self._childScale )
				DTCursorEntity.splice( self._f_getPrevCursorEntityBeforeChild( child ), self._f_getNextCursorEntityAfterChild( child ), child.getFirstCursorEntity(), child.getLastCursorEntity() )

			self._o_queueResize()
	
	
	def getMainChild(self):
		return self.getChild( self.MAIN )
	
	def getLeftSuperscriptChild(self):
		return self.getChild( self.LEFTSUPER )
	
	def getLeftSubscriptChild(self):
		return self.getChild( self.LEFTSUB )
	
	def getRightSuperscriptChild(self):
		return self.getChild( self.RIGHTSUPER )
	
	def getRightSubscriptChild(self):
		return self.getChild( self.RIGHTSUB )
	

	
	def setMainChild(self, child):
		return self.setChild( self.MAIN, child )
	
	def setLeftSuperscriptChild(self, child):
		return self.setChild( self.LEFTSUPER, child )
	
	def setLeftSubscriptChild(self, child):
		return self.setChild( self.LEFTSUB, child )
	
	def setRightSuperscriptChild(self, child):
		return self.setChild( self.RIGHTSUPER, child )
	
	def setRightSubscriptChild(self, child):
		return self.setChild( self.RIGHTSUB, child )

	
	
	def getChildScale(self):
		return self._childScale

	def setChildScale(self, childScale):
		self._childScale = childScale
		self._o_queueResize()


	def _f_removeChild(self, child):
		slot = self._children.index( child )
		self.setChild( slot, None )




	def _o_getRequiredWidth(self):
		for slot in self.childSlots:
			if self._children[slot] is not None:
				self._childRequisitions[slot].x = self._children[slot]._f_getRequisitionWidth()
			else:
				self._childRequisitions[slot].x = 0
				
		self._leftRequisitionWidth = max( self._childRequisitions[self.LEFTSUB].x, self._childRequisitions[self.LEFTSUPER].x )
		self._mainRequisitionWidth = self._childRequisitions[self.MAIN].x
		self._rightRequisitionWidth = max( self._childRequisitions[self.RIGHTSUB].x, self._childRequisitions[self.RIGHTSUPER].x )
		
		count = 0
		for w in [ self._leftRequisitionWidth, self._mainRequisitionWidth, self._rightRequisitionWidth ]:
			if w > 0.0:
				count += 1
		spacing = self._spacing  *  ( count - 1 )

		return self._leftRequisitionWidth + self._mainRequisitionWidth + self._rightRequisitionWidth + spacing


	def _o_getRequiredHeightAndBaseline(self):
		childBaselines = [ 0.0   for i in xrange( 0, 5 ) ]

		for slot in self.childSlots:
			if self._children[slot] is not None:
				self._childRequisitions[slot].y, childBaselines[slot] = self._children[slot]._f_getRequisitionHeightAndBaseline()
				if childBaselines[slot] is None:
					childBaselines[slot] = 0.0
			else:
				self._childRequisitions[slot].y = 0.0
				childBaselines[slot] = 0.0
			self._childAboveBaselines[slot] = self._childRequisitions[slot].y - childBaselines[slot]
				
		self._Ma  = self._childAboveBaselines[self.MAIN]
		self._Mb  = childBaselines[self.MAIN]
		self._Mh  = self._childRequisitions[self.MAIN].y
		
		self._Pa = max( self._childAboveBaselines[self.LEFTSUPER], self._childAboveBaselines[self.RIGHTSUPER] )
		self._Pb = max( childBaselines[self.LEFTSUPER], childBaselines[self.RIGHTSUPER] )
		self._Ph = max( self._childRequisitions[self.LEFTSUPER].y, self._childRequisitions[self.RIGHTSUPER].y )

		self._Ba = max( self._childAboveBaselines[self.LEFTSUB], self._childAboveBaselines[self.RIGHTSUB] )
		self._Bb = max( childBaselines[self.LEFTSUB], childBaselines[self.RIGHTSUB] )
		self._Bh = max( self._childRequisitions[self.LEFTSUB].y, self._childRequisitions[self.RIGHTSUB].y )
		
		
		# top: TOP
		# a: super top
		# b: main top
		# c: super baseline
		# d: super bottom
		# e: sub top
		# f: main baseline
		# g: main bottom
		# h: sub baseline
		# i: sub bottom
		# bottom: BOTTOM
		#
		# q: spacing between super bottom and sub top
		# r: min distance between super baseline and main baseline (1/3 of Mh)
		# s: min distance between main baseline and sub baseline (1/3 of Mh)
		# r and s can be thought of as springs
		
		if self._Ph > 0.0  and  self._Bh > 0.0:
			q = self._scriptSpacing
		
			# Start with f = 0
			f = 0.0
			
			# We can compute b and g immediately
			b = f - self._Ma
			g = f + self._Mb
			
			# Next compute c and h
			# The distance between c and h is max( Pb + Ba + q, r + s )
			r = self._Mh * _superOffset
			s = self._Mh * _subOffset
			cToH = max( self._Pb + self._Ba + q,   r + s )
		
			# Divide cToH between r and s according to their proportion
			r = cToH  *  _superOffsetFraction
			s = cToH  *  _subOffsetFraction
			
			# We can now compute c and h
			c = f - r
			h = f + s
			
			# We can compute a and d
			a = c - self._Pa
			d = c + self._Pb
			
			# We can compute i and e
			e = h - self._Ba
			i = h + self._Bb
			
			# We can now compute the top and the bottom
			top = min( b, a )
			bottom = max( g, i )
			
			self._superBaseFromTop = c - top
			self._mainBaseFromTop = f - top
			self._subBaseFromTop = h - top
		elif self._Ph > 0.0:
			# Start with f = 0
			f = 0.0
			
			# We can compute b and g immediately
			b = f - self._Ma
			g = f + self._Mb
			
			# R
			r = self._Mh * _superOffset
			
			# C
			c = f - r

			# We can compute a and d
			a = c - self._Pa
			d = c + self._Pb
			
			# We can now compute the top and the bottom
			top = min( b, a )
			bottom = max( g, d )
			
			self._superBaseFromTop = c - top
			self._mainBaseFromTop = f - top
			self._subBaseFromTop = f - top
		elif self._Bh > 0.0:
			# Start with f = 0
			f = 0.0
			
			# We can compute b and g immediately
			b = f - self._Ma
			g = f + self._Mb
			
			# Next compute h
			s = self._Mh * _subOffset
			
			# We can now compute h
			h = f + s
			
			# We can compute i and e
			e = h - self._Ba
			i = h + self._Bb
			
			# We can now compute the top and the bottom
			top = min( b, e )
			bottom = max( g, i )
			
			self._superBaseFromTop = f - top
			self._mainBaseFromTop = f - top
			self._subBaseFromTop = h - top
		else:
			f = 0.0
			
			top = -self._Ma
			bottom = self._Mb
			
			self._superBaseFromTop = f - top
			self._mainBaseFromTop = f - top
			self._subBaseFromTop = f - top
			
		height = bottom - top
		
		#print self._Ma, self._Mb, self._Mh
		#print self._Pa, self._Pb, self._Ph
		#print self._Ba, self._Bb, self._Bh
		#print self._superBaseFromTop, self._mainBaseFromTop, self._subBaseFromTop
		#print top, bottom
		#print ''

		
		return height, height - self._mainBaseFromTop
		
		
		


	def _o_onAllocateX(self, allocation):
		padding = max( ( allocation - self._requiredSize.x )  *  0.5, 0.0 )
		x = padding
		
		# Allocate left children
		bSpaceNext = False
		if self._children[self.LEFTSUPER] is not None:
			self._o_allocateChildX( self._children[self.LEFTSUPER], x  +  ( self._leftRequisitionWidth - self._childRequisitions[self.LEFTSUPER].x ), self._childRequisitions[self.LEFTSUPER].x )
			bSpaceNext = True
		if self._children[self.LEFTSUB] is not None:
			self._o_allocateChildX( self._children[self.LEFTSUB], x  +  ( self._leftRequisitionWidth - self._childRequisitions[self.LEFTSUB].x ), self._childRequisitions[self.LEFTSUB].x )
			bSpaceNext = True
		
		x += self._leftRequisitionWidth

		# Allocate main child
		if self._children[self.MAIN] is not None:
			if bSpaceNext:
				x += self._spacing
			self._o_allocateChildX( self._children[self.MAIN], x, self._mainRequisitionWidth )
			bSpaceNext = True
			
		x += self._mainRequisitionWidth

		# Allocate right children
		if self._children[self.RIGHTSUPER] is not None:
			if bSpaceNext:
				x += self._spacing
				bSpaceNext = False
			self._o_allocateChildX( self._children[self.RIGHTSUPER], x, self._childRequisitions[self.RIGHTSUPER].x )
		if self._children[self.RIGHTSUB] is not None:
			if bSpaceNext:
				x += self._spacing
				bSpaceNext = False
			self._o_allocateChildX( self._children[self.RIGHTSUB], x, self._childRequisitions[self.RIGHTSUB].x )


	def _o_onAllocateY(self, allocation):
		padding = max( ( allocation - self._requiredSize.y ) * 0.5, 0.0 )
		
		
		# Allocate superscript children
		y = padding + self._superBaseFromTop
		if self._children[self.LEFTSUPER] is not None:
			self._o_allocateChildY( self._children[self.LEFTSUPER], y - self._childAboveBaselines[self.LEFTSUPER], self._childRequisitions[self.LEFTSUPER].y )
		if self._children[self.RIGHTSUPER] is not None:
			self._o_allocateChildY( self._children[self.RIGHTSUPER], y - self._childAboveBaselines[self.RIGHTSUPER], self._childRequisitions[self.RIGHTSUPER].y )
			
		# Allocate main children
		y = padding + self._mainBaseFromTop
		if self._children[self.MAIN] is not None:
			self._o_allocateChildY( self._children[self.MAIN], y - self._childAboveBaselines[self.MAIN], self._childRequisitions[self.MAIN].y )
		
		# Allocate subscript children
		y = padding + self._subBaseFromTop
		if self._children[self.LEFTSUB] is not None:
			self._o_allocateChildY( self._children[self.LEFTSUB], y - self._childAboveBaselines[self.LEFTSUB], self._childRequisitions[self.LEFTSUB].y )
		if self._children[self.RIGHTSUB] is not None:
			self._o_allocateChildY( self._children[self.RIGHTSUB], y - self._childAboveBaselines[self.RIGHTSUB], self._childRequisitions[self.RIGHTSUB].y )


			
	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()


	def _f_refreshScale(self, scale, rootScale):
		for slot in self.childSlots:
			if slot != self.MAIN:
				if self._children[slot] is not None:
					self._children[slot]._f_setScale( self._childScale, rootScale * self._childScale )




	#
	# CURSOR ENTITY METHODS
	#

	def _o_getFirstCursorEntity(self):
		for slot in self.childSlots:
			if self._children[slot] is not None:
				return self._children[slot].getFirstCursorEntity()
		return None


	def _o_getLastCursorEntity(self):
		for slot in reversed( self.childSlots ):
			if self._children[slot] is not None:
				return self._children[slot].getFirstCursorEntity()
		return None
		
		
	
	def _o_getPrevCursorEntityBeforeChild(self, child):
		index = self._children.index( child )
		if index != self.RIGHTSUB:
			for slot in reversed( self.childSlots[:index] ):
				if self._children[slot] is not None:
					return self._children[slot].getLastCursorEntity()
		return None
	
		
	def _o_getNextCursorEntityAfterChild(self, child):
		index = self._children.index( child )
		if index != self.RIGHTSUB:
			for slot in self.childSlots[index+1:]:
				if self._children[slot] is not None:
					return self._children[slot].getFirstCursorEntity()
		return None
	
	
	#
	# FOCUS NAVIGATION METHODS
	#
	
	def horizontalNavigationList(self):
		return [ child   for child in self._children   if child is not None ]

	def verticalNavigationList(self):
		return []



	leftSuperscriptChild = property( getLeftSuperscriptChild, setLeftSuperscriptChild )
	leftSubscriptChild = property( getLeftSubscriptChild, setLeftSubscriptChild )
	mainChild = property( getMainChild, setMainChild )
	rightSuperscriptChild = property( getRightSuperscriptChild, setRightSuperscriptChild )
	rightSubscriptChild = property( getRightSubscriptChild, setRightSubscriptChild )
	childScale = property( getChildScale, setChildScale )






if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	from Britefury.DocPresent.Toolkit.DTBox import DTBox
	from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder
	import cairo
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.getGtkWidget().show()
	
	
	def makeScriptWidget(mainText, leftSuperText, leftSubText, rightSuperText, rightSubText):
		main = DTLabel( mainText )
		script = DTScript()
		
		script.mainChild = main
		
		if leftSuperText is not None:
			script.leftSuperscriptChild = DTLabel( leftSuperText )
		if leftSubText is not None:
			script.leftSubscriptChild = DTLabel( leftSubText )
		if rightSuperText is not None:
			script.rightSuperscriptChild = DTLabel( rightSuperText )
		if rightSubText is not None:
			script.rightSubscriptChild = DTLabel( rightSubText )
		
		border = DTActiveBorder()
		border.child = script
		
		
		labelA = DTLabel( 'Label A yYgGjJpPqQ', font='Sans 8' )
		labelB = DTLabel( 'Label B yYgGjJpPqQ', font='Sans 18' )
		box = DTBox( alignment=DTBox.ALIGN_BASELINES, spacing=15.0 )
		box.append( labelA )
		box.append( border )
		box.append( labelB )
			
		border2 = DTActiveBorder()
		border2.child = box

		return border2
	
	
	box = DTBox( DTBox.TOP_TO_BOTTOM, spacing=20.0 )
	for i in xrange( 0, 16 ):
		if ( i & 1 )  !=  0:
			leftSuperText = 'left super'
		else:
			leftSuperText = None
		
		if ( i & 2 )  !=  0:
			leftSubText = 'left sub'
		else:
			leftSubText = None

		if ( i & 4 )  !=  0:
			rightSuperText = 'right super'
		else:
			rightSuperText = None

		if ( i & 8 )  !=  0:
			rightSubText = 'right sub'
		else:
			rightSubText = None
			
		script = makeScriptWidget( 'MAIN%d'  %  ( i, ), leftSuperText, leftSubText, rightSuperText, rightSubText )
		
		box.append( script )


	doc.child = box


	window.add( doc.getGtkWidget() )
	window.show_all()

	gtk.main()

