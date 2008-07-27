##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref


from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
from Britefury.DocPresent.Web.Context.WebViewNodeContext import WebViewNodeContext

from Britefury.DocPresent.Web.WDDomEdit import WDDomEdit


_refStartTag = u'\ue000'
_refEndTag = u'\ue001'



class WDNode (WebViewNodeContext):
	_nodeTable = weakref.WeakValueDictionary()
	
	
	def __init__(self, viewContext, htmlFn):
		super( WDNode, self ).__init__( viewContext )

		self._htmlFn = htmlFn
		self._id = viewContext.allocID( 'WDNode' )
		self._bRefreshRequired = True
		viewContext._queueNodeRefresh( self )
		
		WDNode._nodeTable[self._id] = self
		
		
		
	def htmlForClient(self):
		"""
		Get HTML to send to client
		If this not is not flagged as being already on the client:
			htmlForClient()  ->  html, None
		else:
			htmlForClient()  ->  placeholder_html, placeholder_id
		"""
		if self._bRefreshRequired:
			return self._contentSpan( self._htmlFn( self ) ), None
		else:
			return self._placeholder(), self._id
		
		
	def resolvedSubtreeHtmlForClient(self, nodeIDToResolvedData=None, bUpdateResolvedDataTable=True):
		"""
		Get the HTML to send to the client, for the document subtree rooted at @self.
		All references will have been replaced by the subtree HTML for nodes that are referenced.
		resolvedSubtreeHtmlForClient()  ->  html, resolvedRefNodes, placeHolderIDs
		"""
		if nodeIDToResolvedData is None:
			nodeIDToResolvedData = {}
			bUpdateResolvedDataTable = False
			
		# Set of place holder IDs and nodes that were referred to, and had their references resolved
		placeHolderIDs = set()
		resolvedRefNodes = set()

		# Get the HTML, and add the place holder ID to the place holder ID set if one was given
		html, placeHolderID = self.htmlForClient()
		if placeHolderID is not None:
			placeHolderIDs.add( placeHolderID )
		
		# Loop until no more references to resolve
		while True:
			# Extract the reference IDs from @html
			refIDs = WDNode._getReferenceIDs( html )
			
			# If there are no references to resolve, we are done
			if len( refIDs ) == 0:
				break
			
			# Table mapping reference ID to content to insert
			refIDToContent = {}
			
			# Build the @refIDToContent table
			for refID in refIDs:
				# Get the node that is being referenced from the global table
				node = WDNode._nodeTable[refID]
				# Add the node to the resolved reference list
				resolvedRefNodes.add( node )
				try:
					resolvedHtml, resolvedRefs, resolvedPlaceHolders = nodeIDToResolvedData[refID]
				except KeyError:
					# Get the HTML from the node, and perhaps a place holder ID
					nodeHtml, nodePlaceHolderID = node.htmlForClient()
					# Replace the references in @html with content
					html = html.replace( _refStartTag + refID + _refEndTag,  nodeHtml )
					# Add the place holder ID to the place holder ID list if one was returned
					if nodePlaceHolderID is not None:
						placeHolderIDs.add( nodePlaceHolderID )
				else:
					# Replace the references in @html with content
					html = html.replace( _refStartTag + refID + _refEndTag,  resolvedHtml )
					# Add the resolved ref nodes, and the resolved place holder IDs to the overall sets
					resolvedRefNodes = resolvedRefNodes.union( resolvedRefs )
					placeHolderIDs = placeHolderIDs.union( resolvedPlaceHolders )
					
					# Remove this entry from @nodeIDToResolvedData if requested
					if bUpdateResolvedDataTable:
						del nodeIDToResolvedData[refID]
		
		if bUpdateResolvedDataTable:
			nodeIDToResolvedData[self._id] = html, resolvedRefNodes, placeHolderIDs
		return html, resolvedRefNodes, placeHolderIDs

	
	
	def reference(self):
		return _refStartTag + self._id + _refEndTag
		
		
	def setHtmlFn(self, htmlFn):
		self._htmlFn = htmlFn
		self.contentsChanged()
		
	def contentsChanged(self):
		self._bRefreshRequired = True
		self.viewContext._queueNodeRefresh( self )
		
	def onClient(self):
		self._bRefreshRequired = False
		self.viewContext._dequeueNodeRefresh( self )
		
	
		
	def getID(self):
		return self._id
	
	
	
	def _contentSpan(self, content):
		return '<span class="WDNode" id="%s">'  %  self._id    +    content    +    '</span>'
	
	def _placeholder(self):
		return '<span class="__gsym__placeholder" id="%s"></span>'  %  self._id
	
	
	@staticmethod
	def _getReferenceIDs(x):
		"""
		Find the references in x and extract their IDs
		"""
		start = 0
		end = len( x )
		refIDs = set()
		
		# Loop while we have not reached the end
		while start < end:
			# Try to find a the start tag indicating a reference
			try:
				startTagIndex = x.index( _refStartTag, start )
			except ValueError:
				# Couldn't find a reference; we are finished
				break
			
			# Find the end tag
			try:
				endTagIndex = x.index( _refEndTag, startTagIndex + 1 )
			except ValueError:
				# No end tag; mal-formed reference
				raise ValueError, 'malformed reference; could not find matching end tag'
		
			# Get the reference ID and store
			refIDs.add( x[startTagIndex+1:endTagIndex] )
			
			# Move the start index onward
			start = endTagIndex + 1
		
		return refIDs
	



	
