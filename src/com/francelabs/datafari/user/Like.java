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
import com.francelabs.datafari.service.db.LikeService;

public class Like {

	

	
	/**
	 * Add a document to the list of documents liked by the user
	 * @param username of the user
	 * @param idDocument the id that should be liked
	 * @return Like.ALREADYPERFORMED if the like was already done, CodesUser.ALLOK 
	 * if all was ok and CodesUser.PROBLEMCONNECTIONDATABASE if there's an error
	 */
	public static int addLike(String username, String idDocument){
		try{
			return LikeService.addLike(username, idDocument);
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}
	
	
	/**
	 * unlike a document 
	 * @param username of the user who unlike a document
	 * @param idDocument the id that should be unliked
	 * @return Like.ALREADYPERFORMED if the like was already done, Like.ALLOK 
	 * if all was ok and Like.CodesReturned.PROBLEMCONNECTIONDATABASE if there's an error
	 */
	public static int unlike(String username, String idDocument){
		try{
			return LikeService.unlike(username, idDocument);
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}
	
	/**
	 * get all the likes of a user
	 * @param username of the user
	 * @return an array list of all the the likes of the user. Return null if there's an error.
	 */
	public static ArrayList<String> getLikes(String username){
		try{
			return LikeService.getLikes(username);
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * get the number of Likes of each document from MongoDB. If a document is not returned, it would mean that he hasn't a like.
	 * @return array containing the id of document and the correspoding likes
	 */
	public static ArrayList<NbLikes> getNbLikes(){
		try{
			return LikeService.getNbLikes();
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * Delete all likes of a user without deleting also his favorites
	 * @param username
	 * @return CodesReturned.ALLOK if the operation was success and CodesReturned.PROBLEMCONNECTIONDATABASE if the mongoDB isn't running	
	 */
	public static int removeLikes(String username){
		try{
			return LikeService.removeLikes(username);
		}catch(Exception e){
			return CodesReturned.PROBLEMCONNECTIONDATABASE;
		}
	}


}