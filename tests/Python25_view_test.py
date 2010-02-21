##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys

print 'This is a view test - it is not executable; import it in gSym'

sys.exit()


# Blank line


# UNPARSED : edit manually


# Comment
# test


# String literal
x = 'ascii'
x = u'unicode'
x = r'ascii-regex'
x = ur'unicode-regex'


# Int literal
decimalInt = 1
decimalLong = 1L
hexInt = 0xf
hexLong = 0xfL


# Float literal
f = 1.0
f = 1.0e18


# Imaginary literal
i = 3j


# Single target
x = None


# Tuple target
a, b, c = 1, 2, 3


# List target
[a,b,c] = 1,2,3


# Load
x = a


# Tuple literal
x = a,b,c


# List literal
x = [a,b,c]


# List comprehension; for and if
x = [a   for a in x   if a == 1]


# Generator expression; for and if
x = (a   for a in x   if a == 1)


# Dictionary literal
x = {1:1, 2:2, 3:3}


# Yield expression
def f():
	x = (yield 1)


# Attribute ref
x = a.b


# Subscript - basic, slice, long slide, ellipsis, tuple
x = a[0]
x = a[0:1],  a[0:],  a[:1]
x = a[0:1:2],  a[0:1:],  a[0::2],  a[:1:2],  a[0::],  a[:0:],  a[::0],  a[::]
x = a[...]
x = a[0,1:2,3:4:5,...]


# Call - arg, kwarg, arglist, kwarglist
a(a,b=2,*c,**d)


# Pow
x = a**b


# Invert, negate, pos
x = ~a, -b, +c


# mul, div, mod
x = a*b
x = a/b
x = a%b


# add, sub
x = a+b
x = a-b


# lshift, rshift
x = a<<b
x = a>>b


# bit and, bit or, bit xor
x = a&b
x = a|b
x = a^b


# cmp - <=, <, >=, >, ==, !=, is not, is, not in, in
x = a <= b < c >= d > e == f != g is not h is i not in j in k


# not, and, or
x = not a
x = a and b
y = a or b


# lambda with params
x = lambda: 1
x = lambda a, b=2, *c, **d: 1


# conditional expr
x = 1 if b   else 2


# expr stmt
1


# assert
assert 1
assert 1, 'hi'


# assignment
a = b = c = d = e = f = 1


# augassign
a += 1
a *= 1


# pass
if True:
	pass


# del
del x


# return
def f():
	return 1


# yield
def f():
	yield 1



# raise
raise
raise a
raise a,b
raise a,b,c


# break
while True:
	break


# continue
while False:
	continue


# import
import a
import a.b.c
import a, b


# From import
from a import b, c
from a.b.c import b, c


# From import all
from a import *


# Global
global gy
global gz,gy,gx


# Exec
exec 'x'
exec 'x' in {}
exec 'x' in {}, {}


# Print
print
print a
print >>a
print >>a, b
print >>a, b, c




#
#
# COMPOUNDS
#
#



# If
if True:
	pass

if True:
	pass
else:
	pass

if True:
	pass
elif True:
	pass
else:
	pass


# While
while True:
	pass

while True:
	pass
else:
	pass


# For
for a in b:
	pass

for a in b:
	pass
else:
	pass


# Try
try:
	pass
except A:
	pass

try:
	pass
except B:
	pass
else:
	pass

try:
	pass
except B:
	pass
finally:
	pass

try:
	pass
except B:
	pass
else:
	pass
finally:
	pass

try:
	pass
finally:
	pass

try:
	a
	b
finally:
	pass


# Except
try:
	pass
except A:
	pass
except A, a:
	pass
except:
	pass


# Def
def f():
	pass

def f(a,b=2,*c,**d):
	pass


# Decorator
@a
@a()
@a(1,2,3)
def f():
	pass


# Class
class A:
	pass

class A (object):
	pass

class A (a,b,c):
	pass

