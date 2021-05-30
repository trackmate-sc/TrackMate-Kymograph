package fiji.plugin.trackmate.kymograph;

public enum KymographAlignment
{

	CENTER( "Center" ),
	FIRST( "First track" ),
	SECOND( "Second track" );

	private final String name;

	KymographAlignment( final String name )
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
