##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import math

import pygtk
pygtk.require( '2.0' )
import gtk
import cairo


from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Math.Math import Vector2, Point2, BBox2
from Britefury.Event.QueuedEvent import *

from Britefury.GraphView.GraphViewHelper import *





NODE_OUTLINE_CURVE_RADIUS	= 10.0
NODE_WIDTH					= 150.0
NODE_NAME_FONT_SIZE		= 12.0
NODE_TITLEBAR_HEIGHT		= 20.0
NODE_BORDER_WIDTH			= 5.0
PIN_CIRCLE_RADIUS			= 6.0
PIN_CIRCLE_YOFFSET			= 9.0
PIN_FONT_SIZE				= 10.0
PIN_NAME_XOFFSET			= 3.0
PIN_HEIGHT					= PIN_CIRCLE_YOFFSET * 2
ESTIMATED_TEXT_HEIGHT		= 12.0

HIGHLIGHT_LINK_RANGE		= 5.0

NODE_SPACING				= 25.0








#
# HELPER CLASSES
#

class _SheetGraphViewItemListHelper (object):
	appendSignal = ClassSignal()
	removeSignal = ClassSignal()


	def __init__(self):
		super( _SheetGraphViewItemListHelper, self ).__init__()
		self._items = []


	def __contains__(self, item):
		return item in self._items

	def __getitem__(self, index):
		return self._items[index]

	def __len__(self):
		return len( self._items )

	def __iter__(self):
		return iter( self._items )


	def _p_append(self, item):
		self._items.append( item )
		self.appendSignal.emit( item )

	def _p_remove(self, item):
		self.removeSignal.emit( item )
		self._items.remove( item )



class _SheetGraphViewPinListHelper (_SheetGraphViewItemListHelper):
	def __init__(self, sheetGraphNode):
		super( _SheetGraphViewPinListHelper, self ).__init__()
		self._sheetGraphNode = sheetGraphNode



class _SheetGraphViewSinkListHelper (_SheetGraphViewPinListHelper):
	def __init__(self, sheetGraphnode):
		super( _SheetGraphViewSinkListHelper, self ).__init__( sheetGraphnode )
		self._items = self._sheetGraphNode.sinks


class _SheetGraphViewSourceListHelper (_SheetGraphViewPinListHelper):
	def __init__(self, sheetGraphnode):
		super( _SheetGraphViewSourceListHelper, self ).__init__( sheetGraphnode )
		self._items = self._sheetGraphNode.sources






#
# LINK LIST HELPER
#

class _SheetGraphViewGraphLink (object):
	def __init__(self, source, sink):
		super( _SheetGraphViewGraphLink, self ).__init__()
		self.source = source
		self.sink = sink





class _SheetGraphViewGraphLinkList (_SheetGraphViewItemListHelper):
	def __init__(self):
		super( _SheetGraphViewGraphLinkList, self ).__init__()
		self._graph = None
		self._sinkToListener = {}
		self._sinkSourcePairToLink = {}



	def attachGraph(self, graph):
		self._graph = graph

		for node in self._graph.nodes:
			self._f_nodeAdded( graph, node )


	def detachGraph(self):
		for node in self._graph.nodes:
			self._f_nodeRemoved( self._graph, node )

		self._graph = None



	def _p_makeSinkListener(self, sink):
		def onEvaluator(oldIns, newIns):
			oldInsSet = set( oldIns )
			newInsSet = set( newIns )
			for source in ( oldInsSet - newInsSet ):
				link = self._sinkSourcePairToLink.pop( ( sink, source ) )
				self._p_remove( link )
			for source in ( newInsSet - oldInsSet ):
				link = _SheetGraphViewGraphLink( source, sink )
				self._sinkSourcePairToLink[ ( sink, source ) ] = link
				self._p_append( link )
		return onEvaluator

	def _f_nodeAdded(self, graph, node):
		# TODO: handle changing sink list
		for sink in node.sinks:
			listener = self._p_makeSinkListener( sink )
			self._sinkToListener[sink] = listener
			sink.evaluatorSignal.connect( listener )
			listener( [], sink.value )


	def _f_nodeRemoved(self, graph, node):
		# TODO: handle changing sink list
		for sink in node.sinks:
			listener = self._sinkToListener.pop( sink )
			listener( sink.value, [] )
			sink.evaluatorSignal.disconnect( listener )




# Win32 BUG WORK-AROUNDS
# The Win32 version of cairo raises a MemoryError if an empty string is passed to either
# Context.text_extents() or Context.show_text()

def textExtents(context, text):
	if text == '':
		return 0, 0, 0, 0, 0, 0
	else:
		return context.text_extents( text )


def showText(context, text):
	if text != '':
		context.show_text( text )






def _computeLinkCurveControlPoints(fromOut, toIn):
	# Control points p1 and p4 are the end points
	p1 = fromOut
	p4 = toIn

	# dx (delta-x) is the x-difference from p1 to p4, clamped to >=20, divided by 3; space the control points evenly
	dx = max( abs( p4.x - p1.x ), 20.0 )  /  3.0

	# Compute the y-offsets for p2 and p3
	dy2 = ( p4.y - p1.y ) * 0.05
	dy3 = -dy2

	# Compute the control points p2 and p3
	p2 = p1 + Vector2( dx, dy2 )
	p3 = p4 + Vector2( -dx, dy3 )

	return p1, p2, p3, p4



def _drawLinkCurve(fromOut, toIn, context):
	context.new_path()

	# Bezier curve
	p1, p2, p3, p4 = _computeLinkCurveControlPoints( fromOut, toIn )

	context.move_to( p1.x, p1.y )
	context.curve_to( p2.x, p2.y, p3.x, p3.y, p4.x, p4.y )

	context.stroke()





class _SheetGraphViewRectangle (object):
	def __init__(self, position, size):
		self._position = position
		self._size = size
		self.box = BBox2()
		self.box.addPoint( self._position )
		self.box.addPoint( self._position + self._size )
		self.onPositionModify = None
		self.onSizeModify = None
		self.onModify = None


	def translate(self, delta):
		self._position += delta
		self._p_modified()
		if self.onPositionModify is not None:
			self.onPositionModify()


	def _p_modified(self):
		self.box = BBox2()
		self.box.addPoint( self._position )
		self.box.addPoint( self._position + self._size )


	def _p_getPosition(self):
		return self._position

	def _p_setPosition(self, pos):
		bModified = pos != self._position
		self._position = pos
		self._p_modified()
		if bModified:
			if self.onPositionModify is not None:
				self.onPositionModify()
			if self.onModify is not None:
				self.onModify()


	def _p_getSize(self):
		return self._size

	def _p_setSize(self, size):
		bModified = size != self._size
		self._size = size
		self._p_modified()
		if bModified:
			if self.onSizeModify is not None:
				self.onSizeModify()
			if self.onModify is not None:
				self.onModify()


	def _p_getPositionAndSize(self):
		return self._position, self._size

	def _p_setPositionAndSize(self, posAndSize):
		bPositionModified = posAndSize[0] != self._position
		bSizeModified = posAndSize[1] != self._size
		self._position, self._size = posAndSize
		self._p_modified()
		if bPositionModified  and  self.onPositionModify is not None:
			self.onPositionModify()
		if bSizeModified  and  self.onSizeModify is not None:
			self.onSizeModify()
		if ( bPositionModified  or  bSizeModified )  and  self.onModify is not None:
			self.onModify()


	position = property( _p_getPosition, _p_setPosition )
	size = property( _p_getSize, _p_setSize )
	positionAndSize = property( _p_getPositionAndSize, _p_setPositionAndSize )





class _SheetGraphViewWidgetContainer (object):
	def __init__(self):
		super( _SheetGraphViewWidgetContainer, self ).__init__()
		self._children = []
		self._childIdToChild = {}
		self._childToId = {}
		self._widgetBoxTable = GraphViewWidgetBoxTable()
		self._pointerChild = None
		self._grabChild = None
		self._grabButton = None


	def _p_addChild(self, child):
		self._children.append( child )
		rect = child.rect
		if rect is not None:
			widgetId = self._widgetBoxTable.addWidgetBox( rect )
			self._childIdToChild[widgetId] = child
			self._childToId[child] = widgetId

	def _p_removeChild(self, child):
		self._children.remove( child )
		rect = child.rect
		if rect is not None:
			widgetId = self._childToId[child]
			self._widgetBoxTable.removeWidgetBox( widgetId )
			del self._childIdToChild[widgetId]
			del self._childToId[child]

	def _p_onChildRectModify(self, child):
		rect = child.rect
		if rect is None:
			try:
				widgetId = self._childToId[child]
				self._widgetBoxTable.removeWidgetBox( widgetId )
				del self._childIdToChild[widgetId]
				del self._childToId[child]
			except KeyError:
				pass
		else:
			try:
				widgetId = self._childToId[child]
				self._widgetBoxTable.setWidgetBox( widgetId, child.rect.box )
			except KeyError:
				widgetId = self._widgetBoxTable.addWidgetBox( rect.box )
				self._childIdToChild[widgetId] = child
				self._childToId[child] = widgetId


	def _p_rootPositionModified(self):
		for child in self._children:
			child._p_rootPositionModified()


	def _p_evButtonDown(self, pos, button, state):
		pass

	def _p_evButtonDown2(self, pos, button, state):
		pass

	def _p_evButtonDown3(self, pos, button, state):
		pass

	def _p_evButtonUp(self, pos, button, state):
		pass

	def _p_evMotion(self, pos):
		pass

	def _p_evEnter(self, pos):
		pass

	def _p_evLeave(self, pos):
		pass


	def _p_onButtonDown(self, pos, button, state):
		if self._grabChild is None:
			widgetId = self._widgetBoxTable.getWidgetAtPoint( pos )
			if widgetId != -1:
				child = self._childIdToChild[widgetId]
				child._p_onButtonDown( child._p_relativePoint( pos ), button, state )
				self._grabChild = child
				self._grabButton = button
			if self._grabChild is None:
				self._p_evButtonDown( pos, button, state )
		else:
			self._grabChild._p_onButtonDown( self._grabChild._p_relativePoint( pos ), button, state )

	def _p_onButtonDown2(self, pos, button, state):
		if self._grabChild is not None:
			self._grabChild._p_onButtonDown2( self._grabChild._p_relativePoint( pos ), button, state )
		else:
			self._p_evButtonDown2( pos, button, state )

	def _p_onButtonDown3(self, pos, button, state):
		if self._grabChild is not None:
			self._grabChild._p_onButtonDown3( self._grabChild._p_relativePoint( pos ), button, state )
		else:
			self._p_evButtonDown3( pos, button, state )

	def _p_onButtonUp(self, pos, button, state):
		if self._grabChild is not None:
			if button == self._grabButton:
				self._grabButton = None
				if not self._grabChild._p_isPointWithinBounds( pos ):
					self._grabChild._p_onLeave( self._grabChild._p_relativePoint( pos ) )
				self._grabChild._p_onButtonUp( self._grabChild._p_relativePoint( pos ), button, state )
				self._grabChild = None
			else:
				self._grabChild._p_onButtonUp( self._grabChild._p_relativePoint( pos ), button, state )
		else:
			self._p_evButtonUp( pos, button, state )


	def _p_onMotion(self, pos):
		if self._grabChild is not None:
			self._grabChild._p_onMotion( self._grabChild._p_relativePoint( pos ) )
		else:
			if self._pointerChild is not None:
				if not self._pointerChild._p_isPointWithinBounds( pos ):
					self._pointerChild._p_onLeave( self._pointerChild._p_relativePoint( pos ) )
					self._pointerChild = None
				else:
					self._pointerChild._p_onMotion( self._pointerChild._p_relativePoint( pos ) )
			else:
				widgetId = self._widgetBoxTable.getWidgetAtPoint( pos )
				if widgetId != -1:
					child = self._childIdToChild[widgetId]
					child._p_onEnter( child._p_relativePoint( pos ) )
					self._pointerChild = child
		self._p_evMotion( pos )


	def _p_onEnter(self, pos):
		for child in reversed( self._children ):
			if child._p_isPointWithinBounds( pos ):
				child._p_onEnter( child._p_relativePoint( pos ) )
				self._pointerChild = child
				break
		self._p_evEnter( pos )


	def _p_onLeave(self, pos):
		if self._grabChild is None:
			if self._pointerChild is not None:
				self._pointerChild._p_onLeave( self._pointerChild._p_relativePoint( pos ) )
				self._pointerChild = None
		self._p_evLeave( pos )





