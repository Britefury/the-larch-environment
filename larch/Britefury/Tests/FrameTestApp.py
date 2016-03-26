##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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
