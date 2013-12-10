package org.ndexbio.rest.domain;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.Map;

@TypeValue("Function")
public interface IFunctionTerm extends ITerm
{
    @Property("termParameters")
    public Map<Integer, ITerm> getTermParameters();

    @Property("termParameters")
    public void setTermParameters(Map<Integer, ITerm> termParameters);

    @Adjacency(label = "termFunc")
    public IBaseTerm getTermFunc();

    @Adjacency(label = "termFunc")
    public void setTermFunc(IBaseTerm term);

    @Property("textParameters")
    public Map<Integer, String> getTextParameters();

    @Property("textParameters")
    public void setTextParameters(Map<Integer, String> textParameters);
}
