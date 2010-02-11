##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema

from Britefury.gSym.gSymDocumentClass import GSymDocumentClass

from GSymCore.Languages.LISP.View import viewLISPDocNodeAsElement, viewLISPDocNodeAsPage, resolveLISPLocation


schema = DMSchema( 'lisp', 'lisp', 'GSymCore.Languages.LISP' )


documentClass = GSymDocumentClass( schema )
documentClass.registerViewDocNodeAsElementFn( viewLISPDocNodeAsElement )
documentClass.registerViewDocNodeAsPageFn( viewLISPDocNodeAsPage )
documentClass.registerResolveLocationFn( resolveLISPLocation )


