//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.awt.datatransfer.DataFlavor;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public abstract class Pres
{
	public DPElement present()
	{
		return present( new PresentationContext() );
	}

	public abstract DPElement present(PresentationContext ctx);
	
	
	
	public Align align(HAlignment hAlign, VAlignment vAlign)
	{
		return new Align( hAlign, vAlign, this );
	}
	

	public Align alignH(HAlignment hAlign)
	{
		return new Align( hAlign, this );
	}
	
	public Align alignV(VAlignment vAlign)
	{
		return new Align( vAlign, this );
	}
	

	public Align alignHPack()
	{
		return alignH( HAlignment.PACK );
	}

	public Align alignHLeft()
	{
		return alignH( HAlignment.LEFT );
	}

	public Align alignHCentre()
	{
		return alignH( HAlignment.CENTRE );
	}

	public Align alignHRight()
	{
		return alignH( HAlignment.RIGHT );
	}

	public Align alignHExpand()
	{
		return alignH( HAlignment.EXPAND );
	}
	
	
	public Align alignVRefY()
	{
		return alignV( VAlignment.REFY );
	}

	public Align alignVRefYExpand()
	{
		return alignV( VAlignment.REFY_EXPAND );
	}

	public Align alignVTop()
	{
		return alignV( VAlignment.TOP );
	}

	public Align alignVCentre()
	{
		return alignV( VAlignment.CENTRE );
	}

	public Align alignVBottom()
	{
		return alignV( VAlignment.BOTTOM );
	}

	public Align alignVExpand()
	{
		return alignV( VAlignment.EXPAND );
	}
	
	
	
	//
	// Padding methods
	//
	
	public Pres pad(double leftPad, double rightPad, double topPad, double bottomPad)
	{
		return new Pad( this, leftPad, rightPad, topPad, bottomPad );
	}
	
	public Pres pad(double xPad, double yPad)
	{
		return pad( xPad, xPad, yPad, yPad );
	}
	
	public Pres padX(double xPad)
	{
		return pad( xPad, xPad, 0.0, 0.0 );
	}
	
	public Pres padX(double leftPad, double rightPad)
	{
		return pad( leftPad, rightPad, 0.0, 0.0 );
	}
	
	public Pres padY(double yPad)
	{
		return pad( 0.0, 0.0, yPad, yPad );
	}
	
	public Pres padY(double topPad, double bottomPad)
	{
		return pad( 0.0, 0.0, topPad, bottomPad );
	}
	
	
	
	//
	// Element reference
	//
	
	public ElementRef elementRef()
	{
		return new ElementRef( this );
	}
	
	
	
	//
	// Drag and drop
	//
	
	public AddDragSource addDragSource(ObjectDndHandler.DragSource source)
	{
		return new AddDragSource( this, source );
	}
	
	public AddDragSource addDragSource(Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		return new AddDragSource( this, dataType, sourceAspects, sourceDataFn, exportDoneFn );
	}
	
	public AddDragSource addDragSource(Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		return new AddDragSource( this, dataType, sourceAspects, sourceDataFn );
	}
	
	
	public AddDropDest addDropDest(ObjectDndHandler.DropDest dest)
	{
		return new AddDropDest( this, dest );
	}
	
	public AddDropDest addDropDest(Class<?> dataType, ObjectDndHandler.CanDropFn canDropFn, ObjectDndHandler.DropFn dropFn)
	{
		return new AddDropDest( this, dataType, canDropFn, dropFn );
	}
	
	public AddDropDest addDropDest(Class<?> dataType, ObjectDndHandler.DropFn dropFn)
	{
		return new AddDropDest( this, dataType, dropFn );
	}
	
	
	public AddNonLocalDropDest addNonLocalDropDest(ObjectDndHandler.NonLocalDropDest dest)
	{
		return new AddNonLocalDropDest( this, dest );
	}
	
	public AddNonLocalDropDest addNonLocalDropDest(DataFlavor dataFlavor, ObjectDndHandler.DropFn dropFn)
	{
		return new AddNonLocalDropDest( this, dataFlavor, dropFn );
	}
	
	
	public AddInteractor addInteractor(ElementInteractor interactor)
	{
		return new AddInteractor( this, interactor );
	}
	
	
	
	
	
	protected static DPElement[] mapPresent(PresentationContext ctx, Pres children[])
	{
		DPElement result[] = new DPElement[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = children[i].present( ctx );
		}
		return result;
	}
	
	protected static DPElement[] mapPresent(PresentationContext ctx, List<Pres> children)
	{
		DPElement result[] = new DPElement[children.size()];
		int i = 0;
		for (Pres child: children)
		{
			result[i++] = child.present( ctx );
		}
		return result;
	}
	
	
	protected static PresentElement presentAsCombinator(PresentationContext ctx, Pres child)
	{
		return new PresentElement( child.present( ctx ) );
	}
	
	protected static PresentElement[] mapPresentAsCombinators(PresentationContext ctx, Pres children[])
	{
		PresentElement result[] = new PresentElement[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = new PresentElement( children[i].present( ctx ) );
		}
		return result;
	}
	
	protected static PresentElement[] mapPresentAsCombinators(PresentationContext ctx, List<Pres> children)
	{
		PresentElement result[] = new PresentElement[children.size()];
		int i = 0;
		for (Pres child: children)
		{
			result[i++] = new PresentElement( child.present( ctx ) );
		}
		return result;
	}
	
	
	protected static DPElement higherOrderPresent(PresentationContext ctx, StyleSheetValues style, Pres combinator)
	{
		return combinator.present( ctx.withStyle( style ) );
	}
	
	
	
	public static Pres coerce(Object x)
	{
		if ( x == null )
		{
			return null;
		}
		else if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else if ( x instanceof DPElement )
		{
			return new PresentElement( (DPElement)x );
		}
		else
		{
			return new PresentObject( x );
		}
	}

	public static Pres[] mapCoerce(Object children[])
	{
		Pres result[] = new Pres[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = coerce( children[i] );
		}
		return result;
	}
	

	public static Pres[] mapCoerce(List<Object> children)
	{
		Pres result[] = new Pres[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			result[i] = coerce( children.get( i ) );
		}
		return result;
	}
}
