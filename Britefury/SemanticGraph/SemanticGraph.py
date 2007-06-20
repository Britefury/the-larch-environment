##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *




class SemanticGraphSubtreeField (object):
	pass




class SemanticGraphSourceField (SheetGraphSourceField):
	pass


class SemanticGraphSinkSingleField (SheetGraphSinkSingleField):
	pass


class SemanticGraphSinkMultipleField (SheetGraphSinkMultipleField):
	pass




class SemanticGraphSinkSingleSubtreeField (SemanticGraphSinkSingleField, SemanticGraphSubtreeField):
	pass


class SemanticGraphSinkMultipleSubtreeField (SemanticGraphSinkMultipleField, SemanticGraphSubtreeField):
	pass




class SemanticGraphNodeClass (SheetGraphNodeClass):
	def __init__(cls, clsName, clsBases, clsDict):
		super( SemanticGraphNodeClass, cls ).__init__( clsName, clsBases, clsDict )

		cls._SemanticGraphNode_subtreeFields = [ pinField   for pinField in cls._SheetGraphNode_sinkFields.values()   if isinstance( pinField, SemanticGraphSubtreeField ) ]




class SemanticGraphNode (SheetGraphNode):
	__metaclass__ = SemanticGraphNodeClass

	def getReferenceableNodeByName(self, targetName, sourceNode=None):
		return None


	def destroy(self):
		self._graph.nodes.remove( self )

	def destroySubtree(self):
		self._p_destroyChildren()
		self.destroy()


	def _p_destroyChildren(self):
		for subtreeField in self._SemanticGraphNode_subtreeFields:
			sink = subtreeField._f_getPinFromInstance( self )
			for source in sink:
				source.node.destroySubtree()





class SemanticGraph (SheetGraph):
	pass
