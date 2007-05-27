##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.FileIO.IOXml import ioObjectFactoryRegister

from Britefury.Math.Math import Point2

from Britefury.Util.SignalSlot import *

from Britefury.GraphView import SheetGraphViewDisplayTableCommandTracker



class SheetGraphViewDisplayTable (object):
	trackerClass = SheetGraphViewDisplayTableCommandTracker.SheetGraphViewDisplayTableCommandTracker


	nodePositionSignal = ClassSignal()



	def __init__(self):
		super( SheetGraphViewDisplayTable, self ).__init__()

		self._commandTracker_ = None
		self._nodePositions = {}



	def __getitem__(self, node):
		try:
			return copy( self._nodePositions[node] )
		except KeyError:
			return None

	def get(self, node, default=None):
		return copy( self._nodePositions.get( node, default ) )

	def __setitem__(self, node, pos):
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onPosSet( self, node, self[node], pos )
		if pos is None:
			del self[node]
		else:
			assert isinstance( pos, Point2 ), '@pos must be a Point2'
			self._nodePositions[node] = pos
		self.nodePositionSignal.emit( node, pos )

	def __delitem__(self, node):
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onPosSet( self, node, self[node], None )
		try:
			del self._nodePositions[node]
		except KeyError:
			pass
		self.nodePositionSignal.emit( node, None )



	def __readxml__(self, xmlNode):
		self._nodePositions.clear()

		for nodePosNode in xmlNode.childrenNamed( 'node_position' ):
			node = nodePosNode.getChild( 'node' ).readObject()
			pos = Point2()
			nodePosNode.getChild( 'position' )  >>  pos
			self._nodePositions[node] = pos




	def __writexml__(self, xmlNode):
		for node, pos in self._nodePositions.items():
			nodePosNode = xmlNode.addChild( 'node_position' )
			nodePosNode.addChild( 'node' ).writeObject( node )
			nodePosNode.addChild( 'position' )  <<  pos


ioObjectFactoryRegister( 'SheetGraphViewDisplayTable', SheetGraphViewDisplayTable )
