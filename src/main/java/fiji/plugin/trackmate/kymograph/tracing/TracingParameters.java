/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
