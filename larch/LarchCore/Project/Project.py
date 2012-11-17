##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from Britefury.Kernel.Document import Document
from Britefury.Kernel.World import _DocumentFactory

# Fix cyclic imports:
import LarchCore.Project.ProjectEditor.View
import LarchCore.Project.ProjectEditor.Subject

from LarchCore.Project.ProjectRoot import ProjectRoot


	


def newProject():
	return ProjectRoot()

def _newProjectDocment(world):
	return Document( world, newProject() )


newDocumentFactory = _DocumentFactory( 'Larch Document', _newProjectDocment )

