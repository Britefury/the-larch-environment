//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.ContextMenu;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.ApplyStyleSheetFromAttribute;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleValues;

public class ControlsColumn extends SequentialPres
{
	public ControlsColumn(Object children[])
	{
		super( children );
	}
	
	public ControlsColumn(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement xs[] = mapPresent( ctx, ContextMenuStyle.controlsColumnUsage.useAttrs( style ), children );
		return new ApplyStyleSheetFromAttribute( ContextMenuStyle.controlsColumnStyle, new Column( xs ) ).present( ctx, style );
	}
}
