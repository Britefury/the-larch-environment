##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary





class DVTRuleTable (object):
	def __init__(self):
		super( DVTRuleTable, self ).__init__()
		self._graphNodeClassToRule = {}


	def registerRule(self, rule, priority=0):
		if rule.graphNodeClass is None:
			raise ValueError, 'rule %s does not have a graph node class' % ( rule.__name__, )

		try:
			rules = self._graphNodeClassToRule[rule.graphNodeClass]
		except KeyError:
			rules = []
			self._graphNodeClassToRule[rule.graphNodeClass] = rules

		rules.append( ( rule, priority ) )
		rules.sort( key=lambda x: x[1] )


	def getRuleForGraphNode(self, graphNode):
		try:
			rules = self._graphNodeClassToRule[graphNode.__class__ ]
		except KeyError:
			return None

		for rule, priority in rules:
			if rule.isApplicableTo( graphNode ):
				return rule

		return None







class DVTRule (object):
	graphNodeClass = None


	@classmethod
	def isApplicableTo(cls, graphNode):
		return False


	@classmethod
	def buildDVT(cls, tree, graphNode):
		raise ValueError, 'rule not applicable'


	@classmethod
	def register(cls, priority=0):
		DocViewTree.registerRule( cls, priority )




class DVTRuleSimple (DVTRule):
	graphNodeClass = None
	cvtNodeClass = None


	@classmethod
	def isApplicableTo(cls, graphNode):
		return True


	@classmethod
	def buildDVT(cls, tree, graphNode):
		node = cls.cvtNodeClass( graphNode, tree )
		return node





class DocViewTree (object):
	_ruleTable = DVTRuleTable()


	def __init__(self, graph, rootGraphNode):
		super( DocViewTree, self ).__init__()

		self._nodeTable = WeakKeyDictionary()
		self._graph = graph
		self._rootGraphNode = rootGraphNode



	def buildNode(self, graphNode, rule=None):
		if graphNode is None:
			return None
		else:
			if rule is None:
				rule = self._ruleTable.getRuleForGraphNode( graphNode )

				if rule is None:
					raise ValueError, 'could not getrule for graph node %s'  %  ( graphNode, )

			try:
				subTable = self._nodeTable[graphNode]
			except KeyError:
				subTable = {}
				self._nodeTable[graphNode] = subTable

			try:
				treeNode = subTable[rule]
			except KeyError:
				treeNode = rule.buildDVT( self, graphNode )
				subTable[rule] = treeNode

			return treeNode



	@classmethod
	def registerRule(cls, rule, priority=0):
		cls._ruleTable.registerRule( rule, priority )



	def getGraph(self):
		return self._graph


	def getRootNode(self):
		return self.buildNode( self._rootGraphNode )


	graph = property( getGraph, None )


