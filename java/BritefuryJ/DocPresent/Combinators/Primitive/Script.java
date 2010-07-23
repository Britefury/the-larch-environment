//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Script extends Pres
{
	private Pres main, leftSuper, leftSub, rightSuper, rightSub;
	
	public Script(Object main, Object leftSuper, Object leftSub, Object rightSuper, Object rightSub)
	{
		this.main = coerce( main );
		this.leftSuper = coerce( leftSuper );
		this.leftSub = coerce( leftSub );
		this.rightSuper = coerce( rightSuper );
		this.rightSub = coerce( rightSub );
	}
	
	
	
	public static Script scriptLSuper(Object main, Object leftSuper)
	{
		return new Script( main, leftSuper, null, null, null );
	}
	
	public static Script scriptLSub(Object main, Object leftSub)
	{
		return new Script( main, null, leftSub, null, null );
	}
	
	public static Script scriptRSuper(Object main, Object rightSuper)
	{
		return new Script( main, null, null, rightSuper, null );
	}
	
	public static Script scriptRSub(Object main, Object rightSub)
	{
		return new Script( main, null, null, null, rightSub );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		StyleSheetValues usedStyle = style.useScriptParams().useTextParams();
		PresentationContext childCtx = null;
		if ( leftSuper != null  ||  leftSub != null  ||  rightSuper != null  ||  rightSub != null )
		{
			childCtx = ctx.withStyle( scriptScriptChildStyle( usedStyle ) );
		}
		
		DPScript element = new DPScript( style.getScriptParams(), style.getTextParams() );
		element.setMainChild( main.present( ctx.withStyle( usedStyle ) ) );
		if ( leftSuper != null )
		{
			element.setLeftSuperscriptChild( leftSuper.present( childCtx ) );
		}
		if ( leftSub != null )
		{
			element.setLeftSubscriptChild( leftSub.present( childCtx ) );
		}
		if ( rightSuper != null )
		{
			element.setRightSuperscriptChild( rightSuper.present( childCtx ) );
		}
		if ( rightSub != null )
		{
			element.setRightSubscriptChild( rightSub.present( childCtx ) );
		}
		return element;
	}



	private static StyleSheetValues scriptScriptChildStyle(StyleSheetValues style)
	{
		double scale = style.get( StyleSheet2.fontScale, Double.class );
		double scriptScale = style.get( StyleSheet2.scriptFontScale, Double.class );
		double minScriptScale = style.get( StyleSheet2.scriptMinFontScale, Double.class );
		scale = Math.max( scale * scriptScale, minScriptScale );
		return style.withAttr( StyleSheet2.fontScale, scale );
	}
}
