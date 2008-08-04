##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import cherrypy



class ServerInterface (object):
	def page(self):
		return 'Hello world'
	
	def io(self):
		return 'IO'
	
	page.exposed = True
	io.exposed = True
	
	
	
def runServer(serverInterface):
	cherrypy.quickstart( serverInterface )
	
	

if __name__ == '__main__':
	runServer( ServerInterface() )
	
	

	
	
	
