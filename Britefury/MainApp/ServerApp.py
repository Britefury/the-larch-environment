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


from Britefury.DocPresent.Web.Page import Page



class ServerApp (object):
	def __init__(self, pageFactory):
		self._page = pageFactory()
	
	@cherrypy.expose
	def index(self):
		return self._page.html()
	
	@cherrypy.expose
	def objectexchange(self, objs):
		return self._page.doServerObjectExchange( objs )




class Resources (object):
	@cherrypy.expose
	def json2_js(self):
		return open( os.path.join( 'resources', 'json2.js' ), 'r' ).read()

	@cherrypy.expose
	def jquery_1_2_6_js(self):
		return open( os.path.join( 'resources', 'jquery-1.2.6.js' ), 'r' ).read()

	@cherrypy.expose
	def highlight_js(self):
		return open( os.path.join( 'resources', 'highlight.js' ), 'r' ).read()

	
	
	
	

def startServer(app=None):
	if app is None:
		app = ServerApp()

	app.resources = Resources()

	cherrypy.quickstart( app, '/', 'serverconfig.cfg' )
