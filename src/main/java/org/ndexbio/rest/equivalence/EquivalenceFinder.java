package org.ndexbio.rest.equivalence;

import java.util.Map;

import org.ndexbio.common.models.data.IBaseTerm;
import org.ndexbio.common.models.data.ICitation;
import org.ndexbio.common.models.data.IEdge;
import org.ndexbio.common.models.data.IFunctionTerm;
import org.ndexbio.common.models.data.INamespace;
import org.ndexbio.common.models.data.INetwork;
import org.ndexbio.common.models.data.INode;
import org.ndexbio.common.models.data.ISupport;
import org.ndexbio.common.models.object.BaseTerm;
import org.ndexbio.common.models.object.Citation;
import org.ndexbio.common.models.object.Edge;
import org.ndexbio.common.models.object.FunctionTerm;
import org.ndexbio.common.models.object.Namespace;
import org.ndexbio.common.models.object.Node;
import org.ndexbio.common.models.object.Support;

import com.tinkerpop.frames.VertexFrame;



public interface EquivalenceFinder {

    
    INetwork getTarget();
    
    Map<String, VertexFrame> getNetworkIndex();
    
    INamespace getNamespace(Namespace namespace);
    
    IBaseTerm getBaseTerm(BaseTerm baseTerm);
    
    IFunctionTerm getFunctionTerm(FunctionTerm functionTerm);
    
    ICitation getCitation(Citation citation);
    
    ISupport getSupport(Support support);
    
    INode getNode(Node node);
    
    IEdge getEdge(Edge edge);


}

