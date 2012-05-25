##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from javax.swing import JFrame, JEditorPane, AbstractAction, JMenuItem, JMenu, JMenuBar
from java.awt import Dimension



class FrameTestApp (object):
	def __init__(self, title, component):
		self._frame = JFrame( title )
		
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE )
		
		self._frame.setPreferredSize( Dimension( 640, 480 ) )
		
		self._frame.add( component )
		
		self._frame.pack()

		
	def run(self):
		self._frame.setVisible( True )
