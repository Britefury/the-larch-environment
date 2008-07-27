##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from collections import defaultdict

from Britefury.DocPresent.Web.WDDomEdit import WDDomEdit



class WebViewContext (object):
	def __init__(self, page):
		self._bInitialised = False
		self._idTable = defaultdict( int )
		self._onReadyScriptQueue = []
		self._operationStack = []
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
	
	
	def pushOperation(self, op):
		self._operationStack.append( op )
		
	def popOperation(self):
		return self._operationStack.pop()
	
	def topOperation(self):
		try:
			return self._operationStack[-1]
		except IndexError:
			return None
		
		
		
	def _queueNodeRefresh(self, node):
		self._nodesToRefresh.add( node )
		
	def _dequeueNodeRefresh(self, node):
		if not self._bIgnoreDequeueRequests:
			try:
				self._nodesToRefresh.remove( node )
			except KeyError:
				pass
		
		
	def refreshNodes(self):
		self._bIgnoreDequeueRequests = True
		
		nodeIDToResolvedData = {}
		
		# Take a copy of the list of nodes to refresh
		refreshedNodes = list( self._nodesToRefresh )
		
		while len( self._nodesToRefresh ) > 0:
			# Get a node from the set of nodes to refresh
			node = self._nodesToRefresh.pop()
			
			# Generate the resolved HTML, reference nodes, and place holder IDs for the DOM subtree rooted at @node
			resolvedHtml, resolvedRefNodes, resolvedPlaceHolderIDs = node.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
			
			# Remove the nodes from @self._nodesToRefresh which are in @resolvedRefNodes, as their content is included in the resolved content
			self._nodesToRefresh.difference_update( resolvedRefNodes )
			
			
		# The table @nodeIDToResolvedData will map node ID to resolved data
		# Each entry will map the node ID to the data for the subtree rooted at that node.
		# This will result in the minimum number of DOM modifications to bring the client DOM up to date.
		for nodeID, ( resolvedHtml, resolvedRefNodes, resolvedPlaceHolderIDs )  in  nodeIDToResolvedData.items():
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
		
		
		
		