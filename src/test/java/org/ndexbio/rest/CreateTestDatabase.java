package org.ndexbio.rest;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.rest.domain.IFunctionTerm;
import org.ndexbio.rest.domain.IGroup;
import org.ndexbio.rest.domain.ITerm;
import org.ndexbio.rest.domain.IUser;
import org.ndexbio.rest.models.Group;
import org.ndexbio.rest.models.Node;
import org.ndexbio.rest.models.Network;
import org.ndexbio.rest.models.User;
import org.ndexbio.rest.services.GroupService;
import org.ndexbio.rest.services.NetworkService;
import org.ndexbio.rest.services.UserService;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateTestDatabase {
	private static FramedGraphFactory _graphFactory = null;
	private static ODatabaseDocumentTx _ndexDatabase = null;
	private static FramedGraph<OrientBaseGraph> _orientDbGraph = null;

	private GroupService _groupService ;
	private  NetworkService _networkService; 
	private static final ObjectMapper _jsonMapper = new ObjectMapper();
	private  UserService _userService ;

	@Test
	public void checkDatabase() {
		try {
			// TODO: Refactor this to connect using a configurable
			// username/password, and database
			
		//	_ndexDatabase = ODatabaseDocumentPool.global().acquire(
		//			"remote:localhost/ndex", "ndex", "ndex");
		//	_ndexDatabase.drop();

			new OServerAdmin("remote:localhost/ndex")
                .connect("ndex", "ndex")
                .dropDatabase("ndex");

			System.out.println("Existing ndex database found and dropped");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			//_ndexDatabase.close();
		}

		try {
			System.out.println("Creating new ndex database");
			//new OServerAdmin("plocal:databases/ndex").connect("ndex", "ndex")
			//		.createDatabase("ndex", "document", "local");
			
			new OServerAdmin("remote:localhost/ndex").connect("ndex", "ndex")
			.createDatabase("ndex", "document", "local");

			System.out.println("Connecting to ndex database");
			_ndexDatabase = ODatabaseDocumentPool.global().acquire(
				"remote:localhost/ndex", "ndex", "ndex");
			
			
			if (null == _ndexDatabase){
				System.out.println("database is null");
			}

			///System.out.println("Connecting to ndex database");
			//_ndexDatabase = ODatabaseDocumentPool.global().acquire(
			//		"plocal:databases/ndex", "ndex", "ndex"); 
			
			System.out.println("Connected to ndex database");

			_graphFactory = new FramedGraphFactory(new GremlinGroovyModule(),
					new TypedGraphModuleBuilder().withClass(IGroup.class)
							.withClass(IUser.class).withClass(ITerm.class)
							.withClass(IFunctionTerm.class).build());
			
			System.out.println("GraphFactory created");

			_orientDbGraph = _graphFactory.create((OrientBaseGraph) new OrientGraph(_ndexDatabase));
			System.out.println("orientDbGraph  created");
	            
			OrientBaseGraph orientDbGraph = _orientDbGraph.getBaseGraph();
			System.out.println("orientdb database obtained");
			
			NdexSchemaManager.INSTANCE.init(_orientDbGraph.getBaseGraph());
			System.out.println("instance   created");
			
			if(null == this._networkService) {
				this._networkService = new NetworkService();
				System.out.println("network service created");
			}
			if (null == this._groupService) {
				this._groupService = new GroupService();
				System.out.println("group service created");
			}
			if (null == this._userService){
				this._userService = new UserService();
				System.out.println("user service created");
			}
			
			
			
		} catch (Exception e) {
			
			System.out.println("Exception in setup:" +e.getMessage());
			Assert.fail(e.getMessage());
			
		}
	}

	@Test
	public void createTestUser() {
		URL testUserUrl = getClass().getResource("dexterpratt.json");

		try {
			JsonNode rootNode = _jsonMapper.readTree(new File(testUserUrl
					.toURI()));

			System.out.println("Creating test user: "
					+ rootNode.get("username").asText());
			User testUser = _userService.createUser(rootNode.get("username")
					.asText(), rootNode.get("password").asText(),
					rootNode.get("emailAddress").asText());

			System.out.println("Updating " + rootNode.get("username").asText()
					+ "'s profile");
			createTestUserProfile(rootNode.get("profile"), testUser);

			System.out.println("Creating " + rootNode.get("username").asText()
					+ "'s networks");
			createTestUserNetworks(rootNode.get("networkFilenames"), testUser);

			System.out.println("Creating " + rootNode.get("username").asText()
					+ "'s groups");
			createTestUserGroups(rootNode.get("ownedGroups"), testUser);
		} catch (Exception e) {
			System.out.println("Exception in create user: " +e.getMessage());
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	private void createTestUserProfile(JsonNode profileNode, User testUser)
			throws Exception {
		testUser.setDescription(profileNode.get("description").asText());
		testUser.setFirstName(profileNode.get("firstName").asText());
		testUser.setLastName(profileNode.get("lastName").asText());
		testUser.setWebsite(profileNode.get("website").asText());

		_userService.updateUser(testUser);
	}

	
	
	private void createTestUserNetworks(JsonNode networkFilesNode, User testUser)
			throws Exception {
		Iterator<JsonNode> networksIterator = networkFilesNode.getElements();
		while (networksIterator.hasNext()) {
			// TODO:
			// JsonNode networkNode = _jsonMapper.readTree(new
			// File(networksIterator.next().asText()));
			File dataFile = new File(networksIterator.next().asText());
			System.out.println("Creating network from file: " +dataFile.getName());
			Network newNetwork = _jsonMapper.readValue(dataFile, Network.class);
			Network n = _networkService.createNetwork(testUser.getId(), newNetwork);
			Assert.assertNotNull(n);
		}

	}

	private void createTestUserGroups(JsonNode groupsNode, User testUser)
			throws Exception {
		Iterator<JsonNode> groupsIterator = groupsNode.getElements();
		while (groupsIterator.hasNext()) {
			JsonNode groupNode = groupsIterator.next();

			Group newGroup = new Group();
			newGroup.setName(groupNode.get("name").asText());

			JsonNode profileNode = groupNode.get("profile");
			newGroup.setDescription(profileNode.get("description").asText());
			newGroup.setOrganizationName(profileNode.get("organizationName")
					.asText());
			newGroup.setWebsite(profileNode.get("website").asText());

			_groupService.createGroup(testUser.getId(), newGroup);
		}
	}
}
