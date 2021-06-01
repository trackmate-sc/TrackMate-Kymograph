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
package fiji.plugin.trackmate.kymograph;

public class KymographCreationParams
{

	public final Integer trackID1;

	public final Integer trackID2;

	public final int thickness;

	public final KymographProjectionMethod projectionMethod;

	public final KymographAlignment alignment;

	private KymographCreationParams(
			final Integer trackID1,
			final Integer trackID2,
			final int thickness,
			final KymographProjectionMethod projectionMethod,
			final KymographAlignment alignment )
	{
		this.trackID1 = trackID1;
		this.trackID2 = trackID2;
		this.thickness = thickness;
		this.projectionMethod = projectionMethod;
		this.alignment = alignment;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		str.append( "\n - track ID 1: " + trackID1 );
		str.append( "\n - track ID 2: " + trackID2 );
		str.append( "\n - thickness:  " + thickness );
		str.append( "\n - projection: " + projectionMethod );
		str.append( "\n - alignment:  " + alignment );
		return str.toString();
	}

	public static final class Builder
	{
		private Integer trackID1;

		private Integer trackID2;

		private int thickness = 5;

		private KymographProjectionMethod projectionMethod = KymographProjectionMethod.MIP;

		private KymographAlignment alignment = KymographAlignment.FIRST;

		private Builder()
		{}

		public Builder trackID1( final Integer trackID1 )
		{
			this.trackID1 = trackID1;
			return this;
		}

		public Builder trackID2( final Integer trackID2 )
		{
			this.trackID2 = trackID2;
			return this;
		}

		public Builder thickness( final int thickness )
		{
			this.thickness = thickness;
			return this;
		}

		public Builder alignment( final KymographAlignment alignment )
		{
			this.alignment = alignment;
			return this;
		}

		public Builder projectionMethod( final KymographProjectionMethod projectionMethod )
		{
			this.projectionMethod = projectionMethod;
			return this;
		}

		public KymographCreationParams get()
		{
			return new KymographCreationParams( trackID1, trackID2, thickness, projectionMethod, alignment );
		}
	}

	public static Builder create()
	{
		return new Builder();
	}
}
