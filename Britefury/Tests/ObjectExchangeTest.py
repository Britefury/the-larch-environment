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




class TestSO (SharedObject):
	def __init__(self, text):
		self.text = text
	__init__.jsFunction =\
"""function (text)
{
	this.text = text;
}
"""
		
	
		
	def jsonContent(self):
		return [ self.text ]
	jsonContent.jsFunction =\
"""function ()
{
	return [ this.text ];
}
"""
	
	@classmethod
	def fromJSonContent(cls, content):
		return TestSO( content[0] )
	
	
	fromJSonContent_js = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	return new TestSO( content[0] );
}
""" )


	__js__handle = JSMethod( 'handle', \
"""function ()
{
	$(".test").html( this.text );
}
""" )
	
	
	

	

class TestPage (Page):
	def _readyScripts(self):
		return \
"""
$(".clickme").click(
	function (event)
	{
		sendObject( new TestSO( "From the clicked button" ) );
	}
);
"""

	
	def _htmlBody(self):
		body = \
"""
<h1>gSym Event test</h1>
<span class="clickme">Click me</span>
<br>
<span class="test">Test</span>
"""
		return body
	
	
	
	
	def handleIncomingObject(self, obj):
		if isinstance( obj, TestSO ):
			newObj = TestSO( "From the server!" )
			self.queueObject( newObj )



if __name__ == '__main__':
	app = ServerApp.ServerApp( lambda: TestPage( 'gSymTest' ) )
	ServerApp.startServer( app )
