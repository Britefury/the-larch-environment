package BritefuryJ.Cell;


public class LiteralCell extends CellBase
{
	private CellEvaluatorLiteral evaluator;
	private CellOwner owner;
	
	public LiteralCell()
	{
		super();
		evaluator = new CellEvaluatorLiteral( null );
		owner = null;
	}
	
	
	public CellEvaluator getEvaluator()
	{
		return evaluator;
	}

	public void setEvaluator(CellEvaluator eval)
	{
		CellEvaluatorLiteral oldEval = evaluator;
		evaluator = (CellEvaluatorLiteral)eval;
		emitEvaluator( oldEval, evaluator );
		if ( owner != null )
		{
			owner.onCellEvaluator( this, oldEval, evaluator );
		}
		changed();
	}


	public Object getValue()
	{
		refreshState = RefreshState.REFRESH_NOT_REQUIRED;
		
		if ( cellAccessList != null )
		{
			cellAccessList.put( this, null );
		}
		
		return evaluator.evaluate();
	}

	public boolean isValid()
	{
		return true;
	}


	public Object getLiteralValue()
	{
		return evaluator.evaluate();
	}

	public void setLiteralValue(Object value)
	{
		setEvaluator( new CellEvaluatorLiteral( value ) );
	}

	public boolean isLiteral()
	{
		return true;
	}
}
