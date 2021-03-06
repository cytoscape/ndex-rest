/**
 * Copyright (c) 2013, 2016, The Regents of the University of California, The Cytoscape Consortium
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.ndexbio.common.solr;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.ndexbio.common.NdexClasses;
import org.ndexbio.model.tools.SearchUtilities;
import org.ndexbio.model.tools.TermUtilities;
import org.ndexbio.common.util.Util;
import org.ndexbio.cx2.aspect.element.core.CxNetworkAttribute;
import org.ndexbio.cx2.aspect.element.core.CxNode;
import org.ndexbio.cx2.aspect.element.core.DeclarationEntry;
import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.model.cx.FunctionTermElement;
import org.ndexbio.model.exceptions.BadRequestException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.Configuration;

public class NetworkGlobalIndexManager implements AutoCloseable{

	private String solrUrl ;
	
	private static final String coreName = 
			"ndex-networks" ; 
	private HttpSolrClient client;
	
	private SolrInputDocument doc ;
	
	// holds the mapping between node ID and member attributes. 
	// members will be added to the represent field as additional lists.
	private Map<Long, Set<String>> nodeMembers;  
	
	public static final String UUID = "uuid";
	private static final String NAME = "name";
	private static final String DESC = "description";
	private static final String VERSION = "version";
	//private static final String LABELS = "labels";
	private static final String USER_READ= "userRead";
	private static final String USER_EDIT = "userEdit";
	private static final String USER_ADMIN = "owner";
	private static final String GRP_READ = "grpRead";
	private static final String GRP_EDIT = "grpEdit";
//	private static final String GRP_ADMIN = "grpAdmin";
	
	private static final String VISIBILITY = "visibility";
	
	private static final String EDGE_COUNT = "edgeCount";
	
	private static final String NODE_COUNT = "nodeCount";
	private static final String CREATION_TIME = "creationTime";
	private static final String MODIFICATION_TIME = "modificationTime";
	private static final String NDEX_SCORE = "ndexScore";
	
	
	private static final String NODE_NAME = "nodeName";
	
//	public static final String NCBI_GENE_ID = "NCBIGeneID";
//	public static final String GENE_SYMBOL = "geneSymbol";
	
	private static final String REPRESENTS = "represents";
	private static final String ALIASES = "alias";
//	private static final String RELATED_TO = "relatedTo";
	
	// user required indexing fields. hardcoded for now. Will turn them into configurable list in 1.4.
	
	private static final Set<String> otherAttributes = 
			new HashSet<>(Arrays.asList("objectCategory", "organism",
	"platform",
	"graphPropertiesHash",
	"networkType",
	"disease",
	"tissue",
	 "rightsHolder",
	 "author",
	 "createdAt",
	 "methods",
	 "subnetworkType","subnetworkFilter","graphHash","rights", "labels"));
	
//	private static  Map<String,String> attTable = null;
		
//	private int counter;
	
	
	public NetworkGlobalIndexManager() {
		solrUrl = Configuration.getInstance().getSolrURL();
		client = new HttpSolrClient.Builder(solrUrl).build();
		doc = new SolrInputDocument();
		
		nodeMembers = new TreeMap<>();
		
/*		if ( attTable == null) { 
			attTable = new HashMap<>(otherAttributes.size());
			for ( String att : otherAttributes) {
				attTable.put(att.toLowerCase(), att);
			}
		} */
	}
	
	public void createCoreIfNotExists() throws SolrServerException, IOException, NdexException {
			
		CoreAdminResponse foo = CoreAdminRequest.getStatus(coreName,client);	
		if (foo.getStatus() != 0 ) {
			throw new NdexException ("Failed to get status of solrIndex for " + coreName + ". Error: " + foo.getResponseHeader().toString());
		}
		NamedList<Object> bar = foo.getResponse();
		
		NamedList<Object> st = (NamedList<Object>)bar.get("status");
		
		NamedList<Object> core = (NamedList<Object>)st.get(coreName);
		if ( core.size() == 0 ) {
			System.out.println("Solr core " + coreName + " doesn't exist. Creating it now ....");

			CoreAdminRequest.Create creator = new CoreAdminRequest.Create(); 
			creator.setCoreName(coreName);
			creator.setConfigSet( coreName); 
			foo = creator.process(client);				
			if ( foo.getStatus() != 0 ) {
				throw new NdexException ("Failed to create solrIndex for network " + coreName + ". Error: " + foo.getResponseHeader().toString());
			}
			System.out.println("Done.");		
		}
		else {
			System.out.println("Found core "+ coreName + " in Solr.");	
		}
		
	}
	
	public SolrDocumentList searchForNetworks (String searchTerms, String userAccount, int limit, int offset, String adminedBy, Permissions permission,
			   List<UUID> groupUUIDs) 
			throws  IOException, SolrServerException, NdexException {
		client.setBaseURL(solrUrl+ "/" + coreName);

		SolrQuery solrQuery = new SolrQuery();
		
		//create the result filter
		
		String adminFilter = "";		
		if ( adminedBy !=null) {
			adminFilter = " AND (" + USER_ADMIN + ":\"" + adminedBy +  "\")";
		}
		
		String resultFilter = "";
		if ( userAccount !=null) {     // has a signed in user.
			String userAccountStr = "\"" + userAccount +"\"";
			if ( permission == null) {
				resultFilter =  VISIBILITY + ":PRIVATE";
				resultFilter += " AND -(" + USER_ADMIN + ":" + userAccountStr + ") AND -(" +
						USER_EDIT + ":" + userAccountStr + ") AND -("+ USER_READ + ":" + userAccountStr + ")";
				if ( groupUUIDs!=null) {
					for (UUID groupUUID : groupUUIDs) {
					  resultFilter +=  " AND -(" + GRP_EDIT + ":\"" + groupUUID.toString() + "\") AND -("+ GRP_READ + ":\"" + groupUUID.toString() + "\")";
					}
				}
				resultFilter = "-("+ resultFilter + ")";
			} 
			else if ( permission == Permissions.READ) {
				resultFilter = "(" + USER_ADMIN + ":" + userAccountStr + ") OR (" +
						USER_EDIT + ":" + userAccountStr + ") OR ("+ USER_READ + ":" + userAccountStr + ")";
				if ( groupUUIDs!=null) {
					for (UUID groupUUID : groupUUIDs) {
						  resultFilter +=  " OR (" +
							  GRP_EDIT + ":\"" + groupUUID.toString() + "\") OR ("+ GRP_READ + ":\"" + groupUUID.toString() + "\")";
					}
				}
			} else if ( permission == Permissions.WRITE) {
				resultFilter = "(" + USER_ADMIN + ":" + userAccountStr + ") OR (" +
						USER_EDIT + ":" + userAccountStr + ")";
				if ( groupUUIDs !=null) {
					for ( UUID groupUUID : groupUUIDs )  {
						  String groupUUIDStr = "\"" + groupUUID.toString() + "\"";	
						  resultFilter += " OR (" +
							GRP_EDIT + ":" + groupUUIDStr + ")" ;
					}
				} 
			}
		}  else {
			resultFilter = VISIBILITY + ":PUBLIC";
		}
			
		resultFilter = resultFilter + adminFilter;
		
			
		if ( searchTerms.equalsIgnoreCase("*:*"))
			solrQuery.setSort(MODIFICATION_TIME, ORDER.desc);

//		solrQuery.setQuery( searchTerms ).setFields(UUID);
		solrQuery.setQuery("( " + SearchUtilities.preprocessSearchTerm(searchTerms) + " ) AND _val_:\"div(" + NDEX_SCORE+ ",10)\"" ).setFields(UUID);
    	solrQuery.set("defType", "edismax");
		solrQuery.set("qf","uuid^20 name^10 description^5 labels^6 owner^2 networkType^4 organism^3 disease^3 tissue^3 author^2 methods nodeName represents alias rights^0.6 rightsHolder^0.6");
		if ( offset >=0)
		  solrQuery.setStart(offset);
		if ( limit >0 )
			solrQuery.setRows(limit);
		else 
			solrQuery.setRows(100000);
		
		solrQuery.setFilterQueries(resultFilter) ;
		
		try {
			QueryResponse rsp = client.query(solrQuery, METHOD.POST);		
			
			SolrDocumentList  dds = rsp.getResults();
			return dds;
		} catch (BaseHttpSolrClient.RemoteSolrException e) {
			throw convertException(e, "ndex-networks");
		}
		
	}
	
	protected static NdexException convertException(BaseHttpSolrClient.RemoteSolrException e, String core_name) {
		if (e.code() == 400) {
			String err = e.getMessage();
			Pattern p = Pattern.compile("Error from server at .*/" + core_name +": (.*)");
			Matcher m = p.matcher(e.getMessage());
			if ( m.matches()) {
				err = m.group(1);
			} 
			return new BadRequestException(err);
		}	
		return new NdexException("Error from NDEx Solr server: " + e.getMessage());
	}
	
