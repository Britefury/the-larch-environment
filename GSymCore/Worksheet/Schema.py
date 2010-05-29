##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass




schema = DMSchema( 'Worksheet', 'ws', 'GSymCore.Worksheet' )



Worksheet = schema.newClass( 'Worksheet', [ 'title', 'contents' ] )

Node = schema.newClass( 'Node', [] )

Text = schema.newClass( 'Text', Node, [ 'text' ] )
Paragraph = schema.newClass( 'Paragraph', Text, [] )
Heading = schema.newClass( 'Heading', Text, [] )
H1 = schema.newClass( 'H1', Heading, [] )
H2 = schema.newClass( 'H2', Heading, [] )
H3 = schema.newClass( 'H3', Heading, [] )
H4 = schema.newClass( 'H4', Heading, [] )
H5 = schema.newClass( 'H5', Heading, [] )
H6 = schema.newClass( 'H6', Heading, [] )


PythonCode = schema.newClass( 'PythonCode', Node, [ 'code', 'showCode', 'codeEditable', 'showResult' ] )


