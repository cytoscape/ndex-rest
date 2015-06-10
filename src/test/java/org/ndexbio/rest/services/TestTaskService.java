/**
 *   Copyright (c) 2013, 2015
 *  	The Regents of the University of California
 *  	The Cytoscape Consortium
 *
 *   Permission to use, copy, modify, and distribute this software for any
 *   purpose with or without fee is hereby granted, provided that the above
 *   copyright notice and this permission notice appear in all copies.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.ndexbio.rest.services;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.TaskType;
import org.ndexbio.model.object.Task;

import com.orientechnologies.orient.core.id.ORID;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestTaskService extends TestNdexService
{
    private static final TaskService _taskService = new TaskService(_mockRequest);

    
  /*  
    @Test
    public void createTask()
    {
        Assert.assertTrue(createNewTask());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTaskInvalid() throws IllegalArgumentException, NdexException
    {
 //       _taskService.createTask(null);
    }

    @Test
    public void deleteTask()
    {
        Assert.assertTrue(createNewTask());

        final ORID testTaskRid = getRid("This is a test task.");
        Assert.assertTrue(deleteTargetTask(IdConverter.toJid(testTaskRid)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteTaskInvalid() throws IllegalArgumentException, NdexException
    {
 //       _taskService.deleteTask("");
    }

    @Test
    public void getTask()
    {
        try
        {
            Assert.assertTrue(createNewTask());
            
            final ORID testTaskRid = getRid("This is a test task.");
            final Task testTask = _taskService.getTask(IdConverter.toJid(testTaskRid));
            Assert.assertNotNull(testTask);

            Assert.assertTrue(deleteTargetTask(testTask.getId())); 
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTaskInvalid() throws IllegalArgumentException, NdexException
    {
   //     _taskService.getTask("");
    }
*/
//    @Test
/*    public void updateTask()
    {
        try
        {
            Assert.assertTrue(createNewTask());
            
            final ORID testTaskRid = getRid("This is a test task.");
            final Task testTask = _taskService.getTask(IdConverter.toJid(testTaskRid));

            testTask.setDescription("This is an updated test task.");
            _taskService.updateTask(testTask);
            Assert.assertEquals(_taskService.getTask(testTask.getId()).getDescription(), testTask.getDescription());
            
            Assert.assertTrue(deleteTargetTask(testTask.getId())); 
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }
*/
 /*   @Test(expected = IllegalArgumentException.class)
    public void updateTaskInvalid() throws IllegalArgumentException, NdexException
    {
  //      _taskService.updateTask(null);
    }
    
    
    
    private boolean createNewTask()
    {
        final Task newTask = new Task();
        newTask.setDescription("This is a test task.");
        newTask.setType(TaskType.PROCESS_UPLOADED_NETWORK);
        
        try
        {
        //    final Task createdTask = _taskService.createTask(newTask);
          //  Assert.assertNotNull(createdTask);
            
            return true;
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private boolean deleteTargetTask(String taskId)
    {
        try
        {
          //  _taskService.deleteTask(taskId);
          //  Assert.assertNull(_taskService.getTask(taskId));
            
            return true;
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    } */
}
