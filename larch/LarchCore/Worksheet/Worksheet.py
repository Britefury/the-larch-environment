##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from LarchCore.Worksheet.WorksheetViewer.View import perspective as worksheetViewerPerspective, WorksheetViewerSubject
from LarchCore.Worksheet import Schema
from LarchCore.Worksheet.TextExporter import WorksheetTextExporter
from LarchCore.Worksheet.source_extractor import WorksheetSourceExtractor

from LarchCore.Project.PageData import PageData, registerPageFactory



def newWorksheet():
	return Schema.Worksheet( body=Schema.Body( contents=[] ) )



class WorksheetPageData (PageData):
	def makeEmptyContents(self):
		return newWorksheet()

	def get_source_code(self):
		src_extractor = WorksheetSourceExtractor()
		source = []
		src_extractor(self.contents, source)
		return source

	def exportAsString(self, filename):
		exporter = WorksheetTextExporter( filename )
		return exporter( self.contents )

	def __new_subject__(self, document, enclosingSubject, path, importName, title):
		return WorksheetViewerSubject( document, self.contents, enclosingSubject, path, importName, title )

	
registerPageFactory( 'Worksheet', WorksheetPageData, 'Worksheet' )