/*	public void createIndexDocFromSummary(NetworkSummary summary) throws SolrServerException, IOException, NdexException, SQLException {
		client.setBaseURL(solrUrl + "/" + coreName);
	
		doc.addField(UUID,  summary.getExternalId().toString() );
		doc.addField(EDGE_COUNT, summary.getEdgeCount());
		doc.addField(NODE_COUNT, summary.getNodeCount());
		doc.addField(VISIBILITY, summary.getVisibility().toString());
		
		doc.addField(CREATION_TIME, summary.getCreationTime());
		doc.addField(MODIFICATION_TIME, summary.getModificationTime());
		
		try (NetworkDocDAO dao = new NetworkDocDAO()) {
			List<Map<Permissions, Collection<String>>> members = dao.getAllMembershipsOnNetwork(summary.getExternalId());
			doc.addField(USER_READ, members.get(0).get(Permissions.READ));
			doc.addField(USER_EDIT, members.get(0).get(Permissions.WRITE));
			doc.addField(USER_ADMIN, members.get(0).get(Permissions.ADMIN));
			doc.addField(GRP_READ, members.get(1).get(Permissions.READ));
			doc.addField(GRP_EDIT, members.get(1).get(Permissions.WRITE));
		}

	} */
	
	public void createIndexDocFromSummary(NetworkSummary summary, String ownerUserName, Collection<String> userReads,Collection<String> userEdits,
			Collection<String> grpReads, Collection<String> grpEdits) {
		client.setBaseURL(solrUrl + "/" + coreName);
		
	  //  doc = new SolrInputDocument();
		doc.addField(UUID,  summary.getExternalId().toString() );
		doc.addField(EDGE_COUNT, summary.getEdgeCount());
		doc.addField(NODE_COUNT, summary.getNodeCount());
		doc.addField(VISIBILITY, summary.getVisibility().toString());
		
		if ( summary.getName() !=null && summary.getName().length()>1) {
			doc.addField(NAME, summary.getName());
		}
		
		if (summary.getDescription() !=null && summary.getDescription().length()>1) {
			doc.addField(DESC, summary.getDescription());
		}
		
		if ( summary.getVersion() !=null && summary.getVersion().length()>1) {
			doc.addField(VERSION, summary.getVersion());
		}
		
		doc.addField(CREATION_TIME, summary.getCreationTime());
		doc.addField(MODIFICATION_TIME, summary.getModificationTime());
		
		doc.addField(NDEX_SCORE, Util.getNdexScoreFromSummary(summary));
		
		doc.addField(USER_ADMIN, ownerUserName);
		//doc.setDocumentBoost(documentBoost);;
		if( userReads != null) {
			for(String userName : userReads) {
				doc.addField(USER_READ, userName);
			}
		}
		
		if ( userEdits !=null) {
			for ( String userName: userEdits) {
				doc.addField(USER_EDIT, userName);
			}
		}
		if ( grpReads !=null) {
			for ( String grpName : grpReads) {
				doc.addField(GRP_READ, grpName);
			}
		}
		if(grpEdits !=null) {
			for ( String grpName : grpEdits) {
				doc.addField(GRP_EDIT, grpName);
			}
		}

	}
	

	public void addCXNodeToIndex(NodesElement node)  {
			
		   if ( node.getNodeName() != null ) 
			   doc.addField(NODE_NAME, node.getNodeName());
		   if ( node.getNodeRepresents() !=null ) {
			   for (String indexableString : getIndexableString(node.getNodeRepresents())) {
				   doc.addField(REPRESENTS, indexableString);
			   }
		//	   if( indexableString !=null)
		   }	
	

	}
	
	public void addCX2NodeToIndex(CxNode node, Map<String, Map.Entry<String,DeclarationEntry>> attributeNameMapping)  {
		
		Map<String,Object> nodeAttrs = node.getAttributes();
		Object nodeName = nodeAttrs.get(CxNode.NAME);
		if ( nodeName != null) {
			   doc.addField(NODE_NAME, nodeName);			
		}
		Object represents= nodeAttrs.get(CxNode.REPRESENTS);
		if ( represents != null) {
			for (String indexableString : getIndexableString((String)represents)) {
				   doc.addField(REPRESENTS, indexableString);
			}
		}
	
		if ( attributeNameMapping.get(SingleNetworkSolrIdxManager.ALIAS)!=null) {
			
			for (String v : SingleNetworkSolrIdxManager.getSplitableTerms(SingleNetworkSolrIdxManager.ALIAS, node,
					attributeNameMapping) ){
				doc.addField(ALIASES, v);
			}
			
		} 
		
		String nodeType = SingleNetworkSolrIdxManager.getSingleIndexableTermFromNode(SingleNetworkSolrIdxManager.TYPE,
				node, attributeNameMapping);
		
		if ( nodeType != null && (nodeType.equalsIgnoreCase(SingleNetworkSolrIdxManager.PROTEINFAMILY) || 
    			nodeType.equalsIgnoreCase(SingleNetworkSolrIdxManager.COMPLEX) )) {
    		List<String> memberGenes = SingleNetworkSolrIdxManager.getSplitableTerms (SingleNetworkSolrIdxManager.MEMBER,
    				node, attributeNameMapping);
    		for ( String memberIdStr : memberGenes) {
				for ( String indexableString : getIndexableString(memberIdStr) ){
					doc.addField(REPRESENTS, indexableString);
				}
			}
    	}
	
	}

	
	
	public void addCXNodeAttrToIndex(NodeAttributesElement e)  {
		
		if ( e.getName().equals(NdexClasses.Node_P_alias)) {
			if ( e.getDataType() == ATTRIBUTE_DATA_TYPE.LIST_OF_STRING 	&& !e.getValues().isEmpty()) {
				for ( String v : e.getValues()) {
					for ( String indexableString : getIndexableString(v) ){
						doc.addField(ALIASES, indexableString);
					}
				}
			} else if ( e.getDataType() == ATTRIBUTE_DATA_TYPE.STRING) {
				String v = e.getValue();
				for ( String indexableString : getIndexableString(v) ){
					doc.addField(ALIASES, indexableString);
				}
			}
		} else if ( e.getName().toLowerCase().equals("type")) {
			if ( e.getDataType() == ATTRIBUTE_DATA_TYPE.STRING) {
				String v = e.getValue().toLowerCase();
				if ( v.equals("complex") || v.equals("proteinfamily")) {
					Set<String> members = nodeMembers.get(e.getPropertyOf());
					if ( members != null) {  // saw the member attribute on this node before
						for ( String memberIdStr : members) {
							for ( String indexableString : getIndexableString(memberIdStr) ){
								doc.addField(REPRESENTS, indexableString);
							}
						}
						nodeMembers.remove(e.getPropertyOf());
					}
					else {
						nodeMembers.put(e.getPropertyOf(), new TreeSet<String>());
					}
				}
			}
		} else if (  e.getName().toLowerCase().equals("member")) {
			if ( e.getDataType() == ATTRIBUTE_DATA_TYPE.LIST_OF_STRING) {
				Set<String> members = nodeMembers.get(e.getPropertyOf());
				if ( members != null) {  // this node is proteinfamily or complex
					for ( String memberIdStr : e.getValues()) {
						for ( String indexableString : getIndexableString(memberIdStr) ){
							doc.addField(REPRESENTS, indexableString);
						}
					}
					nodeMembers.remove(e.getPropertyOf());
				} else {
					members = new HashSet<>(e.getValues());
					nodeMembers.put(e.getPropertyOf(), members);
				}		
			}
		}

	}

	
	public void addFunctionTermToIndex(FunctionTermElement e)  {
				
		for ( String term : getIndexableStringsFromFunctionTerm(e)) {
			 
			doc.addField(REPRESENTS, term);
		}

	}
	
	protected static List<String> getIndexableStringsFromFunctionTerm(FunctionTermElement e)  {
		
		List<String> terms = getIndexableString(e.getFunctionName());
		for (Object arg: e.getArgs()) {
			if (arg instanceof String ) {
				terms.addAll(getIndexableString((String)arg));
			} else if ( arg instanceof FunctionTermElement ){
				terms.addAll(getIndexableStringsFromFunctionTerm((FunctionTermElement)arg));
			}
		}
	
		return terms;
	}

	
	public List<String> addCXNetworkAttrToIndex(NetworkAttributesElement e)  {
		
		List<String> warnings = new ArrayList<>();
		if ( e.getName().equals(NdexClasses.Network_P_name) ) {
			addStringAttrFromAttributeElement(e, NAME, warnings);
		} else if ( e.getName().equals(NdexClasses.Network_P_desc ) ) {
			addStringAttrFromAttributeElement(e, DESC, warnings);
		} else if ( e.getName().equals(NdexClasses.Network_P_version)  ) {
			addStringAttrFromAttributeElement(e, VERSION, warnings);			
		} else {
			if ( otherAttributes.contains(e.getName())  ) {
				addStringListgAttribute(e, e.getName(), warnings);
			}			
		}
		
		return warnings;
		
	}
	
	public List<String> addCX2NetworkAttrToIndex(CxNetworkAttribute e)  {
		
		List<String> warnings = new ArrayList<>();
		if ( e.getNetworkName()!= null) {
			doc.addField(NAME, e.getNetworkName());
		} else if ( e.getNetworkDescription() !=null ) {
			doc.addField(DESC, e.getNetworkDescription());
		} else if ( e.getNetworkVersion() !=null) {
			doc.addField(VERSION, e.getNetworkVersion());			
		}
		
		for ( String otherIndexedName: otherAttributes) {
			if ( e.getAttributes().get(otherIndexedName) !=null) {
				addStringOrListgObj(e.getAttributes().get(otherIndexedName), otherIndexedName, warnings);
			}
		}
	
		return warnings;
		
	}

	private void addStringAttrFromAttributeElement(NetworkAttributesElement e, String solrFieldName, List<String>  warnings ) {
		if (e.getDataType() == ATTRIBUTE_DATA_TYPE.STRING) {
			if ( e.getValue() !=null && e.getValue().length()>0)
				doc.addField(solrFieldName, e.getValue());
		} else 
			warnings.add("Network attribute " + e.getName() + " is not indexed because its data type is not 'string'.");
	}
	
	private void addStringOrListgObj(Object e, String solrFieldName, List<String>  warnings ) {
		if (e instanceof String) {
			doc.addField(solrFieldName, e);
		} else if (e instanceof List<?>) {
			for ( Object value : ((List<?>)e)) {
				if ( value instanceof String)
					doc.addField(solrFieldName, value);
				else {
					warnings.add("Network attribute " + solrFieldName +  " is not indexed because its data type is not 'string' or 'list_of_string'.");
					break;
				}
			}
		} else 
			warnings.add("Network attribute " + solrFieldName + " is not indexed because its data type is not 'string' or 'list_of_string'.");
	}	
	
	private void addStringListgAttribute(NetworkAttributesElement e, String solrFieldName, List<String>  warnings ) {
		if (e.getDataType() == ATTRIBUTE_DATA_TYPE.STRING) {
			if ( e.getValue() !=null && e.getValue().length()>0)
				doc.addField(solrFieldName, e.getValue());
		} else if (e.getDataType() == ATTRIBUTE_DATA_TYPE.LIST_OF_STRING) {
			for ( String value : e.getValues()) {
				if ( value !=null && value.length()>0)
					doc.addField(solrFieldName, value);
			}
		} else 
			warnings.add("Network attribute " + e.getName() + " is not indexed because its data type is not 'string' or 'list_of_string'.");
	}	
	
	public void deleteNetwork(String networkId) throws SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		client.deleteById(networkId);
	//	client.commit(false,true,true);
	}
	
	public void commit () throws SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		Collection<SolrInputDocument> docs = new ArrayList<>(1);
		docs.add(doc);
		client.add(docs);
		client.commit(false,true,true);
		docs.clear();
		doc = new SolrInputDocument();
		nodeMembers = new TreeMap<>();

	}
	
