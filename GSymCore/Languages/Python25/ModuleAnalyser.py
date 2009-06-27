##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeMethodDispatchMetaClass, ObjectNodeDispatchMethod, objectNodeMethodDispatch
from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Util.NodeUtil import isStringNode

from GSymCore.Languages.Python25 import NodeClasses as Nodes



class ScopeInterface (object):
	def getLocal(self, name):
		assert False, 'abstract'


class ExpressionAnalyser (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_module__ = Nodes.module
	__dispatch_num_args__ = 1
	
	
	def __call__(self, node, scope):
		return objectNodeMethodDispatch( self, node, scope )
	
	
	@ObjectNodeDispatchMethod
	def Load(self, scope, node, name):
		return scope.getLocal( name )

	

_expressionAnalyser = ExpressionAnalyser()





class Python25ModuleInfo (ScopeInterface):
	def __init__(self, node):
		self.node = node
		self.table = {}
		
		
	def getScope(self):
		return self
	
	def getLocal(self, name):
		try:
			return self.table[name]
		except KeyError:
			return NameError
		
	def assign(self, target, value):
		targetClass = target.getDMClass()
		if targetClass == Nodes.SingleTarget:
			self.table[target['name']] = _expressionAnalyser( value, getScope() )
		elif targetClass == Nodes.TupleTarget:
			assert False, 'not implemented'
		elif targetClass == Nodes.ListTarget:
			assert False, 'not implemented'
		else:
			raise ValueError
			
		
		
		
class ModuleAnalyser (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_module__ = Nodes.module
	__dispatch_num_args__ = 1
	
	
	def __call__(self, node, moduleInfo):
		try:
			return objectNodeMethodDispatch( self, node, moduleInfo )
		except DispatchError:
			pass
		
		
	@ObjectNodeDispatchMethod
	def AssignStmt(self, moduleInfo, node, targets, value):
		for target in targets:
			moduleInfo.assign( target, value )
		

_moduleAnalyser = ModuleAnalyser()





class DocAnalyser (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_module__ = Nodes.module
	__dispatch_num_args__ = 0
	
	
	def __call__(self, node):
		return objectNodeMethodDispatch( self, node )
	
	
	@ObjectNodeDispatchMethod
	def PythonModule(self, node, suite):
		module = Python25ModuleInfo( node )
		for x in suite:
			_moduleAnalyser( module, x )
	
	
	
_docAnalyser = DocAnalyser()
	
def analyseModule(moduleDoc):
	return _docAnalyser( moduleDoc )
