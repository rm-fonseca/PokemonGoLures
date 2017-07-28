import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Point;
import com.pokegoapi.util.MapUtil;


/*
 * Important
 * 
 * This class should be in PokemonGo APi but wanst able to import it so i just copy paste it
 * 
 * 
 * 
 */

public class Path {
	private Point source;
	private Point destination;
	private Point intermediate;
	private double speed;
	private long startTime;
	private long endTime;
	private long totalTime;
	private boolean complete;

	/**
	 * Creates a Path with the given positions
	 *
	 * @param source the source of this path
	 * @param destination the destination for this path
	 * @param speed the speed to move at in kmph
	 */
	public Path(Point source, Point destination, double speed) {
		this.source = source;
		this.destination = destination;
		double metersPerHour = speed * 1000;
		this.speed = metersPerHour / 60 / 60 / 1000;
		this.intermediate = new Point(source.getLatitude(), source.getLongitude());
		this.totalTime = (long) (MapUtil.distFrom(source, destination) / this.speed);
	}

	/**
	 * Sets the start and end time for this Path, ready to begin moving
	 *
	 * @param api the current API
	 * @return the total time it will take for this path to complete
	 */
	public long start(PokemonGo api) {
		startTime = api.currentTimeMillis();
		endTime = startTime + totalTime;
		complete = false;
		return totalTime;
	}

	/**
	 * Calculates the desired intermediate point for this path, based on the current time
	 *
	 * @param api the current API
	 * @return the intermediate point for the given time
	 */
	public Point calculateIntermediate(PokemonGo api) {
		if (totalTime <= 0) {
			this.complete = true;
			return this.destination;
		}
		long time = Math.min(api.currentTimeMillis(), endTime) - startTime;
		if (time >= totalTime) {
			this.complete = true;
		}
		double intermediate = (double) time / totalTime;
		double latitude = source.getLatitude() + (destination.getLatitude() - source.getLatitude()) * intermediate;
		double longitude = source.getLongitude() + (destination.getLongitude() - source.getLongitude()) * intermediate;
		this.intermediate.setLatitude(latitude);
		this.intermediate.setLongitude(longitude);
		return this.intermediate;
	}

	/**
	 * Gets the amount of millis left before this path is complete
	 *
	 * @param api the current API
	 * @return the amount of millis left before this path completes
	 */
	public long getTimeLeft(PokemonGo api) {
		return Math.max(0, endTime - api.currentTimeMillis());
	}

	/**
	 * Changes the speed of this path
	 *
	 * @param api the current API
	 * @param speed the new speed to travel at
	 */
	public void setSpeed(PokemonGo api, double speed) {
		double metersPerHour = speed * 1000;
		this.speed = metersPerHour / 60 / 60 / 1000;
		this.source = calculateIntermediate(api);
		this.totalTime = (long) (MapUtil.distFrom(source, destination) / this.speed);
		start(api);
	}

	public boolean isComplete() {
		return complete;
	}
}
