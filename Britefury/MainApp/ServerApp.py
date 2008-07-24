<<<<<<< .mine
##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os

import cherrypy

from Britefury.extlibs.json import json


from Britefury.DocPresent.Web.DPView import DPView


pageTemplate = """
<html>
<head>
<script type="text/javascript" src="resources/json2.js"></script>
<script type="text/javascript" src="resources/jquery_1.2.6.js"></script>
%s
<title>%s</title>
</head>

<body>
%s
</body>

</html>

"""

class ServerApp (object):
	def __init__(self):
		self._view = DPView( 'gSym test' )
	
	@cherrypy.expose
	def index(self):
		scripts = self._view.scripts()
		title = self._view.title()
		body = self._view.htmlBody()
		
		html = pageTemplate  %  ( scripts,   title,   body )
		
		return html
	
	@cherrypy.expose
	def eventexchange(self):
		params = cherrypy.request.params
		try:
			events = params['events']
		except KeyError:
			return ''
		else:
			jsData = json.read( events )
			self._view.receiveEventsAsJSon()

		jsData = self._view.sendEventsAsJSon()
		return json.write( jsData )




class Resources (object):
	@cherrypy.expose
	def json2_js(self):
		return open( os.path.join( 'resources', 'json2.js' ), 'r' ).read()

	@cherrypy.expose
	def jquery_1_2_6_js(self):
		return open( os.path.join( 'resources', 'jquery-1.2.6.js' ), 'r' ).read()

	
	
	
	

app = ServerApp()
app.resources = Resources()


def startServer():    
	cherrypy.quickstart( app, '/', 'serverconfig.cfg' )