/*	public void updateNetworkProperties (String networkId, Collection<NdexPropertyValuePair> props, Date updateTime) throws SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		SolrInputDocument tmpdoc = new SolrInputDocument();
		tmpdoc.addField(UUID, networkId);

	
		Set<String> indexedAttributes = new TreeSet<> ();	
		for ( NdexPropertyValuePair prop : props) {
			if ( otherAttributes.contains(prop.getPredicateString()) ) {
				indexedAttributes.add(prop.getPredicateString());
				Map<String,String> cmd = new HashMap<>();
				cmd.put("set", prop.getValue());
				tmpdoc.addField(prop.getPredicateString(), cmd);
			}
		}

		for ( String attr : otherAttributes) {
			if ( !indexedAttributes.contains(attr)) {
				Map<String,String> cmd = new HashMap<>();
				cmd.put("set", null);
				tmpdoc.addField(attr, cmd);
			}
		}
		
		Map<String,Timestamp> cmd = new HashMap<>();
		cmd.put("set",  new java.sql.Timestamp(updateTime.getTime()));
		tmpdoc.addField(MODIFICATION_TIME, cmd);
		
		Collection<SolrInputDocument> docs = new ArrayList<>(1);
		docs.add(tmpdoc);
		client.add(docs);
		client.commit(false, true,true);
	} */
	
	
