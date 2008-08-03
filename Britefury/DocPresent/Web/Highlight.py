##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocPresent.Web.ModifierKeys import modifierKeyStringToFlags, MOD_ALT, MOD_CTRL, MOD_SHIFT




class HighlightHtmlClass (object):
	def __init__(self, className, normalStyle, highlightedStyle, modifierKeysValue, modifierKeysMask):
		self._className = className
		self._normalStyle = normalStyle
		self._highlightedStyle = highlightedStyle
		self._modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
		self._modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
		
		
	def onLoadJS(self):
		def jsBool(b):
			if b:
				return 'true'
			else:
				return 'false'
		return '%s = new HighlightClass( "%s", "%s", %s, %s, %s, %s, %s, %s );\n'  %  ( self._className, self._normalStyle, self._highlightedStyle,
											    jsBool( self._modifierKeysValue & MOD_CTRL != 0 ),
											    jsBool( self._modifierKeysValue & MOD_SHIFT != 0 ),
											    jsBool( self._modifierKeysValue & MOD_ALT != 0 ),
											    jsBool( self._modifierKeysMask & MOD_CTRL != 0 ),
											    jsBool( self._modifierKeysMask & MOD_SHIFT != 0 ),
											    jsBool( self._modifierKeysMask & MOD_ALT != 0 ) )
		
	def apply(self, nodeContext, html):
		nodeID = nodeContext.viewContext.allocID( 'highlight' )
		classStr = 'class="%s" ' % self._normalStyle   if self._normalStyle != ''   else   ''
		return '<span %s id="%s">%s</span><script type="text/javascript">%s.applyTo( $("#%s") );</script>'  %  ( classStr, nodeID, html, self._className, nodeID )
	
	
	
def highlight(nodeContext, highlightClass, html):
	return highlightClass.apply( nodeContext, html )
	
	
	
	