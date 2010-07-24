//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class Pres
{
	public static class PresentationContext
	{
		private GSymFragmentView fragment = null;
		private GSymAbstractPerspective perspective = null;
		private StyleSheetValues style;
		private AttributeTable inheritedState = null;
		
		
		public PresentationContext()
		{
			style = StyleSheetValues.instance;
		}
		
		public PresentationContext(GSymFragmentView fragment, GSymAbstractPerspective perspective, StyleSheetValues style, AttributeTable inheritedState)
		{
			this.fragment = fragment;
			this.perspective = perspective;
			this.style = style;
			this.inheritedState = inheritedState;
		}
		
		
		public GSymFragmentView getFragment()
		{
			return fragment;
		}
		
		public GSymAbstractPerspective getPerspective()
		{
			return perspective;
		}
		
		public StyleSheetValues getStyle()
		{
			return style;
		}
		
		public AttributeTable getInheritedState()
		{
			return inheritedState;
		}
		
		
		public PersistentState persistentState(Object key)
		{
			if ( fragment != null )
			{
				return fragment.persistentState( key );
			}
			else
			{
				return null;
			}
		}
		
		
		public PresentationContext withStyle(StyleSheetValues style)
		{
			if ( style == this.style )
			{
				return this;
			}
			else
			{
				return new PresentationContext( fragment, perspective, style, inheritedState );
			}
		}
	}
	
	
	
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
	// Custom handler
	//
	
	public Pres customAction(CustomAction handler)
	{
		return new CustomElementAction( this, handler );
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
	
	
	protected static DPElement higherOrderPresent(PresentationContext ctx, StyleSheetValues style, Pres combinator)
	{
		return combinator.present( ctx.withStyle( style ) );
	}
	
	
	
	protected static Pres coerce(Object x)
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

	protected static Pres[] mapCoerce(Object children[])
	{
		Pres result[] = new Pres[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = coerce( children[i] );
		}
		return result;
	}
	

	protected static Pres[] mapCoerce(List<Object> children)
	{
		Pres result[] = new Pres[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			result[i] = coerce( children.get( i ) );
		}
		return result;
	}
}
