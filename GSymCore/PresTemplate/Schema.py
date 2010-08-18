##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass




schema = DMSchema( 'PresTemplate', 'tmpl', 'GSymCore.PresTemplate' )



TemplateNode = schema.newClass( 'TemplateNode', [] )
TemplatePartialNode = schema.newClass( 'TemplatePartialNode', [] )


Template = schema.newClass( 'Template', TemplateNode, [ 'body' ] )

Body = schema.newClass( 'Body', TemplateNode, [ 'contents' ] )
Text = schema.newClass( 'Text', TemplateNode, [ 'text' ] )
Paragraph = schema.newClass( 'Paragraph', Text, [ 'style' ] )
PartialParagraph = schema.newClass( 'PartialParagraph', TemplatePartialNode, [ 'style' ] )


PythonExpr = schema.newClass( 'PythonExpr', TemplateNode, [ 'code' ] )




