//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import EDU.Washington.grad.gjb.cassowary.ClLinearEquation;
import EDU.Washington.grad.gjb.cassowary.ClLinearExpression;
import EDU.Washington.grad.gjb.cassowary.ClSimplexSolver;
import EDU.Washington.grad.gjb.cassowary.ClVariable;
import EDU.Washington.grad.gjb.cassowary.ExCLInternalError;
import EDU.Washington.grad.gjb.cassowary.ExCLNonlinearExpression;
import EDU.Washington.grad.gjb.cassowary.ExCLRequiredFailure;

public class LSConstrainedContainer extends LSContainerNonOverlayed
{
	public static interface ConstrainedExpression
	{
		ClLinearExpression expression(LSConstrainedContainer element);
	}
	
	public static class ConstrainedChild
	{
		private static class Exp implements ConstrainedExpression
		{
			private ClLinearExpression e;
			
			private Exp(ClLinearExpression e)
			{
				this.e = e;
			}
			
			public ClLinearExpression expression(LSConstrainedContainer element)
			{
				return e;
			}
		}
		
		private LSElement child;
		private ClVariable x, y, minWidth, prefWidth, allocWidth, refY, height;
		private Exp hLeft, hCentre, hRight, vTop, vCentre, vBottom, vRefY;
		private HashMap<Double, Exp> hPropVars, vPropVars;
		
		
		public ConstrainedChild(LSElement child)
		{
			this.child = child;
			
			x = new ClVariable( "x" );
			y = new ClVariable( "y" );
			
		}
		
		
		public ConstrainedExpression hLeft()
		{
			if ( hLeft == null )
			{
				hLeft = new Exp( new ClLinearExpression( x ) ));
			}
			return hLeft;
		}

		public ConstrainedExpression hCentre()
		{
			hLeft();
			hRight();
			if ( hCentre == null )
			{
				hCentre = new Exp( new ClLinearExpression( x ));
			}
			return hCentre;
		}

		public ConstrainedExpression hRight()
		{
			if ( hRight == null )
			{
				hRight = new Exp();
			}
			return hRight;
		}

		public ConstrainedExpression vTop()
		{
			if ( vTop == null )
			{
				vTop = new Exp();
			}
			return vTop;
		}

		public ConstrainedExpression vCentre()
		{
			vTop();
			vBottom();
			if ( vCentre == null )
			{
				vCentre = new Exp();
			}
			return vCentre;
		}

		public ConstrainedExpression vBottom()
		{
			if ( vBottom == null )
			{
				vBottom = new Exp();
			}
			return vBottom;
		}

		public ConstrainedExpression vRefY()
		{
			if ( vRefY == null )
			{
				vRefY = new Exp();
			}
			return vRefY;
		}
		
		
		public ConstrainedExpression hProp(double p)
		{
			hLeft();
			hRight();
			Exp v = hPropVars.get( p );
			if ( v == null )
			{
				v = new Exp();
				hPropVars.put( p, v );
			}
			return v;
		}
		
		public ConstrainedExpression hPvrop(double p)
		{
			vTop();
			vBottom();
			Exp v = vPropVars.get( p );
			if ( v == null )
			{
				v = new Exp();
				vPropVars.put( p, v );
			}
			return v;
		}
		
		
		private void addToSolver(ClSimplexSolver solver, LSConstrainedContainer element) throws ExCLInternalError, ExCLRequiredFailure, ExCLNonlinearExpression
		{
			ClVariable hl = null, hc = null, hr = null, vt = null, vc = null, vb = null, vry = null;
			if ( hLeft != null )
			{
				hl = hLeft.expression( element );
				solver.addVar( hl );
			}
			if ( hRight != null )
			{
				hr = hRight.expression( element );
				solver.addVar( hr );
			}
			if ( hCentre != null )
			{
				hc = hCentre.expression( element );
				solver.addVar( hc );
				solver.addConstraint( new ClLinearEquation( new ClLinearExpression( hl ).plus( hr ).times( 0.5 ), hc ) );
			}
			for (Map.Entry<Double, Exp> e: hPropVars.entrySet())
			{
				ClVariable hp = e.getValue().expression( element );
				double p = e.getKey();
				solver.addVar( hp );
				solver.addConstraint( new ClLinearEquation( new ClLinearExpression( hl ).times( 1.0 - p ).plus( new ClLinearExpression( hr ).times( p ) ), hp ) );
			}

			if ( vTop != null )
			{
				vt = vTop.expression( element );
				solver.addVar( vt );
			}
			if ( vBottom != null )
			{
				vb = vBottom.expression( element );
				solver.addVar( vb );
			}
			if ( vRefY != null )
			{
				vry = vRefY.expression( element );
				solver.addVar( vry );
			}
			if ( vCentre != null )
			{
				vc = vCentre.expression( element );
				solver.addVar( vc );
				solver.addConstraint( new ClLinearEquation( new ClLinearExpression( vt ).plus( vb ).times( 0.5 ), vc ) );
			}
			for (Map.Entry<Double, Exp> e: vPropVars.entrySet())
			{
				ClVariable vp = e.getValue().expression( element );
				double p = e.getKey();
				solver.addVar( vp );
				solver.addConstraint( new ClLinearEquation( new ClLinearExpression( vt ).times( 1.0 - p ).plus( new ClLinearExpression( vb ).times( p ) ), vp ) );
			}
		}
	}
	
	
	@Override
	protected void replaceChildWithEmpty(LSElement child)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<LSElement> getChildren()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSingleElementContainer()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
