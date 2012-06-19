##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.Pres.Primitive import *

from Britefury.Config.ConfigurationPage import ConfigurationPage




class Python2ConfigurationPage (ConfigurationPage):
	def getSubjectTitle(self):
		return '[CFG] Python 2'
	
	def getTitleText(self):
		return 'Python 2 Configuration'
	
	def getLinkText(self):
		return 'Python 2 editor'

	def __present_contents__(self, fragment, inheritedState):
		return Label( '<NOT YET IMPLEMENTED>' )