/*	public void updateNetworkProfile(String networkId, Map<String,String> table) throws SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		SolrInputDocument tmpdoc = new SolrInputDocument();
		tmpdoc.addField(UUID, networkId);
		 
		String newTitle = table.get(NdexClasses.Network_P_name); 
		if ( newTitle !=null) {
			Map<String,String> cmd = new HashMap<>();
			cmd.put("set", newTitle);
			tmpdoc.addField(NAME, cmd);
		}
		
		String newDesc =table.get(NdexClasses.Network_P_desc);
		if ( newDesc != null) {
			Map<String,String> cmd = new HashMap<>();
			cmd.put("set", newDesc);
			tmpdoc.addField(DESC, cmd);
		}
		
		String newVersion = table.get(NdexClasses.Network_P_version);
		if ( newVersion !=null) {
			Map<String,String> cmd = new HashMap<>();
			cmd.put("set", newVersion);
			tmpdoc.addField(VERSION, cmd);
		}
		
			Map<String,Timestamp> cmd = new HashMap<>();
			java.util.Date now = Calendar.getInstance().getTime();
			cmd.put("set",  new java.sql.Timestamp(now.getTime()));
			tmpdoc.addField(MODIFICATION_TIME, cmd);
	
		
		Collection<SolrInputDocument> docs = new ArrayList<>(1);
		docs.add(tmpdoc);
		client.add(docs);
		client.commit(false,true,true);

	} */
	
