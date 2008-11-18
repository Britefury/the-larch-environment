##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguage
from Britefury.gSym.View.gSymView import GSymViewFactory


from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25.View import Python25View


def pyTransformModify(cur, new):
	cur[1:] = new[1:]


	
viewFac = GSymViewFactory( Python25View )


language = GSymLanguage()
language.registerCodeGeneratorFactory( 'ascii', Python25CodeGenerator )
language.registerViewFactory( viewFac )
language.registerTransformModifyFn( pyTransformModify )
