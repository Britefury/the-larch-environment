##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguage, GSymDocumentFactory
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.Project.View import viewProjectLocationAsElement, getDocNodeForProjectLocation
from GSymCore.Project import NodeClasses as Nodes


def newProject():
	package = Nodes.Package( name='Root', contents=[] )
	project = Nodes.Project( rootPackage=package )
	return gSymUnit( 'GSymCore.Project', project )


def initialiseModule(world):
	world.registerDMModule( Nodes.module )



language = GSymLanguage()
language.registerViewLocationAsElementFn( viewProjectLocationAsElement )
language.registerGetDocNodeForLocationFn( getDocNodeForProjectLocation )


newDocumentFactory = GSymDocumentFactory( 'gSym Document', newProject )

