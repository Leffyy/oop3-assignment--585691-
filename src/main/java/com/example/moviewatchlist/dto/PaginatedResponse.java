package com.example.moviewatchlist.dto;

import java.util.List;

/**
 * A generic wrapper class that holds paginated data along with pagination info.
 *
 * @param <T> the type of content in the page
 */
public class PaginatedResponse<T> {
    /**
     * The actual data items for this page.
     */
    private List<T> content;
    /**
     * Current page number (starts from 0).
     */
    private int pageNumber;
    /**
     * How many items per page.
     */
    private int pageSize;
    /**
     * Total number of items across all pages.
     */
    private long totalElements;
    /**
     * Total number of pages available.
     */
    private int totalPages;
    /**
     * Whether this is the last page.
     */
    private boolean last;
    /**
     * Whether this is the first page.
     */
    private boolean first;

    /**
     * Constructs a paginated response with the given data and pagination info.
     *
     * @param content the data items for this page
     * @param pageNumber the current page number (starts from 0)
     * @param pageSize the number of items per page
     * @param totalElements the total number of items across all pages
     * @param totalPages the total number of pages available
     */
    public PaginatedResponse(List<T> content, int pageNumber, int pageSize,
                             long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = pageNumber == 0;
        this.last = pageNumber == totalPages - 1;
    }

    /**
     * Gets the data items for this page.
     * @return the content list
     */
    public List<T> getContent() { return content; }

    /**
     * Gets the current page number.
     * @return the page number
     */
    public int getPageNumber() { return pageNumber; }

    /**
     * Gets the number of items per page.
     * @return the page size
     */
    public int getPageSize() { return pageSize; }

    /**
     * Gets the total number of items across all pages.
     * @return the total elements
     */
    public long getTotalElements() { return totalElements; }

    /**
     * Gets the total number of pages available.
     * @return the total pages
     */
    public int getTotalPages() { return totalPages; }

    /**
     * Returns true if this is the last page.
     * @return true if last page, false otherwise
     */
    public boolean isLast() { return last; }

    /**
     * Returns true if this is the first page.
     * @return true if first page, false otherwise
     */
    public boolean isFirst() { return first; }
}