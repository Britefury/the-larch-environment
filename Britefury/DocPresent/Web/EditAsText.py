##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocPresent.Web.HtmlTag import HtmlTag
from Britefury.DocPresent.Web.ModifierKeys import modifierKeyStringToFlags



def editAsText(ctx, html, text, modifierKeysValue, modifierKeysMask):
	modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
	modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
	tagID = ctx.allocIDForNodeContent( 'editastext' )
	script = \
"""
$("#%s").click(
	function (event)
	{
		if ( ( gsym_global_modifierKeysState & %d )  ==  %d )
		{
			$("#%s").html( "<input text="%s">" )
		}
	}
);
"""  %  ( tagID, modifierKeysMask, modifierKeysValue, tagID, text )
	ctx.viewContext.runScript( script )
	return HtmlTag( html, className='editastext', tagID=tagID )
	
	
	
	
	
import unittest


class TestCase_EditAsText (unittest.TestCase):
	def test_editAsText(self):
		from Britefury.gSym.View.Web.WebViewContext import WebViewContext
		from Britefury.gSym.View.Web.WebViewNodeContext import WebViewNodeContext
		vctx = WebViewContext()
		nctx = WebViewNodeContext( vctx )

		expectedHtml = '<span class="editastext" id="editastext0">test</span>'
		expectedScript = \
"""
$("#editastext0").click(
	function (event)
	{
		if ( ( gsym_global_modifierKeysState & 2 )  ==  2 )
		{
			$("#editastext0").html( "<input text="abc">" )
		}
	}
);
"""
		self.assert_( editAsText( nctx, 'test', 'abc', 'shift', 'shift' )  ==  expectedHtml )
		self.assert_( vctx._scriptQueue  ==  [ expectedScript ] )
		

	