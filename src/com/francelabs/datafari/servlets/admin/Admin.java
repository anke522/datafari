/*******************************************************************************
 * Copyright 2015 France Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.francelabs.datafari.servlets.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SolrIndexSearcher;

import com.francelabs.datafari.solrj.SolrServers;
import com.francelabs.datafari.solrj.SolrServers.Core;

/**
 * 
 * This servlet is used to print/add/edit/delete promolinks directly from the promolink core of Solr
 * It is only called by the promolink.html
 * doGet is used to print all the promolinks of the core
 * doPost is used to edit/add/delete a promolink
 * @author Alexis Karassev
 *
 */
@WebServlet("/admin/Admin")
public class Admin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SolrInputDocument doc;
	private int maxCaps = 100;
	private final static Logger LOGGER = Logger.getLogger(Admin.class
			.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Admin() {
		super();
	}
	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * Used to print the existing promolinks, and to check when you add a promolink if an other exist with this keyword.
	 * Makes a Solr request and put the results into a JSON file.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		final SolrQuery query = new SolrQuery();
		QueryResponse queryResponse = null;
		doc = new SolrInputDocument();
		HttpSolrServer server=null;
		try {
			server = (HttpSolrServer) SolrServers			//Select the right core
					.getSolrServer(Core.PROMOLINK);
		} catch (IOException e1) {
			PrintWriter out = response.getWriter();
			out.append("Unavailable core, please check if the core has booted up"); 	
			out.close();
			LOGGER.error("Error while getting the Solr core in doGet, admin servlet ", e1);
			throw e1;
		}
		SolrQueryRequest req = new SolrQueryRequest() {
			@Override
			public SolrParams getParams() {
				return query;
			}

			@Override
			public void setParams(SolrParams params) {
			}

			@Override
			public Iterable<ContentStream> getContentStreams() {
				return null;
			}

			@Override
			public SolrParams getOriginalParams() {
				return null;
			}

			@Override
			public Map<Object, Object> getContext() {
				return null;
			}

			@Override
			public void close() {
			}

			@Override
			public long getStartTime() {
				return 0;
			}

			@Override
			public SolrIndexSearcher getSearcher() {
				return null;
			}

			@Override
			public SolrCore getCore() {
				return null;
			}

			@Override
			public IndexSchema getSchema() {
				return null;
			}

			@Override
			public String getParamString() {
				return null;
			}

			@Override
			public void updateSchemaToLatest() {

			}

		};
		if(request.getParameter("title")!=null){										//If the servlet has been called to check if there was an existing promolink with this keyword
			query.setParam("q", "\""+request.getParameter("keyword").toString()+"\"");	//set the keyword to what was sent
			query.setParam("q.op", "AND");
		}else{																			//the servlet has been called to print the existing promolinks
			if(request.getParameter("keyword").equals("")){								//If nothing was typed into the search field
				query.setParam("q", "*:*");												//the query will return all the promolinks
			}
			else{
				query.setParam("q", "\""+request.getParameter("keyword").toString()+"\"");	//else set the a research query with the keyword typed in the search field
				query.setParam("q.op", "AND");
			}
			query.setParam("rows", String.valueOf(maxCaps));							//Hardcoded limit of 100 results
		}
		query.setRequestHandler("/select");											
		try {
			queryResponse = server.query(query);										//send the query
		}catch(SolrServerException | SolrException e ){
			PrintWriter out = response.getWriter();
			out.append("Unavailable core, please check if the core has booted up"); 	
			out.close();
			LOGGER.error("Error while getting the results of the Solr Request in doGet, admin servlet ", e);
			throw new RuntimeException();
		}
		SolrQueryResponse res = new SolrQueryResponse();								
		JSONResponseWriter json = new JSONResponseWriter();
		res.setAllValues(queryResponse.getResponse());
		try {
			json.write(response.getWriter(), req, res);
		} catch (IOException e) {
			LOGGER.error("Error while writing the results of the Solr Request in the doGet response, admin servlet", e);
			throw e;
		}										//send the answer in a json
		response.setStatus(200);
		response.setContentType("text/json;charset=UTF-8");

	}
	public String formatDate(String date, String time){										//format date to the format of the datepicker
		if(date.equals("")){
			return time;
		}
		return date.substring(6,10)+"-"+date.substring(0,2)+"-"+date.substring(3,5)+time;
	}
	/**
	 * @throws IOException 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * Used to delete/add/edit an promolink
	 * Send request to Solr and returns nothing
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSolrServer server=null;
		try {
			server = (HttpSolrServer) SolrServers						//Select the right core
					.getSolrServer(Core.PROMOLINK);
		} catch (IOException e1) {
			LOGGER.error("Error while getting the Solr core in Admin Servlets", e1);
			throw e1;
		}
		if( request.getParameter("title")!=null && request.getParameter("keyword")!=null && request.getParameter("contentPromoLink")!=null){ //If it's an edit or an add
			Object key = request.getParameter("keyword"), title = request.getParameter("title"), value = request.getParameter("contentPromoLink"), oldKey = request.getParameter("oldKey");
			String dateB = formatDate(request.getParameter("dateB").toString(),"T00:00:00Z"), dateE = formatDate(request.getParameter("dateE").toString(),"T23:59:59Z"); //Get all the parameters & format the Date
			doc = new SolrInputDocument();
			try {
				doc.addField("keyword", key);					//add the keyword to the Solrdoc						
				doc.addField("title", title);					//add the title to the Solrdoc		
				doc.addField("content", value);					//add the value to the Solrdoc												
				if(!dateB.equals("T00:00:00Z"))									
					doc.addField("dateBeginning", dateB);		//add the Starting Date (if there is one) to the Solrdoc										
				if(!dateE.equals("T23:59:59Z"))								
					doc.addField("dateEnd", dateE);				//add the ending Date (if there is one) to the Solrdoc												
				if(request.getParameter("oldKey")!=null){		//If it's an edit and the keyword has been changed
					if(request.getParameter("oldKey")!=request.getParameter("keyword"))
						server.deleteById(oldKey.toString());	//Delete the previous promolink on the keyword
				} 
				server.deleteById(doc.get("keyword").toString());//delete a promolink with the same keyword (either it's an edit with the same keyword, either it's an add with a keyword already existing that has been confirmed)																
				server.add(doc);								//Insert the new promolink
				server.commit();										
			} catch (SolrServerException | IOException e) {
				LOGGER.error("Error while adding/editing a promolink in the Admin Servlet ", e);
				throw new RuntimeException();
			}
		}
		else{													//delete a promolink
			String key = request.getParameter("keyword").toString();
			try {
				server.deleteById(key.toString());
				server.commit();
			}catch(SolrServerException e){
				LOGGER.error("Error while deleting a promolink in the Admin Servlet ", e);
				throw new RuntimeException();
			}
		}
	}
}