class _SheetGraphViewWidget (_SheetGraphViewWidgetContainer):
	def __init__(self, parent, graphView):
		super( _SheetGraphViewWidget, self ).__init__()
		self._rect = None
		self._requisition = Vector2()
		self._parent = parent
		self._graphView = graphView



	def _p_isPointWithinBounds(self, point):
		if self._rect is not None:
			return self._rect.box.contains( point )
		else:
			return False

	def _p_relativePoint(self, point):
		if self._rect is not None:
			return Point2( point - self._rect.position )
		else:
			return point



	def _p_evButtonDown(self, pos, button, state):
		self._graphView._p_onWidgetEvButtonDown( self, pos, button, state )

	def _p_evButtonDown2(self, pos, button, state):
		self._graphView._p_onWidgetEvButtonDown2( self, pos, button, state )

	def _p_evButtonDown3(self, pos, button, state):
		self._graphView._p_onWidgetEvButtonDown3( self, pos, button, state )

	def _p_evButtonUp(self, pos, button, state):
		self._graphView._p_onWidgetEvButtonUp( self, pos, button, state )

	def _p_evMotion(self, pos):
		self._graphView._p_onWidgetEvMotion( self, pos )

	def _p_evEnter(self, pos):
		self._graphView._p_onWidgetEvEnter( self, pos )

	def _p_evLeave(self, pos):
		self._graphView._p_onWidgetEvLeave( self, pos )



	def _p_queueResize(self):
		self._rect = None
		self._parent._p_onChildRectModify( self )
		self._parent._p_childQueueResize( self )

	def _p_childQueueResize(self, child):
		self._p_queueResize()

	def _p_postFullRedraw(self):
		self._parent._p_postFullRedraw()

	def _p_postRedraw(self):
		if self._rect is not None:
			self._p_postRedrawArea( Point2(), self._rect.size )

	def _p_postRedrawArea(self, localPos, size):
		if self._rect is not None:
			self._parent._p_postRedrawArea( localPos + self._rect.position.toVector2(), size )


	def _p_getRootPosition(self):
		if self._rect is None:
			return self._parent._p_getRootPosition()
		else:
			return self._rect.position  +  self._parent._p_getRootPosition().toVector2()


	def _p_allocate(self, rect):
		self.rect = rect
		self._p_postRedraw()


	def _p_onRectModify(self):
		self._parent._p_onChildRectModify( self )


	def _p_sizeModified(self):
		pass



	def _p_getRect(self):
		return self._rect

	def _p_setRect(self, rect):
		if rect is None  and  self._rect is not None:
			self._rect = None
		elif rect is not None  and  self._rect is None:
			self._rect = _SheetGraphViewRectangle( rect.position, rect.size )
			self._rect.onPositionModify = self._p_rootPositionModified
			self._rect.onSizeModify = self._p_sizeModified
			self._rect.onModify = self._p_onRectModify
		elif rect is not None  and  self._rect is not None:
			self._rect.positionAndSize = rect.position, rect.size
		self._p_onRectModify()



	rect = property( _p_getRect, _p_setRect )




