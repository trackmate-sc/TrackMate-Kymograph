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

	public int offset( final int length, final int width )
	{
		switch ( this )
		{
		default:
		case FIRST:
			return 0;
		case CENTER:
			return ( width - length ) / 2;
		case SECOND:
			return width - length;
		}
	}
}
