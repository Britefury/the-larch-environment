##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Enum import Enum


class DTDirection (Enum):
	LEFT_TO_RIGHT = 0
	RIGHT_TO_LEFT = 1
	TOP_TO_BOTTOM = 2
	BOTTOM_TO_TOP = 3