class SheetGraphViewNode (_SheetGraphViewWidget):
	class _TitleBar (_SheetGraphViewWidget):
		def __init__(self, nodeView, graphView):
			super( SheetGraphViewNode._TitleBar, self ).__init__( nodeView, graphView )
			self._nodeView = nodeView
			self._name = ''
			self._tipText = ''
			self._nodeView = nodeView
			self._bHighlight = False


		def setText(self, name, tipText):
			self._name = name
			self._tipText = tipText
			self._p_queueResize()
			self._p_postRedraw()


		def _p_evEnter(self, pos):
			super( SheetGraphViewNode._TitleBar, self )._p_evEnter( pos )
			self._bHighlight = True
			self._p_postRedraw()

		def _p_evLeave(self, pos):
			super( SheetGraphViewNode._TitleBar, self )._p_evLeave( pos )
			self._bHighlight = False
			self._p_postRedraw()


		def _p_draw(self, context):
			assert self.rect is not None

			context.save()

			context.translate( self.rect.position.x, self.rect.position.y )

			if self._nodeView._graphNode is None:
				context.set_source_rgb( 0.55, 0.55, 0.55 )
			else:
				if self._nodeView._bIsCurrentNode:
					context.set_source_rgb( 0.4, 0.7, 0.4 )
				elif self._nodeView._bSelected:
					context.set_source_rgb( 0.3, 0.5, 0.7 )
				else:
					context.set_source_rgb( 0.45, 0.45, 0.55 )
			context.rectangle( 0.0, 0.0, self.rect.size.x, NODE_TITLEBAR_HEIGHT )
			context.fill()


			# DRAW THE NAME

			# Set the name font
			context.select_font_face( 'Sans', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL )
			context.set_font_size( NODE_NAME_FONT_SIZE )

			# Get the text extents
			xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, self._name )

			# Compute the position
			nameX = self.rect.size.x/2 - ( width / 2  +  xBearing )
			nameY = NODE_TITLEBAR_HEIGHT / 2  -  ( height / 2  +  yBearing )

			# Draw with drop-shadow
			context.set_source_rgb( 0.0, 0.0, 0.0 )
			context.move_to( nameX + 1.0, nameY + 1.0 )
			showText( context, self._name )

			if self._bHighlight:
				context.set_source_rgb( 1.0, 1.0, 0.0 )
			else:
				context.set_source_rgb( 1.0, 1.0, 1.0 )
			context.move_to( nameX, nameY )
			showText( context, self._name )

			context.restore()



		def _p_getRequisition(self, context):
			xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, self._name )
			self._requisition = Vector2( width, NODE_TITLEBAR_HEIGHT )
			return self._requisition

		def _p_getEstimatedRequisition(self):
			return Vector2( NODE_WIDTH, NODE_TITLEBAR_HEIGHT )







	class _PinView (_SheetGraphViewWidget):
		def __init__(self, pinListView, graphView):
			super( SheetGraphViewNode._PinView, self ).__init__( pinListView, graphView )
			self._name = ''
			self._pinListView = pinListView
			self._pin = None
			self._bHighlight = False
			self._bLinkHighlight = False

			self._linkViews = []
			self._requisition = Vector2()


		def attachPin(self, pin):
			assert self._pin is None, 'pin already attached'
			self._pin = pin
			self._p_onName( self._pin.name )
			#self._pin.nameSignal.connect( self._p_onName )
			self._p_postRedraw()


		def detachPin(self):
			assert self._pin is not None, 'no pin attached'
			#self._pin.nameSignal.disconnect( self._p_onName )
			self._p_onName( '' )
			self._pin = None
			self._p_postRedraw()


		def addLinkView(self, linkView):
			self._linkViews.append( linkView )


		def removeLinkView(self, linkView):
			self._linkViews.remove( linkView )



		def _p_onName(self, name):
			self._name = name
			self.rect = None
			self._p_queueResize()
			self._p_postRedraw()


		def _p_evEnter(self, pos):
			super( SheetGraphViewNode._PinView, self )._p_evEnter( pos )
			for linkView in self._linkViews:
				linkView.highlight()
			self._bHighlight = True
			self._p_postRedraw()

		def _p_evLeave(self, pos):
			super( SheetGraphViewNode._PinView, self )._p_evLeave( pos )
			for linkView in self._linkViews:
				linkView.unhighlight()
			self._bHighlight = False
			self._p_postRedraw()


		def _p_linkHighlight(self):
			self._bLinkHighlight = True

		def _p_linkUnhighlight(self):
			self._bLinkHighlight = False




		def _p_getRequisition(self, context):
			xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, self._name )
			self._requisition = Vector2( PIN_CIRCLE_RADIUS + PIN_NAME_XOFFSET + width, PIN_HEIGHT )
			return self._requisition

		def _p_getEstimatedRequisition(self):
			return Vector2( NODE_WIDTH, PIN_HEIGHT )


		def _p_setPinColour(self, context):
			if self._pin is None:
				context.set_source_rgb( 0.55, 0.55, 0.55 )
			else:
				if self._bLinkHighlight:
					context.set_source_rgb( 0.0, 1.0, 0.0 )
				else:
					if self._bHighlight:
						context.set_source_rgb( 1.0, 1.0, 0.0 )
					else:
						context.set_source_rgb( 0.5, 0.3, 0.6 )


		def _p_setPinNameColour(self, context):
			if self._bLinkHighlight:
				context.set_source_rgb( 0.0, 1.0, 0.0 )
			else:
				if self._bHighlight:
					context.set_source_rgb( 1.0, 1.0, 0.0 )
				else:
					context.set_source_rgb( 1.0, 1.0, 1.0 )


		def _p_rootPositionModified(self):
			super( SheetGraphViewNode._PinView, self )._p_rootPositionModified()
			for linkView in self._linkViews:
				linkView._p_pinPositionModified()

		def _p_sizeModified(self):
			super( SheetGraphViewNode._PinView, self )._p_sizeModified()
			for linkView in self._linkViews:
				linkView._p_pinPositionModified()



		def _p_getPin(self):
			return self._pin


		pin = property( _p_getPin )







	class _SinkView (_PinView):
		def _p_draw(self, context):
			assert self.rect is not None

			context.save()

			context.translate( self.rect.position.x, self.rect.position.y )

			context.new_path()
			context.set_line_width( 1.0 )
			self._p_setPinColour( context )
			context.arc( 0.0, self.rect.size.y * 0.5, PIN_CIRCLE_RADIUS, math.pi * 1.5, math.pi * 2.5 )
			context.fill_preserve()
			context.set_source_rgb( 0.2, 0.2, 0.2 )
			context.stroke()

			# Set the name font
			context.select_font_face( 'Sans', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL )
			context.set_font_size( PIN_FONT_SIZE )

			# Get the text extents
			xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, self._name )

			# Compute the position
			nameX = PIN_CIRCLE_RADIUS + PIN_NAME_XOFFSET - xBearing
			nameY = self.rect.size.y * 0.5 - ( height/2 + yBearing )

			# Draw
			self._p_setPinNameColour( context )
			context.move_to( nameX, nameY )
			showText( context, self._name )

			context.restore()


		def _p_getConnectionPoint(self):
			if self.rect is not None:
				size = self.rect.size
			else:
				size = Vector2()
			return self._p_getRootPosition()  +  Vector2( 0.0, size.y * 0.5 )





	class _SourceView (_PinView):
		def _p_draw(self, context):
			assert self.rect is not None

			context.save()
			context.translate( self.rect.position.x, self.rect.position.y )

			context.new_path()
			self._p_setPinColour( context )
			context.arc( self.rect.size.x, self.rect.size.y * 0.5, PIN_CIRCLE_RADIUS, math.pi * 0.5, math.pi * 1.5 )
			context.fill_preserve()
			context.set_source_rgb( 0.2, 0.2, 0.2 )
			context.stroke()

			# Set the name font
			context.select_font_face( 'Sans', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL )
			context.set_font_size( PIN_FONT_SIZE )

			# Get the text extents
			xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, self._name )

			# Compute the position
			nameX = self.rect.size.x - PIN_CIRCLE_RADIUS - PIN_NAME_XOFFSET - width - xBearing
			nameY = self.rect.size.y * 0.5 - ( height/2 + yBearing )

			# Draw
			if self._bHighlight:
				context.set_source_rgb( 1.0, 1.0, 0.0 )
			else:
				context.set_source_rgb( 1.0, 1.0, 1.0 )
			context.move_to( nameX, nameY )
			showText( context, self._name )

			context.restore()



		def _p_getConnectionPoint(self):
			if self.rect is not None:
				size = self.rect.size
			else:
				size = Vector2()
			return self._p_getRootPosition()  +  Vector2( size.x, size.y * 0.5 )






	class PinListView (_SheetGraphViewWidget):
		JUSTIFY_LEFT = 0
		JUSTIFY_RIGHT = 1

		def __init__(self, nodeView, graphView, pinViewClass, justify):
			super( SheetGraphViewNode.PinListView, self ).__init__( nodeView, graphView )
			self._pinViewClass = pinViewClass
			self._nodeView = nodeView
			self._pinList = None
			self._pinViews = []
			self._pinToPinView = {}
			self._requisition = Vector2()
			self._justify  = justify


		def attachPinList(self, pinList):
			assert self._pinList is None, 'pin list already attached'

			self._pinList = pinList
			for pin in self._pinList:
				pinView = self._pinViewClass( self, self._graphView )
				pinView.attachPin( pin )
				self._pinViews.append( pinView )
				self._pinToPinView[pin] = pinView
				self._children.append( pinView )

			self._pinList.appendSignal.connect( self._p_onAppend )
			self._pinList.removeSignal.connect( self._p_onRemove )

			self._p_postRedraw()


		def detachPinList(self):
			assert self._pinList is not None, 'no pin attached'

			self._pinList.appendSignal.disconnect( self._p_onAppend )
			self._pinList.removeSignal.disconnect( self._p_onRemove )

			for pinView in self._pinViews:
				pin = pinView.pin
				pinView.detachPin()
				self._children.remove( pinView )

			self._pinViews = []
			self._pinToPinView = {}

			self._pinList = None

			self._p_postRedraw()


		def _p_getPinView(self, pin):
			return self._pinToPinView[pin]


		def _p_onAppend(self, pin):
			pinView = self._pinViewClass( self, self._graphView )
			pinView.attachPin( pin )
			self._pinViews.append( pinView )
			self._pinToPinView[pin] = pinView
			self._p_queueResize()
			self._p_postRedraw()


		def _p_onRemove(self, pin):
			pinView = self._pinToPinView[pin]
			pinView.detachPin()
			self._pinViews.remove( pinView )
			del self._pinToPinView[pin]
			self._p_queueResize()
			self._p_postRedraw()



		def _p_draw(self, context):
			context.save()
			context.translate( self.rect.position.x, self.rect.position.y )
			for pinView in self._pinViews:
				pinView._p_draw( context )
			context.restore()


		def _p_getRequisition(self, context):
			if len( self._pinViews ) > 0:
				self._requisition = reduce( lambda a, b: Vector2( max( a.x, b.x ), a.y + b.y ),  [ pinView._p_getRequisition( context )   for pinView in self._pinViews ] )
			else:
				self._requisition = Vector2()
			return self._requisition

		def _p_getEstimatedRequisition(self):
			if len( self._pinViews ) > 0:
				return reduce( lambda a, b: Vector2( max( a.x, b.x ), a.y + b.y ),  [ pinView._p_getEstimatedRequisition()   for pinView in self._pinViews ] )
			else:
				return Vector2()


		def _p_allocate(self, rect):
			super( SheetGraphViewNode.PinListView, self )._p_allocate( rect )
			size = rect.size
			y = 0.0
			for pinView in self._pinViews:
				w = min( size.x, pinView._requisition.x )
				if self._justify == self.JUSTIFY_LEFT:
					x = 0.0
				else:
					x = size.x - w
				pinView._p_allocate( _SheetGraphViewRectangle( Point2( x, y ), Vector2( w, pinView._requisition.y ) ) )
				y += pinView._requisition.y


		def _p_getPinList(self):
			return self._pinList


		pinList = property( _p_getPinList )





	class _SinkListView (PinListView):
		def __init__(self, nodeView, graphView):
			super( SheetGraphViewNode._SinkListView, self ).__init__( nodeView, graphView, SheetGraphViewNode._SinkView, SheetGraphViewNode.PinListView.JUSTIFY_LEFT )




	class _SourceListView (PinListView):
		def __init__(self, nodeView, graphView):
			super( SheetGraphViewNode._SourceListView, self ).__init__( nodeView, graphView, SheetGraphViewNode._SourceView, SheetGraphViewNode.PinListView.JUSTIFY_RIGHT )








	def __init__(self, graphView):
		super( SheetGraphViewNode, self ).__init__( graphView, graphView )
		self._graphNode = None
		self._name = ''
		self._tipText = ''
		self._inputs = []
		self._outputs = []
		self._titleBar = SheetGraphViewNode._TitleBar( self, graphView )
		self._sinkViews = SheetGraphViewNode._SinkListView( self, graphView )
		self._sourceViews = SheetGraphViewNode._SourceListView( self, graphView )
		self._p_addChild( self._titleBar )
		self._p_addChild( self._sinkViews )
		self._p_addChild( self._sourceViews )
		self._bSelected = False
		self._bIsCurrentNode = False
		self._bHighlight = False
		self._sinkList = None
		self._sourceList = None

		self._requisition = Vector2()





	def attachGraphNode(self, graphNode):
		assert self._graphNode is None, 'graph node already attached'
		self._graphNode = graphNode

		for node in graphNode.inputNodes:
			viewNode = self._graphView._p_getViewNodeForGraphNode( node )
			self.addInput( viewNode )

		self._sinkList = _SheetGraphViewSinkListHelper( graphNode )
		self._sourceList = _SheetGraphViewSourceListHelper( graphNode )

		graphNode.inputNodes.appendSignal.connect( self._p_onAppendInputNode )
		graphNode.inputNodes.removeSignal.connect( self._p_onRemoveInputNode )
		self._graphView._graphViewDisplayTable.nodePositionSignal.connect( self._p_onViewNodePosition )

		self._sinkViews.attachPinList( self._sinkList )
		self._sourceViews.attachPinList( self._sourceList )

		self.setText( graphNode.__class__.__name__, '' )

		self._p_postRedraw()


	def detachGraphNode(self):
		assert self._graphNode is not None, 'no graph node attached'

		self._sinkList = None
		self._sourceList = None

		self._sinkViews.detachPinList()
		self._sourceViews.detachPinList()

		self._graphNode.inputNodes.appendSignal.disconnect( self._p_onAppendInputNode )
		self._graphNode.inputNodes.removeSignal.disconnect( self._p_onRemoveInputNode )
		self._graphView._graphViewDisplayTable.nodePositionSignal.disconnect( self._p_onViewNodePosition )

		self.clearInputs()

		self._graphNode = None

		self._p_postRedraw()



	def setText(self, name, tipText):
		self._titleBar.setText( name, tipText )




	def addOutput(self, viewNode):
		assert viewNode not in self._outputs, 'viewNode not in output list'
		self._outputs.append( viewNode )


	def removeOutput(self, viewNode):
		assert viewNode in self._outputs, 'viewNode not in output list'
		self._outputs.remove( viewNode )


	def clearOutputs(self):
		while len( self._outputs ) > 0:
			viewNode = self._outputs[-1]
			viewNode.removeInput( self )
		self.refreshDepth()


	def hasOutput(self, viewNode):
		return viewNode in self._outputs

	def getNumOutputs(self):
		return len( self._outputs )

	def getOutput(self, index):
		return self._outputs[index]


	def isRootNode(self):
		return len( self._outputs )  ==  0



	def checkAddInputForCycles(self, viewNode):
		assert self._graphView == viewNode._graphView, 'views do not match'

		if viewNode == self:
			return True
		else:
			return viewNode.lookForInput( self )

		return False


	def addInput(self, viewNode):
		assert not self.checkAddInputForCycles( viewNode ), 'adding input would result in cycles'
		assert viewNode not in self._inputs, 'viewNode already in input list'
		self._inputs.append( viewNode )
		viewNode.addOutput( self )


	def removeInput(self, viewNode):
		assert viewNode in self._inputs, 'viewNode not in input list'
		self._inputs.remove( viewNode )
		viewNode.removeOutput( self )

	def clearInputs(self):
		for viewNode in self._inputs:
			viewNode.removeOutput( self )
		self._inputs = []

	def hasInput(self, viewNode):
		return viewNode in self._inputs

	def lookForInput(self, viewNode):
		if viewNode in self._inputs:
			return True
		else:
			for inputNode in self._inputs:
				if inputNode.lookForInput( viewNode ):
					return True

		return False

	def getNumInputs(self):
		return len( self._inputs )

	def getInput(self, index):
		return self._inputs[index]




	def select(self):
		self._bSelected = True
		self._p_postRedraw()

	def unselect(self):
		self._bSelected = False
		self._p_postRedraw()

	def setCurrent(self):
		self._bIsCurrentNode = True
		self._p_postRedraw()

	def clearCurrent(self):
		self._bIsCurrentNode = False
		self._p_postRedraw()


	def _p_onAppendInputNode(self, node):
		viewNode = self._graphView._p_getViewNodeForGraphNode( node )
		self.addInput( viewNode )


	def _p_onRemoveInputNode(self, node):
		viewNode = self._graphView._p_getViewNodeForGraphNode( node )
		self.removeInput( viewNode )



	def _p_getSourceView(self, graphSource):
		return self._sourceViews._p_getPinView( graphSource )

	def _p_getSinkView(self, graphSink):
		return self._sinkViews._p_getPinView( graphSink )



	def _p_translate(self, delta):
		self.position = self.position + delta



	def _p_drawOutlinePath(self, context):
		size = self.rect.size
		context.arc( NODE_OUTLINE_CURVE_RADIUS, NODE_OUTLINE_CURVE_RADIUS, NODE_OUTLINE_CURVE_RADIUS, math.pi, math.pi * 1.5 )
		context.arc( size.x - NODE_OUTLINE_CURVE_RADIUS, NODE_OUTLINE_CURVE_RADIUS, NODE_OUTLINE_CURVE_RADIUS, math.pi * 1.5, math.pi * 2.0 )
		context.arc( size.x - NODE_OUTLINE_CURVE_RADIUS, size.y - NODE_OUTLINE_CURVE_RADIUS, NODE_OUTLINE_CURVE_RADIUS, 0.0, math.pi * 0.5 )
		context.arc( NODE_OUTLINE_CURVE_RADIUS, size.y - NODE_OUTLINE_CURVE_RADIUS, NODE_OUTLINE_CURVE_RADIUS, math.pi * 0.5, math.pi * 1.0 )
		context.close_path()



	def _p_draw(self, context):
		context.save()

		# Move to node position
		context.translate( self.rect.position.x, self.rect.position.y )


		# Draw node background
		context.new_path()
		context.set_line_width( 1 )
		self._p_drawOutlinePath( context )
		context.set_source_rgb( 0.5, 0.5, 0.5 )
		context.fill_preserve()

		context.save()


		# Clip node contents to shape
		context.clip()


		# DRAW THE TITLE BAR
		self._titleBar._p_draw( context )


		# DRAW THE PINS
		self._sinkViews._p_draw( context )
		self._sourceViews._p_draw( context )


		context.restore()


		# DRAW NODE BORDER
		context.new_path()
		self._p_drawOutlinePath( context )
		if self._bHighlight:
			context.set_source_rgb( 1.0, 1.0, 0.2 )
		else:
			context.set_source_rgb( 1.0, 1.0, 1.0 )
		context.stroke()


		context.restore()




	def _p_evEnter(self, pos):
		super( SheetGraphViewNode, self )._p_evEnter( pos )
		self._bHighlight = True
		self._p_postRedraw()

	def _p_evLeave(self, pos):
		super( SheetGraphViewNode, self )._p_evLeave( pos )
		self._bHighlight = False
		self._p_postRedraw()




	def _p_getRequisition(self, context):
		width = NODE_WIDTH
		height = self._titleBar._p_getRequisition( context ).y  +  self._sinkViews._p_getRequisition( context ).y  +  self._sourceViews._p_getRequisition( context ).y + NODE_BORDER_WIDTH
		return Vector2( width, height )

	def _p_getEstimatedRequisition(self):
		width = NODE_WIDTH
		height = self._titleBar._p_getEstimatedRequisition().y  +  self._sinkViews._p_getEstimatedRequisition().y  +  self._sourceViews._p_getEstimatedRequisition().y + NODE_BORDER_WIDTH
		return Vector2( width, height )

	def _p_allocate(self, rect):
		rect.position = self._graphView._graphViewDisplayTable.get( self._graphNode, Point2() )

		super( SheetGraphViewNode, self )._p_allocate( rect )
		size = rect.size
		self._titleBar._p_allocate( _SheetGraphViewRectangle( Point2( 0.0, 0.0 ), Vector2( size.x, self._titleBar._requisition.y ) ) )
		sinkWidth = min( size.x, self._sinkViews._requisition.x )
		sinkHeight = self._sinkViews._requisition.y
		sourceWidth = min( size.x, self._sourceViews._requisition.x )
		sourceHeight = self._sourceViews._requisition.y
		self._sinkViews._p_allocate( _SheetGraphViewRectangle( Point2( 0.0, NODE_TITLEBAR_HEIGHT ), Vector2( sinkWidth, sinkHeight ) ) )
		self._sourceViews._p_allocate( _SheetGraphViewRectangle( Point2( size.x - sourceWidth, NODE_TITLEBAR_HEIGHT + self._sinkViews._requisition.y ), Vector2( sourceWidth, sourceHeight ) ) )



	def _p_onViewNodePosition(self, node, position):
		if node is self._graphNode  and  position is not None:
			rect = self.rect
			if rect is not None:
				rect.position = position
				self._p_postFullRedraw()


	def _p_getGraphNode(self):
		return self._graphNode


	def _p_getPosition(self):
		return self._graphView._graphViewDisplayTable.get( self._graphNode, Point2() )

	def _p_setPosition(self, pos):
		self._graphView._graphViewDisplayTable[self._graphNode] = pos


	graphNode = property( _p_getGraphNode )
	position = property( _p_getPosition, _p_setPosition )







