##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.MainApp import ServerApp
from Britefury.DocPresent.Web.DPView import DPView
from Britefury.DocPresent.Web.DPEvent import DPEvent




class TestSO (SharedObject):
	def __init__(self, text):
		self.text = text
	
	__init__.js = \
"""
function (self, text)
{
	self.text = text;
}
"""


class TestEvent (DPEvent):
	def __init__(self, text):
		self.text = text
	
	
	
	def jsonContent(self):
		return [ self.text ]
	
	
	
	@classmethod
	def fromJSonContent(cls, content):
		return TestEvent( content[0] )



	

class TestView (DPView):
	def scripts(self):
		return ''


	def readyScripts(self):
		return \
"""
$(".clickme").click(
	function (event)
	{
		var eventToServer = JSON.stringify( [ [ "TestEvent", [ "From the clicked button" ] ] ] );
		var x = jQuery.post( "eventexchange", { events : eventToServer }, function (events)
			{
				events = JSON.parse( events );
				var event0 = events[0];
				var eventText = event0[0]  +  ":"  +  event0[1][0];
				$(".test").html( eventText );
			},
			"JSON"
		);
	}
);
"""

	
	def title(self):
		return self._title


	def htmlBody(self):
		body = \
"""
<h1>gSym Event test</h1>
<span class="clickme">Click me</span>
<br>
<span class="test">Test</span>
"""
		return body
	
	
	
	
	def dispatchIncomingEvent(self, event):
		if isinstance( event, TestEvent ):
			newEvent = TestEvent( "From the server!" )
			self.queueEvent( newEvent )



if __name__ == '__main__':
	app = ServerApp.ServerApp( TestView )
	ServerApp.startServer( app )
