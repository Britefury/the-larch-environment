//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Help;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import BritefuryJ.Controls.AbstractHyperlink.AbstractHyperlinkControl;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.RichText.RichSpan;
import BritefuryJ.Pres.UI.SectionHeading3;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class TipBox extends CompositePres
{
	private static final SolidBorder tipBorder = new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.6f, 0.725f, 0.6f ), new Color( 0.965f, 0.965f, 0.965f ) );
	private static final StyleSheet tipControlStyle = StyleSheet.style( Primitive.fontSize.as( 10 ) );
	private static final StyleSheet tipStyle = StyleSheet.style( Primitive.fontSize.as( 11 ), Primitive.foreground.as( new Color( 0.1f, 0.1f, 0.1f ) ), Primitive.editable.as( false ) );
	private static final StyleSheet multilineStyle = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) );
	private static final StyleSheet strongStyle = StyleSheet.style( Primitive.fontBold.as( true ) );
	private static final StyleSheet emphStyle = StyleSheet.style( Primitive.fontItalic.as( true ) );
	private static final double marginX = 15.0, marginY = 5.0;
	
	
	
	private Pres tip;
	private String tipKey;
	private LiveFunction liveFn;
	
	
	public TipBox(Object tip, String tipKey)
	{
		this.tip = Pres.coerce( tip );
		this.tipKey = tipKey;
		liveFn = buildLiveFn();
	}
	
	public TipBox(Object tip)
	{
		this( tip, null );
	}

	public TipBox(String tip, String tipKey)
	{
		this.tip = multilineTextTip( tip );
		this.tipKey = tipKey;
		liveFn = buildLiveFn();
	}
	
	public TipBox(String tip)
	{
		this( tip, null );
	}

	public TipBox(Object tips[], String tipKey)
	{
		Pres p[] = new Pres[tips.length];
		for (int i = 0; i < tips.length; i++)
		{
			Object t = tips[i];
			if ( t instanceof String )
			{
				p[i] = new NormalText( (String)t );
			}
			else
			{
				p[i] = Pres.coerce( t );
			}
		}

		this.tip = multilineTips( p );
		this.tipKey = tipKey;
		liveFn = buildLiveFn();
	}

	public TipBox(Object tips[])
	{
		this( tips, null );
	}


	protected static Pres multilineTextTip(String tip)
	{
		String lineTexts[] = tip.split( "\\r?\\n" );
		Pres lines[] = new Pres[lineTexts.length];
		for (int i = 0; i < lineTexts.length; i++)
		{
			lines[i] = new NormalText( lineTexts[i] );
		}
		return multilineTips( lines );
	}
	
	protected static Pres multilineTips(Pres tips[])
	{
		return multilineStyle.applyTo( new Column( tips ) );
	}


	private LiveFunction buildLiveFn()
	{
		LiveFunction.Function fn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				if ( tipKey != null  &&  isHidden( tipKey ) )
				{
					return new Proxy();
				}
				else
				{
					Pres tipTitle = new SectionHeading3( "Tip" );
					Pres header;

					if ( tipKey != null )
					{
						Hyperlink.LinkListener dismissListener = new Hyperlink.LinkListener()
						{
							@Override
							public void onLinkClicked(AbstractHyperlinkControl link, PointerButtonClickedEvent event)
							{
								setHidden( tipKey, true );
							}
						};
						Hyperlink dismiss = new Hyperlink( "Dismiss", dismissListener );
						
						header = new Column( new Pres[] { tipTitle, new Spacer( 0.0, 5.0 ), tipControlStyle.applyTo( dismiss ) } ).alignVTop();
					}
					else
					{
						header = tipTitle;
					}
					
					Pres t = tipStyle.applyTo( new Bin( tip ) ).alignVRefY();
					t = new Row( new Pres[] { header.alignHPack(), new Spacer( 10.0, 0.0 ).alignHPack(), t.alignHExpand() } );
					t = tipBorder.surround( t );
					return t;
				}
			}
		};
		
		return new LiveFunction( fn );
	}
	
	
	
	
	@Override
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		return liveFn.pad( marginX, marginY );
	}
	
	
	
	private static HashMap<String, LiveValue> tipHiddenFlags = new HashMap<String, LiveValue>();
	
	private static LiveValue hiddenFlag(String key)
	{
		LiveValue live = tipHiddenFlags.get( key );
		if ( live == null )
		{
			live = new LiveValue( false );
			tipHiddenFlags.put( key, live );
		}
		return live;
	}
	
	private static boolean isHidden(String key)
	{
		return (Boolean)hiddenFlag( key ).getValue();
	}
	
	private static void setHidden(String key, boolean hidden)
	{
		hiddenFlag( key ).setLiteralValue( hidden );
	}



	public static Pres tipText(Object values[])
	{
		return new NormalText( values );
	}

	public static Pres emph(String text)
	{
		return emphStyle.applyTo( new RichSpan( text ) );
	}

	public static Pres strong(String text)
	{
		return strongStyle.applyTo( new RichSpan( text ) );
	}


	
	public static void resetTipHiddenStates()
	{
		for (LiveValue state: tipHiddenFlags.values())
		{
			if ( state.getStaticValue() == Boolean.TRUE )
			{
				state.setLiteralValue( false );
			}
		}
	}

	
	public static Map<String, Boolean> getTipHiddenStates()
	{
		HashMap<String, Boolean> states = new HashMap<String, Boolean>();
		
		for (Map.Entry<String, LiveValue> entry: tipHiddenFlags.entrySet())
		{
			states.put( entry.getKey(), (Boolean)entry.getValue().getValue() );
		}
		
		return states;
	}

	public static void initialiseTipHiddenStates(Map<String, Boolean> states)
	{
		for (Map.Entry<String, Boolean> entry: states.entrySet())
		{
			tipHiddenFlags.put( entry.getKey(), new LiveValue( (Boolean)entry.getValue() ) );
		}
	}
}
