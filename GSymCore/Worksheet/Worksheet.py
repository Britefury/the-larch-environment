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

from GSymCore.Project2.PageData import PageData, registerPageFactory, registerPageImporter



def newWorksheet():
	return Schema.Worksheet( body=Schema.Body( contents=[] ) )

def _worksheetNewUnit():
	return gSymUnit( Schema.schema, newWorksheet() )



class WorksheetPageData (PageData):
	def makeEmptyContents(self):
		return newWorksheet()
	
	def __new_subject__(self, document, enclosingSubject, location, title):
		return WorksheetViewerSubject( document, self.contents, enclosingSubject, location, title )

	
registerPageFactory( 'Worksheet', WorksheetPageData, 'Worksheet' )




unitClass = GSymUnitClass( Schema.schema, WorksheetViewerSubject )


pageUnitFactory = GSymPageUnitFactory( 'Worksheet', _worksheetNewUnit )

