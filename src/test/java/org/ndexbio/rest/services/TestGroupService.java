package org.ndexbio.rest.services;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.rest.exceptions.DuplicateObjectException;
import org.ndexbio.rest.exceptions.NdexException;
import org.ndexbio.rest.exceptions.ObjectNotFoundException;
import org.ndexbio.rest.helpers.RidConverter;
import org.ndexbio.rest.models.Group;
import org.ndexbio.rest.models.SearchParameters;
import com.orientechnologies.orient.core.id.ORID;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGroupService extends TestNdexService
{
    private static final GroupService _groupService = new GroupService(_mockRequest);

    
    
    @Test
    public void createGroup()
    {
        Assert.assertTrue(createNewGroup());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGroupInvalid() throws IllegalArgumentException, NdexException
    {
        _groupService.createGroup(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGroupInvalidName() throws IllegalArgumentException, NdexException
    {
        final Group newGroup = new Group();
        newGroup.setDescription("This is a test group.");
        newGroup.setOrganizationName("Unit Tested Group");
        newGroup.setWebsite("http://www.ndexbio.org");
        
        _groupService.createGroup(null);
    }
    
    @Test
    public void deleteGroup()
    {
        Assert.assertTrue(createNewGroup());

        final ORID testGroupRid = getRid("Test Group");
        Assert.assertTrue(deleteTargetGroup(RidConverter.convertToJid(testGroupRid)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteGroupInvalid() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        _groupService.deleteGroup("");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteGroupNonexistant() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        _groupService.deleteGroup("C999R999");
    }

    @Test
    public void findGroups()
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("triptychjs");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        try
        {
            _groupService.findGroups(searchParameters);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void findGroupsInvalid() throws IllegalArgumentException, NdexException
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        _groupService.findGroups(null);
    }

    @Test
    public void getGroupById()
    {
        try
        {
            final ORID groupRid = getRid("triptychjs");
            final Group testGroup = _groupService.getGroup(RidConverter.convertToJid(groupRid));
            Assert.assertNotNull(testGroup);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void getGroupByName()
    {
        try
        {
            final Group testGroup = _groupService.getGroup("triptychjs");
            Assert.assertNotNull(testGroup);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGroupInvalid() throws IllegalArgumentException, NdexException
    {
        _groupService.getGroup("");
    }

    @Test
    public void updateGroup()
    {
        try
        {
            Assert.assertTrue(createNewGroup());
            
            //Refresh the user or the system won't know they have access to
            //update the group
            this.resetLoggedInUser();
            this.setLoggedInUser();
            
            final ORID testGroupRid = getRid("Test Group");
            final Group testGroup = _groupService.getGroup(RidConverter.convertToJid(testGroupRid));

            testGroup.setName("Updated Test Group");
            _groupService.updateGroup(testGroup);
            Assert.assertEquals(_groupService.getGroup(testGroup.getId()).getName(), testGroup.getName());

            Assert.assertTrue(deleteTargetGroup(testGroup.getId()));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateGroupInvalid() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        _groupService.updateGroup(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateGroupNonexistant() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final Group updatedGroup = new Group();
        updatedGroup.setId("C999R999");
        updatedGroup.setDescription("This is a test group.");
        updatedGroup.setName("Test Group");
        updatedGroup.setOrganizationName("Unit Tested Group");
        updatedGroup.setWebsite("http://www.ndexbio.org");

        _groupService.updateGroup(updatedGroup);
    }
    
    
    
    private boolean createNewGroup()
    {
        final Group newGroup = new Group();
        newGroup.setDescription("This is a test group.");
        newGroup.setName("Test Group");
        newGroup.setOrganizationName("Unit Tested Group");
        newGroup.setWebsite("http://www.ndexbio.org");

        try
        {
            final Group createdGroup = _groupService.createGroup(newGroup);
            Assert.assertNotNull(createdGroup);
            
            return true;
        }
        catch (DuplicateObjectException doe)
        {
            return true;
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private boolean deleteTargetGroup(String groupId)
    {
        try
        {
            _groupService.deleteGroup(groupId);
            Assert.assertNull(_groupService.getGroup(groupId));
            
            return true;
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}