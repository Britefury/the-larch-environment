##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMModule, DMObjectClass




module = DMModule( 'GSymApp', 'app', 'GSymCore.GSymApp.GSymApp' )


AppState = module.newClass( 'AppState', [ 'openDocuments', 'configuration' ] )

AppDocument = module.newClass( 'AppDocument', [ 'name', 'location' ] )

AppConfiguration = module.newClass( 'AppConfiguration', [] )