import unittest

class TestCase_WDNode (unittest.TestCase):
	def _makeContext(self):
		from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
		return WebViewContext( None )
	
	

	def test_getID(self):
		ctx = self._makeContext()
		n = WDNode( ctx, lambda nodeContext: '' )
		self.assert_( n.getID() == 'WDNode0' )
	
		
	def test_reference(self):
		ctx = self._makeContext()
		n = WDNode( ctx, lambda nodeContext: '' )
		self.assert_( n.reference() == u'\ue000WDNode0\ue001' )
		
		
	def test_contentSpan(self):
		ctx = self._makeContext()
		n = WDNode( ctx, lambda nodeContext: 'hi' )
		self.assert_( n._contentSpan( 'hi' ) == u'<span class="WDNode" id="WDNode0">hi</span>' )
		
		
	def test_placeholder(self):
		ctx = self._makeContext()
		n = WDNode( ctx, lambda nodeContext: 'hi' )
		self.assert_( n._placeholder() == u'<span class="__gsym__placeholder" id="WDNode0"></span>' )
		
		
	def test_htmlForClient(self):
		ctx = self._makeContext()
		n = WDNode( ctx, lambda nodeContext: 'hi' )
		self.assert_( n.htmlForClient() == ( n._contentSpan( 'hi' ),   None ) )

		
	def test_htmlForClient_alreadyOnClient(self):
		ctx = self._makeContext()
		n = WDNode( ctx, lambda nodeContext: 'hi' )
		n.onClient()
		self.assert_( n.htmlForClient() == ( n._placeholder(),   n.getID() ) )

		n.contentsChanged()
		self.assert_( n.htmlForClient() == ( n._contentSpan( 'hi' ),   None ) )

		
	def test_htmlWithReference(self):
		ctx = self._makeContext()
		n1 = WDNode( ctx, lambda nodeContext: 'hi' )
		n2 = WDNode( ctx, lambda nodeContext: 'a' + n1.reference() + 'b' )
		self.assert_( n2.htmlForClient() == ( n2._contentSpan( u'a\ue000WDNode0\ue001b' ),   None ) )

		
	def test_getReferenceIDs(self):
		self.assert_( WDNode._getReferenceIDs( '' )  ==  set( [] ) )
		self.assert_( WDNode._getReferenceIDs( 'a' + _refStartTag + 'a0' + _refEndTag + 'b' )  ==  set( [ 'a0' ] ) )
		self.assert_( WDNode._getReferenceIDs( 'a' + _refStartTag + 'a0' + _refEndTag + 'b'   +   ' '   +   'c' + _refStartTag + 'a1' + _refEndTag + 'd' )  ==  set( [ 'a0', 'a1' ] ) )
		self.assert_( WDNode._getReferenceIDs( 'a' + _refStartTag + 'a0' + _refEndTag + 'b'   +   ' '   +   'c' + _refStartTag + 'a0' + _refEndTag + 'd' )  ==  set( [ 'a0' ] ) )			
		
		
		
	def test_resolvedSubtreeHtmlForClient(self):
		ctx = self._makeContext()
		n1 = WDNode( ctx, lambda nodeContext: 'hi' )
		n2 = WDNode( ctx, lambda nodeContext: 'a' + n1.reference() + 'b' )
		n3 = WDNode( ctx, lambda nodeContext: 'there' )
		n4 = WDNode( ctx, lambda nodeContext: 'c' + n2.reference() + 'd' + n3.reference() + 'e' )
		
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )
		
		n1.onClient()
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._placeholder() + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [ n1.getID() ] ) ) )
		
		n1.contentsChanged()
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )

		n2.onClient()
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._placeholder()+ 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n2, n3 ] ),   set( [ n2.getID() ] ) ) )

		n2.contentsChanged()
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )

		n2.onClient()
		n3.onClient()
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._placeholder()+ 'd' + n3._placeholder() + 'e' ),    set( [ n2, n3 ] ),   set( [ n2.getID(), n3.getID() ] ) ) )

		n2.contentsChanged()
		n3.contentsChanged()
		self.assert_( n4.resolvedSubtreeHtmlForClient()  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )
		
		nodeIDToResolvedData = {}
		n2Html, n2Resolved, n2Placeholders = n2.resolvedSubtreeHtmlForClient()
		self.assert_( n2Html == n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) )
		self.assert_( n2Resolved == set( [ n1 ] ) )
		self.assert_( n2Placeholders == set( [] ) )
		nodeIDToResolvedData[n2.getID()] = n2Html, n2Resolved, n2Placeholders
		self.assert_( n4.resolvedSubtreeHtmlForClient( nodeIDToResolvedData, False )  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )

		self.assert_( n4.resolvedSubtreeHtmlForClient( nodeIDToResolvedData, True )  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )
		self.assert_( n2.getID() not in nodeIDToResolvedData )
		self.assert_( n4.getID() in nodeIDToResolvedData )
		
		
		nodeIDToResolvedData = {}
		n1.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
		self.assert_( n1.getID() in nodeIDToResolvedData )
		self.assert_( n4.resolvedSubtreeHtmlForClient( nodeIDToResolvedData, False )  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )

		nodeIDToResolvedData = {}
		n2.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
		self.assert_( n2.getID() in nodeIDToResolvedData )
		self.assert_( n4.resolvedSubtreeHtmlForClient( nodeIDToResolvedData, False )  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )

		nodeIDToResolvedData = {}
		n1.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
		n2.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
		n3.resolvedSubtreeHtmlForClient( nodeIDToResolvedData )
		self.assert_( n1.getID() not in nodeIDToResolvedData )
		self.assert_( n2.getID() in nodeIDToResolvedData )
		self.assert_( n3.getID() in nodeIDToResolvedData )
		self.assert_( n4.resolvedSubtreeHtmlForClient( nodeIDToResolvedData, False )  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )
		self.assert_( n1.getID() not in nodeIDToResolvedData )
		self.assert_( n2.getID() in nodeIDToResolvedData )
		self.assert_( n3.getID() in nodeIDToResolvedData )
		
		self.assert_( n4.resolvedSubtreeHtmlForClient( nodeIDToResolvedData, True )  ==  ( n4._contentSpan( u'c' + n2._contentSpan( 'a' + n1._contentSpan( 'hi' ) + 'b' ) + 'd' + n3._contentSpan( 'there' ) + 'e' ),    set( [ n1, n2, n3 ] ),   set( [] ) ) )
		self.assert_( n1.getID() not in nodeIDToResolvedData )
		self.assert_( n2.getID() not in nodeIDToResolvedData )
		self.assert_( n3.getID() not in nodeIDToResolvedData )
		self.assert_( n4.getID() in nodeIDToResolvedData )

		
												    
												    
												    