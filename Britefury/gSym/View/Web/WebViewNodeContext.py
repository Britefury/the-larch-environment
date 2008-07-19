##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.View.Web.WebViewContext import WebViewContext


class WebViewNodeContext (object):
	def __init__(self, viewContext):
		self.viewContext = viewContext
		self.idsInNode = set()
		
		
	def allocIDForNodeContent(self, prefix):
		x = self.viewContext.allocID( prefix )
		self.idsInNode.add( x )
		return x
	
	

	
	
import unittest

class TestCase_WebViewNodeContext (unittest.TestCase):
	def test_allocID(self):
		nctx = WebViewNodeContext( WebViewContext() )
		self.assert_( nctx.allocIDForNodeContent( 'a' )  ==  'a0' )
		self.assert_( nctx.allocIDForNodeContent( 'a' )  ==  'a1' )
		self.assert_( nctx.allocIDForNodeContent( 'b' )  ==  'b0' )
		self.assert_( nctx.allocIDForNodeContent( 'b' )  ==  'b1' )
		self.assert_( nctx.allocIDForNodeContent( 'a' )  ==  'a2' )
		
		self.assert_( nctx.idsInNode  ==  set( [ 'a0', 'a1', 'a2', 'b0', 'b1' ] ) )
		
		
		
		