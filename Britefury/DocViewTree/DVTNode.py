##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import ClassSignal

from Britefury.DocViewTree.DocViewTree import DocViewTree

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNode import CGNode





class DVTSimpleSinkProductionOptionalField (FunctionRefField):
	def __init__(self, sinkField, rule=None, doc=''):
		def _function(dvtNode):
			pin = sinkField._f_getPinFromInstance( dvtNode.graphNode )
			if len( pin ) > 0:
				return dvtNode._tree.buildNode( pin[0].node, rule )
			else:
				return None
		super( DVTSimpleSinkProductionOptionalField, self ).__init__( _function, doc )




class DVTSimpleSinkProductionSingleField (FunctionRefField):
	def __init__(self, sinkField, rule=None, doc=''):
		def _function(dvtNode):
			pin = sinkField._f_getPinFromInstance( dvtNode.graphNode )
			if len( pin ) > 0:
				return dvtNode._tree.buildNode( pin[0].node, rule )
			else:
				return dvtNode._f_makeInvalidNode()
		super( DVTSimpleSinkProductionSingleField, self ).__init__( _function, doc )





class DVTSimpleSinkProductionMultipleField (FunctionField):
	def __init__(self, sinkField, rule=None, doc=''):
		def _function(dvtNode):
			pin = sinkField._f_getPinFromInstance( dvtNode.graphNode )
			return [ dvtNode._tree.buildNode( source.node, rule )   for source in pin ]
		super( DVTSimpleSinkProductionMultipleField, self ).__init__( _function, doc )






class DVTSimpleNodeProductionSingleField (FunctionRefField):
	def __init__(self, rule=None, doc=''):
		def _function(dvtNode):
			return dvtNode._tree.buildNode( dvtNode.graphNode, rule )
		super( DVTSimpleNodeProductionSingleField, self ).__init__( _function, doc )













class DVTNode (Sheet):
	graphNode = SheetRefField( CGNode )


	def __init__(self, graphNode, tree):
		super( DVTNode, self ).__init__()

		self.graphNode = graphNode
		self._tree = tree




	def __eq__(self, node):
		return self.graphNode  is  node.graphNode


	def getTree(self):
		return self._tree

	def getGraph(self):
		return self._tree.graph



	def _f_makeInvalidNode(self):
		return DVTNodeInvalid( None, self._tree )


	tree = property( getTree )
	graph = property( getGraph )




class DVTNodeInvalid (DVTNode):
		pass

