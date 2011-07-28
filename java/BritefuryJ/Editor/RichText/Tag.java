//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

abstract class Tag implements Presentable
{
	protected abstract String getTagName();
	
	
	protected static final StyleSheet tagStyle = StyleSheet.instance.withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
}
