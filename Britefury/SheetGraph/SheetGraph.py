##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.Util.SignalSlot import *

from Britefury.Math.Math import Point2

from Britefury.FileIO import IOXml

from Britefury.Kernel import KMeta

from Britefury.Cell.CellInterface import CellInterface

from Britefury.Sheet.Sheet import SheetClass, Sheet, Field
from Britefury.SheetGraph import SheetGraphCommandTracker





class SheetGraphItemList (object):
	__slots__ = [ '_items' ]


	appendSignal = ClassSignal()
	removeSignal = ClassSignal()


	def __init__(self):
		super( SheetGraphItemList, self ).__init__()
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



class SheetGraphNodePin (CellInterface):
	"""Funciton graph node pin"""

	__slots__ = [ '_node', '_name', 'owner' ]


	def __init__(self, node=None, name=''):
		super( SheetGraphNodePin, self ).__init__()
		self._node = node
		self._name = name
		self.owner = None



	def isValid(self):
		return True


	def isLiteral(self):
		return True



	def _o_onGet(self):
		self._bRefreshRequired = False

		# Add @self to the global dependency list if it exists; this ensures that any cell that
		# is recomputing its value will know that the @value of self is required
		if CellInterface._cellDependencies is not None:
			CellInterface._cellDependencies[self] = 0



	def __readxml__(self, xmlNode):
		pass

	def __writexml__(self, xmlNode):
		pass


	def _f_setName(self, name):
		self._name = name

	def _f_setNode(self, node):
		self._node = node


	def _p_getName(self):
		return self._name

	def _p_getNode(self):
		return self._node


	bValid = property( isValid, None )

	bLiteral = property( isLiteral )


	name = property( _p_getName, doc='Name of the pin' )
	node = property( _p_getNode, doc='Node' )




class SheetGraphNodeSource (SheetGraphNodePin):
	"""Function graph node source (output) pin"""

	__slots__ = [ '_outputs' ]


	def __init__(self, node=None, name=''):
		super( SheetGraphNodeSource, self ).__init__( node, name )
		self._outputs = []


	def getEvaluator(self):
		return self._outputs

	def setEvaluator(self, evaluator):
		raise TypeError, 'cannot set output list of sheet graph node source'


	def getLiteralValue(self):
		return self._outputs

	def setLiteralValue(self, literal):
		raise TypeError, 'cannot set output list of sheet graph node source'




	def getValue(self):
		self._o_onGet()
		return copy( self._outputs )

	def getImmutableValue(self):
		self._o_onGet()
		return copy( self._outputs )




	def __len__(self):
		self._o_onGet()
		return len( self._outputs )

	def __contains__(self, output):
		self._o_onGet()
		return output in self._outputs

	def __getitem__(self, index):
		self._o_onGet()
		return self._outputs[index]

	def __iter__(self):
		self._o_onGet()
		return iter( self._outputs )

	def index(self, output):
		self._o_onGet()
		return self._outputs.index( output )


	def _f_clearOutputs(self):
		while len( self._outputs ) > 0:
			output = self._outputs[-1]
			output.remove( self )


	def _f_addOutput(self, sink):
		assert self._node is not None, 'no node'
		assert sink not in self._outputs, '@sink already present in output list'
		oldOuts = copy( self._outputs )
		self._outputs.append( sink )
		self._node._f_addOutputNode( sink._node )
		self.evaluatorSignal.emit( oldOuts, self._outputs )
		self._o_changed()

	def _f_removeOutput(self, sink):
		assert self._node is not None, 'no node'
		assert sink in self._outputs, '@sink not present in output list'
		oldOuts = copy( self._outputs )
		self._outputs.remove( sink )
		self._node._f_removeOutputNode( sink._node )
		self.evaluatorSignal.emit( oldOuts, self._outputs )
		self._o_changed()



	evaluator = property( getEvaluator, setEvaluator )
	literalValue = property( getLiteralValue, setLiteralValue )

	value = property( getValue )
	immutableValue = property( getImmutableValue )



