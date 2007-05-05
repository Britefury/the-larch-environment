##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CommandHistory import CommandHistory
from Britefury.CommandHistory import CommandTracker

from Britefury.Sheet.SheetCommandTracker import SheetCommandTracker
import SheetGraph







class SheetGraphNodeSinkAppendCommand (CommandHistory.Command):
	def __init__(self, sink, source):
		super( SheetGraphNodeSinkAppendCommand, self ).__init__()
		self._sink = sink
		self._source = source


	def execute(self):
		self._sink.append( self._source )

	def unexecute(self):
		self._sink.remove( self._source )




class SheetGraphNodeSinkExtendCommand (CommandHistory.Command):
	def __init__(self, sink, sources):
		super( SheetGraphNodeSinkExtendCommand, self ).__init__()
		self._sink = sink
		self._sources = sources
		self._numSources = len( sources )


	def execute(self):
		self._sink.extend( self._sources )

	def unexecute(self):
		del self._sink[-self._numSources:0]




class SheetGraphNodeSinkInsertCommand (CommandHistory.Command):
	def __init__(self, sink, index, source):
		super( SheetGraphNodeSinkInsertCommand, self ).__init__()
		self._sink = sink
		self._index = index
		self._source = source


	def execute(self):
		self._sink.insert( self._index, self._source )

	def unexecute(self):
		self._sink.remove( self._source )




class SheetGraphNodeSinkRemoveCommand (CommandHistory.Command):
	def __init__(self, sink, source):
		super( SheetGraphNodeSinkRemoveCommand, self ).__init__()
		self._sink = sink
		self._source = source
		self._index = sink.index( source )


	def execute(self):
		self._sink.remove( self._source )

	def unexecute(self):
		self._sink.insert( self._index, self._source )




class SheetGraphNodeSinkClearCommand (CommandHistory.Command):
	def __init__(self, sink):
		super( SheetGraphNodeSinkClearCommand, self ).__init__()
		self._sink = sink
		self._sources = sink[:]


	def execute(self):
		self._sink.clear()

	def unexecute(self):
		self._sink.extend( self._sources )




class SheetGraphNodeSinkSetCommand (CommandHistory.Command):
	def __init__(self, sink, oldContents, contents):
		super( SheetGraphNodeSinkSetCommand, self ).__init__()
		self._sink = sink
		self._oldContents = oldContents
		self._contents = contents


	def execute(self):
		self._sink._f_setContents( self._contents )

	def unexecute(self):
		self._sink._f_setContents( self._oldContents )




class SheetGraphNodeSinkCommandTracker (CommandTracker.CommandTracker):
	def track(self, sink):
		super( SheetGraphNodeSinkCommandTracker, self ).track( sink )
		assert isinstance( sink, SheetGraph.SheetGraphNodeSink )


	def stopTracking(self, sink):
		assert isinstance( sink, SheetGraph.SheetGraphNodeSink )
		super( SheetGraphNodeSinkCommandTracker, self ).stopTracking( sink )



	def _f_onSinkAppended(self, sink, source):
		self._commandHistory.addCommand( SheetGraphNodeSinkAppendCommand( sink, source ) )


	def _f_onSinkExtended(self, sink, sources):
		self._commandHistory.addCommand( SheetGraphNodeSinkExtendCommand( sink, sources ) )


	def _f_onSinkInserted(self, sink, index, source):
		self._commandHistory.addCommand( SheetGraphNodeSinkInsertCommand( sink, index, source ) )


	def _f_onSinkRemoved(self, sink, source):
		self._commandHistory.addCommand( SheetGraphNodeSinkRemoveCommand( sink, source ) )


	def _f_onSinkCleared(self, sink):
		self._commandHistory.addCommand( SheetGraphNodeSinkClearCommand( sink ) )


	def _f_onSinkSet(self, sink, oldContents, contents):
		self._commandHistory.addCommand( SheetGraphNodeSinkSetCommand( sink, oldContents, contents ) )






class SheetGraphAddNodeCommand (CommandHistory.Command):
	def __init__(self, functionGraph, node):
		super( SheetGraphAddNodeCommand, self ).__init__()
		self._functionGraph = functionGraph
		self._node = node


	def execute(self):
		self._functionGraph.nodes.append( self._node )

	def unexecute(self):
		self._functionGraph.nodes.remove( self._node )




class SheetGraphRemoveNodeCommand (CommandHistory.Command):
	def __init__(self, functionGraph, node):
		super( SheetGraphRemoveNodeCommand, self ).__init__()
		self._functionGraph = functionGraph
		self._node = node



	def execute(self):
		self._functionGraph.nodes.remove( self._node )

	def unexecute(self):
		self._functionGraph.nodes.append( self._node )




class SheetGraphNodeCommandTracker (SheetCommandTracker):
	def track(self, node):
		super( SheetGraphNodeCommandTracker, self ).track( node )
		assert isinstance( node, SheetGraph.SheetGraphNode )
		for sink in node.sinks:
			self._commandHistory.track( sink )


	def stopTracking(self, node):
		for sink in node.sinks:
			self._commandHistory.stopTracking( sink )
		assert isinstance( node, SheetGraph.SheetGraphNode )
		super( SheetGraphNodeCommandTracker, self ).stopTracking( node )





class SheetGraphCommandTracker (CommandTracker.CommandTracker):
	def track(self, functionGraph):
		super( SheetGraphCommandTracker, self ).track( functionGraph )
		assert isinstance( functionGraph, SheetGraph.SheetGraph )

		#self._commandHistory.track( functionGraph.graph )

		for node in functionGraph.nodes:
			self._commandHistory.track( node )


	def stopTracking(self, functionGraph):
		assert isinstance( functionGraph, SheetGraph.SheetGraph )

		#self._commandHistory.stopTracking( functionGraph.graph )

		for node in functionGraph.nodes:
			self._commandHistory.stopTracking( node )

		super( SheetGraphCommandTracker, self ).stopTracking( functionGraph )



	def _f_onFunctionGraphNodeAppended(self, functionGraph, node):
		for sink in node.sinks:
			self._commandHistory.track( sink )
		self._commandHistory.track( node )
		self._commandHistory.addCommand( SheetGraphAddNodeCommand( functionGraph, node ) )


	def _f_onFunctionGraphNodeRemoved(self, functionGraph, node):
		self._commandHistory.addCommand( SheetGraphRemoveNodeCommand( functionGraph, node ) )
		self._commandHistory.stopTracking( node )
		for sink in node.sinks:
			self._commandHistory.stopTracking( sink )





