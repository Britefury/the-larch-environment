##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.MainApp import ServerApp
from Britefury.Web.Page import Page
from Britefury.Web.SharedObject import SharedObject, JSClassNamedMethod, JSMethod
from Britefury.Web.WDNode import WDNode
from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
from Britefury.DocPresent.Web.Context.WebViewNodeContext import WebViewNodeContext




class ClickedMessage (SharedObject):
	def __init__(self, x):
		self.x = x
	__init__.jsFunction =\
"""function (x)
{
	this.x = x;
}
"""
		
	
		
	def jsonContent(self):
		return [ self.x ]
	jsonContent.jsFunction =\
"""function ()
{
	return [ this.x ];
}
"""
	
	@classmethod
	def fromJSonContent(cls, content):
		return ClickedMessage( content[0] )
	
	
	fromJSonContent_js = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	return new ClickedMessage( content[0] );
}
""" )


_rootFn1 = None
_rootFn2 = None
_bRootFn1 = True
	
def _dom(page):
	global _rootFn1, _rootFn2
	viewContext = WebViewContext( page )
	nodeContext = WebViewNodeContext( viewContext )
	
	_rootFn1 = lambda ctx: '<span class="power">' + a.html( ctx ) + ' <span class="operator">*</span> ' + b.html( ctx ) + '</span>'
	_rootFn2 = lambda ctx: '<span class="power">' + a.html( ctx ) + '<sup>' + b.html( ctx ) + '</sup>' + '</span>'

	root = WDNode( viewContext, _rootFn1 )
	a = WDNode( viewContext, lambda ctx: 'a <span class="operator">+</span> b' )
	b = WDNode( viewContext, lambda ctx: 'c <span class="operator">*</span> d' )
	
	return root, viewContext, nodeContext

	

class TestPage (Page):
	def __init__(self, title):
		super( TestPage, self ).__init__( title )
		self._root, self._viewContext, self._nodeContext = _dom( self )
		
		
	def _styles(self):
		return '.operator { background-color:#f0f0ff; }'
		
		
	def _readyScripts(self):
		return \
"""
$(".clickme").click(
	function (event)
	{
		sendObject( new ClickedMessage( "A" ) );
	}
);
"""

	
	def _htmlBody(self):
		body = \
"""
<h1>gSym DOM Edit test</h1>
""" + self._root.html( self._viewContext ) + \
"""
<br>
<br>
<span class="clickme">Click me</span>
"""
		return body
	
	
	
	
	def handleIncomingObject(self, obj):
		global _bRootFn1
		if isinstance( obj, ClickedMessage ):
			if _bRootFn1:
				self._root.setHtmlFn( _rootFn2 )
				_bRootFn1 = False
			else:
				self._root.setHtmlFn( _rootFn1 )
				_bRootFn1 = True
			self._viewContext.refreshNodes()



if __name__ == '__main__':
	app = ServerApp.ServerApp( lambda: TestPage( 'DOM Edit test' ) )
	ServerApp.startServer( app )
