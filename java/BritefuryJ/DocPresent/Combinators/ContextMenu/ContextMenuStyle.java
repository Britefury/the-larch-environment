//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.ContextMenu;

import java.awt.Color;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeNonNull;
import BritefuryJ.AttributeTable.AttributeUsageSet;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class ContextMenuStyle
{
	public static final AttributeNamespace contextMenuNamespace = new AttributeNamespace( "contextMenu" );
	
	
	public static final AttributeNonNull sectionTitleStyle = new AttributeNonNull( contextMenuNamespace, "sectionTitleStyle", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.3f, 0.5f ) ).withAttr( Primitive.fontBold, true ) );
	public static final AttributeNonNull sectionVBoxStyle = new AttributeNonNull( contextMenuNamespace, "sectionVBoxStyle", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 5.0 ) );
	public static final AttributeNonNull controlsHBoxStyle = new AttributeNonNull( contextMenuNamespace, "controlsHBoxStyle", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 10.0 ) );
	public static final AttributeNonNull controlsVBoxStyle = new AttributeNonNull( contextMenuNamespace, "sectionVBoxStyle", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 2.0 ) );
	
	
	
	protected static final AttributeUsageSet sectionVBoxUsage = new AttributeUsageSet( sectionVBoxStyle );
	protected static final AttributeUsageSet controlsHBoxUsage = new AttributeUsageSet( controlsHBoxStyle );
	protected static final AttributeUsageSet controlsVBoxUsage = new AttributeUsageSet( controlsVBoxStyle );
}
