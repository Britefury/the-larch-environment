##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTBin import DTBin
from Britefury.DocPresent.Toolkit.DTFlow import DTFlow
from Britefury.DocPresent.Toolkit.DTFlowWithSeparators import DTFlowWithSeparators

from Britefury.DocView.DVNode import DVNode




def _listViewCoerce(x):
	if isinstance( x, str )  or  isinstance( x, unicode ):
		return DTLabel( x )
	else:
		return x


def _listViewFactoryCoerce(x):
	if isinstance( x, str )  or  isinstance( x, unicode ):
		return lambda: DTLabel( x )
	else:
		return x

	
	


class ListViewLayout (object):
	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		pass
	
	def _p_itemWithSeparator(self, item, separatorFactory):
		if separatorFactory is not None:
			b = DTBox( alignment=DTBox.ALIGN_BASELINES )
			b[:] = [ item, separatorFactory() ]
			return b
		else:
			return item
	
	def _p_itemsInHBox(self, spacing, *items):
		b = DTBox( alignment=DTBox.ALIGN_BASELINES, spacing=spacing )
		b[:] = items
		return b
	
	def _p_getContentWidgets(self, contents, xs):
		widgets = []
		for c in contents:
			if isinstance( c, DVNode ):
				c.refresh()
				widgets.append( c.widget )
			elif isinstance( c, DTWidget ):
				widgets.append( c )
			else:
				raiseRuntimeError( TypeError, xs, 'ListViewLayout._p_getContentWidgets: could not process child of type %s'  %  ( type( c ).__name__, ) )
		return widgets
		
	
	
	def _p_buildRefreshCell(self, refreshFunction):
		cell = Cell()
		cell.function = refreshFunction
		return cell
	
	
	
	
class FlowListViewLayout (ListViewLayout):
	def __init__(self, spacing, delimSpacing):
		super( FlowListViewLayout, self ).__init__()
		self._spacing = spacing
		self._contentsPadding = delimSpacing * 2.0
		
		
	
	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		separatorFactory = _listViewFactoryCoerce( separatorFactory )

		def _refresh():
			if len( contents ) > 0:
				contentWidgets = self._p_getContentWidgets( contents, xs )
				contentsContainer[:] = [ self._p_itemWithSeparator( c, separatorFactory )   for c in contentWidgets[:-1] ]  +  [ contentWidgets[-1] ]
		refreshCell = self._p_buildRefreshCell( _refresh )
			
		# Produce contents container
		contentsContainer = DTFlow( self._spacing )
		
		# Wrap in delimeter container if necessary
		if beginDelim is not None  or  endDelim is not None:
			box = DTBox( alignment=DTBox.ALIGN_BASELINES )
			if beginDelim is not None:
				beginDelim = _listViewCoerce( beginDelim )
				box.append( beginDelim )
			box.append( contentsContainer, padding=self._contentsPadding )
			if endDelim is not None:
				endDelim = _listViewCoerce( endDelim )
				box.append( endDelim )
			return box, refreshCell
		else:
			return contentsContainer, refreshCell


		
		
		
class HorizontalListViewLayout (ListViewLayout):
	def __init__(self, spacing, delimSpacing):
		super( HorizontalListViewLayout, self ).__init__()
		self._spacing = spacing
		self._contentsPadding = delimSpacing * 2.0


	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		separatorFactory = _listViewFactoryCoerce( separatorFactory )
		
		def _refresh():
			if len( contents ) > 0:
				contentWidgets = self._p_getContentWidgets( contents, xs )
				contentsContainer[:] = [ self._p_itemWithSeparator( c, separatorFactory )   for c in contentWidgets[:-1] ]  +  [ contentWidgets[-1] ]
		refreshCell = self._p_buildRefreshCell( _refresh )

		# Produce contents container
		contentsContainer = DTBox( alignment=DTBox.ALIGN_BASELINES, spacing=self._spacing )
		
		# Wrap in delimeter container if necessary
		if beginDelim is not None  or  endDelimFactory is not None:
			box = DTBox( alignment=DTBox.ALIGN_BASELINES )
			if beginDelim is not None:
				beginDelim = _listViewCoerce( beginDelim )
				box.append( beginDelim )
			box.append( contentsContainer, padding=self._contentsPadding )
			if endDelim is not None:
				endDelim = _listViewCoerce( endDelim )
				box.append( endDelim )
			return box, refreshCell
		else:
			return contentsContainer, refreshCell


	