IOXml.ioObjectFactoryRegister( 'SheetGraphNodeSource', SheetGraphNodeSource )




class SheetGraphNodeSink (SheetGraphNodePin):
	"""Function graph node sink abstract base class"""

	__slots__ = [ '_commandTracker_' ]


	trackerClass = SheetGraphCommandTracker.SheetGraphNodeSinkCommandTracker

	appendSignal = ClassSignal()
	extendSignal = ClassSignal()
	insertSignal = ClassSignal()
	removeSignal = ClassSignal()
	clearSignal = ClassSignal()
	setSignal = ClassSignal()
	changedSignal = ClassSignal()


	def __init__(self, node=None, name=''):
		super( SheetGraphNodeSink, self ).__init__( node, name )
		self._commandTracker_ = None




	def setEvaluator(self, evaluator):
		if isinstance( evaluator, CellEvaluator ):
			raise TypeError, 'sheet graph node sink cannot take CellEvaluator instances as the evaluator; use a literal value'
		self._f_set( evaluator )


	def setLiteralValue(self, literal):
		if isinstance( evaluator, CellEvaluator ):
			raise TypeError, 'sheet graph node sink cannot take CellEvaluator instances as the evaluator; use a literal value'
		self._f_set( literal )







	def append(self, source):
		assert isinstance( source, SheetGraphNodeSource )
		assert self._node is not None, 'no node'
		assert len( self ) < self.maxLen() or  self.maxLen() is None, 'cannot add another input'
		assert source not in self, 'input already present'
		source._f_addOutput( self )
		self._f_append( source )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSinkAppended( self, source )
		self.appendSignal.emit( self, source )
		self.changedSignal.emit( self )
		self._node._o_onAddInputSource( source )
		self._node._f_sinkModified( self )

	def extend(self, sources):
		assert self._node is not None, 'no node'
		assert ( len( self ) + len( sources ) <= self.maxLen() or  self.maxLen() is None ), 'insufficient space for inputs'
		for source in sources:
			assert isinstance( source, SheetGraphNodeSource )
			assert source not in self, 'input already present'
		for source in sources:
			source._f_addOutput( self )
		self._f_extend( sources )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSinkExtended( self, sources )
		self.extendSignal.emit( self, sources )
		self.changedSignal.emit( self )
		for source in sources:
			self._node._o_onAddInputSource( source )
		self._node._f_sinkModified( self )

	def insert(self, index, source):
		assert isinstance( source, SheetGraphNodeSource )
		assert self._node is not None, 'no node'
		assert len( self ) < self.maxLen() or  self.maxLen() is None, 'cannot add another input'
		assert source not in self, 'input already present'
		assert ( index >= 0  and  index <= len( self ) )   or   ( index < 0  and  index >= -( len( self ) + 1 ) ), 'index out of range'
		source._f_addOutput( self )
		self._f_insert( index, source )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSinkInserted( self, index, source )
		self.insertSignal.emit( self, source )
		self.changedSignal.emit( self )
		self._node._o_onAddInputSource( source )
		self._node._f_sinkModified( self )

	def insertBefore(self, source, beforeSource):
		try:
			index = self.index( beforeSource )
		except ValueError:
			self.append( source ):
		else:
			self.insert( index, source )

	def insertAfter(self, source, afterSource):
		try:
			index = self.index( beforeSource )
		except ValueError:
			self.append( source ):
		else:
			self.insert( index + 1, source )

	def remove(self, source):
		assert isinstance( source, SheetGraphNodeSource )
		assert self._node is not None, 'no node'
		assert source in self, 'input not present'
		self.removeSignal.emit( self, source )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSinkRemoved( self, source )
		self._node._o_onRemoveInputSource( source )
		source._f_removeOutput( self )
		self._f_remove( source )
		self.changedSignal.emit( self )
		self._node._f_sinkModified( self )

	def pop(self):
		source = self[-1]
		self.remove( source )
		return source

	def replace(self, source, withSource):
		assert isinstance( source, SheetGraphNodeSource )
		assert isinstance( withSource, SheetGraphNodeSource )
		assert self._node is not None, 'no node'
		assert source in self, 'input @source not present'
		assert withSource not in self, 'input @withSource already present'
		n = self.index( source )
		self[n] = withSource

	def splitLinkWithNode(self, linkToSource, nodeSink, nodeSource):
		assert isinstance( linkToSource, SheetGraphNodeSource )
		assert isinstance( nodeSink, SheetGraphNodeSink )
		assert isinstance( nodeSource, SheetGraphNodeSource )
		self.replace( linkToSource, nodeSource )
		nodeSink.append( linkToSource )


	def clear(self):
		assert self._node is not None, 'no node'
		self.clearSignal.emit( self )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSinkCleared( self )
		for source in self:
			self._node._o_onRemoveInputSource( source )
			source._f_removeOutput( self )
		self._f_clear()
		self.changedSignal.emit( self )
		self._node._f_sinkModified( self )


	def _f_setContents(self, contents):
		assert self._node is not None, 'no node'
		assert len( contents ) <= self.maxLen() or  self.maxLen() is None, 'too many inputs (%d/%d)'  %  ( len( contents ), self.maxLen() )
		if isinstance( contents, tuple ):
			contents = list( contents )
		for source in self:
			assert isinstance( source, SheetGraphNodeSource )
			source._f_removeOutput( self )
			self._node._o_onRemoveInputSource( source )
		oldContents = self[:]
		self._f_set( contents )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSinkSet( self, oldContents, contents )
		self.setSignal.emit( self, oldContents, contents )
		for source in self:
			source._f_addOutput( self )
			self._node._o_onAddInputSource( source )
		self.changedSignal.emit( self )
		self._node._f_sinkModified( self )


	def canAddInput(self):
		maxLen = self.maxLen()
		return len( self ) < maxLen or  maxLen is None


	def checkAddInputForCycles(self, source):
		return self._node._p_checkAddInputForCycles( source._node )


	def __len__(self):
		assert False, 'abstract'

	def maxLen(self):
		assert False, 'abstract'

	def __contains__(self, source):
		assert False, 'abstract'

	def __getitem__(self, index):
		assert False, 'abstract'

	def __setitem__(self, index, value):
		contents = self[:]
		contents[index] = value
		self._f_setContents( contents )

	def __delitem__(self, index):
		source = self[index]
		if isinstance( source, list ):
			for s in source:
				self.remove( s )
		else:
			self.remove( source )

	def __iter__(self):
		assert False, 'abstract'

	def index(self, source):
		assert False, 'abstract'


	def compareInputs(self, x):
		contents = self[:]
		if isinstance( x, SheetGraphNodeSink ):
			x = x[:]
		return cmp( contents, x )


	def copyInputsFrom(self, sink):
		self._p_clearInputs()
		for source in sink:
			self.append( source )


	def _f_append(self, source):
		assert False, 'abstract'

	def _f_extend(self, sources):
		assert False, 'abstract'

	def _f_insert(self, index, source):
		assert False, 'abstract'

	def _f_remove(self, source):
		assert False, 'abstract'

	def _f_clear(self):
		assert False, 'abstract'

	def _f_set(self, contents):
		assert False, 'abstract'