class SheetGraphViewLink (object):
	def __init__(self, graphView):
		super( SheetGraphViewLink, self ).__init__()
		self._graphView = graphView
		self._graphLink = None
		self._bHighlight = False
		self._bEraseHighlight = False

		self._sourceNode = None
		self._sinkNode = None
		self._source = None
		self._sink = None

		self._controlPoints = None


	def attachGraphLink(self, graphLink):
		assert self._graphLink is None, 'link already attached'
		self._graphLink = graphLink

		self._sourceNode = self._graphView._p_getViewNodeForGraphNode( graphLink.source.node )
		self._sinkNode = self._graphView._p_getViewNodeForGraphNode( graphLink.sink.node )
		self._source = self._sourceNode._p_getSourceView( graphLink.source )
		self._sink = self._sinkNode._p_getSinkView( graphLink.sink )

		self._source.addLinkView( self )
		self._sink.addLinkView( self )



	def detachGraphLink(self):
		assert self._graphLink is not None, 'no link attached'

		self._source.removeLinkView( self )
		self._sink.removeLinkView( self )

		self._sourceNode = None
		self._sinkNode = None
		self._source = None
		self._sink = None
		self._graphLink = None



	def highlight(self):
		self._bHighlight = True
		self._p_postRedraw()


	def unhighlight(self):
		self._bHighlight = False
		self._p_postRedraw()



	def eraseHighlight(self):
		self._bEraseHighlight = True
		self._p_postRedraw()


	def eraseUnhighlight(self):
		self._bEraseHighlight = False
		self._p_postRedraw()



	def _p_postRedraw(self):
		self._graphView._p_linkViewPostRedraw( self )


	def _p_pinPositionModified(self):
		self._controlPoints = None
		self._graphView._p_linkCurveModified( self )


	def _p_getCurveControlPoints(self):
		if self._controlPoints is None:
			self._controlPoints = _computeLinkCurveControlPoints( self._source._p_getConnectionPoint(), self._sink._p_getConnectionPoint() )
		return self._controlPoints



	def _p_draw(self, context):
		context.set_line_width( 1.0 )
		if self._bEraseHighlight:
			context.set_source_rgb( 1.0, 0.0, 0.0 )
		elif self._bHighlight:
			context.set_source_rgb( 1.0, 1.0, 0.0 )
		else:
			context.set_source_rgb( 0.0, 0.0, 0.0 )
		context.new_path()

		p1, p2, p3, p4 = self._p_getCurveControlPoints()
		context.move_to( p1.x, p1.y )
		context.curve_to( p2.x, p2.y, p3.x, p3.y, p4.x, p4.y )
		context.stroke()