class VerticalInlineListViewLayout (ListViewLayout):
	def __init__(self, spacing, delimSpacing, indentation=0.0):
		super( VerticalInlineListViewLayout, self ).__init__()
		self._spacing = spacing
		self._delimSpacing = delimSpacing
		self._indentation = indentation
		
		
	def _p_indent(self, x):
		b = DTBorder( leftMargin=self._indentation )
		b.child = x
		return b


	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		separatorFactory = _listViewFactoryCoerce( separatorFactory )
		
		if beginDelim is not None:
			beginDelim = _listViewCoerce( beginDelim )
		
		if endDelim is not None:
			endDelim = _listViewCoerce( endDelim )
			
			
		bin = DTBin()
			
			
		def _refresh():
			if len( contents ) <= 1:
				contentWidgets = self._p_getContentWidgets( contents, xs )
				contentWidgets = [ beginDelim ] + contentWidgets   if beginDelim is not None   else contentWidgets
				contentWidgets = contentWidgets + [ endDelim ]   if endDelim is not None   else contentWidgets
				bin.child = self._p_itemsInHBox( self._delimSpacing, *contentWidgets )
			elif len( contents ) > 0:
				contentWidgets = self._p_getContentWidgets( contents, xs )
				first = self._p_itemsInHBox( self._delimSpacing, beginDelim, contentWidgets[0] )   if beginDelim is not None   else contentWidgets[0]
				#last = self._p_indent( self._p_itemsInHBox( self._delimSpacing, contentWidgets[-1], endDelim )   if endDelim is not None   else contentWidgets[-1] )
				last = endDelim
				#middle = [ self._p_indent( c )   for c in contentWidgets[1:-1] ]
				middle = [ self._p_indent( c )   for c in contentWidgets[1:] ]
				
				contentsContainer = DTBox( direction=DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_LEFT, spacing=self._spacing )
				contentsContainer[:] = [ first ]  +  middle  +  [ last ]
			
				bin.child = contentsContainer
		refreshCell = self._p_buildRefreshCell( _refresh )

		
		return bin, refreshCell

	
	
	
class VerticalListViewLayout (ListViewLayout):
	def __init__(self, spacing, delimSpacing, indentation):
		super( VerticalListViewLayout, self ).__init__()
		self._spacing = spacing
		self._contentsPadding = delimSpacing * 2.0
		self._indentation = 30.0
		

	def layoutContents(self, xs, contents, beginDelim, endDelim, separatorFactory):
		separatorFactory = _listViewFactoryCoerce( separatorFactory )
		
		def _refresh():
			if len( contents ) > 0:
				contentWidgets = self._p_getContentWidgets( contents, xs )
				contentsContainer[:] = [ self._p_itemWithSeparator( c, separatorFactory )   for c in contentWidgets[:-1] ]  +  [ contentWidgets[-1] ]
		refreshCell = self._p_buildRefreshCell( _refresh )

		
		# Produce contents container
		contentsContainer = DTBox( direction=DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_LEFT, spacing=self._spacing )
			
		indent = DTBorder( leftMargin=self._indentation )
		indent.child = contentsContainer
		
		# Wrap in delimeter container if necessary
		if beginDelim is not None  or  endDelim is not None:
			box = DTBox( direction=DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_LEFT )
			if beginDelim is not None:
				beginDelim = _listViewCoerce( beginDelim )
				box.append( beginDelim )
			box.append( indent, padding=self._contentsPadding )
			if endDelim is not None:
				endDelim = _listViewCoerce( endDelim )
				box.append( endDelim )
			return box, refreshCell
		else:
			return indent, refreshCell


	

		

def listView(xs, layout, beginDelim, endDelim, separatorFactory, contents):
	return layout.layoutContents( xs, contents, beginDelim, endDelim, separatorFactory )




