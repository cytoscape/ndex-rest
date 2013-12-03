package org.ndexbio.rest.models;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkQueryParameters
{
    private List<String> _startingTermIds;
    private List<String> _startingTermStrings;
    private String _representationCriterion;
    private String _searchType;
    private List<String> _includedPredicateIds;
    private List<String> _excludedPredicateIds;
    private int _searchDepth;

    public NetworkQueryParameters()
    {
        super();

        this.initCollections();
    }

    
    
    public List<String> getStartingTermIds()
    {
        return _startingTermIds;
    }

    public void setStartingTermIds(List<String> startingTermIds)
    {
        _startingTermIds = startingTermIds;
    }

    public void addStartingTermId(String startingTermId)
    {
        _startingTermIds.add(startingTermId);
    }

    public List<String> getStartingTermStrings()
    {
        return _startingTermStrings;
    }

    public void setStartingTermStrings(List<String> startingTermStrings)
    {
        _startingTermStrings = startingTermStrings;
    }

    public String getRepresentationCriterion()
    {
        return _representationCriterion;
    }

    public void setRepresentationCriterion(String representationCriterion)
    {
        _representationCriterion = representationCriterion;
    }

    public String getSearchType()
    {
        return _searchType;
    }

    public void setSearchType(String searchType)
    {
        _searchType = searchType;
    }

    public List<String> getIncludedPredicateIds()
    {
        return _includedPredicateIds;
    }

    public void setIncludedPredicateIds(List<String> includedPredicateIds)
    {
        _includedPredicateIds = includedPredicateIds;
    }

    public List<String> getExcludedPredicateIds()
    {
        return _excludedPredicateIds;
    }

    public void setExcludedPredicateIds(List<String> excludedPredicateIds)
    {
        _excludedPredicateIds = excludedPredicateIds;
    }

    public int getSearchDepth()
    {
        return _searchDepth;
    }

    public void setSearchDepth(int searchDepth)
    {
        _searchDepth = searchDepth;
    }

    
    
    /**************************************************************************
    * Initializes the collections. 
    **************************************************************************/
    private void initCollections()
    {
        _startingTermIds = new ArrayList<String>();
        _startingTermStrings = new ArrayList<String>();
        _includedPredicateIds = new ArrayList<String>();
        _excludedPredicateIds = new ArrayList<String>();
    }
}