class SheetGraphNodeSinkSingle (SheetGraphNodeSink):
	"""Function graph node sink that can take a single input"""

	__slots__ = [ '_input' ]


	def __init__(self, node=None, name=''):
		super( SheetGraphNodeSinkSingle, self ).__init__( node, name )
		self._input = None



	def getEvaluator(self):
		return self._p_getContentsAsList()


	def getLiteralValue(self):
		return self._p_getContentsAsList()


	def getValue(self):
		self._o_onGet()
		return self._p_getContentsAsList()

	def getImmutableValue(self):
		self._o_onGet()
		return self._p_getContentsAsList()



	def __len__(self):
		self._o_onGet()
		if self._input is None:
			return 0
		else:
			return 1

	def maxLen(self):
		return 1

	def __contains__(self, source):
		self._o_onGet()
		return source is self._input

	def __getitem__(self, index):
		self._o_onGet()
		if isinstance( index, slice ):
			return self._p_getContentsAsList()[index]
		else:
			if self._input is None:
				raise IndexError
			else:
				if index > 0  or  index < -1:
					raise IndexError
				return self._input

	def __iter__(self):
		self._o_onGet()
		if self._input is not None:
			yield self._input

	def index(self, source):
		self._o_onGet()
		if source is self._input:
			return 0
		else:
			raise ValueError, '@source not in list'


	def _f_append(self, source):
		assert self._input is None, 'input already set'
		oldIn = self._input
		self._input = source
		self.evaluatorSignal.emit( self._p_inputAsList( oldIn ), self._p_inputAsList( self._input ) )
		self._o_changed()

	def _f_extend(self, sources):
		assert self._input is None, 'input already set'
		assert len( sources ) < 2, 'too many inputs (%d)'  %  ( len( sources ), )
		oldIn = self._input
		newIn = None
		if len( sources ) > 0:
			newIn = sources[0]
		self._input = newIn
		self.evaluatorSignal.emit( self._p_inputAsList( oldIn ), self._p_inputAsList( self._input ) )
		self._o_changed()

	def _f_insert(self, index, source):
		assert self._input is None, 'input already set'
		assert index == 0  or  index == -1, 'index out of range'
		oldIn = self._input
		self._input = source
		self.evaluatorSignal.emit( self._p_inputAsList( oldIn ), self._p_inputAsList( self._input ) )
		self._o_changed()

	def _f_remove(self, source):
		assert source is self._input, '@source does not match my input'
		oldIn = self._input
		self._input = None
		self.evaluatorSignal.emit( self._p_inputAsList( oldIn ), self._p_inputAsList( self._input ) )
		self._o_changed()

	def _f_clear(self):
		oldIn = self._input
		self._input = None
		self.evaluatorSignal.emit( self._p_inputAsList( oldIn ), self._p_inputAsList( self._input ) )
		self._o_changed()

	def _f_set(self, contents):
		assert len( contents ) < 2, 'too many inputs (%d)'  %  ( len( contents ), )
		oldIn = self._input
		if len( contents ) == 1:
			self._input = contents[0]
		else:
			self._input = None
		self.evaluatorSignal.emit( self._p_inputAsList( oldIn ), self._p_inputAsList( self._input ) )
		self._o_changed()


	def _p_inputAsList(self, input):
		if input is None:
			return []
		else:
			return [ input ]

	def _p_getContentsAsList(self):
		if self._input is None:
			return []
		else:
			return [ self._input ]



	evaluator = property( getEvaluator, SheetGraphNodeSink.setEvaluator )
	literalValue = property( getLiteralValue, SheetGraphNodeSink.setLiteralValue )

	value = property( getValue )
	immutableValue = property( getImmutableValue )


