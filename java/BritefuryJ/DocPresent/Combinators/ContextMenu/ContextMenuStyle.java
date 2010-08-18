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
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ContextMenuStyle
{
	public static final AttributeNamespace contextMenuNamespace = new AttributeNamespace( "contextMenu" );
	
	
	public static final AttributeNonNull sectionTitleStyle = new AttributeNonNull( contextMenuNamespace, "sectionTitleStyle", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.3f, 0.5f ) ).withAttr( Primitive.fontBold, true ) );
	public static final AttributeNonNull sectionColumnStyle = new AttributeNonNull( contextMenuNamespace, "sectionColumnStyle", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ) );
	public static final AttributeNonNull controlsRowStyle = new AttributeNonNull( contextMenuNamespace, "controlsRowStyle", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.rowSpacing, 10.0 ) );
	public static final AttributeNonNull controlsColumnStyle = new AttributeNonNull( contextMenuNamespace, "controlsColumnStyle", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.columnSpacing, 2.0 ) );
	
	
	
	protected static final AttributeUsageSet sectionColumnUsage = new AttributeUsageSet( sectionColumnStyle );
	protected static final AttributeUsageSet controlsRowUsage = new AttributeUsageSet( controlsRowStyle );
	protected static final AttributeUsageSet controlsColumnUsage = new AttributeUsageSet( controlsColumnStyle );
}
