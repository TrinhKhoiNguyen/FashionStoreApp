package com.example.fashionstoreapp.utils;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Endless scroll listener for RecyclerView pagination
 * Triggers loading when user scrolls near the end of the list
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 5; // Load more when 5 items from the end

    private int previousTotal = 0; // Total item count after last load
    private boolean loading = true; // Loading flag
    private int visibleThreshold = VISIBLE_THRESHOLD;

    private LinearLayoutManager layoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;

        // Adjust threshold for GridLayoutManager
        if (layoutManager instanceof GridLayoutManager) {
            visibleThreshold = VISIBLE_THRESHOLD * ((GridLayoutManager) layoutManager).getSpanCount();
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        // Only load when scrolling down
        if (dy <= 0)
            return;

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached, load more
            onLoadMore();
            loading = true;
        }
    }

    /**
     * Reset the listener state when data is cleared or refreshed
     */
    public void reset() {
        previousTotal = 0;
        loading = true;
    }

    /**
     * Callback when more items need to be loaded
     */
    public abstract void onLoadMore();
}