IOXml.ioObjectFactoryRegister( 'SheetGraphNodeSinkSingle', SheetGraphNodeSinkSingle )





class SheetGraphNodeSinkMultiple (SheetGraphNodeSink):
	"""Function graph node sink that can take multiple inputs"""

	__slots__ = [ '_inputs' ]


	def __init__(self, node=None, name=''):
		super( SheetGraphNodeSinkMultiple, self ).__init__( node, name )
		self._inputs = []


	def getEvaluator(self):
		return self._inputs


	def getLiteralValue(self):
		return self._inputs


	def getValue(self):
		self._o_onGet()
		return copy( self._inputs )

	def getImmutableValue(self):
		self._o_onGet()
		return copy( self._inputs )



	def __len__(self):
		self._o_onGet()
		return len( self._inputs )

	def maxLen(self):
		return None

	def __contains__(self, source):
		self._o_onGet()
		return source in self._inputs

	def __getitem__(self, index):
		self._o_onGet()
		return self._inputs[index]

	def __iter__(self):
		self._o_onGet()
		return iter( self._inputs )

	def index(self, source):
		self._o_onGet()
		return self._inputs.index( source )


	def _f_append(self, source):
		assert source not in self._inputs, '@source already in input list'
		oldIns = copy( self._inputs )
		self._inputs.append( source )
		self.evaluatorSignal.emit( oldIns, self._inputs )
		self._o_changed()


	def _f_extend(self, sources):
		for source in sources:
			assert source not in self._inputs, '@source already in input list'
		oldIns = copy( self._inputs )
		self._inputs.extend( sources )
		self.evaluatorSignal.emit( oldIns, self._inputs )
		self._o_changed()

	def _f_insert(self, index, source):
		assert source not in self._inputs, '@source already in input list'
		oldIns = copy( self._inputs )
		self._inputs.insert( index, source )
		self.evaluatorSignal.emit( oldIns, self._inputs )
		self._o_changed()

	def _f_remove(self, source):
		assert source in self._inputs, '@source not in input list'
		oldIns = copy( self._inputs )
		self._inputs.remove( source )
		self.evaluatorSignal.emit( oldIns, self._inputs )
		self._o_changed()

	def _f_clear(self):
		oldIns = copy( self._inputs )
		self._inputs = []
		self.evaluatorSignal.emit( oldIns, self._inputs )
		self._o_changed()

	def _f_set(self, contents):
		oldIns = copy( self._inputs )
		self._inputs = copy( contents )
		self.evaluatorSignal.emit( oldIns, self._inputs )
		self._o_changed()


	evaluator = property( getEvaluator, SheetGraphNodeSink.setEvaluator )
	literalValue = property( getLiteralValue, SheetGraphNodeSink.setLiteralValue )

	value = property( getValue )
	immutableValue = property( getImmutableValue )



