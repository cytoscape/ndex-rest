package org.ndexbio.task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServerException;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.common.cx.AspectIterator;
import org.ndexbio.common.models.dao.postgresql.NetworkDAO;
import org.ndexbio.common.solr.NetworkGlobalIndexManager;
import org.ndexbio.common.solr.SingleNetworkSolrIdxManager;
import org.ndexbio.common.util.Util;
import org.ndexbio.model.cx.FunctionTermElement;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.TaskType;
import org.ndexbio.model.object.network.NetworkSummary;

public class SolrTaskRebuildNetworkIdx extends NdexSystemTask {

//private static Logger logger = Logger.getLogger(CXNetworkLoadingTask.class.getName());
	
	private UUID networkId;
	private SolrIndexScope idxScope;
	private boolean createOnly;
	
    private static final TaskType taskType = TaskType.SYS_SOLR_REBUILD_NETWORK_INDEX;
    public static final String AttrScope = "scope";
    public static final String AttrCreateOnly ="createOnly";
    private Set<String> indexedFields;
    
	
	public SolrTaskRebuildNetworkIdx (UUID networkUUID, SolrIndexScope scope, boolean createOnly, Set<String> indexedFields) {
		super();
		this.networkId = networkUUID;
		this.idxScope = scope;
		this.createOnly = createOnly;
		this.indexedFields =indexedFields;
	}
	
	@Override
	public void run() throws Exception { //throws NdexException, SolrServerException, IOException, SQLException  {

		try (NetworkDAO dao = new NetworkDAO()) {

				NetworkSummary summary = dao.getNetworkSummaryById(networkId);
				  if (summary == null)
					  throw new NdexException ("Network "+ networkId + " not found in the server." );
				  
				  //drop the old ones.
				  if ( !createOnly) {
					  if ( idxScope != SolrIndexScope.individual) {
							NetworkGlobalIndexManager globalIdx = new NetworkGlobalIndexManager();
						  globalIdx.deleteNetwork(networkId.toString());
					  } 	  
					  if ( idxScope != SolrIndexScope.global)
						  try (SingleNetworkSolrIdxManager idx2 = new SingleNetworkSolrIdxManager(networkId.toString())) {
							  idx2.dropIndex();
					  	  }
				  }

				  if ( this.idxScope != SolrIndexScope.global) {
					  try (SingleNetworkSolrIdxManager idx2 = new SingleNetworkSolrIdxManager(networkId.toString())) {
						  idx2.createIndex(indexedFields);
						  idx2.close();
			  			}
				  }
				  
				  if ( this.idxScope != SolrIndexScope.individual) {
					  
					  NetworkGlobalIndexManager globalIdx = new NetworkGlobalIndexManager();

					  // build the solr document obj
					  List<Map<Permissions, Collection<String>>> permissionTable =  dao.getAllMembershipsOnNetwork(networkId);
					  Map<Permissions,Collection<String>> userMemberships = permissionTable.get(0);
					  Map<Permissions,Collection<String>> grpMemberships = permissionTable.get(1);
					  globalIdx.createIndexDocFromSummary(summary,summary.getOwner(),
							userMemberships.get(Permissions.READ),
							userMemberships.get(Permissions.WRITE),
							grpMemberships.get(Permissions.READ),
							grpMemberships.get(Permissions.WRITE));

					  //process node attribute aspect and add to solr doc
					
					  try (AspectIterator<NetworkAttributesElement> it = new AspectIterator<>(networkId, NetworkAttributesElement.ASPECT_NAME, NetworkAttributesElement.class)) {
						while (it.hasNext()) {
							NetworkAttributesElement e = it.next();
							
							List<String> indexWarnings = globalIdx.addCXNetworkAttrToIndex(e);	
							if ( !indexWarnings.isEmpty())
								for (String warning : indexWarnings) 
									System.err.println("Warning: " + warning);
							
						}
					  }

					
					  try (AspectIterator<FunctionTermElement> it = new AspectIterator<>(networkId, FunctionTermElement.ASPECT_NAME, FunctionTermElement.class)) {
						while (it.hasNext()) {
							FunctionTermElement fun = it.next();
							
							globalIdx.addFunctionTermToIndex(fun);

						}
					  }

					  try (AspectIterator<NodeAttributesElement> it = new AspectIterator<>(networkId, NodeAttributesElement.ASPECT_NAME, NodeAttributesElement.class)) {
						while (it.hasNext()) {
							NodeAttributesElement e = it.next();					
							globalIdx.addCXNodeAttrToIndex(e);	
						}
					  }

					  try (AspectIterator<NodesElement> it = new AspectIterator<>(networkId, NodesElement.ASPECT_NAME, NodesElement.class)) {
						while (it.hasNext()) {
							NodesElement e = it.next();					
							globalIdx.addCXNodeToIndex(e);	
						}
					  }
									
					  globalIdx.commit();
				  }
					
				  try {
						  dao.setFlag(this.networkId, "iscomplete", true);
						  dao.commit();
				  } catch (SQLException e) {
						  throw new NdexException ("DB error when setting iscomplete flag: " + e.getMessage(), e);
				  }	
				  	
			} catch (SQLException | IOException | NdexException | SolrServerException e1) {
				e1.printStackTrace();
				try (NetworkDAO dao = new NetworkDAO()) {
					dao.setErrorMessage(networkId, "Failed to create Index on network. Index type: " + this.idxScope + ". Cause: " + e1.getMessage());
					dao.commit();
				}
				throw e1;
			}	 

	}
	
	private int getScoreFromSummary(NetworkSummary summary) {
		int score = 0;
		
		if ( summary.getName()!=null && !summary.getName().isEmpty())
			score +=10;
		if ( summary.getDescription() !=null && !summary.getDescription().isEmpty())
			score += 10;
		if ( summary.getVersion() !=null && !summary.getVersion().isEmpty()){
			score +=10;
		}
		
		score += Util.getNetworkScores(summary.getProperties(), false);
		
		return score;
	}


	@Override
	public Task createTask() {
		Task t = super.createTask();
		t.setResource(networkId.toString());
		t.getAttributes().put(AttrScope, this.idxScope);
		t.getAttributes().put(AttrCreateOnly, this.createOnly);
		t.setAttribute("fields", indexedFields);
		return t;
	}

	@Override
	public TaskType getTaskType() {
		return taskType;
	}
}
