##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocPresent.Web.JSScript import JSScript

from Britefury.DocPresent.Web.ModifierKeys import testEventModifierKeys_js



highlight_js = \
"""
function HighlightClass(normalStyle, highlightedStyle, ctrlKey, shiftKey, altKey, ctrlMask, shiftMask, altMask)
{
	this.normalStyle = normalStyle;
	this.highlightedStyle = highlightedStyle;
	this.ctrlKey = ctrlKey;
	this.shiftKey = shiftKey;
	this.altKey = altKey;
	this.ctrlMask = ctrlMask;
	this.shiftMask = shiftMask;
	this.altMask = altMask;
	this.bOn = false;
	this.stack = new Array();
}


HighlightClass.prototype.getHighlightedElement = function()
{
	if ( this.bOn )
	{
		if ( this.stack.length > 0 )
		{
			return this.stack[ this.stack.length-1 ];
		}
	}
	
	return undefined
}

HighlightClass.prototype.handleKeyState = function(event)
{
	if ( ( event.ctrlKey == this.ctrlKey  ||  !this.ctrlMask )   &&   ( event.shiftKey == this.shiftKey  ||  !this.shiftMask )   &&   ( event.altKey == this.altKey  ||  !this.altMask ) )
	{
		this.bOn = true;
	}
	else
	{
		this.bOn = false;
	}
}

HighlightClass.prototype.handleHighlightChange = function(prev, cur)
{
	if ( prev != cur )
	{
		if ( prev != undefined )
		{
			if ( this.highlightedStyle != "" )
			{
				prev.removeClass( this.highlightedStyle );
			}
			if ( this.normalStyle != "" )
			{
				prev.addClass( this.normalStyle );
			}
		}

		if ( cur != undefined )
		{
			if ( this.normalStyle != "" )
			{
				cur.removeClass( this.normalStyle );
			}
			if ( this.highlightedStyle != "" )
			{
				cur.addClass( this.highlightedStyle );
			}
		}
	}
}

HighlightClass.prototype.onEnter = function(element, event)
{
	prev = this.getHighlightedElement();
	this.stack.push( element );
	this.handleKeyState( event );
	cur = this.getHighlightedElement();
	this.handleHighlightChange( prev, cur );
}





"""


applyHighlightFnJS = \
"""
function applyHighlight_%(className)s(element)
{
	// Get the stack for the relevant highlight class name
	var stack = _highlightTable[%(className)s];
	if ( stack == undefined )
	{
		stack = new Array();
		_highlightTable[%(className)s] = stack;
	}
	
	
	element.hover(
		function (event)
		{
		},
		function (event)
		{
		}
	);
}
"""



class HighlightHtmlClass (object):
	def __init__(self, className, modifierKeysValue, modifierKeysMask):
		self._className = className + '_hl'
		self._activeClassName = className + '_ahl'
		self._modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
		self._modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
		
	def headerScript(self):
		assert False
		
	def apply(self, ctx, html):
		return '<span class="%s">%s</span>'  %  ( self._className, html )
	
	
	
def highlight(ctx, html, highlightClass):
	return highlightClass.apply( ctx, html )
	
	
	
import unittest


class TestCase_Highlight (unittest.TestCase):
	def testHighlight(self):
		self.assert_( False )
	