IOXml.ioObjectFactoryRegister( 'SheetGraphNodeSinkMultiple', SheetGraphNodeSinkMultiple )





class SheetGraphPinField (KMeta.KMetaMember):
	"""Node pin field"""
	def __init__(self, name, doc = ''):
		super( SheetGraphPinField, self ).__init__( doc )
		self._attrName = None
		self._pinName = name


	def _f_metaMember_initMetaMember(self, cls, name):
		super( SheetGraphPinField, self )._f_metaMember_initMetaMember( cls, name )
		self._attrName = intern( SheetGraphPinField._p_computeAttrName( name ) )
		self._xmlName = self._o_computeXmlName( name )


	def _f_readInstancePinXml(self, instance, xmlNode):
		pin = self._f_getPinFromInstance( instance )
		xmlNode.getChild( self._xmlName )  >>  pin
		pin._f_setName( self._pinName )

	def _f_writeInstancePinXml(self, instance, xmlNode):
		xmlNode.addChild( self._xmlName )  <<  self._f_getPinFromInstance( instance )



	def __get__(self, obj, objtype = None):
		if obj is not None:
			return self._f_getPinFromInstance( obj )
		else:
			return self

	def __set__(self, obj, value):
		raise AttributeError, 'cannot set pin'

	def __delete__(self, obj):
		raise AttributeError, 'cannot delete pin'


	def _f_getPinFromInstance(self, instance):
		return getattr( instance, self._attrName )


	@staticmethod
	def _p_computeAttrName(name):
		return '_pin_' + name





class SheetGraphSourceField (SheetGraphPinField):
	"""Node source field"""
	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		source = self._o_createSource( instance, self._pinName )
		instance.addSource( source )
		setattr( instance, self._attrName, source )


	def _o_createSource(self, instance, name):
		return SheetGraphNodeSource( instance, name )





