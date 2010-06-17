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


Text = schema.newClass( 'Text', [ 'text' ] )
Paragraph = schema.newClass( 'Paragraph', Text, [ 'style' ] )


PythonCode = schema.newClass( 'PythonCode', [ 'code', 'style' ] )


