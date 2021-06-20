package fiji.plugin.trackmate.kymograph.tracing;

import org.scijava.listeners.Listeners;

public class TracingParameters
{

	private final transient Listeners.List< UpdateListener > updateListeners;

	private double threshold = 0.;

	private double penalty = 10.;

	private double sigma = 1.5;

	public TracingParameters()
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
	}

	public double getThreshold()
	{
		return threshold;
	}

	public double getPenalty()
	{
		return penalty;
	}

	public double getSigma()
	{
		return sigma;
	}

	public synchronized void setThreshold( final double threshold )
	{
		if ( this.threshold != threshold )
		{
			this.threshold = threshold;
			notifyListeners();
		}
	}

	public synchronized void setPenalty( final double penalty )
	{
		if ( this.penalty != penalty )
		{
			this.penalty = penalty;
			notifyListeners();
		}
	}

	public synchronized void setSigma( final double sigma )
	{
		if ( this.sigma != sigma )
		{
			this.sigma = sigma;
			notifyListeners();
		}
	}

	public interface UpdateListener
	{
		public void tracingParametersChanged();
	}

	public Listeners.List< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.tracingParametersChanged();
	}

}
