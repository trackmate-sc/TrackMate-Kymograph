package fiji.plugin.trackmate.kymograph.tracing;

import java.lang.reflect.Type;

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

	private static Gson getGson()
	{
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter( KymographsSerializer.class, new KymographsSerializer() );
		return builder.setPrettyPrinting().create();
	}

	private static final class KymographsSerializer implements JsonSerializer< Kymographs >, JsonDeserializer< Kymographs >
	{

		@Override
		public JsonElement serialize( final Kymographs kymographs, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject modelEl = new JsonObject();
			modelEl.addProperty( "name", kymographs.toString() );

			final JsonArray kymArrayEl = new JsonArray( kymographs.size() );
			for ( final Kymograph kymograph : kymographs )
			{

				final JsonObject kymEl = new JsonObject();
				kymEl.addProperty( "name", kymograph.toString() );

				final JsonArray segmentsEl = new JsonArray( kymograph.size() );
				for ( final Segment segment : kymograph )
				{
					final JsonObject segmentEl = new JsonObject();
					segmentEl.addProperty( "name", segment.toString() );

					final JsonArray pointArrayEl = new JsonArray( segment.size() );
					for ( final RealLocalizable point : segment )
					{
						final JsonArray pointEl = new JsonArray( 2 );
						pointEl.add( Double.valueOf( point.getDoublePosition( 0 ) ) );
						pointEl.add( Double.valueOf( point.getDoublePosition( 1 ) ) );
						pointArrayEl.add( pointEl );
					}
					segmentEl.add( "points", pointArrayEl );
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
			final Kymographs kymographs = new Kymographs( name );
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
					builder.segment( segmentName );

					final JsonArray pointArrayEl = ( JsonArray ) segmentEl.get( "points" );
					for ( final JsonElement elc : pointArrayEl )
					{
						final JsonArray pointEl = ( JsonArray ) elc;
						builder.point( pointEl.get( 0 ).getAsDouble(), pointEl.get( 1 ).getAsDouble() );
					}
				}
			}
			builder.done();
			return kymographs;
		}
	}
}