class SheetGraphSinkField (SheetGraphPinField):
	"""Node sink field"""
	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		sink = self._o_createSink( instance, self._pinName )
		instance.addSink( sink )
		# Copy links from the source instance
		if srcInstance is not None:
			sink.copyLinksFrom( self._f_getPinFromInstance( srcInstance ) )
		setattr( instance, self._attrName, sink )


	def _o_createSink(self, instance, name):
		assert False, 'abstract'



	def __set__(self, obj, value):
		if obj is not None:
			sink = self._f_getPinFromInstance( obj )
			if isinstance( value, list )  or  isinstance( value, tuple ):
				sink._f_setContents( value )
			elif isinstance( value, SheetGraphNodeSource ):
				sink._f_setContents( [ value ] )
			else:
				raise ValueError, 'cannot set sink to %s'  %  ( value, )
		else:
			raise AttributeError, 'cannot set sink field'




class SheetGraphSinkSingleField (SheetGraphSinkField):
	"""Node single input sink field"""
	def _o_createSink(self, instance, name):
		return SheetGraphNodeSinkSingle( instance, name )


class SheetGraphSinkMultipleField (SheetGraphSinkField):
	"""Node multiple input sink field"""
	def _o_createSink(self, instance, name):
		return SheetGraphNodeSinkMultiple( instance, name )











class SheetGraphNodeClass (SheetClass):
	"""SheetGraphNode metaclass"""
	def __init__(cls, clsName, clsBases, clsDict):
		super( SheetGraphNodeClass, cls ).__init__( clsName, clsBases, clsDict )

		cls._p_processPins( clsName, clsBases, clsDict, SheetGraphSinkField, intern( '_SheetGraphNode_sinkFields' ) )
		cls._p_processPins( clsName, clsBases, clsDict, SheetGraphSourceField, intern( '_SheetGraphNode_sourceFields' ) )



	def getSinkFieldByName(cls, sinkName):
		return cls._SheetGraphNode_sinkFields[sinkName]

	def getSourceFieldByName(cls, sourceName):
		return cls._SheetGraphNode_sourceFields[sourceName]


	def _p_processPins(cls, clsName, clsBases, clsDict, pinClass, pinFieldDictAttrName):
		pinFields = cls._o_gatherDictFromBases( clsBases, pinFieldDictAttrName )

		for name, value in clsDict.items():
			if isinstance( value, pinClass ):
				pinFields[name] = value


		# Combine the gathered lists of fields with the lists from base classes
		setattr( cls, pinFieldDictAttrName, pinFields )





