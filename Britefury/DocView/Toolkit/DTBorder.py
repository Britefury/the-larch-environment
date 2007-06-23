##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocView.Toolkit.DTBin import DTBin



class DTBorder (DTBin):
	def __init__(self, leftMargin=0.0, rightMargin=0.0, topMargin=0.0, bottomMargin=0.0, backgroundColour=None):
		super( DTBorder, self ).__init__( backgroundColour )

		self._leftMargin = leftMargin
		self._rightMargin = rightMargin
		self._topMargin = topMargin
		self._bottomMargin = bottomMargin


	def setLeftMargin(self, leftMargin):
		self._leftMargin = leftMargin
		self._o_queueResize()

	def getLeftMargin(self):
		return self._leftMargin


	def setRightMargin(self, rightMargin):
		self._rightMargin = rightMargin
		self._o_queueResize()

	def getRightMargin(self):
		return self._rightMargin


	def setTopMargin(self, topMargin):
		self._topMargin = topMargin
		self._o_queueResize()

	def getTopMargin(self):
		return self._topMargin


	def setBottomMargin(self, bottomMargin):
		self._bottomMargin = bottomMargin
		self._o_queueResize()

	def getBottomMargin(self):
		return self._bottomMargin


	def setAllMargins(self, margin):
		self._leftMargin = margin
		self._rightMargin = margin
		self._topMargin = margin
		self._bottomMargin = margin
		self._o_queueResize()



	def _o_getRequiredWidth(self):
		if self._child is not None:
			self._childRequisition.x = self._child._f_getRequisitionWidth()
		else:
			self._childRequisition.x = 0.0
		return self._childRequisition.x + self._leftMargin + self._rightMargin

	def _o_getRequiredHeight(self):
		if self._child is not None:
			self._childRequisition.y = self._child._f_getRequisitionHeight()
		else:
			self._childRequisition.x = 0.0
		return self._childRequisition.y + self._topMargin + self._bottomMargin


	def _o_onAllocateX(self, allocation):
		if self._child is not None:
			self._o_allocateChildX( self._child, self._leftMargin, allocation - self._leftMargin - self._rightMargin )

	def _o_onAllocateY(self, allocation):
		if self._child is not None:
			self._o_allocateChildY( self._child, self._topMargin, allocation - self._topMargin - self._bottomMargin )



	leftMargin = property( getLeftMargin, setLeftMargin )
	rightMargin = property( getRightMargin, setRightMargin )
	topMargin = property( getTopMargin, setTopMargin )
	bottomMargin = property( getBottomMargin, setBottomMargin )
	allMargins = property( None, setAllMargins )
