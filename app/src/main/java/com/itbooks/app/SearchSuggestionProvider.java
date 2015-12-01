package com.itbooks.app;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Database for all searched suggestions.
 *
 * @author Xinyue Zhao
 */
public final class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "com.itbooks";
	public final static int    MODE      = DATABASE_MODE_QUERIES;

	public SearchSuggestionProvider() {
		setupSuggestions( AUTHORITY, MODE );
	}
}
