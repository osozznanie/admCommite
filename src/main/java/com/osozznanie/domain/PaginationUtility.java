package com.osozznanie.domain;

import java.util.Arrays;

public class PaginationUtility {
	public static final int DEFAULT_BUTTONS_NUM = 10;
	public static final int INITIAL_PAGE_NUM = 1;
	public static final int INITIAL_PAGE_SIZE = 10;
	public static final Integer[] PAGE_SIZES = {5, 10, 20};

	private int buttonsToShow;
	private int startPage;
	private int endPage;

	public PaginationUtility(int totalPages, int currentPage) {
		buttonsToShow = DEFAULT_BUTTONS_NUM;
		int halfPagesToShow = buttonsToShow / 2;
		if (totalPages <= buttonsToShow) {
			startPage = INITIAL_PAGE_NUM;
			endPage = totalPages;
		} else if (currentPage - halfPagesToShow <= 0) {
			startPage = INITIAL_PAGE_NUM;
			endPage = buttonsToShow;
		} else if (currentPage + halfPagesToShow == totalPages) {
			startPage = currentPage - halfPagesToShow;
			endPage = totalPages;
		} else if (currentPage + halfPagesToShow > totalPages) {
			startPage = (totalPages - buttonsToShow + 1);
			endPage = totalPages;
		} else {
			startPage = currentPage - halfPagesToShow;
			endPage = currentPage + halfPagesToShow;
		}
	}

	public static int parsePageSize(String pageSizeStr) {
		int pageSize;
		try {
			if (pageSizeStr == null) {
				throw new NumberFormatException();
			}
			pageSize = Integer.parseInt(pageSizeStr);
			if (!Arrays.asList(PAGE_SIZES).contains(pageSize)) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			pageSize = INITIAL_PAGE_SIZE;
		}
		return pageSize;
	}

	public static int parsePageNumber(String pageNumStr) {
		int pageNum;
		try {
			if (pageNumStr == null) {
				throw new NumberFormatException();
			}
			pageNum = Integer.parseInt(pageNumStr);
			if (pageNum < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			pageNum = INITIAL_PAGE_NUM;
		}
		return pageNum;
	}

	public static int limitPageIndex(long itemsCount, int pageIndex, int pageSize) {
		int maxPageIndex = (int) (itemsCount / pageSize);
		maxPageIndex -= (itemsCount % pageSize == 0) ? 1 : 0;
		pageIndex = Math.min(pageIndex, maxPageIndex);
		return pageIndex;
	}

	public int getStartPage() {
		return startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	@Override
	public String toString() {
		return "Pager [startPage=" + startPage + ", endPage=" + endPage + "]";
	}
}

