##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import ClassSignal

from Britefury.CodeViewTree.CodeViewTree import CodeViewTree

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNode import CGNode





class CVTSimpleSinkProductionOptionalField (FunctionRefField):
	def __init__(self, sinkField, rule=None, doc=''):
		def _function(cvtNode):
			pin = sinkField._f_getPinFromInstance( cvtNode.graphNode )
			if len( pin ) > 0:
				return cvtNode._tree.buildNode( pin[0].node, rule )
			else:
				return None
		super( CVTSimpleSinkProductionOptionalField, self ).__init__( _function, doc )




class CVTSimpleSinkProductionSingleField (FunctionRefField):
	def __init__(self, sinkField, rule=None, doc=''):
		def _function(cvtNode):
			pin = sinkField._f_getPinFromInstance( cvtNode.graphNode )
			if len( pin ) > 0:
				return cvtNode._tree.buildNode( pin[0].node, rule )
			else:
				return cvtNode._f_makeInvalidNode()
		super( CVTSimpleSinkProductionSingleField, self ).__init__( _function, doc )





class CVTSimpleSinkProductionMultipleField (FunctionField):
	def __init__(self, sinkField, rule=None, doc=''):
		def _function(cvtNode):
			pin = sinkField._f_getPinFromInstance( cvtNode.graphNode )
			return [ cvtNode._tree.buildNode( source.node, rule )   for source in pin ]
		super( CVTSimpleSinkProductionMultipleField, self ).__init__( _function, doc )






class CVTSimpleNodeProductionSingleField (FunctionRefField):
	def __init__(self, rule=None, doc=''):
		def _function(cvtNode):
			return cvtNode._tree.buildNode( cvtNode.graphNode, rule )
		super( CVTSimpleNodeProductionSingleField, self ).__init__( _function, doc )













class CVTNode (Sheet):
	graphNode = SheetRefField( CGNode )


	def __init__(self, graphNode, tree):
		super( CVTNode, self ).__init__()

		self.graphNode = graphNode
		self._tree = tree




	def __eq__(self, node):
		return self.graphNode  is  node.graphNode


	def getTree(self):
		return self._tree

	def getGraph(self):
		return self._tree.graph



	def _f_makeInvalidNode(self):
		return CVTNodeInvalid( None, self._tree )


	tree = property( getTree )
	graph = property( getGraph )




class CVTNodeInvalid (CVTNode):
		pass

