##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymUnitClass import GSymUnitClass, GSymDocumentFactory
from Britefury.gSym.gSymDocument import gSymUnit, GSymDocument

from GSymCore.Project.ProjectEditor.View import perspective as projectEditorPerspective
from GSymCore.Project.ProjectEditor.Subject import ProjectSubject
from GSymCore.Project import Schema


def newProject():
	project = Schema.Project( contents=[] )
	return project

def _newProjectUnit():
	return gSymUnit( Schema.schema, newProject() )

def _newProjectDocment(world):
	return GSymDocument( world, _newProjectUnit() )


unitClass = GSymUnitClass( Schema.schema, ProjectSubject )


newDocumentFactory = GSymDocumentFactory( 'gSym Document', _newProjectDocment )