class SheetGraphNode (Sheet):
	"""Function graph node base class"""
	__metaclass__ = SheetGraphNodeClass
	trackerClass = SheetGraphCommandTracker.SheetGraphNodeCommandTracker
	description = 'Abstract function graph node'


	sinkChangedSignal = ClassSignal()


	def __init__(self, src=None):
		"""Constructor:
		@name - name of node
		@src - source object to copy"""
		self._sinks = []
		self._sources = []
		self._inputNodeCounts = {}
		self._inputNodes = SheetGraphItemList()
		self._outputNodeCounts = {}
		self._outputNodes = SheetGraphItemList()
		self._graph = None

		super( SheetGraphNode, self ).__init__( src )




	def addSink(self, sink):
		self._sinks.append( sink )
		sink._f_setNode( self )
		for source in sink:
			self._o_onAddInputSource( source )

	def removeSink(self, sink):
		for source in sink:
			self._o_onRemoveInputSource( source )
		self._sinks.remove( sink )
		sink._f_setNode( None )


	def addSource(self, source):
		self._sources.append( source )
		source._f_setNode( self )
		for sink in source:
			self._f_addOutputNode( sink.node )

	def removeSource(self, source):
		for sink in source:
			self._f_removeOutputNode( sink.node )
		self._sources.remove( source )
		source._f_setNode( None )


	def hasOutputs(self):
		return len( self._outputNodes ) > 0

	def hasInputNode(self, node):
		return node in self._inputNodes


	def _p_checkAddInputForCycles(self, node):
		if node is self:
			return True
		for inputNode in node._inputNodes:
			if self._p_checkAddInputForCycles( inputNode ):
				return True
		return False


	def _o_onAddInputSource(self, source):
		node = source.node
		try:
			self._inputNodeCounts[node] += 1
		except KeyError:
			self._inputNodeCounts[node] = 1
			self._inputNodes._p_append( node )

	def _o_onRemoveInputSource(self, source):
		node = source.node
		self._inputNodeCounts[node] -= 1
		count = self._inputNodeCounts[node]
		if count == 0:
			del self._inputNodeCounts[node]
			self._inputNodes._p_remove( node )


	def _f_addOutputNode(self, node):
		try:
			self._outputNodeCounts[node] += 1
		except KeyError:
			self._outputNodeCounts[node] = 1
			self._outputNodes._p_append( node )

	def _f_removeOutputNode(self, node):
		self._outputNodeCounts[node] -= 1
		count = self._outputNodeCounts[node]
		if count == 0:
			del self._outputNodeCounts[node]
			self._outputNodes._p_remove( node )


	def _f_clear(self):
		for source in self._sources:
			source._f_clearOutputs()

		assert len( self._outputNodes ) == 0

		for sink in self._sinks:
			sink.clear()

		assert len( self._inputNodes ) == 0


	def _f_sinkModified(self, sink):
		self.sinkChangedSignal.emit( self, sink )


	def __readxml__(self, xmlNode):
		super( SheetGraphNode, self ).__readxml__( xmlNode )

		self._f_clear()

		for sinkField in self._SheetGraphNode_sinkFields.values():
			sinkField._f_readInstancePinXml( self, xmlNode )

		for sourceField in self._SheetGraphNode_sourceFields.values():
			sourceField._f_readInstancePinXml( self, xmlNode )




	def __writexml__(self, xmlNode):
		super( SheetGraphNode, self ).__writexml__( xmlNode )

		for sinkField in self._SheetGraphNode_sinkFields.values():
			sinkField._f_writeInstancePinXml( self, xmlNode )

		for sourceField in self._SheetGraphNode_sourceFields.values():
			sourceField._f_writeInstancePinXml( self, xmlNode )



	def _p_getSinks(self):
		return self._sinks

	def _p_getSources(self):
		return self._sources

	def _p_getGraph(self):
		return self._graph

	def _p_getInputNodes(self):
		return self._inputNodes

	def _p_getOutputNodes(self):
		return self._outputNodes


	sinks = property( _p_getSinks )
	sources = property( _p_getSources )
	graph = property( _p_getGraph )
	inputNodes = property( _p_getInputNodes )
	outputNodes = property( _p_getOutputNodes )







