package BritefuryJ.Projection;

public abstract class TransientSubjectPathEntry extends AbstractSubjectPathEntry
{
	@Override
	public boolean canPersist()
	{
		return false;
	}
}
