/*******************************************************************************
 *  * Copyright 2015 France Labs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *******************************************************************************/
package com.francelabs.datafari.user;

import java.util.ArrayList;


import com.francelabs.datafari.constants.CodesReturned;
import com.francelabs.datafari.service.db.FavoriteService;
public class Favorite {
	public static String FAVORITEDB = UserConstants.FAVORITEDB;
	
	/**
	 * Add a document to the favorites list of the user
	 * @param username of the user
	 * @param idDocument the id that should be add as a favorite
	 * @return true if it was success and false if not
	 */
	public static int addFavorite(String username, String idDocument){
		try{
			FavoriteService.addFavorite(username, idDocument);
			return CodesReturned.ALLOK;
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}
	
	
	/**
	 * delete a document from the favorites list of the user
	 * @param username of the user
	 * @param idDocument the id that should be deleted from the favorites
	 * @return true if it was success and false if not
	 */
	public static int deleteFavorite(String username, String idDocument){
		try{
			FavoriteService.deleteFavorite(username, idDocument);
			return CodesReturned.ALLOK;
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}
	
	/**
	 * get all the favorites of a user
	 * @param username of the user
	 * @return an array list of all the favorites document of the user. Return null if there's an error.
	 */
	public static ArrayList<String> getFavorites(String username){
		try{
			return FavoriteService.getFavorites(username);
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * Delete all favorites of a user without deleting also his likes
	 * @param username
	 * @return CodesReturned.ALLOK if the operation was success and CodesReturned.PROBLEMCONNECTIONDATABASE if the mongoDB isn't running	
	 */
	public static int removeFavorites(String username){
		try{
			return FavoriteService.removeFavorites(username);
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}
	
	/**
	 * change the database of Favorites and Likes
	 * @param db the new database
	 */
	public static void setFavoriteDB(String db){
		Favorite.FAVORITEDB = db;
	}

	/**
	 * Remove a user from the collection favorites. This will delete his likes and his favorites
	 * @param username
	 * @return 
	 */
	public static int removeUserFromFavoriteDB(String username){
		try{
			return FavoriteService.removeUserFromFavoriteDB(username);
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}
}