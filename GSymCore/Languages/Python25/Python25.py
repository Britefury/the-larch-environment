##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguage

from BritefuryJ.GSym.View import PyGSymViewFactory

from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25.View import Python25View, initialiseViewContext
from GSymCore.Languages.Python25 import NodeClasses as Nodes



def pyTransformModify(cur, new):
	cur['contents'] = new['contents']



viewFac = PyGSymViewFactory( Python25View, initialiseViewContext )



def initialiseModule(world):
	world.registerDMModule( Nodes.module )



language = GSymLanguage()
language.registerCodeGeneratorFactory( 'ascii', Python25CodeGenerator )
language.registerViewFactory( viewFac )
language.registerTransformModifyFn( pyTransformModify )



