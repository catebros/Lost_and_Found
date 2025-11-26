package net.javaguids.lost_and_found.model.interfaces;

import net.javaguids.lost_and_found.search.SearchCriteria;

// Interface for objects that can be searched using SearchCriteria
public interface Searchable {
    // Checks if this object matches the given search criteria
    boolean matches(SearchCriteria criteria);

    // Returns a string containing all searchable keywords from this object
    String getSearchKeywords();
}