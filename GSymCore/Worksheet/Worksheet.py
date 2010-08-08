##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymUnitClass import GSymUnitClass, GSymPageUnitFactory
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.Worksheet.WorksheetViewer.View import perspective as worksheetViewerPerspective, WorksheetViewerSubject
from GSymCore.Worksheet import Schema


def newWorksheet():
	return Schema.Worksheet( body=Schema.Body( contents=[] ) )

def _worksheetNewUnit():
	return gSymUnit( Schema.schema, newWorksheet() )


unitClass = GSymUnitClass( Schema.schema, WorksheetViewerSubject )


newPageFactory = GSymPageUnitFactory( 'Worksheet', _worksheetNewUnit )

