##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMModule, DMObjectClass




module = DMModule( 'GSymProject', 'py', 'GSymCore.Project.Project' )



Project = module.newClass( 'Project', [ 'rootPackage' ] )

Package = module.newClass( 'Package', [ 'name', 'contents' ] )
Page = module.newClass( 'Page', [ 'name', 'unit' ] )

