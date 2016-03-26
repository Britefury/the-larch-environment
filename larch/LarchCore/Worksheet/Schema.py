##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass





schema = DMSchema( 'Worksheet', 'ws', 'LarchCore.Worksheet', 3 )



WorksheetNode = schema.newClass( 'WorksheetNode', [] )


Worksheet = schema.newClass( 'Worksheet', WorksheetNode, [ 'body' ] )

Body = schema.newClass( 'Body', WorksheetNode, [ 'contents' ] )
Text = schema.newClass( 'Text', WorksheetNode, [ 'text' ] )
Paragraph = schema.newClass( 'Paragraph', Text, [ 'style' ] )
TextSpan = schema.newClass( 'TextSpan', Text, [ 'styleAttrs' ] )

Link = schema.newClass( 'Link', WorksheetNode, [ 'text', 'path' ] )

StyleAttr = schema.newClass( 'StyleAttr', WorksheetNode, [ 'name', 'value' ] )


PythonCode = schema.newClass( 'PythonCode', WorksheetNode, [ 'code', 'style' ] )
InlinePythonCode = schema.newClass( 'InlinePythonCode', WorksheetNode, [ 'expr', 'style' ] )


InlineEmbeddedObject = schema.newClass( 'InlineEmbeddedObject', WorksheetNode, [ 'embeddedValue' ] )
ParagraphEmbeddedObject = schema.newClass( 'ParagraphEmbeddedObject', WorksheetNode, [ 'embeddedValue' ] )




#
#
# Version 1 backwards compatibility
#
#

def _readWorksheet_v1(fieldValues):
	# V1 included the title as a field in the worksheet node. Create a title paragraph to replace it.
	title = fieldValues['title']
	body = fieldValues['body']
	
	body['contents'].insert( 0, Paragraph( text=title, style='title' ) )
	
	return Worksheet( body=body )

schema.registerReader( 'Worksheet', 1, _readWorksheet_v1 )




#
#
# Version 2 backwards compatibility
#
#

def _readParagraph_v2(fieldValues):
	# V2 represented text as a string. Wrap it in a list
	text = fieldValues['text']
	style = fieldValues['style']

	return Paragraph( text=[ text ], style=style )

schema.registerReader( 'Paragraph', 2, _readParagraph_v2 )


