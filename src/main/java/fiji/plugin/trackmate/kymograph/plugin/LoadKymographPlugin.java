package fiji.plugin.trackmate.kymograph.plugin;

import java.io.IOException;

import fiji.plugin.trackmate.kymograph.tracing.ui.KymographTracingController;
import ij.IJ;
import ij.plugin.PlugIn;

public class LoadKymographPlugin implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		try
		{
			if ( arg == null || arg.isEmpty() )
				KymographTracingController.load();
			else
				KymographTracingController.load( arg );
		}
		catch ( final IOException e )
		{
			IJ.error( "Could not load klymograph file:\n" + e.getMessage() );
		}
	}
}
