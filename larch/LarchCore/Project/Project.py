##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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

