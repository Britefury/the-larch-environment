##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.MainApp import ServerApp
from Britefury.DocPresent.Web.Page import Page
from Britefury.DocPresent.Web.SharedObject import SharedObject, JSClassNamedMethod, JSMethod
from Britefury.DocPresent.Web.WDNode import WDNode
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


_aFn1 = None
_aFn2 = None
_aFnIndex = 0
	
def _dom(page):
	global _aFn1, _aFn2
	viewContext = WebViewContext( page )
	
	_aFn1 = lambda ctx: '<span class="power">' + a.reference() + ' <span class="operator">*</span> ' + b.reference() + '</span>'
	_aFn2 = lambda ctx: '<span class="power">' + a.reference() + '<sup>' + b.reference() + '</sup>' + '</span>'

	root = WDNode( viewContext, _aFn1 )
	a = WDNode( viewContext, lambda ctx: 'a <span class="operator">+</span> b' )
	b = WDNode( viewContext, lambda ctx: 'c <span class="operator">*</span> d' )
	
	return root, viewContext

	

class TestPage (Page):
	def __init__(self, title):
		super( TestPage, self ).__init__( title )
		self._root, self._viewContext = _dom( self )
		
		
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
		html, resolvedRefNodes, placeHolderIDs = self._root.resolvedSubtreeHtmlForClient()
		body = \
"""
<h1>gSym DOM Edit test</h1>
""" + html + \
"""
<br>
<br>
<span class="clickme">Click me</span>
"""
		return body
	
	
	
	
	def handleIncomingObject(self, obj):
		global _aFnIndex
		if isinstance( obj, ClickedMessage ):
			if _aFnIndex == 0:
				self._root.setHtmlFn( _aFn2 )
				_aFnIndex = 1
			else:
				self._root.setHtmlFn( _aFn1 )
				_aFnIndex = 0
			self._viewContext.refreshNodes()



if __name__ == '__main__':
	app = ServerApp.ServerApp( lambda: TestPage( 'DOM Edit test' ) )
	ServerApp.startServer( app )