class SheetGraph (object):
	"""Function graph - a function graph
	Contains the list of nodes, and the graph which controls their connections"""
	trackerClass = SheetGraphCommandTracker.SheetGraphCommandTracker


	class _NodeList (object):
	 	def __init__(self, functionGraph):
			super( SheetGraph._NodeList, self ).__init__()
			self._nodes = []
			self._functionGraph = functionGraph


		def __iter__(self):
			return iter( self._nodes )

		def __len__(self):
			return len( self._nodes )

		def __contains__(self, node):
			return node in self._nodes

		def __getitem__(self, index):
			return self._nodes[index]

		def __iadd__(self, nodes):
			self.extend( nodes )

		def index(self, node):
			return self._nodes.index( node )


		def append(self, node):
			assert node not in self._nodes, 'function graph node already in list'
			self._nodes.append( node )
			self._functionGraph._p_onNodeAppended( node )

		def extend(self, nodes):
			for node in nodes:
				self.append( node )

		def remove(self, node):
			assert node in self._nodes, 'function graph node not in list'
			self._functionGraph._p_onRemoveNode( node )
			self._nodes.remove( node )
			self._functionGraph._p_onNodeRemoved( node )


	nodeAddedSignal = ClassSignal()
	removeNodeSignal = ClassSignal()
	nodeRemovedSignal = ClassSignal()


	def __init__(self):
		super( SheetGraph, self ).__init__()
		self._nodes = SheetGraph._NodeList( self )

		self._commandTracker_ = None



	def __readxml__(self, xmlNode):
		self._p_clear()

		for n in xmlNode.getChild( 'nodes' ).childrenNamed( 'node' ):
			node = n.readObject()
			if node is not None  and  isinstance( node, SheetGraphNode ):
				self._nodes.append( node )


		for n in xmlNode.getChild( 'links' ).childrenNamed( 'link' ):
			sink = n.getChild( 'sink' ).readObject()
			source = n.getChild( 'source' ).readObject()
			if sink is not None  and  source is not None:
				sink.append( source )



	def __writexml__(self, xmlNode):
		nodeListNode = xmlNode.addChild( 'nodes' )
		for node in self._nodes:
			nodeListNode.addChild( 'node' ).writeObject( node )


		linkListNode = xmlNode.addChild( 'links' )
		for node in self._nodes:
			for sink in node.sinks:
				for source in sink:
					linkNode = linkListNode.addChild( 'link' )
					linkNode.addChild( 'sink' ).writeObject( sink )
					linkNode.addChild( 'source' ).writeObject( source )



	def _p_clear(self):
		while len( self._nodes ) > 0:
			self._nodes.remove( self._nodes[-1] )



	def _p_onNodeAppended(self, node):
		node._graph = self
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onFunctionGraphNodeAppended( self, node )
		self.nodeAddedSignal.emit( self, node )

	def _p_onRemoveNode(self, node):
		self.removeNodeSignal.emit( self, node )
		node._f_clear()

	def _p_onNodeRemoved(self, node):
		node._graph = None
		self.nodeRemovedSignal.emit( self, node )
		if self._commandTracker_ is not None:
			self._commandTracker_._f_onFunctionGraphNodeRemoved( self, node )



	def _getNodes(self):
		return self._nodes


	def _p_compareLinks(self, graph):
		# Get a @sinkNode from @self's node list
		for sinkNodeIndex, sinkNode in enumerate( self._nodes ):
			# Get @sink from @sinkNode's sink list
			for sinkIndex, sink in enumerate( sinkNode.sinks ):
				# Get @source from @sink's input list
				for sourceIndex, source in enumerate( sink ):
					# Get the source node
					sourceNode = source.node
					# Get the index of the source node
					sourceNodeIndex = self._nodes.index( sourceNode )
					# Get the index of the source in @sourceNode
					sourceIndex = sourceNode.sources.index( source )

					# Get the sink node from @graph
					graphSinkNode = graph._nodes[sinkNodeIndex]
					# Get the sink from @graphSinkNode
					graphSink = graphSinkNode.sinks[sinkIndex]

					# Get the source node from @graph
					graphSourceNode = graph._nodes[sourceNodeIndex]
					# Get the source from @graphSourceNode
					graphSource = graphSourceNode.sources[sourceIndex]

					# Check if @graphSink is linked to @graphSource
					if graphSource not in graphSink:
						return False

		return True



	def __eq__(self, graph):
		# Compare node list lengths
		if len( self._nodes )  !=  len( graph._nodes ):
			return False

		# Ensure that the node classes are identical
		for node, graphNode in zip( self._nodes, graph._nodes ):
			if node.__class__ is not graphNode.__class__:
				return False

		# Compare the graph links
		return self._p_compareLinks( graph )  and  graph._p_compareLinks( self )


	nodes = property( _getNodes, doc='List of nodes' )


IOXml.ioObjectFactoryRegister( 'SheetGraph', SheetGraph )

