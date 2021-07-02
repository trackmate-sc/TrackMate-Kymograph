/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2010 - 2021 Fiji developers.
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

package fiji.plugin.trackmate.kymograph.path;

public interface SearchProgressCallback
{

	/* How many points have we considered? */

	public void pointsInSearch( SearchInterface source, int inOpen, int inClosed );

	/*
	 * Once finished is called, you should be able to get the result from
	 * whatever means you've implemented, e.g. TracerThreed.getResult()
	 */

	public void finished( SearchInterface source, boolean success );

	/*
	 * This reports the current status of the thread, which may be:
	 *
	 * SearchThread.RUNNING SearchThread.PAUSED SearchThread.STOPPING
	 */

	public void threadStatus( SearchInterface source, int currentStatus );

}
