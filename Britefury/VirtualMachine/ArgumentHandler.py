##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
class ArgumentHandler (object):
	def __init__(self, argNames=[], argNamesWithDefaults=[], argListName=None, kwArgsName=None):
		super( ArgumentHandler, self ).__init__()
		self.argNames = argNames
		self.argNamesWithDefaults = argNamesWithDefaults
		self.argListName = argListName
		self.kwArgsName = kwArgsName

		self.defaultArgValues = [ value  for name, value in argNamesWithDefaults ]
		self.allArgNames = self.argNames + [ name  for name, value in argNamesWithDefaults ]
		self.numArgs = len( self.argNames ) + len( argNamesWithDefaults )

		self.argNameToIndex = {}
		for index, name in enumerate( self.allArgNames ):
			self.argNameToIndex[name] = index

		if argListName is not None:
			self.numArgs += 1
		if kwArgsName is not None:
			self.numArgs += 1
		self.numRequiredArgs = len( self.argNames )


	def checkArgFlags(self, flagsList, checkFrom, checkTo):
		for i in xrange( checkFrom, checkTo ):
			if flagsList[i]:
				return i
 		return None


	def processArguments(self, args=[], kwArgs=[], expandArgs=None, expandKWArgs=None):
		cArgs = [ None ] * len( self.argNames )  +  self.defaultArgValues
		cArgsFound = [ False ] * len( self.allArgNames )
		cArgsList = []
		cKWArgs = {}

		numSuppliedArgs = len( args )  +  len( kwArgs )
		if expandArgs is not None:
			numSuppliedArgs += len( expandArgs )
		if expandKWArgs is not None:
			numSuppliedArgs += len( expandKWArgs )

		unfilledArgSlots = len( self.argNames )
		unfilledDefaultArgSlots = len( self.argNamesWithDefaults )

		argListName = self.argListName
		kwArgsName = self.kwArgsName

		argNamesPosition = 0

		# args can go to: argNames, argNamesWithDefaults, and argListName
		if len( args ) > 0:
			# -> argNames
			x = min( len( args ), unfilledArgSlots )
			cArgs[argNamesPosition:argNamesPosition+x] = args[:x]
			cArgsFound[argNamesPosition:argNamesPosition+x] = [ True ] * x
			unfilledArgSlots -= x
			args = args[x:]
			argNamesPosition += x

			if len( args ) > 0:
				# -> argNamesWithDefaults
				x = min( len( args ), unfilledDefaultArgSlots )
				cArgs[argNamesPosition:argNamesPosition+x] = args[:x]
				cArgsFound[argNamesPosition:argNamesPosition+x] = [ True ] * x
				unfilledDefaultArgSlots -= x
				args = args[x:]
				argNamesPosition += x

				if len( args ) > 0:
					# -> argListName
					if argListName is not None:
						cArgsList += args
						args = []
					else:
						raise TypeError, 'too many arguments: function takes %d arguments, requires %d, %d supplied'  %  ( self.numArgs, self.numRequiredArgs, numSuppliedArgs )

		# expandArgs can go to: argNames, argNamesWithDefaults, and argListName
		if expandArgs is not None:
			# -> argNames
			x = min( len( expandArgs ), unfilledArgSlots )
			pos = self.checkArgFlags( cArgsFound, argNamesPosition, argNamesPosition+x )
			if pos is not None:
				raise TypeError, 'already got argument for %s'  %  ( self.allArgNames[pos], )
			cArgs[argNamesPosition:argNamesPosition+x] = expandArgs[:x]
			cArgsFound[argNamesPosition:argNamesPosition+x] = [ True ] * x
			unfilledArgSlots -= x
			expandArgs = expandArgs[x:]
			argNamesPosition += x

			if len( expandArgs ) > 0:
				# -> argNamesWithDefaults
				x = min( len( expandArgs ), unfilledDefaultArgSlots )
				pos = self.checkArgFlags( cArgsFound, argNamesPosition, argNamesPosition+x )
				if pos is not None:
					raise TypeError, 'already got argument for %s'  %  ( self.allArgNames[pos], )
				cArgs[argNamesPosition:argNamesPosition+x] = expandArgs[:x]
				cArgsFound[argNamesPosition:argNamesPosition+x] = [ True ] * x
				unfilledDefaultArgSlots -= x
				expandArgs = expandArgs[x:]
				argNamesPosition += x

				if len( expandArgs ) > 0:
					# -> argListName
					if argListName is not None:
						cArgsList += expandArgs
						expandArgs = None
					else:
						raise TypeError, 'too many expanded arguments: function takes %d arguments, requires %d, %d supplied'  %  ( self.numArgs, self.numRequiredArgs, numSuppliedArgs )

		# kwArgs can go to argNames, argNamesWithDefaults, and kwArgsName
		if len( kwArgs ) > 0:
			# kwArgs -> argNames, argNamesWithDefaults, and kwArgsName
			for name, value in kwArgs:
				try:
					argNameIndex = self.argNameToIndex[name]
				except KeyError:
					if kwArgsName is not None:
						if name in cKWArgs:
							raise TypeError, 'already got argument for %s'  %  ( name, )
						cKWArgs[name] = value
					else:
						raise TypeError, 'too many keywork arguments: function takes %d arguments, requires %d, %d supplied'  %  ( self.numArgs, self.numRequiredArgs, numSuppliedArgs )
				else:
					if cArgsFound[argNameIndex]:
						raise TypeError, 'already got argument for %s'  %  ( name, )
					cArgs[argNameIndex] = value
					cArgsFound[argNameIndex] = True

		# expandKWArgs can go to argNames, argNamesWithDefaults, and kwArgsName
		if expandKWArgs is not None:
			# -> argNames and argNamesWithDefaults
			for name, value in expandKWArgs:
				try:
					argNameIndex = self.argNameToIndex[name]
				except KeyError:
					if kwArgsName is not None:
						if name in cKWArgs:
							raise TypeError, 'already got argument for %s'  %  ( name, )
						cKWArgs[name] = value
					else:
						raise TypeError, 'too many expanded keyword arguments: function takes %d arguments, requires %d, %d supplied'  %  ( self.numArgs, self.numRequiredArgs, numSuppliedArgs )
				else:
					if cArgsFound[argNameIndex]:
						raise TypeError, 'already got argument for %s'  %  ( name, )
					cArgs[argNameIndex] = value
					cArgsFound[argNameIndex] = True



		for bFound in cArgsFound[:len(self.argNames)]:
			if not bFound:
				raise TypeError, 'insufficient arguments: function takes %d arguments, requires %d, %d supplied'  %  ( self.numArgs, self.numRequiredArgs, numSuppliedArgs )


		if self.argListName is None:
			cArgsList = None
		else:
			cArgsList = tuple( cArgsList )

		if self.kwArgsName is None:
			cKWArgs = None

		return cArgs, cArgsList, cKWArgs





