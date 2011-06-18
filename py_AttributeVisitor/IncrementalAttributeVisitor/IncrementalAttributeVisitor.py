import weakref

from collections import deque

from BritefuryJ.Incremental import IncrementalMonitor, IncrementalValueMonitor, IncrementalFunctionMonitor, IncrementalMonitorListener, IncrementalValueOwner

from Britefury.Util.NodeUtil import isListNode, isObjectNode
 


def _docNodeParent(node):
	p = node.getValidParents()
	return p[0]   if len( p ) > 0   else   None

def _docNodeChildren(node):
	if isListNode( node )  or  isObjectNode( node ):
		return node.getChildren()
	else:
		return []





class _AttributeBase (object):
	def __init__(self, name):
		self.name = name
		
	def getFallbackResult(self, evaluator, node, attribEntry):
		assert False, 'abstract'

class Attribute (_AttributeBase):
	def getFallbackResult(self, evaluator, node, attribEntry):
		return None
	
class InheritedAttribute (_AttributeBase):
	def getFallbackResult(self, evaluator, node, attribEntry):
		p = _docNodeParent( node )
		while p is not None:
			v = attribEntry._lookupAttributeResult( evaluator, self, node )
			if v is not None:
				return v
			p = _docNodeParent( p )
		return None

class SynthesizedAttribute (_AttributeBase):
	def getFallbackResult(self, evaluator, node, attribEntry):
		q = deque()
		q.appendleft( node )
		while len( q ) > 0:
			n = q.pop()
			v = attribEntry._lookupAttributeResult( evaluator, self, n )
			if v is None:
				for c in _docNodeChildren( n ):
					q.appendleft( c )
			else:
				evaluator._linkToInwardDependency( v )
		return None


class _AVResult (IncrementalMonitorListener):
	def __init__(self, node, attribEntry):
		self.node = weakref.ref( node )
		self.attribEntry = attribEntry
		self.incr = IncrementalFunctionMonitor()
		self._inwardDependencies = set()
		self._outwardDependencies = set()
		
		self.incr.addListener( self )
		
		self.value = None
		
		
	def onIncrementalValueChanged(self, incr):
		node = self.node()
		if node is not None:
			self.attribEntry._onResultInvalidated( node )
		for d in self._inwardDependencies:
			d._outwardDependencies.remove( self )
		for d in self._outwardDependencies:
			d._inwardDependencies.remove( self )
			
			
	def _linkToInwardDependency(self, dep):
		self._inwardDependencies.add( dep )
		dep._outwardDependencies.add( self )
		
		

class IncrementalAttributeVisitorEvaluator (object):
	class _NodeEntry (object):
		def __init__(self, evaluator):
			pass
			
			
			
	class _AttribEntry (object):
		def __init__(self, evaluator):
			self._nodeToEntry = weakref.WeakKeyDictionary()
			self._nodeClassToEvalFn = {}
			
		def registerEvalFn(self, nodeClass, evalFn):
			self._nodeClassToEvalFn[nodeClass] = evalFn
			
		def getAttributeResult(self, evaluator, attribute, node):
			result = self._lookupAttributeValue( evaluator, attribute, node )
			if result is None:
				result = attribute.getFallbackResult( evaluator, node, self )
				
			return result
		
		def _lookupAttributeResult(self, evaluator, attribute, node):
			try:
				result = self._nodeToEntry[node]
			except KeyError:
				IncrementalMonitor.blockAccessTracking()
				nodeClass = node.getDMNodeClass()
				IncrementalMonitor.unblockAccessTracking()
				evalFn = self._nodeClassToEvalFn.get( nodeClass )
				if evalFn is not None:
					result = self._createAttributeResult( evaluator, attribute, node, evalFn )
					self._nodeToEntry[node] = result
				else:
					result = None
			return result

		def _createAttributeResult(self, evaluator, attribute, node, evalFn):
			result = _AVResult( self )
			prevResult = evaluator._pushAVResult( result )
			
			refreshState = result.incr.onRefreshBegin()
			# Ensure that the result is dependent on the class of the node
			node.getDMNodeClass()
			value = evalFn( node )
			result.value = value
			result.incr.onRefreshEnd( refreshState )
			
			evaluator._popAVResult( prevResult )
			
			return result
		
		
		def _onResultInvalidated(self, node):
			del self._nodeToEntry[node]

		
		
	def __init__(self, attributes, evalFnSpecs):
		"""
		Constructor
		
		evalFnSpecs  -  [ ( attribute, nodeClass, evalFn ) ]
  		"""
		self.attributes = attributes
		self._attribToEntry = {}
		for attribute in attributes:
			self._attribToEntry[attribute] =  self._AttribEntry( self )
		
		for attribute, nodeClass, evalFn in evalFnSpecs:
			attribEntry = self._attribToEntry[attribute]
			attribEntry.registerEvalFn( nodeClass, evalFn )
			
		self._currentlyComputingAVResult = None
		
		
		
	def getAttributeValue(self, attribute, node):
		try:
			attribEntry = self._attribToEntry[attribute]
		except KeyError:
			raise KeyError, 'Invalid attribute \'%s\''  %  ( attribute.name, )
			
		result = attribEntry.getAttributeResult( self, attribute, node )
		self._linkToInwardDependency( result )
		
		return result.value   if result is not None   else None
	

	def _linkToInwardDependency(self, dep):
		if self._currentlyComputingAVResult is not None  and  dep is not None:
			self._currentlyComputingAVResult._linkToInwardDependency( dep )
	
	
	def _pushAVResult(self, result):
		prevResult = self._currentlyComputingAVResult
		self._currentlyComputingAVResult = result
		return prevResult
	
	def _popAVResult(self, prevResult):
		self._currentlyComputingAVResult = prevResult
		
		
		
		