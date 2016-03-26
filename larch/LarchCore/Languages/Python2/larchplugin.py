##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from LarchCore.Languages.Python2.PythonConfig import Python2ConfigurationPage
from LarchCore.Languages.Python2 import Python2



def initPlugin(plugin, world):
	world.configuration.registerConfigurationPage( Python2ConfigurationPage() )

