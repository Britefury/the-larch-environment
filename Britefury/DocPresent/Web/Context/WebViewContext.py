##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from collections import defaultdict

from Britefury.Web.WDDomEdit import WDDomEdit



class WebViewContext (object):
	def __init__(self, page):
		self._bInitialised = False
		self._idTable = defaultdict( int )
		self._onReadyScriptQueue = []
		self._operationStack = []
		self.page = page
		self._nodesToRefresh = set()
	
	
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
		try:
			self._nodesToRefresh.remove( node )
		except KeyError:
			pass
		
		
	def refreshNodes(self):
		while len( self._nodesToRefresh ) > 0:
			node = self._nodesToRefresh.pop()
			domEdit = WDDomEdit()
			self.pushOperation( domEdit )
			domEdit.html = node.html( self )
			domEdit.nodeID = node.getID()
			self.popOperation()
			self.page.queueObject( domEdit )
			
			
	

		

		
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
		
		
		
		