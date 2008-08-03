##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from collections import defaultdict
import copy

from Britefury.DocPresent.Web.WDDomEdit import WDDomEdit



class WebViewContext (object):
	def __init__(self, owner, page):
		self.owner = owner
		self._idTable = defaultdict( int )
		self._onReadyScriptQueue = []
		self.page = page
		self._nodesToRefresh = set()
		self._bIgnoreDequeueRequests = False
	
	
	def allocID(self, prefix):
		x = self._idTable[prefix]
		self._idTable[prefix] += 1
		return prefix + str( x )
	
	
	def runScriptOnReady(self, script):
		self._onReadyScriptQueue.append( script )
		
	def runScript(self, script):
		self._scriptQueue.append( script )
		
		
	def getOnReadyScript(self):
		return '\n'.join( self._onReadyScriptQueue )
	
	

	def _queueNodeRefresh(self, node):
		self._nodesToRefresh.add( node )
		
	def _dequeueNodeRefresh(self, node):
		if not self._bIgnoreDequeueRequests:
			try:
				self._nodesToRefresh.remove( node )
			except KeyError:
				pass
		
		
	def registerEventHandler(self, sourceID, handler):
		self.page.registerEventHandler( sourceID, handler )
		
	def unregisterEventHandler(self, sourceID):
		self.page.unregisterEventHandler( sourceID )

		
	
	def refreshNodes(self):
		self._bIgnoreDequeueRequests = True
		
		nodeIDToResolvedData = {}
		
		# Filter out the disable nodes
		nodesToRefresh = set( [ node   for node in self._nodesToRefresh   if node.bEnabled ] )
		refreshedNodes = copy.copy( nodesToRefresh )
		
		while len( nodesToRefresh ) > 0:
			# Get a node from the set of nodes to refresh
			node = nodesToRefresh.pop()
			
			# Generate the resolved HTML, reference nodes, and place holder IDs for the DOM subtree rooted at @node
			resolvedHtml, resolvedRefNodes, resolvedPlaceHolderIDs = node.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
			
			# Remove the nodes from @self._nodesToRefresh which are in @resolvedRefNodes, as their content is included in the resolved content
			self._nodesToRefresh.difference_update( resolvedRefNodes )
			
			
		# The table @nodeIDToResolvedData will map node ID to resolved data
		# Each entry will map the node ID to the data for the subtree rooted at that node.
		# This will result in the minimum number of DOM modifications to bring the client DOM up to date.
		for nodeID, ( resolvedHtml, resolvedRefNodes, resolvedPlaceHolderIDs )  in  nodeIDToResolvedData.items():
			# Ensure that all nodes that were visited by this operation are in @refreshedNodes
			refreshedNodes = refreshedNodes.union( resolvedRefNodes )
			# Create the DOM edit operation
			domEdit = WDDomEdit( nodeID, resolvedHtml, list( resolvedPlaceHolderIDs ) )
			# Queue it to be sent to the client
			self.page.queueObject( domEdit )
			
		# Inform all nodes that they are now up to date on the client
		for node in refreshedNodes:
			node.onClient()
			
		self._bIgnoreDequeueRequests = False
	

		

		
import unittest

class TestCase_WebViewContext (unittest.TestCase):
	def test_allocID(self):
		vctx = WebViewContext( None )
		self.assert_( vctx.allocID( 'a' )  ==  'a0' )
		self.assert_( vctx.allocID( 'a' )  ==  'a1' )
		self.assert_( vctx.allocID( 'b' )  ==  'b0' )
		self.assert_( vctx.allocID( 'b' )  ==  'b1' )
		self.assert_( vctx.allocID( 'a' )  ==  'a2' )
		
		
	def test_onReadyScript(self):
		vctx = WebViewContext( None )
		vctx.runScriptOnReady( 'test()\n' )
		vctx.runScriptOnReady( 'test()\na()\nb()\n' )
		self.assert_( vctx.getOnReadyScript()  ==  'test()\n\ntest()\na()\nb()\n' )
		
		
		
	def test_refreshNodes(self):
		from Britefury.DocPresent.Web.WDNode import WDNode
		from Britefury.DocPresent.Web.Page import Page
		
		page = Page( 'test' )
		ctx = WebViewContext( page )
		n1 = WDNode( None, ctx, lambda nodeContext: 'hi' )
		n2 = WDNode( None, ctx, lambda nodeContext: 'a' + n1.reference() + 'b' )
		n3 = WDNode( None, ctx, lambda nodeContext: 'there' )
		n4 = WDNode( None, ctx, lambda nodeContext: 'c' + n2.reference() + 'd' + n3.reference() + 'e' )
		n5 = WDNode( None, ctx, lambda nodeContext: 'test' )
		
		def getNodeIDSetFromQueue():
			return set( [ op.nodeID   for op in page._objectQueue ] )
		
		ctx.refreshNodes()
		
		self.assert_( getNodeIDSetFromQueue()  ==  set( [ n4.getID(), n5.getID() ] ) )
		
		page._sendObjectsAsJSon()
		self.assert_( len( page._objectQueue )  ==  0 )
		
		n5.disable()
		ctx.refreshNodes()
		self.assert_( getNodeIDSetFromQueue()  ==  set( [ n4.getID() ] ) )
		
		page._sendObjectsAsJSon()
		self.assert_( len( page._objectQueue )  ==  0 )
		
		n5.enable()
		n4.disable()
		ctx.refreshNodes()
		self.assert_( getNodeIDSetFromQueue()  ==  set( [ n5.getID() ] ) )
		
		
		
		
		