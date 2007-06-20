##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.Math.Math import Vector2, Point2

from Britefury.Event.QueuedEvent import queueEvent




HORIZONTAL_SPACING = 45.0
VERTICAL_SPACING = 30.0




class _LayoutNode (object):
	def __init__(self, graphLayout):
		super( _LayoutNode, self ).__init__()
		self._graphLayout = graphLayout
		self._graphNode = None
		self._graphViewNode = None
		self._inputs = []
		self._primaryOutput = None
		self._nodeReq = Vector2()
		self._requisition = Vector2()



	def attachGraphNode(self, graphNode, graphViewNode):
		self._graphNode = graphNode
		self._graphViewNode = graphViewNode
		self._graphNode.outputNodes.appendSignal.connect( self._p_onAppendOutput )
		self._graphNode.outputNodes.removeSignal.connect( self._p_onRemoveOutput )
		self._p_onOutputsChanged()


	def detachGraphNode(self):
		if self._primaryOutput is not None:
			self._primaryOutput._p_removeInput( self )
		self._primaryOutput = None
		self._graphNode.outputNodes.appendSignal.disconnect( self._p_onAppendOutput )
		self._graphNode.outputNodes.removeSignal.disconnect( self._p_onRemoveOutput )
		self._primaryOutput = None
		self._graphNode = None
		self._graphViewNode = None


	def isRoot(self):
		return self._primaryOutput is None


	def _p_onAppendOutput(self, sink):
		self._p_onOutputsChanged()

	def _p_onRemoveOutput(self, sink):
		self._p_onOutputsChanged()


	def _p_computePrimaryOutput(self):
		if len( self._graphNode.outputNodes ) == 0:
			return None
		else:
			primaryOutputGraphNode = self._graphNode.outputNodes[0]
			return self._graphLayout._graphNodeToLayoutNode[primaryOutputGraphNode]


	def _p_onOutputsChanged(self):
		primOut = self._p_computePrimaryOutput()
		if primOut is not self._primaryOutput:
			if self._primaryOutput is not None:
				self._primaryOutput._p_removeInput( self )
			self._primaryOutput = primOut
			if self._primaryOutput is not None:
				self._primaryOutput._p_addInput( self )


	def _p_addInput(self, node):
		self._inputs.append( node )
		self._graphLayout._f_structureModified()


	def _p_removeInput(self, node):
		self._inputs.remove( node )
		self._graphLayout._f_structureModified()



	def _p_getEstimatedRequisition(self):
		self._nodeReq = self._graphViewNode._p_getEstimatedRequisition()

		if len( self._inputs ) > 0:
			inputsReq = copy( self._inputs[0]._p_getEstimatedRequisition() )

			for inputNode in self._inputs[1:]:
				inputReq = inputNode._p_getEstimatedRequisition()
				inputsReq.x = max( inputsReq.x, inputReq.x )
				inputsReq.y += ( VERTICAL_SPACING + inputReq.y )

			self._requisition = Vector2( self._nodeReq.x + HORIZONTAL_SPACING + inputsReq.x, max( self._nodeReq.y, inputsReq.y ) )
			return self._requisition
		else:
			self._requisition = self._nodeReq
			return self._requisition


	def _p_allocate(self, position, size):
		nodePos = Vector2( position.x, position.y + ( size.y - self._nodeReq.y ) * 0.5 )
		self._graphLayout._graphViewDisplayTable[self._graphNode] = Point2( nodePos )

		pos = position + Vector2( HORIZONTAL_SPACING + self._nodeReq.x, 0.0 )

		for inputNode in self._inputs:
			nodeReq = inputNode._requisition
			inputNode._p_allocate( pos, nodeReq )
			pos = pos + Vector2( 0.0, VERTICAL_SPACING + nodeReq.y )








class SheetGraphViewLayout (object):
	def __init__(self):
		super( SheetGraphViewLayout, self ).__init__()
		self._graph = None
		self._graphNodeToLayoutNode = {}
		self._layoutNodes = []
		self._graphView = None
		self._graphViewDisplayTable = None


	def attachGraph(self, graph, graphView, graphViewDisplayTable):
		self._graph = graph
		self._graphView = graphView
		self._graphViewDisplayTable = graphViewDisplayTable
		self._graph.nodeAddedSignal.connect( self._p_onNodeAdded )
		self._graph.nodeRemovedSignal.connect( self._p_onNodeRemoved )

		for node in self._graph.nodes:
			layoutNode = _LayoutNode( self )
			self._layoutNodes.append( layoutNode )
			self._graphNodeToLayoutNode[node] = layoutNode

		for node, layoutNode in self._graphNodeToLayoutNode.items():
			layoutNode.attachGraphNode( node, self._graphView._p_getViewNodeForGraphNode( node ) )

		self._f_structureModified()


	def detachGraph(self):
		for node, layoutNode in self._graphNodeToLayoutNode.items():
			layoutNode = self._graphNodeToLayoutNode[node]
			layoutNode.detachGraphNode()

		self._layoutNodes = []
		self._graphNodeToLayoutNode = {}

		self._graph.nodeAddedSignal.disconnect( self._p_onNodeAdded )
		self._graph.nodeRemovedSignal.disconnect( self._p_onNodeRemoved )
		self._graph = None
		self._graphView = None
		self._graphViewDisplayTable = None



	def _p_onNodeAdded(self, graph, node):
		layoutNode = _LayoutNode( self )
		self._layoutNodes.append( layoutNode )
		self._graphNodeToLayoutNode[node] = layoutNode
		layoutNode.attachGraphNode( node, self._graphView._p_getViewNodeForGraphNode( node ) )
		self._f_structureModified()

	def _p_onNodeRemoved(self, graph, node):
		layoutNode = self._graphNodeToLayoutNode[node]
		layoutNode.detachGraphNode()
		self._layoutNodes.remove( layoutNode )
		del self._graphNodeToLayoutNode[node]
		self._f_structureModified()




	def getRootNodes(self):
		return [ layoutNode   for layoutNode in self._layoutNodes   if layoutNode.isRoot() ]



	def _f_structureModified(self):
		queueEvent( self._p_refresh )



	def _p_refresh(self):
		if self._graph is not None:
			roots = self.getRootNodes()

			if len( roots ) > 0:
				requisition = roots[0]._p_getEstimatedRequisition()

				for root in roots[1:]:
					rootReq = root._p_getEstimatedRequisition()
					requisition.x = max( requisition.x, rootReq.x )
					requisition.y += VERTICAL_SPACING + rootReq.y

				pos = Vector2()
				for root in roots:
					root._p_allocate( pos, root._requisition )
					pos.y += VERTICAL_SPACING + root._requisition.y

