##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from LarchCore.Languages.Python25.PythonConfig import Python25ConfigurationPage
from LarchCore.Languages.Python25 import Python25



def initPlugin(plugin, world):
	world.configuration.registerConfigurationPage( Python25ConfigurationPage() )
