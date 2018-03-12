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
package org.ndexbio.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ClientTaskProcessor extends NdexTaskProcessor {

	static Logger logger = LoggerFactory.getLogger(ClientTaskProcessor.class);
	
	public ClientTaskProcessor () {
		super();
	}
	
	@Override
	public void run() {
		while ( !shutdown) {
			NdexTask task = null;
			try {
				task = NdexServerQueue.INSTANCE.takeNextUserTask();
				if ( task == NdexServerQueue.endOfQueue) {
					logger.info("End of queue signal received. Shutdown client task processor.");
					return;
				}
			} catch (InterruptedException e) {
				logger.info("takeNextUserTask Interrupted: " + e.getMessage());
				return;
			}
			
			try {		        
//		        MDC.put("RequestsUniqueId", (String)task.getAttribute("RequestsUniqueId") );
				logger.info("[start: starting task]");
				task.call();
				logger.info("[end: task completed]");

			} catch (Exception e) {
				logger.error("Error occurred when executing task " + task.getTask().getExternalId());
				e.printStackTrace();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);     
				try {
					saveTaskStatus(task.getTask().getExternalId(), Status.FAILED, e.getMessage(), sw.toString() );
				} catch (NdexException | SQLException | IOException e1) {
					logger.error("Error occurred when saving task " + e1);
					e1.printStackTrace();
				} 
				
			} 
		}
	}



	
	
}
