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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Builder;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Kymograph;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Segment;
import net.imglib2.RealLocalizable;

public class KymographsIO
{

	public static final String toJson( final Kymographs kymographs )
	{
		return getGson().toJson( kymographs );
	}

	public static final Kymographs fromJson( final String str )
	{
		return getGson().fromJson( str, Kymographs.class );
	}

	public static final Kymographs load( final File jsonFile )
	{
		String str;
		try
		{
			str = new String( Files.readAllBytes( jsonFile.toPath() ) );
			return KymographsIO.fromJson( str );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	private static Gson getGson()
	{
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter( Kymographs.class, new KymographsSerializer() );
		return builder.setPrettyPrinting().create();
	}

	private static final class KymographsSerializer implements JsonSerializer< Kymographs >, JsonDeserializer< Kymographs >
	{

		@Override
		public JsonElement serialize( final Kymographs kymographs, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject modelEl = new JsonObject();
			modelEl.addProperty( "name", kymographs.toString() );
			modelEl.addProperty( "spaceUnits", kymographs.getSpaceUnits() );
			modelEl.addProperty( "timeUnits", kymographs.getTimeUnits() );

			final JsonArray kymArrayEl = new JsonArray( kymographs.size() );
			for ( final Kymograph kymograph : kymographs )
			{

				final JsonObject kymEl = new JsonObject();
				kymEl.addProperty( "name", kymograph.toString() );

				final JsonArray segmentsEl = new JsonArray( kymograph.size() );
				for ( final Segment segment : kymograph )
				{
					final JsonArray timeArrayEl = new JsonArray( segment.size() );
					final JsonArray positionArrayEl = new JsonArray( segment.size() );
					for ( final RealLocalizable point : segment )
					{
						timeArrayEl.add( Double.valueOf( point.getDoublePosition( 0 ) ) );
						positionArrayEl.add( Double.valueOf( point.getDoublePosition( 1 ) ) );
					}
					final JsonObject segmentEl = new JsonObject();
					segmentEl.addProperty( "name", segment.toString() );
					segmentEl.add( "time", timeArrayEl );
					segmentEl.add( "position", positionArrayEl );
					segmentsEl.add( segmentEl );
				}
				kymEl.add( "segments", segmentsEl );
				kymArrayEl.add( kymEl );
			}

			modelEl.add( "kymographs", kymArrayEl );
			return modelEl;
		}

		@Override
		public Kymographs deserialize( final JsonElement el, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject modelEl = ( JsonObject ) el;
			final String name = modelEl.get( "name" ).getAsString();
			final String spaceUnits = modelEl.get( "spaceUnits" ).getAsString();
			final String timeUnits = modelEl.get( "timeUnits" ).getAsString();
			final Kymographs kymographs = new Kymographs( name, spaceUnits, timeUnits );
			final Builder builder = kymographs.add();

			final JsonArray kymArrayEl = ( JsonArray ) modelEl.get( "kymographs" );
			for ( final JsonElement jsonElement : kymArrayEl )
			{
				final JsonObject kymEl = ( JsonObject ) jsonElement;
				final String kymographName = kymEl.get( "name" ).getAsString();
				builder.kymograph( kymographName );

				final JsonArray segmentsEl = ( JsonArray ) kymEl.get( "segments" );
				for ( final JsonElement elb : segmentsEl )
				{
					final JsonObject segmentEl = ( JsonObject ) elb;
					final String segmentName = segmentEl.get( "name" ).getAsString();
					final JsonArray timeArrayEl = segmentEl.getAsJsonArray( "time" );
					final JsonArray positionArrayEl = segmentEl.getAsJsonArray( "position" );
					builder.segment( segmentName );
					for ( int i = 0; i < timeArrayEl.size(); i++ )
					{
						final double time = timeArrayEl.get( i ).getAsDouble();
						final double position = positionArrayEl.get( i ).getAsDouble();
						builder.point( time, position );
					}
				}
			}
			builder.done();
			return kymographs;
		}
	}
}
