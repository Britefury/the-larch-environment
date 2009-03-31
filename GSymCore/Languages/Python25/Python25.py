##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguage
from Britefury.gSym.View.NodeElementChangeListener import _NodeElementChangeListener

from BritefuryJ.GSym.View import PyGSymViewFactory

from GSymCore.Languages.Python25.CodeGenerator2 import Python25CodeGenerator
from GSymCore.Languages.Python25.View2 import Python25View


def pyTransformModify(cur, new):
	cur[1:] = new[1:]


	
viewFac = PyGSymViewFactory( Python25View, _NodeElementChangeListener )


language = GSymLanguage()
language.registerCodeGeneratorFactory( 'ascii', Python25CodeGenerator )
language.registerViewFactory( viewFac )
language.registerTransformModifyFn( pyTransformModify )
