//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSScript;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Script extends Pres
{
	private Pres main, leftSuper, leftSub, rightSuper, rightSub;
	
	public Script(Object main, Object leftSuper, Object leftSub, Object rightSuper, Object rightSub)
	{
		this.main = coerce( main );
		this.leftSuper = coerceNullable( leftSuper );
		this.leftSub = coerceNullable( leftSub );
		this.rightSuper = coerceNullable( rightSuper );
		this.rightSub = coerceNullable( rightSub );
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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Primitive.useScriptParams( Primitive.useTextParams( style ) );
		StyleValues childStyle = null;
		if ( leftSuper != null  ||  leftSub != null  ||  rightSuper != null  ||  rightSub != null )
		{
			childStyle = scriptScriptChildStyle( usedStyle );
		}
		
		LSElement leftSuperElem = leftSuper != null  ?  leftSuper.present( ctx, childStyle )  :  null;
		LSElement leftSubElem = leftSub != null  ?  leftSub.present( ctx, childStyle )  :  null;
		LSElement rightSuperElem = rightSuper != null  ?  rightSuper.present( ctx, childStyle )  :  null;
		LSElement rightSubElem = rightSub != null  ?  rightSub.present( ctx, childStyle )  :  null;
		LSElement mainElem = main.present( ctx, usedStyle );
		return new LSScript( Primitive.scriptParams.get( style ), Primitive.caretSlotParams.get( style ), leftSuperElem, leftSubElem, mainElem, rightSuperElem, rightSubElem );
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
