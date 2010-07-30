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
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Primitive.useScriptParams( Primitive.useTextParams( style ) );
		StyleValues childStyle = null;
		if ( leftSuper != null  ||  leftSub != null  ||  rightSuper != null  ||  rightSub != null )
		{
			childStyle = scriptScriptChildStyle( usedStyle );
		}
		
		DPScript element = new DPScript( Primitive.scriptParams.get( style ), Primitive.textParams.get( style ) );
		element.setMainChild( main.present( ctx, usedStyle ) );
		if ( leftSuper != null )
		{
			element.setLeftSuperscriptChild( leftSuper.present( ctx, childStyle ) );
		}
		if ( leftSub != null )
		{
			element.setLeftSubscriptChild( leftSub.present( ctx, childStyle ) );
		}
		if ( rightSuper != null )
		{
			element.setRightSuperscriptChild( rightSuper.present( ctx, childStyle ) );
		}
		if ( rightSub != null )
		{
			element.setRightSubscriptChild( rightSub.present( ctx, childStyle ) );
		}
		return element;
	}



	private static StyleValues scriptScriptChildStyle(StyleValues style)
	{
		double scale = style.get( Primitive.fontScale, Double.class );
		double scriptScale = style.get( Primitive.scriptFontScale, Double.class );
		double minScriptScale = style.get( Primitive.scriptMinFontScale, Double.class );
		scale = Math.max( scale * scriptScale, minScriptScale );
		return style.withAttr( Primitive.fontScale, scale );
	}
}