/*	public void updateNetworkVisibility(String networkId, String vt) throws SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		SolrInputDocument tmpdoc = new SolrInputDocument();
		tmpdoc.addField(UUID, networkId);
		 
		if ( vt!=null) {
			Map<String,String> cmd = new HashMap<>();
			cmd.put("set",  vt);
			tmpdoc.addField(VISIBILITY, cmd);
		}
		
		Collection<SolrInputDocument> docs = new ArrayList<>(1);
		docs.add(tmpdoc);
		client.add(docs);
//		client.commit(false,true,true);

	} */
	
		
	
/*	public void revokeNetworkPermission(String networkId, String accountName, Permissions p, boolean isUser) 
			throws NdexException, SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		SolrInputDocument tmpdoc = new SolrInputDocument();
		tmpdoc.addField(UUID, networkId);
		 
		Map<String,String> cmd = new HashMap<>();
		cmd.put("remove", accountName);

		switch ( p) {
		case ADMIN : 
			if ( !isUser)
				throw new NdexException("Can't pass isUser=false for ADMIN permission when deleting Solr index.");
			tmpdoc.addField( USER_ADMIN, cmd);
			break;
		case WRITE:
			tmpdoc.addField( isUser? USER_EDIT: GRP_EDIT, cmd);
			break;
		case READ:
			tmpdoc.addField( isUser? USER_READ: GRP_READ, cmd);
			break;
			
		default: 
			throw new NdexException ("Invalid permission type " + p + " received in network previlege revoke.");
		}
		
		Collection<SolrInputDocument> docs = new ArrayList<>(1);
		docs.add(tmpdoc);
		client.add(docs);
	//	client.commit(false,true,true);

	} */
	
