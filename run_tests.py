##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import unittest

from Britefury.Cell import CellEvaluatorFunction
from Britefury.Cell import CellEvaluator
from Britefury.Cell import CellEvaluatorPythonExpression
from Britefury.Cell import CellInterface
from Britefury.Cell import Cell
from Britefury.Cell import LiteralCell
from Britefury.Cell import ProxyCell


from Britefury.DocModel import DMNode
from Britefury.DocModel import DMSymbol
from Britefury.DocModel import DMString
from Britefury.DocModel import DMListOperator
from Britefury.DocModel import DMListOpNop
from Britefury.DocModel import DMListOpMap
from Britefury.DocModel import DMListOpSlice
from Britefury.DocModel import DMListOpWrap
from Britefury.DocModel import DMListInterface
from Britefury.DocModel import DMList
from Britefury.DocModel import DMLiteralList
from Britefury.DocModel import DMProxyList
from Britefury.DocModel import DocModelLayer


testModules = [ CellEvaluatorFunction, CellEvaluator, CellEvaluatorPythonExpression, CellInterface, Cell, LiteralCell, ProxyCell,
				DMNode, DMSymbol, DMString, DMListOperator, DMListInterface, DMList, DMLiteralList, DMProxyList, DocModelLayer ]


if __name__ == '__main__':
	loader = unittest.TestLoader()

	suites = [ loader.loadTestsFromModule( module )   for module in testModules ]

	runner = unittest.TextTestRunner()

	results = unittest.TestResult()

	overallSuite = unittest.TestSuite()
	overallSuite.addTests( suites )

	runner.run( overallSuite )
