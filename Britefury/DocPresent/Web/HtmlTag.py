##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



class HtmlTag (object):
	def __init__(self, innerHtml, tag=None, className=None, tagID=None):
		if isinstance( innerHtml, HtmlTag ):
			if ( tag is not None   and   innerHtml._tag is not None )   or   ( tagID is not None   and   innerHtml._id is not None ):
				self._innerHtml = innerHtml.html()
				self._tag = tag
				self._id = tagID
				self._classNames = [ className ]   if className is not None   else   []
			else:
				self._innerHtml = innerHtml._innerHtml
				self._tag = tag   if tag is not None   else   innerHtml._tag
				self._id = tagID   if tagID is not None   else   innerHtml._id
				self._classNames = innerHtml._classNames + [ className ]   if className is not None   else   innerHtml._classNames
		else:
			self._innerHtml = innerHtml
			self._tag = tag
			self._id = tagID
			self._classNames = [ className ]   if className is not None   else   []
		
		
	def html(self):
		tag = self._tag   if self._tag is not None   else   'span'
		classNames = ( 'class="%s"'  %  ' '.join( self._classNames ) )   if self._classNames != []   else   ''
		tagID = 'id="%s"'  %  self._id   if self._id is not None   else   ''
		tagHdr = tag
		if classNames != '':
			tagHdr += ' ' + classNames
		if tagID != '':
			tagHdr += ' ' + tagID
		return '<%s>%s</%s>'  %  ( tagHdr, self._innerHtml, tag )
	
	
	def __eq__(self, x):
		if isinstance( x, HtmlTag ):
			x = x.html()
		return self.html()  ==  x
	
	
	def __ne__(self, x):
		if isinstance( x, HtmlTag ):
			x = x.html()
		return self.html()  !=  x
	
	
	
	
import unittest


class TestCase_ApplyHtml (unittest.TestCase):
	def testApply(self):
		self.assert_( HtmlTag( 'a' ).html()  ==  '<span>a</span>' )
		self.assert_( HtmlTag( 'a', tag='div' ).html()  ==  '<div>a</div>' )
		self.assert_( HtmlTag( 'a', className='p' ).html()  ==  '<span class="p">a</span>' )
		self.assert_( HtmlTag( 'a', tagID='e1' ).html()  ==  '<span id="e1">a</span>' )

	def testApplyRecursive(self):
		t = HtmlTag( 'a' )
		t2 = HtmlTag( 'a', tag='div', className='p', tagID='e1' )
		self.assert_( HtmlTag( t ).html()  ==  '<span>a</span>' )
		self.assert_( HtmlTag( t, tag='div' ).html()  ==  '<div>a</div>' )
		self.assert_( HtmlTag( t, className='p' ).html()  ==  '<span class="p">a</span>' )
		self.assert_( HtmlTag( t, tagID='e1' ).html()  ==  '<span id="e1">a</span>' )

		self.assert_( HtmlTag( t2 ).html()  ==  '<div class="p" id="e1">a</div>' )
		self.assert_( HtmlTag( t2, tag='dd' ).html()  ==  '<dd><div class="p" id="e1">a</div></dd>' )
		self.assert_( HtmlTag( t2, className='q' ).html()  ==  '<div class="p q" id="e1">a</div>' )
		self.assert_( HtmlTag( t2, tagID='e2' ).html()  ==  '<span id="e2"><div class="p" id="e1">a</div></span>' )
		
	def testCmp(self):
		self.assert_( HtmlTag( 'a' )  ==  '<span>a</span>' )
