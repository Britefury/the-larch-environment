##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from javax.swing import JTextField, JLabel, BoxLayout, JPanel, BorderFactory
from java.awt import Dimension, Font, Color
from java.awt.event import WindowListener, ActionListener, KeyEvent


class LocationBarListener (object):
	def _onLocation(self, location, format):
		pass


class LocationBar (object):
	def __init__(self, listener, location='', format=''):
		self._listener = listener
		self._location = location
		self._format = format

		locationLabel = JLabel( 'Location:' )
		locationLabel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) )
		self._locationField = JTextField( location )
		self._locationField.setMaximumSize( Dimension( self._locationField.getMaximumSize().width, self._locationField.getMinimumSize().height ) )
		self._locationField.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) )
		
		class _LocationActionListener (ActionListener):
			def actionPerformed(listener_self, event):
				self._onLocationField( self._locationField.getText() )
				
		self._locationField.addActionListener( _LocationActionListener() )
		
		
		
		formatLabel = JLabel( 'Format:' )
		formatLabel.setBorder( BorderFactory.createEmptyBorder( 0, 25, 0, 5 ) )
		
		self._formatField = JTextField( format )
		self._formatField.setPreferredSize( Dimension( 150, self._formatField.getMinimumSize().height ) )
		self._formatField.setMaximumSize( Dimension( 150, self._formatField.getMinimumSize().height ) )
		self._formatField.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) )
		
		class _FormatActionListener (ActionListener):
			def actionPerformed(listener_self, event):
				self._onFormatField( self._formatField.getText() )
				
		self._formatField.addActionListener(_FormatActionListener() )
		
		
		self._component = JPanel()
		self._component.setLayout( BoxLayout( self._component, BoxLayout.X_AXIS ) )
		self._component.add( locationLabel )
		self._component.add( self._locationField )
		self._component.add( formatLabel )
		self._component.add( self._formatField )
		self._component.setBorder( BorderFactory.createEmptyBorder( 5, 0, 5, 5 ) )
		
		
		
	def setLocationAndFormat(self, location, format):
		self._location = location
		self._format = format
		self._locationField.setText( location )
		self._formatField.setText( format )
		
		
	def getLocation(self):
		return self._location
	
	def getFormat(self):
		return self._format
	
	
	
	def getComponent(self):
		return self._component
		
		
	def _onLocationField(self, location):
		self._location = location
		self._listener._onLocation( self._location, self._format )
	
	def _onFormatField(self, format):
		self._format = format
		self._listener._onLocation( self._location, self._format )
		
