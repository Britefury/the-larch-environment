##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.FileIO.IOXml import *




class CellEvaluationError (Exception):
	pass


class CellEvaluatorClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		super( CellEvaluatorClass, cls ).__init__( clsName, clsBases, clsDict )

		ioObjectFactoryRegister( clsName, cls )




class CellEvaluator (object):
	__metaclass__ = CellEvaluatorClass


	bSerialisable = False


	def evalute(self):
		assert False, 'abstract'


	def __readxml__(self, xmlNode):
		pass

	def __writexml__(self, xmlNode):
		pass




