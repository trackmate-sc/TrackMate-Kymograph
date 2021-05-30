package fiji.plugin.trackmate.kymograph;

public enum KymographProjectionMethod
{

	MIP( "Max" ),
	MEAN( "Mean" );

	private final String name;

	KymographProjectionMethod( final String name )
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
