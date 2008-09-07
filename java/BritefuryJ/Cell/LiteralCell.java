package BritefuryJ.Cell;


public class LiteralCell extends CellInterface
{
	private CellEvaluatorLiteral evaluator;
	private CellOwner owner;
	
	
	
	public LiteralCell()
	{
		this( null );
	}
	
	public LiteralCell(Object value)
	{
		super();
		evaluator = new CellEvaluatorLiteral( value );
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
		onChanged();
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

	
	public Object getValue()
	{
		refreshState = RefreshState.REFRESH_NOT_REQUIRED;
		
		onAccess();
		
		return evaluator.evaluate();
	}

	public boolean isValid()
	{
		return true;
	}
}
