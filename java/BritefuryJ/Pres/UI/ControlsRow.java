//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.UI;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.ApplyStyleSheetFromAttribute;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleValues;

public class ControlsRow extends SequentialPres
{
	public ControlsRow(Object children[])
	{
		super( children );
	}
	
	public ControlsRow(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement xs[] = mapPresent( ctx, UI.controlsRowUsage.useAttrs( style ), children );
		return new ApplyStyleSheetFromAttribute( UI.controlsRowStyle, new Row( xs ) ).present( ctx, style );
	}
}