class SheetGraphViewSelection (object):
	selectSignal = ClassSignal()
	unselectSignal = ClassSignal()
	currentNodeSignal = ClassSignal()


	def __init__(self):
		super( SheetGraphViewSelection, self ).__init__()
		self._graph = None
		self._selectedNodes = set()
		self._currentNode = None

		self._attachCount = 0


	def attachGraph(self, graph):
		assert self._graph is None  or  graph is self._graph, 'graph already attached'
		self._graph = graph

		if self._attachCount == 0:
			self._graph.removeNodeSignal.connect( self._p_onNodeRemove )

		self._attachCount += 1


	def detachGraph(self):
		assert self._graph is not None, 'no graph attached'

		self._attachCount -= 1

		if self._attachCount == 0:
			self._graph.removeNodeSignal.disconnect( self._p_onNodeRemove )
			self._graph = None
			self._selectedNodes = set()
			self._currentNode = None



	def __len__(self):
		return len( self._selectedNodes )

	def __contains__(self, node):
		return node in self._selectedNodes

	def __iter__(self):
		return iter( self._selectedNodes )


	def selectNode(self, node):
		assert self._graph is not None, 'no graph attached'
		if node not in self._selectedNodes:
			self._selectedNodes.add( node )
			self.selectSignal.emit( self, node )

	def unselectNode(self, node):
		assert self._graph is not None, 'no graph attached'
		if node in self._selectedNodes:
			self.unselectSignal.emit( self, node )
			self._selectedNodes.remove( node )

	def unselectAll(self):
		assert self._graph is not None, 'no graph attached'
		for node in self._selectedNodes:
			self.unselectSignal.emit( self, node )
		self._selectedNodes = set()



	def _p_onNodeRemove(self, graph, node):
		self.unselectNode( node )
		if node is self._currentNode:
			self.currentNode = None


	def _p_getCurrentNode(self):
		assert self._graph is not None, 'no graph attached'
		return self._currentNode

	def _p_setCurrentNode(self, node):
		assert self._graph is not None, 'no graph attached'
		previous = self._currentNode
		self._currentNode = node
		self.currentNodeSignal.emit( self, previous, node )


	currentNode = property( _p_getCurrentNode, _p_setCurrentNode )






class _SheetGraphViewTool (object):
	def _p_evButtonDown(self, widget, pos, button, state):
		pass

	def _p_evButtonDown2(self, widget, pos, button, state):
		pass

	def _p_evButtonDown3(self, widget, pos, button, state):
		pass

	def _p_evButtonUp(self, widget, pos, button, state):
		pass

	def _p_evMotion(self, widget, pos):
		pass

	def _p_evEnter(self, widget, pos):
		pass

	def _p_evLeave(self, widget, pos):
		pass

	def _p_draw(self, context):
		pass





class _SheetGraphViewDefaultTool (_SheetGraphViewTool):
	def __init__(self, graphView):
		self._dragWidget = None
		self._dragPrevPos = None
		self._graphView = graphView
		self._nodesUnderPointer = set()
		self._highlightedLink = None


	def _p_evButtonDown(self, widget, pos, button, state):
		super( _SheetGraphViewDefaultTool, self )._p_evButtonDown( widget, pos, button, state )

		if isinstance( widget, SheetGraphViewNode._TitleBar )  or  isinstance( widget, SheetGraphViewNode ):
			if button == 1:
				if isinstance( widget, SheetGraphViewNode._TitleBar ):
					graphNode = widget._nodeView._graphNode
				else:
					graphNode = widget._graphNode
				self._graphView._p_dragNodeBegin()
				self._dragWidget = widget
				self._dragPrevPos = pos
				if state & gtk.gdk.SHIFT_MASK  !=  0:
					self._graphView.selection.unselectNode( graphNode )
				elif state & gtk.gdk.CONTROL_MASK  !=  0:
					self._graphView.selection.selectNode( graphNode )
				else:
					self._graphView.selection.unselectAll()
					self._graphView.selection.selectNode( graphNode )
				self._graphView.selection.currentNode = graphNode
		elif isinstance( widget, SheetGraphViewNode._PinView ):
			if button == 1:
				widget._p_linkHighlight()
				self._graphView._p_setTool( _SheetGraphViewLinkTool( self._graphView, widget ) )
		elif widget is self._graphView:
			if button == 1  and  state & gtk.gdk.CONTROL_MASK == 0:
				self._graphView.selection.unselectAll()
				self._graphView.selection.currentNode = None


	def _p_evButtonDown2(self, widget, pos, button, state):
		super( _SheetGraphViewDefaultTool, self )._p_evButtonDown2( widget, pos, button, state )

		if isinstance( widget, SheetGraphViewNode._TitleBar )  or  isinstance( widget, SheetGraphViewNode ):
			if button == 1:
				if isinstance( widget, SheetGraphViewNode._TitleBar ):
					graphNode = widget._nodeView._graphNode
				else:
					graphNode = widget._graphNode
				self._graphView._p_nodeDoubleClicked( graphNode )


	def _p_evButtonUp(self, widget, pos, button, state):
		super( _SheetGraphViewDefaultTool, self )._p_evButtonUp( widget, pos, button, state )

		if isinstance( widget, SheetGraphViewNode._TitleBar )  or  isinstance( widget, SheetGraphViewNode ):
			if button == 1:
				if widget is self._dragWidget:
					self._graphView._p_dragNodeEnd()
					self._dragWidget = None


	def _p_evMotion(self, widget, pos):
		super( _SheetGraphViewDefaultTool, self )._p_evMotion( widget, pos )

		if isinstance( widget, SheetGraphViewNode._TitleBar )  or  isinstance( widget, SheetGraphViewNode ):
			if widget is self._dragWidget:
				delta = pos - self._dragPrevPos
				if isinstance( widget, SheetGraphViewNode._TitleBar ):
					nodeView = widget._nodeView
				else:
					nodeView = widget
				for node in self._graphView.selection:
					self._graphView._p_getViewNodeForGraphNode( node )._p_translate( delta )
		elif widget is self._graphView:
			if len( self._nodesUnderPointer ) == 0:
				linkId = self._graphView._linkCurveTable.getLinkClosestToPoint( pos, HIGHLIGHT_LINK_RANGE / self._graphView._scale, 0.5 / self._graphView._scale )
				if linkId != -1:
					linkView = self._graphView._linkIdToLink[linkId]
				else:
					linkView = None
				if linkView is not self._highlightedLink:
					if self._highlightedLink is not None:
						self._highlightedLink.unhighlight()
					self._highlightedLink = linkView
					if self._highlightedLink is not None:
						self._highlightedLink.highlight()


	def _p_evEnter(self, widget, pos):
		super( _SheetGraphViewDefaultTool, self )._p_evEnter( widget, pos )

		if isinstance( widget, SheetGraphViewNode ):
			self._nodesUnderPointer.add( widget )
			if self._highlightedLink is not None:
				self._highlightedLink.unhighlight()
				self._highlightedLink = None


	def _p_evLeave(self, widget, pos):
		super( _SheetGraphViewDefaultTool, self )._p_evLeave( widget, pos )

		if isinstance( widget, SheetGraphViewNode ):
			self._nodesUnderPointer.discard( widget )





class _SheetGraphViewLinkTool (_SheetGraphViewTool):
	def __init__(self, graphView, sourcePin):
		self._graphView = graphView
		self._startPin = sourcePin
		self._finishPin = None
		self._bFinishPinValid = False
		self._pointerPosition = None
		self._eraseLinkView = None



	def _p_canLink(self, pin1, pin2):
		if isinstance( pin1, SheetGraphViewNode._SinkView )  and  isinstance( pin2, SheetGraphViewNode._SourceView ):
			sink, source = pin1, pin2
		elif isinstance( pin1, SheetGraphViewNode._SourceView )  and  isinstance( pin2, SheetGraphViewNode._SinkView ):
			source, sink = pin1, pin2
		else:
			return False

		if not sink._pin.canAddInput():
			return False

		if sink._pinListView._nodeView.checkAddInputForCycles( source._pinListView._nodeView ):
			return False

		if source._pin in sink._pin:
			return False

		if not self._graphView._p_checkCreateLink( source._pin, sink._pin ):
			return False

		return True


	def _p_getLinkBetweenPins(self, pin1, pin2):
		if isinstance( pin1, SheetGraphViewNode._SinkView )  and  isinstance( pin2, SheetGraphViewNode._SourceView ):
			sink, source = pin1, pin2
		elif isinstance( pin1, SheetGraphViewNode._SourceView )  and  isinstance( pin2, SheetGraphViewNode._SinkView ):
			source, sink = pin1, pin2
		else:
			return None

		return sink._pin.getLinkTo( source._pin )


	def _p_link(self, pin1, pin2):
		assert self._p_canLink( pin1, pin2 )

		if isinstance( pin1, SheetGraphViewNode._SinkView )  and  isinstance( pin2, SheetGraphViewNode._SourceView ):
			self._graphView._p_createLink( pin2._pin, pin1._pin )
		elif isinstance( pin1, SheetGraphViewNode._SourceView )  and  isinstance( pin2, SheetGraphViewNode._SinkView ):
			self._graphView._p_createLink( pin1._pin, pin2._pin )
		else:
			assert False, 'invalid link'


	def _p_canEraseLink(self, linkView):
		return self._graphView._p_checkEraseLink( linkView._graphLink.source, linkView._graphLink.sink )

	def _p_eraseLink(self, linkView):
		assert self._p_canEraseLink( linkView )

		return self._graphView._p_eraseLink( linkView._graphLink.source, linkView._graphLink.sink )


	def _p_finish(self):
		self._startPin._p_linkUnhighlight()
		self._finishPin._p_linkUnhighlight()
		self._graphView._p_setTool( self._graphView._defaultTool )


	def _p_drawLinkCurveBetweenNodes(self, pin1, pin2, context):
		if isinstance( pin1, SheetGraphViewNode._SinkView )  and  isinstance( pin2, SheetGraphViewNode._SourceView ):
			_drawLinkCurve( pin2._p_getConnectionPoint(), pin1._p_getConnectionPoint(), context )
		elif isinstance( pin1, SheetGraphViewNode._SourceView )  and  isinstance( pin2, SheetGraphViewNode._SinkView ):
			_drawLinkCurve( pin1._p_getConnectionPoint(), pin2._p_getConnectionPoint(), context )


	def _p_drawPotentialLinkCurve(self, pin, point, context):
		if isinstance( pin, SheetGraphViewNode._SinkView ):
			_drawLinkCurve( point, pin._p_getConnectionPoint(), context )
		elif isinstance( pin, SheetGraphViewNode._SourceView ):
			_drawLinkCurve( pin._p_getConnectionPoint(), point, context )



	def _p_evButtonDown(self, widget, pos, button, state):
		super( _SheetGraphViewLinkTool, self )._p_evButtonDown( widget, pos, button, state )

		if button == 3:
			self._startPin._p_linkUnhighlight()
			self._graphView._p_setTool( self._graphView._defaultTool )
		if isinstance( widget, SheetGraphViewNode._PinView ):
			if button == 1:
				if self._eraseLinkView is not None:
					self._p_eraseLink( self._eraseLinkView )
					self._p_finish()
				else:
					if self._p_canLink( self._startPin, widget ):
						self._p_link( self._startPin, widget )
						self._p_finish()


	def _p_evMotion(self, widget, pos):
		super( _SheetGraphViewLinkTool, self )._p_evMotion( widget, pos )

		if isinstance( widget, SheetGraphView ):
			self._pointerPosition = pos
			widget._p_postFullRedraw()


	def _p_evEnter(self, widget, pos):
		super( _SheetGraphViewLinkTool, self )._p_evEnter( widget, pos )

		if isinstance( widget, SheetGraphViewNode._PinView ):
			self._finishPin = widget
			self._bFinishPinValid = self._p_canLink( self._startPin, self._finishPin )
			if self._bFinishPinValid:
				self._finishPin._p_linkHighlight()
			else:
				link = self._p_getLinkBetweenPins( self._startPin, self._finishPin )
				if link is not None:
					linkView = self._graphView._p_getViewLinkForGraphLink( link )
					if self._p_canEraseLink( linkView ):
						linkView.eraseHighlight()
						self._eraseLinkView = linkView
			self._graphView._p_postFullRedraw()


	def _p_evLeave(self, widget, pos):
		super( _SheetGraphViewLinkTool, self )._p_evLeave( widget, pos )

		if widget is self._finishPin:
			if self._bFinishPinValid:
				self._finishPin._p_linkUnhighlight()
			self._finishPin = None
			self._bFinishPinValid = False
			self._graphView._p_postFullRedraw()
			if self._eraseLinkView is not None:
				self._eraseLinkView.eraseUnhighlight()
				self._eraseLinkView = None


	def _p_draw(self, context):
		super( _SheetGraphViewLinkTool, self )._p_draw( context )
		context.set_source_rgb( 0.0, 1.0, 0.0 )
		if self._finishPin is not None:
			if self._bFinishPinValid:
				self._p_drawLinkCurveBetweenNodes( self._startPin, self._finishPin, context )
		elif self._pointerPosition is not None:
			self._p_drawPotentialLinkCurve( self._startPin, self._pointerPosition, context )




