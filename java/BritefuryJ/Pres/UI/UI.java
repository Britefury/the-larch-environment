//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.UI;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;

public class UI
{
	public static final AttributeNamespace uiNamespace = new AttributeNamespace( "ui" );
	
	
	public static final InheritedAttributeNonNull bubblePopupBorderWidth = new InheritedAttributeNonNull( uiNamespace, "bubblePopupBorderWidth", Double.class, 4.0 );
	public static final InheritedAttributeNonNull bubblePopupCornerRadius = new InheritedAttributeNonNull( uiNamespace, "bubblePopupCornerRadius", Double.class, 8.0 );
	public static final InheritedAttributeNonNull bubblePopupArrowLength = new InheritedAttributeNonNull( uiNamespace, "bubblePopupArrowLength", Double.class, 12.0 );
	public static final InheritedAttributeNonNull bubblePopupArrowWidth = new InheritedAttributeNonNull( uiNamespace, "bubblePopupArrowWidth", Double.class, 12.0 );
}
