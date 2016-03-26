##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Pres.Primitive import Label

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