/*	public void grantNetworkPermission(String networkId, String accountName, Permissions newPermission, 
			 Permissions oldPermission, boolean isUser) 
			throws NdexException, SolrServerException, IOException {
		client.setBaseURL(solrUrl + "/" + coreName);
		SolrInputDocument tmpdoc = new SolrInputDocument();
		tmpdoc.addField(UUID, networkId);
		 
		Map<String,String> cmd = new HashMap<>();
		cmd.put("add", accountName);

		switch ( newPermission) {
		case ADMIN : 
			if ( !isUser)
				throw new NdexException("Can't pass isUser=false for ADMIN permission when creating Solr index.");
			tmpdoc.addField(  USER_ADMIN, cmd);
			break;
		case WRITE:
			tmpdoc.addField( isUser? USER_EDIT: GRP_EDIT, cmd);
			break;
		case READ:
			tmpdoc.addField( isUser? USER_READ: GRP_READ, cmd);
			break;		
		default: 
			throw new NdexException ("Invalid permission type " + newPermission
					+ " received in network previlege revoke.");
		}
		
		if ( oldPermission !=null ) {
			Map<String,String> rmCmd = new HashMap<>();
			rmCmd.put("remove", accountName);

			switch ( oldPermission) {
			case ADMIN : 
			
				tmpdoc.addField(  USER_ADMIN, rmCmd);
				break;
			case WRITE:
				tmpdoc.addField( isUser? USER_EDIT: GRP_EDIT, rmCmd);
				break;
			case READ:
				tmpdoc.addField( isUser? USER_READ: GRP_READ, rmCmd);
				break;
				
			default: 
				throw new NdexException ("Invalid permission type " + oldPermission + " received in network previlege revoke.");
			}
		}
		
		Collection<SolrInputDocument> docs = new ArrayList<>(1);
		docs.add(tmpdoc);
		client.add(docs);
	//	client.commit(false,true,true);

	}
	*/
	
	protected static List<String> getIndexableString(String termString) {
		
		// case 1 : termString is a URI
		// example: http://identifiers.org/uniprot/P19838
		// treat the last element in the URI as the identifier and the rest as
		// prefix string. Just to help the future indexing.
		//
	    List<String> result = new ArrayList<>(2) ;
		String identifier = null;
		if ( termString.length() > 10 && (termString.substring(0, 7).equalsIgnoreCase("http://") ||
				termString.substring(0, 8).equalsIgnoreCase("https://") )&&
				(!termString.endsWith("/"))) {
  		  try {
			URI termStringURI = new URI(termString);
				identifier = termStringURI.getFragment();
			
			    if ( identifier == null ) {
				    String path = termStringURI.getPath();
				    if (path != null && path.indexOf("/") != -1) {
				       int pos = termString.lastIndexOf('/');
					   identifier = termString.substring(pos + 1);
				    } else
				       return result; // the string is a URL in the format that we don't want to index it in Solr. 
			    } 
			    result.add(identifier);
			    return result;
			  
		  } catch (URISyntaxException e) {
			// ignore and move on to next case
		  }
		}
		
		String[] termStringComponents = TermUtilities.getNdexQName(termString);
		if (termStringComponents != null && termStringComponents.length == 2) {
			// case 2: termString is of the form (NamespacePrefix:)*Identifier
	//		if ( !termStringComponents[0].contains(" "))
			  result.add(termString);
			result.add(termStringComponents[1]);
			return  result;
		} 
		
		// case 3: termString cannot be parsed, use it as the identifier.
		// so leave the prefix as null and return the string
		result.add(termString);
		return result;
			
	}
	
	@Override
	public void close () {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
