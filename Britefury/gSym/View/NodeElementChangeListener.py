##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys

from datetime import datetime

import difflib

from copy import copy

from BritefuryJ.Cell import *

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Marker import *

from BritefuryJ.DocTree import DocTree
from BritefuryJ.DocTree import DocTreeNode

from BritefuryJ.DocView import DVNode
from BritefuryJ.DocView import DocView

from BritefuryJ.GSym.View import StringDiff, NodeElementChangeListenerDiff

from Britefury.Util.NodeUtil import isListNode, nodeToSXString

from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch



class _NodeElementChangeListener (NodeElementChangeListenerDiff):
	def computeNewPositionWithDiff(self, position, bias, newBiasArray, contentString, newContentString, prefixLen, suffixLen, origChangeRegion, newChangeRegion):
		newBias = newBiasArray[0]
		
		matcher = difflib.SequenceMatcher( lambda x: x in ' \t', origChangeRegion, newChangeRegion )
		# Get the opcodes from the matcher
		opcodes = matcher.get_opcodes()
		# Apply the prefix offset
		opcodes = [ ( tag, i1 + prefixLen, i2 + prefixLen, j1 + prefixLen, j2 + prefixLen )   for tag, i1, i2, j1, j2 in opcodes ]
		# Prepend and append some 'equal' opcodes that cover the prefix and suffix
		opcodes = [ ( 'equal', 0, prefixLen, 0, prefixLen ) ]  +  opcodes  +  [ ( 'equal', len(contentString)-suffixLen, len(contentString), len(newContentString)-suffixLen, len(newContentString) ) ]
		for tag, i1, i2, j1, j2 in opcodes:
			if ( position > i1  or  ( position == i1  and  bias == Marker.Bias.END ) )   and   position < i2:
				# Caret is in the range of this opcode
				if tag == 'delete':
					# Range deleted; move to position in destination; bias:START
					newPosition = j1
					newBias = Marker.Bias.START
				elif tag == 'replace'  or  tag == 'equal'  or  tag == 'insert':
					# Range replaced or equal; move position by delta
					newPosition += j1 - i1
				else:
					raise ValueError, 'unreckognised tag'
				
		newBiasArray[0] = newBias
		return newPosition
		