class SheetGraphView (_SheetGraphViewWidgetContainer, gtk.DrawingArea):
	nodeDoubleClickedSignal = ClassSignal()
	dragNodeBeginSignal = ClassSignal()
	dragNodeEndSignal = ClassSignal()


	def __init__(self, createLinkCallback, eraseLinkCallback, checkCreateLinkCallback=None, checkEraseLinkCallback=None):
		super( SheetGraphView, self ).__init__()

		self._viewNodes = []

		self._viewLinks = []

		self._viewNodesToResize = set()
		self._linkViewsToRefresh = set()
		self._linkCurveTable = GraphViewLinkCurveTable()
		self._linkIdToLink = {}
		self._linkToLinkId = {}

		self._graphNodeToViewNode = {}
		self._graphLinkToViewLink = {}

		self._graph = None
		self._graphViewDisplayTable = None
		self._graphLinks = _SheetGraphViewGraphLinkList()

		self._selection = None
		# Set via the @selection property
		self.selection = SheetGraphViewSelection()


		self._scale = 1.0
		self._viewportOffset = Vector2( NODE_SPACING, NODE_SPACING )
		self._widgetCentre = Vector2()
		self._widgetSize = Vector2()

		self._prevPointer = None
		self._navButton = None

		self._bRealised = False


		self._tool = None

		self._defaultTool = _SheetGraphViewDefaultTool( self )
		self._tool = self._defaultTool

		self._bRefreshQueued = False

		self._checkCreateLinkCallback = checkCreateLinkCallback
		self._createLinkCallback = createLinkCallback
		self._checkEraseLinkCallback = checkEraseLinkCallback
		self._eraseLinkCallback = eraseLinkCallback


		# Connect signals
		self.connect_after( 'configure-event', self._p_onConfigure )
		self.connect( 'expose-event', self._p_onExpose )
		self.connect( 'button-press-event', self._p_onButtonPress )
		self.connect( 'button-release-event', self._p_onButtonRelease )
		self.connect( 'motion-notify-event', self._p_onMotionNotify )
		self.connect( 'enter-notify-event', self._p_onEnterNotify )
		self.connect( 'leave-notify-event', self._p_onLeaveNotify )
		self.connect( 'scroll-event', self._p_onScroll )
		self.connect_after( 'realize', self._p_onRealise )
		self.connect( 'unrealize', self._p_onUnrealise )

		# Tell the widget to send these events
		self.add_events( gtk.gdk.EXPOSURE_MASK |
					gtk.gdk.BUTTON_PRESS_MASK |
					gtk.gdk.BUTTON_RELEASE_MASK |
					gtk.gdk.POINTER_MOTION_MASK |
					gtk.gdk.POINTER_MOTION_HINT_MASK |
					gtk.gdk.ENTER_NOTIFY_MASK |
					gtk.gdk.LEAVE_NOTIFY_MASK |
					gtk.gdk.SCROLL_MASK )



	def attachGraph(self, graph, graphViewDisplayTable):
		assert self._graph is None, 'graph already attached'
		self._graph = graph
		self._graphViewDisplayTable = graphViewDisplayTable
		self._graphLinks.attachGraph( graph )

		if self._selection is not None:
			self._selection.attachGraph( graph )

		for graphNode in graph.nodes:
			viewNode = self._p_createViewNode()
			self._p_addNode( viewNode )
			self._graphNodeToViewNode[graphNode] = viewNode

		for graphNode in graph.nodes:
			viewNode = self._graphNodeToViewNode[graphNode]
			viewNode.attachGraphNode( graphNode )

		for viewNode in self._viewNodes:
			if self._graphViewDisplayTable[viewNode.graphNode] is None:
				self._p_positionViewNode( viewNode )

		for graphLink in self._graphLinks:
			viewLink = self.createViewLink()
			self._graphLinkToViewLink[graphLink] = viewLink
			viewLink.attachGraphLink( graphLink )
			self._p_addLink( viewLink )

		graph.nodeAddedSignal.connect( self._p_onNodeAdded )
		graph.nodeRemovedSignal.connect( self._p_onNodeRemoved )
		self._graphLinks.appendSignal.connect( self._p_onAppendLink )
		self._graphLinks.removeSignal.connect( self._p_onRemoveLink )

		self.queue_draw()


	def detachGraph(self):
		assert self._graph is not None, 'no graph attached'

		if self._selection is not None:
			self._selection.detachGraph()

		self._graph.nodeAddedSignal.disconnect( self._p_onNodeAdded )
		self._graph.nodeRemovedSignal.disconnect( self._p_onNodeRemoved )
		self._graphLinks.appendSignal.disconnect( self._p_onAppendLink )
		self._graphLinks.removeSignal.disconnect( self._p_onRemoveLink )

		for graphLink in self._graphLinks:
			viewLink = self._graphLinkToViewLink[graphLink]
			viewLink.detachGraphLink()

		for graphLink in self._graphLinks:
			viewLink = self._graphLinkToViewLink[graphLink]
			self._p_removeLink( viewLink )
			del self._graphLinkToViewLink[graphLink]

		assert len( self._viewLinks ) == 0
		assert len( self._linkViewsToRefresh ) == 0
		assert len( self._linkIdToLink ) == 0
		assert len( self._linkToLinkId ) == 0
		assert len( self._graphLinkToViewLink ) == 0

		for graphNode in self._graph.nodes:
			viewNode = self._graphNodeToViewNode[graphNode]
			viewNode.detachGraphNode()

		for graphNode in self._graph.nodes:
			viewNode = self._graphNodeToViewNode[graphNode]
			self._p_removeNode( viewNode )
			del self._graphNodeToViewNode[graphNode]

		assert len( self._viewNodes ) == 0
		assert len( self._viewNodesToResize ) == 0
		assert len( self._graphNodeToViewNode ) == 0

		self._graphLinks.detachGraph()

		self._graph = None
		self._graphViewDisplayTable = None


	def focusOnOrigin(self):
		self._viewportOffset = -self._widgetCentre  *  ( 1.0 / self._scale )  +  Vector2( NODE_SPACING, NODE_SPACING )
		self._p_postFullRedraw()

	def zoom1To1(self):
		self._scale = 1.0
		self._p_postFullRedraw()

	def zoomExtentsAll(self):
		box = BBox2()
		if len( self._viewNodes ) > 0:
			for viewNode in self._viewNodes:
				rect = viewNode.rect
				if rect is not None:
					box.addBox( rect.box )
			self._p_zoomBox( box )

	def zoomExtentsSelected(self):
		selection = self.selection
		if len( selection ) > 0:
			box = BBox2()
			for graphNode in selection:
				viewNode = self._p_getViewNodeForGraphNode( graphNode )
				rect = viewNode.rect
				if rect is not None:
					box.addBox( rect.box )
			self._p_zoomBox( box )


	def _p_zoomBox(self, box):
		size = box.getSize()  +  Vector2( NODE_SPACING, NODE_SPACING ) * 2.0
		self._viewportOffset = -box.getCentre().toVector2()
		xScale = self._widgetSize.x / size.x
		yScale = self._widgetSize.y / size.y
		self._scale = min( xScale, yScale )
		self._p_postFullRedraw()


	def _p_addNode(self, viewNode):
		assert viewNode not in self._viewNodes, 'node already in node list'
		self._viewNodes.append( viewNode )
		self._children.append( viewNode )
		self._p_queueRefresh()

	def _p_removeNode(self, viewNode):
		assert viewNode in self._viewNodes, 'node not in node list'
		self._viewNodes.remove( viewNode )
		self._children.remove( viewNode )
		self._viewNodesToResize.discard( viewNode )



	def _p_addLink(self, viewLink):
		assert viewLink not in self._viewLinks, 'link already in link list'
		self._viewLinks.append( viewLink )
		# Build with dummy curve at first
		linkId = self._linkCurveTable.addLinkCurve( Point2(), Point2(), Point2(), Point2() )
		self._linkIdToLink[linkId] = viewLink
		self._linkToLinkId[viewLink] = linkId
		# Add this link to the list of link views to be refreshed
		self._linkViewsToRefresh.add( viewLink )
		# Queue a refresh, upon which the curve will be recomputed
		self._p_queueRefresh()

	def _p_removeLink(self, viewLink):
		assert viewLink in self._viewLinks, 'link not in link list'
		self._viewLinks.remove( viewLink )
		linkId = self._linkToLinkId[viewLink]
		del self._linkIdToLink[linkId]
		del self._linkToLinkId[viewLink]
		self._linkCurveTable.removeLinkCurve( linkId )
		self._linkViewsToRefresh.discard( viewLink )


	def _p_createViewNode(self):
		return SheetGraphViewNode( self )

	def createViewLink(self):
		return SheetGraphViewLink( self )




	def _p_onNodeAdded(self, graph, graphNode):
		viewNode = self._p_createViewNode()
		self._p_addNode( viewNode )
		self._graphNodeToViewNode[graphNode] = viewNode
		viewNode.attachGraphNode( graphNode )
		self._graphLinks._f_nodeAdded( graph, graphNode )
		if self._graphViewDisplayTable[viewNode.graphNode] is None:
			self._p_positionViewNode( viewNode )
		self._p_postFullRedraw()


	def _p_onNodeRemoved(self, graph, graphNode):
		self._graphLinks._f_nodeRemoved( graph, graphNode )
		viewNode = self._graphNodeToViewNode[graphNode]
		viewNode.detachGraphNode()
		del self._graphNodeToViewNode[graphNode]
		self._p_removeNode( viewNode )
		self._p_postFullRedraw()



	def _p_onAppendLink(self, graphLink):
		viewLink = self.createViewLink()
		self._graphLinkToViewLink[graphLink] = viewLink
		viewLink.attachGraphLink( graphLink )
		self._p_addLink( viewLink )
		self._p_postFullRedraw()


	def _p_onRemoveLink(self, graphLink):
		viewLink = self._graphLinkToViewLink[graphLink]
		viewLink.detachGraphLink()
		del self._graphLinkToViewLink[graphLink]
		self._p_removeLink( viewLink )
		self._p_postFullRedraw()



	def _p_getViewNodeForGraphNode(self, graphNode):
		return self._graphNodeToViewNode[graphNode]


	def _p_getViewLinkForGraphLink(self, graphLink):
		return self._graphLinkToViewLink[graphLink]



	def _p_onSelect(self, selection, node):
		self._p_getViewNodeForGraphNode( node ).select()

	def _p_onUnselect(self, selection, node):
		self._p_getViewNodeForGraphNode( node ).unselect()


	def _p_onCurrentNode(self, selection, oldNode, newNode):
		if oldNode is not None:
			self._p_getViewNodeForGraphNode( oldNode ).clearCurrent()
		if newNode is not None:
			self._p_getViewNodeForGraphNode( newNode ).setCurrent()






	def _p_postRedrawArea(self, pos, size):
		self._p_invalidateRect( pos, size )

	def _p_postFullRedraw(self):
		self.queue_draw()

	def _p_linkViewPostRedraw(self, linkView):
		self.queue_draw()

	def _p_linkCurveModified(self, linkView):
		self._linkViewsToRefresh.add( linkView )
		self._p_queueRefresh();
		self.queue_draw()


	def _p_childQueueResize(self, nodeView):
		assert isinstance( nodeView, SheetGraphViewNode ), 'child not a SheetGraphViewNode'
		self._viewNodesToResize.add( nodeView )
		self._p_queueRefresh()


	def _p_invalidateRect(self, pos, size):
		size = ( size  +  Vector2( 2.0, 2.0 ) )   *   self._scale
		pos = self._p_viewCoordsToWidgetCoords( pos - Vector2( 1.0, 1.0 ) )
		self.queue_draw_area( int( pos.x ), int( pos.y ), int( size.x + 0.5 ), int( size.y + 0.5 ) )



	def _p_positionViewNode(self, viewNode):
		y = 0.0
		for n in self._viewNodes:
			if n is not viewNode  and  self._graphViewDisplayTable[n._graphNode] is not None:
				rect = n.rect
				if rect is not None:
					position = n.rect.position
					size = n.rect.size
				else:
					position = self._graphViewDisplayTable.get( n._graphNode, Point2() )
					size = n._p_getEstimatedRequisition()
				xLower = position.x
				xUpper = xLower + size.x
				yLower = position.y
				yUpper = position.y + size.y
				if xUpper >= -NODE_SPACING  and  xLower <= ( NODE_WIDTH + NODE_SPACING ):
					y = max( y, yUpper + NODE_SPACING )
		viewNode.position = Point2( 0.0, y )



	def _p_queueRefresh(self):
		if not self._bRefreshQueued:
			if self._bRealised:
				queueEvent( self._p_refresh )
			self._bRefreshQueued = True

	def _p_refresh(self):
		self._p_refreshNodeViews( self.window.cairo_create() )
		self._p_refreshLinks()
		self._bRefreshQueued = False



	def _p_refreshNodeViews(self, context):
		for nodeView in self._viewNodesToResize:
			req = nodeView._p_getRequisition( context )
			nodeView._p_allocate( _SheetGraphViewRectangle( nodeView.position, req ) )
		self._viewNodesToResize = set()


	def _p_refreshLinks(self):
		for linkView in self._linkViewsToRefresh:
			linkId = self._linkToLinkId[linkView]
			self._linkCurveTable.setLinkCurve( linkId, *linkView._p_getCurveControlPoints() )
		self._linkViewsToRefresh = set()


	def _p_draw(self, context):
		rect = self.get_allocation()


		context.new_path()
		context.rectangle( 0.0, 0.0, self._widgetSize.x, self._widgetSize.y )
		context.set_source_rgb( 0.6, 0.6, 0.6 )
		context.fill()



		context.save()

		context.translate( self._widgetCentre.x, self._widgetCentre.y )
		context.scale( self._scale, self._scale )
		context.translate( self._viewportOffset.x, self._viewportOffset.y )

		self._p_refreshNodeViews( context )

		visibleBox = self._p_getVisibleBox()

		visibleLinkIds = self._linkCurveTable.getIntersectingLinkList( visibleBox )
		for linkId in visibleLinkIds:
			linkView = self._linkIdToLink[linkId]
			linkView._p_draw( context )

		if self._tool is not None:
			self._tool._p_draw( context )

		visibleNodeIds = self._widgetBoxTable.getIntersectingWidgetList( visibleBox )
		for nodeId in visibleNodeIds:
			nodeView = self._childIdToChild[nodeId]
			nodeView._p_draw( context )


		context.restore()

		context.set_source_rgb( 0.0, 0.0, 0.0 )
		context.move_to( 0.0, self._widgetSize.y )
		context.line_to( 0.0, 0.0 )
		context.line_to( self._widgetSize.x, 0.0 )
		context.stroke()

		context.set_source_rgb( 1.0, 1.0, 1.0 )
		context.move_to( 0.0, self._widgetSize.y )
		context.line_to( self._widgetSize.x, self._widgetSize.y )
		context.line_to( self._widgetSize.x, 0.0 )
		context.stroke()



	def _p_getRootPosition(self):
		return Point2()


	def _p_getVisibleBox(self):
		b = BBox2()
		b.addPoint( self._p_widgetCoordsToViewCoords( Point2() ) )
		b.addPoint( self._p_widgetCoordsToViewCoords( Point2( self._widgetSize ) ) )
		return b




	def _p_widgetCoordsToViewCoords(self, p):
		return Point2( ( p.toVector2() - self._widgetCentre )  *  ( 1.0 / self._scale )  -  self._viewportOffset )

	def _p_viewCoordsToWidgetCoords(self, p):
		return Point2( ( p.toVector2() + self._viewportOffset )  *  self._scale  +  self._widgetCentre )




	def _p_onConfigure(self, widget, event):
		newSize = Vector2( event.width, event.height )
		newCentre = newSize * 0.5
		delta = newCentre - self._widgetCentre
		self._viewportOffset -= delta  *  ( 1.0 / self._scale )
		self._widgetSize = newSize
		self._widgetCentre = newCentre


	def _p_onExpose(self, widget, event):
		context = widget.window.cairo_create()

		context.rectangle( event.area.x, event.area.y, event.area.width, event.area.height )
		context.clip()

		self._p_draw( context )

		return False


	def _p_onButtonPress(self, widget, event):
		x, y, state = event.x, event.y, event.state

		if state & gtk.gdk.MOD1_MASK  != 0:
			if event.type == gtk.gdk.BUTTON_PRESS:
				self._navButton = event.button
				self._prevPointer = x, y
		else:
			local = self._p_widgetCoordsToViewCoords( Point2( x, y ) )
			if event.type == gtk.gdk.BUTTON_PRESS:
				self._p_onButtonDown( local, event.button, state )
			elif event.type == gtk.gdk._2BUTTON_PRESS:
				self._p_onButtonDown2( local, event.button, state )
			elif event.type == gtk.gdk._3BUTTON_PRESS:
				self._p_onButtonDown3( local, event.button, state )

	def _p_onButtonRelease(self, widget, event):
		x, y, state = event.x, event.y, event.state

		if event.button == self._navButton:
			self._navButton = None
			self._prevPointer = None
		else:
			local = self._p_widgetCoordsToViewCoords( Point2( x, y ) )
			self._p_onButtonUp( local, event.button, state )


	def _p_onMotionNotify(self, widget, event):
		if event.is_hint:
			x, y, state = event.window.get_pointer()
		else:
			x, y, state = event.x, event.y, event.state

		if self._navButton is None:
			local = self._p_widgetCoordsToViewCoords( Point2( x, y ) )
			self._p_onMotion( local )
		else:
			prevX, prevY = self._prevPointer
			self._prevPointer = x, y
			dx, dy = float( x - prevX ), float( y - prevY )
			if self._navButton == 2:
				self._viewportOffset += Vector2( dx, dy )  *  ( 1.0 / self._scale )
				self.queue_draw()
			elif self._navButton == 3:
				dx = x - prevX
				self._scale *= 2.0  **  ( dx * 0.005 )
				self.queue_draw()



	def _p_onEnterNotify(self, widget, event):
		x, y, state = event.window.get_pointer()

		if self._navButton is None:
			if state & gtk.gdk.MOD1_MASK  == 0:
				local = self._p_widgetCoordsToViewCoords( Point2( x, y ) )
				self._p_onEnter( local )


	def _p_onLeaveNotify(self, widget, event):
		x, y, state = event.window.get_pointer()

		if self._navButton is None:
			if state & gtk.gdk.MOD1_MASK  == 0:
				local = self._p_widgetCoordsToViewCoords( Point2( x, y ) )
				self._p_onLeave( local )



	def _p_onScroll(self, widget, event):
		pass



	def _p_onRealise(self, widget):
		self._bRealised = True
		if self._bRefreshQueued:
			self._p_refresh()


	def _p_onUnrealise(self, widget):
		self._bRealised = False




	def _p_evButtonDown(self, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonDown( self, pos, button, state )

	def _p_evButtonDown2(self, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonDown2( self, pos, button, state )

	def _p_evButtonDown3(self, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonDown3( self, pos, button, state )

	def _p_evButtonUp(self, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonUp( self, pos, button, state )

	def _p_evMotion(self, pos):
		if self._tool is not None:
			self._tool._p_evMotion( self, pos )

	def _p_evEnter(self, pos):
		if self._tool is not None:
			self._tool._p_evEnter( self, pos )

	def _p_evLeave(self, pos):
		if self._tool is not None:
			self._tool._p_evLeave( self, pos )


	def _p_onWidgetEvButtonDown(self, widget, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonDown( widget, pos, button, state )

	def _p_onWidgetEvButtonDown2(self, widget, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonDown2( widget, pos, button, state )

	def _p_onWidgetEvButtonDown3(self, widget, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonDown3( widget, pos, button, state )

	def _p_onWidgetEvButtonUp(self, widget, pos, button, state):
		if self._tool is not None:
			self._tool._p_evButtonUp( widget, pos, button, state )

	def _p_onWidgetEvMotion(self, widget, pos):
		if self._tool is not None:
			self._tool._p_evMotion( widget, pos )

	def _p_onWidgetEvEnter(self, widget, pos):
		if self._tool is not None:
			self._tool._p_evEnter( widget, pos )

	def _p_onWidgetEvLeave(self, widget, pos):
		if self._tool is not None:
			self._tool._p_evLeave( widget, pos )



	def _p_dragNodeBegin(self):
		self.dragNodeBeginSignal.emit()

	def _p_dragNodeEnd(self):
		self.dragNodeEndSignal.emit()



	def _p_nodeDoubleClicked(self, graphNode):
		self.nodeDoubleClickedSignal.emit( graphNode )


	def _p_checkCreateLink(self, sourcePin, sinkPin):
		if self._checkCreateLinkCallback is not None:
			return self._checkCreateLinkCallback( sourcePin, sinkPin )
		else:
			return True

	def _p_createLink(self, sourcePin, sinkPin):
		self._createLinkCallback( sourcePin, sinkPin )


	def _p_checkEraseLink(self, source, sink):
		if self._checkEraseLinkCallback is not None:
			return self._checkEraseLinkCallback( source, sink )
		else:
			return True

	def _p_eraseLink(self, source, sink):
		self._eraseLinkCallback( source, sink )



	def _p_setTool(self, tool):
		self._tool = tool
		self._p_postFullRedraw()




	def _p_getSelection(self):
		return self._selection

	def _p_setSelection(self, selection):
		assert selection is not None
		assert isinstance( selection, SheetGraphViewSelection )

		if selection is not self._selection:
			if self._selection is not None:
				for node in self._selection:
					if node not in selection:
						self._p_getViewNodeForGraphNode( node ).unselect()

				for node in selection:
					if node not in self._selection:
						self._p_getViewNodeForGraphNode( node ).select()

				self._selection.selectSignal.disconnect( self._p_onSelect )
				self._selection.unselectSignal.disconnect( self._p_onUnselect )
				self._selection.currentNodeSignal.disconnect( self._p_onCurrentNode )

				if self._graph is not None:
					self._selection.detachGraph()
			else:
				for node in selection:
					self._p_getViewNodeForGraphNode( node ).select()

			self._selection = selection

			self._selection.selectSignal.connect( self._p_onSelect )
			self._selection.unselectSignal.connect( self._p_onUnselect )
			self._selection.currentNodeSignal.connect( self._p_onCurrentNode )

			if self._graph is not None:
				self._selection.attachGraph( self._graph )


	selection = property( _p_getSelection, _p_setSelection )






if __name__ == '__main__':
	from Britefury.Sheet.Sheet import *
	from Britefury.SheetGraph.SheetGraph import *
	from Britefury.GraphView.SheetGraphViewDisplayTable import SheetGraphViewDisplayTable

	from Britefury.CommandHistory.CommandHistory import CommandHistory

	import random
	import math


	class TestNode0 (SheetGraphNode):
		out = SheetGraphSourceField( 'Out' )


	class TestNode1 (TestNode0):
		in1 = SheetGraphSinkMultipleField( 'In1' )

	class TestNode2 (TestNode1):
		in2 = SheetGraphSinkMultipleField( 'In2' )

	class TestNode3 (TestNode2):
		in3 = SheetGraphSinkMultipleField( 'In3' )


	nodeClasses = [ TestNode0, TestNode1, TestNode2, TestNode3 ]


	def buildGraph(graph, graphViewDisplayTable, bRandom):
		if bRandom:
			numNodes = 25
			numLinks = int( numNodes * 1.5 )
			nodes = []
			width = NODE_WIDTH + NODE_SPACING
			height = NODE_WIDTH * 0.7  +  NODE_SPACING
			xRange = width * math.sqrt( numNodes )
			yRange = height * math.sqrt( numNodes )
			for i in xrange( 0, numNodes ):
				numSinks = random.randint( 1, 3 )
				nodeClass = nodeClasses[numSinks]
				node = nodeClass()
				graphViewDisplayTable[node] = Point2( random.uniform( 0.0, xRange ), random.uniform( 0.0, yRange ) )
				nodes.append( node )
				graph.nodes.append( node )

			for i in xrange( 0, numLinks ):
				bLinked = False
				while not bLinked:
					srcNode = random.choice( nodes )
					dstNode = random.choice( nodes )
					source = srcNode.sources[0]
					sink = random.choice( dstNode.sinks )

					if sink.canAddInput()  and  source not in sink  and  not sink.checkAddInputForCycles( source ):
						sink.append( source )
						bLinked = True
		else:
			node1 = TestNode2()
			graph.nodes.append( node1 )

			node2 = TestNode1()
			graph.nodes.append( node2 )

			node3 = TestNode1()
			graph.nodes.append( node3 )

			node4 = TestNode0()
			graph.nodes.append( node4 )

			node5 = TestNode0()
			graph.nodes.append( node5 )


			node1.sinks[0].append( node2.sources[0] )
			node1.sinks[1].append( node3.sources[0] )
			node2.sinks[0].append( node4.sources[0] )
			node1.sinks[1].append( node5.sources[0] )
			node3.sinks[0].append( node5.sources[0] )


	def onAdd(widget):
		commandHistory.freeze()
		node = TestNode2()
		graph.nodes.append( node )
		commandHistory.thaw()


	def onRemove(widget):
		commandHistory.freeze()
		selectedNodes = list( view.selection )
		for node in selectedNodes:
			graph.nodes.remove( node )
		commandHistory.thaw()


	def onUndo(widget):
		if commandHistory.canUndo():
			commandHistory.undo()


	def onRedo(widget):
		if commandHistory.canRedo():
			commandHistory.redo()


	def createLink(sourcePin, sinkPin):
		commandHistory.freeze()
		sinkPin.append( sourcePin )
		commandHistory.thaw()

	def eraseLink(source, sink):
		commandHistory.freeze()
		sink.remove( source )
		commandHistory.thaw()




	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()



	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 800, 600 )

	commandHistory = CommandHistory()

	graph = SheetGraph()
	graphViewDisplayTable = SheetGraphViewDisplayTable()
	buildGraph( graph, graphViewDisplayTable, True )
	commandHistory.track( graph )
	commandHistory.track( graphViewDisplayTable )
	print 'built graph'


	view = SheetGraphView( createLink, eraseLink )
	view.attachGraph( graph, graphViewDisplayTable )
	#view.dndSinkToSourceSignal.connect( onDndSinkToSource )
	#view.dndSourceToSinkSignal.connect( onDndSourceToSink )
	view.show()

	addNodeButton = gtk.Button( stock=gtk.STOCK_ADD )
	addNodeButton.connect( 'clicked', onAdd )

	removeNodeButton = gtk.Button( stock=gtk.STOCK_REMOVE )
	removeNodeButton.connect( 'clicked', onRemove )

	undoButton = gtk.Button( stock=gtk.STOCK_UNDO )
	undoButton.connect( 'clicked', onUndo )

	redoButton = gtk.Button( stock=gtk.STOCK_REDO )
	redoButton.connect( 'clicked', onRedo )

	buttonBox = gtk.HBox( True )
	buttonBox.pack_start( addNodeButton, True, True, 20 )
	buttonBox.pack_start( removeNodeButton, True, True, 20 )
	buttonBox.pack_start( gtk.VSeparator(), False, False, 20 )
	buttonBox.pack_start( undoButton, True, True, 20 )
	buttonBox.pack_start( redoButton, True, True, 20 )
	buttonBox.show_all()

	box = gtk.VBox()
	box.pack_start( view )
	box.pack_start( gtk.HSeparator(), False, False, 10 )
	box.pack_start( buttonBox, False, False, 10 )
	box.show_all()

	window.add( box )
	window.show()

	gtk.main()
