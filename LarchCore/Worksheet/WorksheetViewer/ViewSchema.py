##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.IncrementalUnit import Unit

from BritefuryJ.Pres import InnerFragment


from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet import AbstractViewSchema





class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return dmObjectNodeMethodDispatch( self, node, worksheet )

	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def worksheet(self, worksheet, node):
		return WorksheetView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Body )
	def body(self, worksheet, node):
		return BodyView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, worksheet, node):
		return ParagraphView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def textSpan(self, worksheet, node):
		return TextSpanView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def pythonCode(self, worksheet, node):
		return PythonCodeView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.QuoteLocation )
	def quoteLocation(self, worksheet, node):
		return QuoteLocationView( worksheet, node )



class WorksheetView (AbstractViewSchema.WorksheetAbstractView):
	_projection = _Projection()

	def __init__(self, worksheet, model):
		super( WorksheetView, self ).__init__( worksheet, model )

		

class BodyView (AbstractViewSchema.BodyAbstractView):
	def __init__(self, worksheet, model):
		super( BodyView, self ).__init__( worksheet, model )

		

class ParagraphView (AbstractViewSchema.ParagraphAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphView, self ).__init__( worksheet, model )

		
		
class TextSpanView (AbstractViewSchema.TextSpanAbstractView):
	def __init__(self, worksheet, model):
		super( TextSpanView, self ).__init__( worksheet, model )

		
		
class PythonCodeView (AbstractViewSchema.PythonCodeAbstractView):
	def __init__(self, worksheet, model):
		super( PythonCodeView, self ).__init__( worksheet, model )

	
	
class QuoteLocationView (AbstractViewSchema.QuoteLocationAbstractView):
	def __init__(self, worksheet, model):
		super( QuoteLocationView, self ).__init__( worksheet, model )
