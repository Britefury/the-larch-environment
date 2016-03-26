//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

abstract class Tag implements Presentable
{
	protected abstract String getTagName();


	protected static final StyleSheet tagStyle = StyleSheet.style( Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
}
