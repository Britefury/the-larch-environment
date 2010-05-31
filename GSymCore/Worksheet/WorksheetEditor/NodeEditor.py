##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakValueDictionary

from java.util import List
from java.awt.event import KeyEvent

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface


from BritefuryJ.DocPresent import *


from BritefuryJ.Logging import LogEntry


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations


from GSymCore.Worksheet import Schema



class _ListenerTable (object):
	def __init__(self, createFn):
		self._table = WeakValueDictionary()
		self._createFn = createFn
	
		
	def get(self, *args):
		key = args
		try:
			return self._table[key]
		except KeyError:
			listener = self._createFn( *args )
			self._table[key] = listener
			return listener
		
	
	
class EmptyLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = []
	
	def __init__(self):
		pass

	def linearRepresentationModified(self, element, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		if '\n' not in value:
			node['contents'] = [ Schema.Paragraph( text=value ) ]
			return True
		else:
			return False
		
	
	_listenerTable = None
		
	@staticmethod
	def newListener():
		if EmptyLinearRepresentationListener._listenerTable is None:
			EmptyLinearRepresentationListener._listenerTable = _ListenerTable( EmptyLinearRepresentationListener )
		return EmptyLinearRepresentationListener._listenerTable.get()



class TextLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = []
	
	def __init__(self):
		pass

	def linearRepresentationModified(self, element, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		if '\n' not in value:
			node['text'] = value
			return True
		else:
			return False
		
	
	_listenerTable = None
		
	@staticmethod
	def newListener():
		if TextLinearRepresentationListener._listenerTable is None:
			TextLinearRepresentationListener._listenerTable = _ListenerTable( TextLinearRepresentationListener )
		return TextLinearRepresentationListener._listenerTable.get()
	
	
	
class TextInteractor (ElementInteractor):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		return False
		
		
	def onKeyPress(self, element, event):
		if event.getModifiers() & KeyEvent.CTRL_MASK  !=  0:
			ctx = element.getFragmentContext()
			node = ctx.getDocNode()

			if event.getKeyCode() == KeyEvent.VK_P:
				return self._changeTextNodeClass( ctx, node, Schema.Paragraph )
			elif event.getKeyCode() == KeyEvent.VK_1:
				return self._changeTextNodeClass( ctx, node, Schema.H1 )
			elif event.getKeyCode() == KeyEvent.VK_2:
				return self._changeTextNodeClass( ctx, node, Schema.H2 )
			elif event.getKeyCode() == KeyEvent.VK_3:
				return self._changeTextNodeClass( ctx, node, Schema.H3 )
			elif event.getKeyCode() == KeyEvent.VK_4:
				return self._changeTextNodeClass( ctx, node, Schema.H4 )
			elif event.getKeyCode() == KeyEvent.VK_5:
				return self._changeTextNodeClass( ctx, node, Schema.H5 )
			elif event.getKeyCode() == KeyEvent.VK_6:
				return self._changeTextNodeClass( ctx, node, Schema.H6 )
			
		return False
	
	def onKeyRelease(self, element, event):
		return False



	def _changeTextNodeClass(self, ctx, node, nodeClass):
		if node.isInstanceOf( Schema.Text ):
			newNode = nodeClass( text=node['text'] )
			EditOperations.replaceNodeContents( ctx, node, newNode )
			return True
		else:
			return False
		
		
	