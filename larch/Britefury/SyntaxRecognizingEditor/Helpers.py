##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.SequentialEditor.Helpers import _cache, _SeqEditorDescriptor, _WrappedEditRule


class ParsingEditFilterDeclaration (_SeqEditorDescriptor):
	def __init__(self, logName, parserExpression):
		self.__logName = logName
		self.__parserExpression = parserExpression
		self.__commitMethod = None
		self.__emptyCommitMethod = None


	def commitMethod(self, method):
		self.__commitMethod = method
		return method

	def emptyCommitMethod(self, method):
		self.__emptyCommitMethod = method
		return method


	def _filterForInstance(self, controller):
		return _cache( controller, self, lambda: controller.parsingEditFilter( self.__logName, self.__parserExpression,
									  lambda model, value: self.__commitMethod( controller, model, value ),
									  ( lambda model, value: self.__emptyCommitMethod( controller, model, value ) )   if self.__emptyCommitMethod is not None   else None ) )

	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			if self.__commitMethod is None:
				raise ValueError, 'Commit method of ParsingEditFilterDeclaration \'{0}.{1}\' not set'.format( type( obj ).__name__, self.__logName )
			return self._filterForInstance( obj )




class PartialParsingEditFilterDeclaration (_SeqEditorDescriptor):
	def __init__(self, logName, parserExpression):
		self.__logName = logName
		self.__parserExpression = parserExpression


	def _filterForInstance(self, controller):
		return _cache( controller, self, lambda: controller.partialParsingEditFilter( self.__logName, self.__parserExpression ) )

	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			return self._filterForInstance( obj )




class UnparsedEditFilterDeclaration (_SeqEditorDescriptor):
	def __init__(self, logName):
		self.__logName = logName
		self.__testMethod = None
		self.__commitMethod = None
		self.__innerCommitMethod = None


	def testMethod(self, method):
		self.__testMethod = method
		return method

	def commitMethod(self, method):
		self.__commitMethod = method
		return method

	def innerCommitMethod(self, method):
		self.__innerCommitMethod = method
		return method


	def _filterForInstance(self, controller):
		return _cache( controller, self, lambda: controller.unparsedEditFilter( self.__logName,
										       lambda value: self.__testMethod( controller, value ),
										       lambda model, value: self.__commitMethod( controller, model, value ),
										       ( lambda model, value: self.__innerCommitMethod( controller, model, value ) )   if self.__innerCommitMethod is not None   else None ) )

	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			if self.__testMethod is None:
				raise ValueError, 'Test method of UnparsedEditFilterDeclaration \'{0}.{1}\' not set'.format( type( obj ).__name__, self.__logName )
			if self.__commitMethod is None:
				raise ValueError, 'Commit method of UnparsedEditFilterDeclaration \'{0}.{1}\' not set'.format( type( obj ).__name__, self.__logName )
			return self._filterForInstance( obj )





class _AbstractEditRuleDeclaration (_SeqEditorDescriptor):
	def __init__(self, filterDeclarations, precedenceHandler=None):
		self.__precedenceHandler = precedenceHandler
		self.__filterDeclarations = filterDeclarations

	def _filters(self, controller):
		return  [ f._filterForInstance( controller )   for f in self.__filterDeclarations ]


class EditRuleDeclaration (_AbstractEditRuleDeclaration):
	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			return _cache( obj, self, lambda: _WrappedEditRule( obj.editRule( self.__precedenceHandler, self._filters( obj ) ) ) )


class SoftStructuralEditRuleDeclaration (_AbstractEditRuleDeclaration):
	def __get__(self, obj, type):
		if obj is None:
			return self
		else:
			return _cache( obj, self, lambda: _WrappedEditRule( obj.softStructuralEditRule( self.__precedenceHandler, self._filters( obj ) ) ) )