if __name__ == '__main__':
	FUN_BEGIN = 0
	FUN_ARGA = 1
	FUN_ARGB = 2
	FUN_DAC = 4
	FUN_DAD = 8
	FUN_ARGLIST = 16
	FUN_KWARGS = 32
	FUN_END = 64

	CALL_BEGIN = 0
	CALL_1 = 1
	CALL_2 = 2
	CALL_3 = 4
	CALL_A1 = 8
	CALL_B2 = 16
	CALL_C3 = 32
	CALL_D4 = 64
	CALL_E5 = 128
	CALL_END = 256


	def flagsToString(x, flagsMax):
		s = ''
		mask = 1
		while mask < flagsMax:
			if x & mask:
				s += '*'
			else:
				s += ' '
			mask *= 2
		return s


	def printTest(funFlags, callFlags, xa, xkwa):
		print 'FUN: AADDLK     CALL: AAADDDDD XA XKWA'
		print '     %s           %s %d  %d' % ( flagsToString( funFlags, FUN_END ), flagsToString( callFlags, CALL_END ), xa, xkwa )


	def funTest():
		for x in xrange( FUN_BEGIN, FUN_END ):
			pySpec = []
			pyValue = []
			pyXValue = 'None'
			pyXKWValue = 'None'

			argNames=[]
			argNamesWithDefaults=[]
			argListName=None
			kwArgsName=None

			if x & FUN_ARGA:
				pySpec.append( 'a' )
				pyValue.append( 'a' )
				argNames.append( 'a' )
			if x & FUN_ARGB:
				pySpec.append( 'b' )
				pyValue.append( 'b' )
				argNames.append( 'b' )
			if x & FUN_DAC:
				pySpec.append( 'c=-3' )
				pyValue.append( 'c' )
				argNamesWithDefaults.append( ( 'c', -3 ) )
			if x & FUN_DAD:
				pySpec.append( 'd=-4' )
				pyValue.append( 'd' )
				argNamesWithDefaults.append( ( 'd', -4 ) )
			if x & FUN_ARGLIST:
				pySpec.append( '*p' )
				pyXValue = 'p'
				argListName = 'p'
			if x & FUN_KWARGS:
				pySpec.append( '**q' )
				pyXKWValue = 'q'
				kwArgsName = 'q'
			pySpec = ', '.join( pySpec )
			pyValue = '[ ' + ', '.join( pyValue ) + ' ]'


			pyFunctionDefinition = 'def pyFunction(%s):\n\treturn %s,%s,%s\n' % ( pySpec, pyValue, pyXValue, pyXKWValue )
			print pyFunctionDefinition

			exec( pyFunctionDefinition )

			fun = ArgumentHandler( argNames, argNamesWithDefaults, argListName, kwArgsName )




			for numKWExpandArgs in xrange( 0, 7 ):
				for numExpandArgs in xrange( 0, 6 ):
					for y in xrange( CALL_BEGIN, CALL_END ):
						callSpec = []

						args = []
						kwargs = []

						if numExpandArgs > 0:
							expandArgs = []
						else:
							expandArgs = None

						if numKWExpandArgs > 0:
							expandKWArgs = []
						else:
							expandKWArgs = None


						if y & CALL_1:
							callSpec.append( '1' )
							args.append( 1 )
						if y & CALL_2:
							callSpec.append( '2' )
							args.append( 2 )
						if y & CALL_3:
							callSpec.append( '3' )
							args.append( 3 )
						if y & CALL_A1:
							callSpec.append( 'a=1' )
							kwargs.append( ( 'a', 1 ) )
						if y & CALL_B2:
							callSpec.append( 'b=2' )
							kwargs.append( ( 'b', 2 ) )
						if y & CALL_C3:
							callSpec.append( 'c=3' )
							kwargs.append( ( 'c', 3 ) )
						if y & CALL_D4:
							callSpec.append( 'd=4' )
							kwargs.append( ( 'd', 4 ) )
						if y & CALL_E5:
							callSpec.append( 'e=5' )
							kwargs.append( ( 'e', 5 ) )

						if numExpandArgs > 0:
							xArgSpec = []
							for a in xrange( 0, numExpandArgs ):
								xArgSpec.append( str( a*100+100 ) )
								expandArgs.append( a*100+100 )
							xArgSpec = ', '.join( xArgSpec )
							callSpec.append( '*[ %s ]' % ( xArgSpec, ) )

						if numKWExpandArgs > 0:
							xArgSpec = []
							for a in xrange( 0, numKWExpandArgs ):
								argName = chr( ord( 'a' ) + a )
								xArgSpec.append( '\'%s\':%d' % ( argName, a*100+100 ) )
								expandKWArgs.append( ( argName, a*100+100 ) )
							xArgSpec = ', '.join( xArgSpec )
							callSpec.append( '**{ %s }' % ( xArgSpec, ) )

						callSpec = ', '.join( callSpec )

						functionCall = 'pyResult = pyFunction(%s)\n' % ( callSpec, )

						pyReason = ''
						testReason = ''

						try:
							exec( functionCall )
						except TypeError, reason:
							pyResult = TypeError
							pyReason = reason

						try:
							testResult = fun.processArguments( args, kwargs, expandArgs, expandKWArgs )
						except TypeError, reason:
							testResult = TypeError
							testReason = reason

						if testResult != pyResult:
							print '### FAILED ###'
							printTest( x, y, numExpandArgs, numKWExpandArgs )
							print functionCall
							print pyResult, pyReason
							print testResult, testReason
							return


	funTest()



