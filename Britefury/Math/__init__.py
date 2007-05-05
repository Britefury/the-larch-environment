##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import Math


def lerp(a, b, t):
	return a  +  ( b - a ) * t


#
# COPYING
#

def __primitive__copy__(self):
	return self.__class__( self )

def __primitive__deepcopy__(self, memo):
	return self.__class__( self )


def implementPrimitiveCopyMethods(cls):
	cls.__copy__ = __primitive__copy__
	cls.__deepcopy__ = __primitive__deepcopy__



implementPrimitiveCopyMethods( Math.BBox2 )
implementPrimitiveCopyMethods( Math.Colour3f )
implementPrimitiveCopyMethods( Math.Point2 )
implementPrimitiveCopyMethods( Math.Polygon2 )
implementPrimitiveCopyMethods( Math.Segment2 )
implementPrimitiveCopyMethods( Math.Vector2 )
implementPrimitiveCopyMethods( Math.Xform2 )




# POLYGON ITERATORS
def Polygon__iter__(self):
	for i in xrange( 0, len( self ) ):
		yield self[i]

Math.Polygon2.__iter__ = Polygon__iter__




##
## XML IO
##

#def implementXml(typeName, cls, readFunction, writeFunction):
	#cls.__readxml__ = readFunction
	#cls.__writexml__ = writeFunction
	#IOXml.ioObjectFactoryRegister( typeName, cls )



#def xyFloat__readxml__(self, xmlNode):
	#self.x = IOXml.ioXmlReadFloatProp( xmlNode.property( 'x' ), self.x )
	#self.y = IOXml.ioXmlReadFloatProp( xmlNode.property( 'y' ), self.y )

#def xyFloat__writexml__(self, xmlNode):
	#IOXml.ioXmlWriteFloatProp( xmlNode.property( 'x' ), self.x )
	#IOXml.ioXmlWriteFloatProp( xmlNode.property( 'y' ), self.y )


#def Segment__readxml__(self, xmlNode):
	#xmlNode.getChild( 'a' )  >>  self.a
	#xmlNode.getChild( 'b' )  >>  self.b

#def Segment__writexml__(self, xmlNode):
	#xmlNode.addChild( 'a' )  <<  self.a
	#xmlNode.addChild( 'b' )  <<  self.b


#def BBox__readxml__(self, xmlNode):
	#xmlNode.getChild( 'lower' )  >>  self._lower
	#xmlNode.getChild( 'upper' )  >>  self._upper

#def BBox__writexml__(self, xmlNode):
	#xmlNode.addChild( 'lower' )  <<  self._lower
	#xmlNode.addChild( 'upper' )  <<  self._upper


#def Polygon2__readxml__(self, xmlNode):
	#self.clear()
	#verticesXml = xmlNode.getChild( 'vertices' )
	#if verticesXml.isValid():
		#for vertexXml in verticesXml.childrenNamed( 'vertex' ):
			#v = Point2()
			#vertexXml  >>  v
			#self.append( v )

#def Polygon2__writexml__(self, xmlNode):
	#verticesXml = xmlNode.addChild( 'vertices' )
	#for v in self:
		#verticesXml.addChild( 'vertex' )  <<  v


#implementXml( 'Point2', Math.Point2, xyFloat__readxml__, xyFloat__writexml__ )

#implementXml( 'Segment2', Math.Segment2, Segment__readxml__, Segment__writexml__ )

#implementXml( 'BBox2', Math.BBox2, BBox__readxml__, BBox__writexml__ )

#implementXml( 'Polygon2', Math.Polygon2, Polygon2__readxml__, Polygon2__writexml__ )




def make_xy__str__(typeName):
	def xy__str__(self):
		return '%s(%f,%f)' % ( typeName, self.x, self.y )
	return xy__str__

def make_xyz__str__(typeName):
	def xyz__str__(self):
		return '%s(%f,%f,%f)' % ( typeName, self.x, self.y, self.z )
	return xyz__str__

Math.Vector2.__str__ = make_xy__str__( 'Vector2' )
Math.Point2.__str__ = make_xy__str__( 'Point2' )


def __BBox2_str__(self):
	return 'BBox2( [%f,%f]  ->  [%f,%f] )'  %  ( self._lower.x, self._lower.y, self._upper.x, self._upper.y )

Math.BBox2.__str__ = __BBox2_str__

