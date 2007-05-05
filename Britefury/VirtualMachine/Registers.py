##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
REGLEVEL_TEMP = -1
REGLEVEL_CONSTANT = -2
REGLEVEL_SELF = -3
REGLEVEL_RESULT = -4


def regName(reg):
	if reg[0] == 0:
		return 'l%d'  %  ( reg[1], )
	elif reg[0] > 0:
		return 'f%dl%d'  %  ( reg[0], reg[1], )
	elif reg[0] == REGLEVEL_TEMP:
		return 't%d'  %  ( reg[1], )
	elif reg[0] == REGLEVEL_CONSTANT:
		return 'c%d'  %  ( reg[1], )
	elif reg[0] == REGLEVEL_SELF:
		return 'self'
	elif reg[0] == REGLEVEL_RESULT:
		return 'result'


def regListNames(regs):
	return '[ '  +  ', '.join( [ regName( reg )  for reg in regs ] )  +  ' ]'


def localRegister(level, regIndex):
	return level, regIndex

def tempRegister(regIndex):
	return REGLEVEL_TEMP, regIndex

def constRegister(regIndex):
	return REGLEVEL_CONSTANT, regIndex

def selfRegister():
	return REGLEVEL_SELF, 0

def resultRegister():
	return REGLEVEL_RESULT, 0

