##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

"""
Defines the CellEvaluator interface, and metaclass
"""




class CellEvaluationError (Exception):
	pass


class CellEvaluatorClass (type):
	"""CellEvaluator metaclass"""
	def __init__(cls, clsName, clsBases, clsDict):
		super( CellEvaluatorClass, cls ).__init__( clsName, clsBases, clsDict )

		#ioObjectFactoryRegister( clsName, cls )




class CellEvaluator (object):
	"""Cell evaluator

	Calling the evaluate() method computes the value for a cell

	bSerialisable - Determines if instances of this class can be serialised
	"""
	#__metaclass__ = CellEvaluatorClass


	bSerialisable = False


	def evalute(self):
		"""Compute the value for a cell"""
		assert False, 'abstract'


	#def __readxml__(self, xmlNode):
	#	pass

	#def __writexml__(self, xmlNode):
	#	pass




