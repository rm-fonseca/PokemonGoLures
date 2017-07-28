/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Point;
import com.pokegoapi.util.MapUtil;
import java.util.Collection;
import java.util.Scanner;

import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import okhttp3.OkHttpClient;
import rx.Observable;

public class Lures {

	
	//Parameters
	private final static String username = "";
	private final static String password = "";
	private final static double latitude = 0.0;
	private final static double longitude = 0.0;
	
	/**
	 * Travels to a Pokestop and loots it
	 *
	 * @param args args
	 * @throws RemoteServerException 
	 * @throws LoginFailedException 
	 */
	public static void main(String[] args) throws LoginFailedException, RemoteServerException {
		OkHttpClient httpClient = new OkHttpClient();
					
		// we should be able to login with this token
		PokemonGo go = new PokemonGo(httpClient);
		go.login(new PtcCredentialProvider(httpClient, username, password));
		go.setLocation(latitude, longitude, 1); // set your position to get stuff around (altitude is not needed, you can use 1 for example)
		
		Collection<Pokestop> pokestops = go.getMap().getMapObjects().getPokestops();
		System.out.println("Found " + pokestops.size() + " pokestops in the current area.");
		
		
		for (Pokestop destinationPokestop : pokestops) {
			if(destinationPokestop.hasLure())
				continue;
			
			Point destination = new Point(destinationPokestop.getLatitude(), destinationPokestop.getLongitude());
			//Use the current player position as the source and the pokestop position as the destination
			//Travel to Pokestop at 10KMPH
			Path path = new Path(new Point(go.getLatitude(), go.getLongitude()), destination, 10.0);
			System.out.println("Traveling to " + destination + " at 10KMPH!");
			path.start(go);
			try {
				while (!path.isComplete()) {
					//Calculate the desired intermediate point for the current time
					Point point = path.calculateIntermediate(go);
					//Set the API location to that point
					go.setLatitude(point.getLatitude());
					go.setLongitude(point.getLongitude());
					System.out.println("Time left: " + (int) (path.getTimeLeft(go) / 1000) + " seconds.");
					//Sleep for 2 seconds before setting the location again
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				return;
			}
			System.out.println("Finished traveling to pokestop!");
			if (destinationPokestop.inRange()) {
				System.out.println("Looting pokestop...");
				// Lure Model ID = 501 (i think)
				Observable<Boolean> result = destinationPokestop.addModifierAsync(ItemId.forNumber(501)); 
				//TODO learn how to use result
				System.out.println("Lure on.");
			} else {
				System.out.println("Something went wrong! We're still not in range of the destination pokestop!");
			}
			
			
			}
		}
		
		
		
		
		
		

	}
