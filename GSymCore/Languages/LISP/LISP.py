##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguage
from Britefury.gSym.View.gSymView import _NodeElementChangeListener

from BritefuryJ.GSym.View import PyGSymViewFactory

from GSymCore.Languages.LISP.View import LISPView


def pyTransformModify(cur, new):
	cur[1:] = new[1:]


	
viewFac = PyGSymViewFactory( LISPView, _NodeElementChangeListener )


language = GSymLanguage()
language.registerViewFactory( viewFac )
language.registerTransformModifyFn( pyTransformModify )
