##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest
import sys

from Britefury.Cell import CellEvaluatorFunction
from Britefury.Cell import CellEvaluator
from Britefury.Cell import CellEvaluatorPythonExpression
from Britefury.Cell import CellInterface
from Britefury.Cell import Cell
from Britefury.Cell import LiteralCell
from Britefury.Cell import ProxyCell


from Britefury.DocModel import DMNode
from Britefury.DocModel import DMNull
from Britefury.DocModel import DMNode
from Britefury.DocModel import DMSymbol
from Britefury.DocModel import DMListOperator
from Britefury.DocModel import DMListOpNop
from Britefury.DocModel import DMListOpMap
from Britefury.DocModel import DMListOpSlice
from Britefury.DocModel import DMListOpJoin
from Britefury.DocModel import DMListInterface
from Britefury.DocModel import DMVirtualList
from Britefury.DocModel import DMList
from Britefury.DocModel import DMProxyList
from Britefury.DocModel import DocModelLayer
from Britefury.DocModel import DMInterpreter
from Britefury.DocModel import DMIORead
from Britefury.DocModel import DMIOWrite


testModules = [ CellEvaluatorFunction, CellEvaluator, CellEvaluatorPythonExpression, CellInterface, Cell, LiteralCell, ProxyCell,
				DMNode, DMNull, DMSymbol, DMListOperator, DMListInterface, DMVirtualList, DMList, DMProxyList, DocModelLayer, DMListOpNop, DMListOpMap, DMListOpSlice, DMListOpJoin,
				DMInterpreter, DMIORead, DMIOWrite ]


if __name__ == '__main__':
	if len( sys.argv ) > 1:
		testModules = [ module   for module in testModules   if module.__name__.split('.')[-1] in sys.argv[1:] ]


	loader = unittest.TestLoader()

	suites = [ loader.loadTestsFromModule( module )   for module in testModules ]

	runner = unittest.TextTestRunner()

	results = unittest.TestResult()

	overallSuite = unittest.TestSuite()
	overallSuite.addTests( suites )

	runner.run( overallSuite )
