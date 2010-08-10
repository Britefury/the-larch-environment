##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass




schema = DMSchema( 'GSymProject', 'prj', 'GSymCore.Project', 2 )



Project = schema.newClass( 'Project', [ 'contents' ] )

Package = schema.newClass( 'Package', [ 'name', 'contents' ] )
Page = schema.newClass( 'Page', [ 'name', 'unit' ] )



#
#
# Version 1 backwards compatibility
#
#

def _readProject_v1(fieldValues):
	# V1 included the title as a field in the worksheet node. Create a title paragraph to replace it.
	rootPackage = fieldValues['rootPackage']
	
	return Project( contents=rootPackage['contents'] )

schema.registerReader( 'Project', 1, _readProject_v1 )
