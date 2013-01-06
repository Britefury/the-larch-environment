//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Browser;

import java.awt.Color;
import java.util.List;

import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSViewport;
import BritefuryJ.LSpace.RootPresentationComponent;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Projection.SubjectTrailLink;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

class BrowserTrail extends RootPresentationComponent
{
	private static final long serialVersionUID = 1L;

	private LSViewport viewport;
	private Range xRange;
	private PersistentState viewportState;
	
	
	public BrowserTrail()
	{
		xRange = new Range( 0.0, 10.0, 0.0, 1.0, 2.0 );
		viewportState = new PersistentState();
		ContainerStyleParams params = new ContainerStyleParams( HAlignment.EXPAND, VAlignment.REFY_EXPAND, null, null, null );
		viewport = new LSViewport( params, xRange, null, viewportState, null );
		LSBorder border = new LSBorder( new SolidBorder( 1.0, 1.0, 5.0, 5.0, new Color( 0.5f, 0.5f, 0.5f, 0.5f ), Color.white ), params, viewport );
		getRootElement().setChild( border );
	}
	
	
	void setTrail(List<SubjectTrailLink> trail)
	{
		Pres trailPres;
		
		if ( trail.size() > 0 )
		{
			Pres rowContents[] = new Pres[trail.size() * 2 - 1];
			boolean bFirst = true;
			int i = 0;
			for (SubjectTrailLink link: trail)
			{
				if ( !bFirst )
				{
					rowContents[i++] = arrow;
				}
				rowContents[i++] = link.hyperlink();
				bFirst = false;
			}
			trailPres = new Row( rowContents ).alignHPack();
		}
		else
		{
			trailPres = new Blank();
		}
		LSElement elem = trailPres.present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignVExpand() );
		viewport.setChild( elem );
	}



	private static StyleSheet arrowStyle = StyleSheet.style( Primitive.shapePainter.as( new FilledOutlinePainter( new Color( 0.5f, 0.5f, 0.5f, 0.2f ),
			new Color( 0.5f, 0.5f, 0.5f, 0.8f ) ) )  );
	private static Pres arrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.RIGHT, 12.0 ) ).padX( 7.0 ).alignVCentre();
}
