package com.example.moviewatchlist.dto;

import java.util.List;

// A wrapper class that holds paginated data along with pagination info
public class PaginatedResponse<T> {
    // The actual data items for this page
    private List<T> content;
    // Current page number (starts from 0)
    private int pageNumber;
    // How many items per page
    private int pageSize;
    // Total number of items across all pages
    private long totalElements;
    // Total number of pages available
    private int totalPages;
    // Whether this is the last page
    private boolean last;
    // Whether this is the first page
    private boolean first;
    
    public PaginatedResponse(List<T> content, int pageNumber, int pageSize, 
                           long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        // First page is always page 0
        this.first = pageNumber == 0;
        // Last page check (handle empty results)
        this.last = pageNumber == totalPages - 1;
    }
    
    // Simple getters to access the data
    public List<T> getContent() { return content; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
    public boolean isFirst() { return first; }
}