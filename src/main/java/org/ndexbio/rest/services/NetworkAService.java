package org.ndexbio.rest.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.ndexbio.common.NdexClasses;
import org.ndexbio.common.access.NdexAOrientDBConnectionPool;
import org.ndexbio.common.access.NetworkAOrientDBDAO;
import org.ndexbio.common.exceptions.NdexException;
import org.ndexbio.model.object.SearchParameters;
import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.PropertyGraphNetwork;
import org.ndexbio.common.models.dao.orientdb.NetworkDAO;
import org.ndexbio.common.models.object.NetworkQueryParameters;
import org.ndexbio.rest.annotations.ApiDoc;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

@Path("/network")
public class NetworkAService extends NdexService {
	
	
	private NetworkAOrientDBDAO dao = NetworkAOrientDBDAO.getInstance();
	
	public NetworkAService(@Context HttpServletRequest httpRequest) {
		super(httpRequest);
	}
	
	@GET
	@Path("/{networkId}/term/{skipBlocks}/{blockSize}")
	@Produces("application/json")
	@ApiDoc("Returns a block of terms from the network specified by networkId as a list. "
			+ "'blockSize' specifies the maximum number of terms to retrieve in the block, "
			+ "'skipBlocks' specifies the number of blocks to skip.")
	public List<BaseTerm> getTerms(
			@PathParam("networkId") final String networkId,
			@PathParam("skipBlocks") final int skipBlocks, 
			@PathParam("blockSize") final int blockSize)
			
			throws IllegalArgumentException, NdexException {
		
		return dao.getTerms(this.getLoggedInUser(), networkId, skipBlocks, blockSize);
		
	}
	
	@GET
	@Path("/{networkId}/edge/{skipBlocks}/{blockSize}")
	@Produces("application/json")
	@ApiDoc("Returns a network based on a set of edges selected from the network "
			+ "specified by networkId. The returned network is fully poplulated and "
			+ "'self-sufficient', including all nodes, terms, supports, citations, "
			+ "and namespaces. The query selects a number of edges specified by the "
			+ "'blockSize' parameter, starting at an offset specified by the 'skipBlocks' parameter.")
	public PropertyGraphNetwork getEdges(
			@PathParam("networkId") final String networkId,
			@PathParam("skipBlocks") final int skipBlocks, 
			@PathParam("blockSize") final int blockSize)
	
			throws IllegalArgumentException, NdexException {
		
		ODatabaseDocumentTx db = NdexAOrientDBConnectionPool.getInstance().acquire();
		NetworkDAO dao = new NetworkDAO(db);
 		PropertyGraphNetwork n = dao.getProperytGraphNetworkById(UUID.fromString(networkId));
		db.close();
        return n;		
	}

	
	@POST
	@Path("/{networkId}/query/{skipBlocks}/{blockSize}")
	@Produces("application/json")
	@ApiDoc("Returns a network based on a block of edges retrieved by the POSTed queryParameters "
			+ "from the network specified by networkId. The returned network is fully poplulated and "
			+ "'self-sufficient', including all nodes, terms, supports, citations, and namespaces.")
	public Network queryNetwork(
			@PathParam("networkId") final String networkId,
			final NetworkQueryParameters queryParameters,
			@PathParam("skipBlocks") final int skipBlocks, 
			@PathParam("blockSize") final int blockSize)
	
			throws IllegalArgumentException, NdexException {
		
		return dao.queryForSubnetwork(this.getLoggedInUser(), networkId, queryParameters, skipBlocks, blockSize);
	}

	
	
	@POST
	@Path("/{networkId}/asPropertyGraph/query/{skipBlocks}/{blockSize}")
	@Produces("application/json")
	@ApiDoc("Returns a network based on a block of edges retrieved by the POSTed queryParameters "
			+ "from the network specified by networkId. The returned network is fully poplulated and "
			+ "'self-sufficient', including all nodes, terms, supports, citations, and namespaces.")
	public Network queryNetworkAsPropertyGraph(
			@PathParam("networkId") final String networkId,
			final NetworkQueryParameters queryParameters,
			@PathParam("skipBlocks") final int skipBlocks, 
			@PathParam("blockSize") final int blockSize)
	
			throws IllegalArgumentException, NdexException {
		
		return dao.queryForSubnetwork(this.getLoggedInUser(), networkId, queryParameters, skipBlocks, blockSize);
	}
	
	

	@POST
	@Path("/search/{skipBlocks}/{blockSize}")
	@Produces("application/json")
	@ApiDoc("Returns a list of NetworkDescriptors based on POSTed NetworkQuery.  The allowed NetworkQuery subtypes "+
	        "for v1.0 will be NetworkSimpleQuery and NetworkMembershipQuery. 'blockSize' specifies the number of " +
			"NetworkDescriptors to retrieve in each block, 'skipBlocks' specifies the number of blocks to skip.")
	public List<NetworkSummary> searchNetwork(
			final SearchParameters query,
			@PathParam("skipBlocks") final int skipBlocks, 
			@PathParam("blockSize") final int blockSize)
	
			throws IllegalArgumentException, NdexException {
        List<NetworkSummary> result = new ArrayList <NetworkSummary> ();
		if (query.getSearchString().equals("*")) {
			ODatabaseDocumentTx db = NdexAOrientDBConnectionPool.getInstance().acquire();
			ORecordIteratorClass<ODocument> networks = db.browseClass(NdexClasses.Network);
			for (ODocument doc : networks) {
				result.add(NetworkDAO.getNetworkSummary(doc));
			}
			db.close();
	        return result;		
		}
		throw new NdexException ("Feature not implemented yet.") ;
	}

	
}