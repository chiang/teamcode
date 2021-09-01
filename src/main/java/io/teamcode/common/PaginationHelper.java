package io.teamcode.common;

import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by chiang on 2017. 4. 26..
 */
public abstract class PaginationHelper {

    private static final String PAGINATION_HEADER_PAGE = "X-Page";

    private static final String PAGINATION_HEADER_PER_PAGE = "X-Per-Page";

    private static final String PAGINATION_HEADER_PREV_PAGE = "X-Prev-Page";

    private static final String PAGINATION_HEADER_NEXT_PAGE = "X-Next-Page";

    private static final String PAGINATION_HEADER_TOTAL = "X-Total";

    private static final String PAGINATION_HEADER_TOTAL_PAGES = "X-Total-Pages";

    /**
     *
     X-Next-Page:2
     X-Page:1
     X-Per-Page:20
     X-Prev-Page:
     X-Total:46200
     X-Total-Pages:2310
     *
     * Spring data jpa 에서는 page 번호가 0부터 시작하므로 이를 고려해서 처리합니다.
     *
     * @param httpServletResponse
     * @param page
     */
    public static final void putPaginationHeader(final HttpServletResponse httpServletResponse, Page<?> page) {
        httpServletResponse.addHeader(PAGINATION_HEADER_PAGE, String.valueOf(page.getNumber() + 1));
        httpServletResponse.addHeader(PAGINATION_HEADER_PER_PAGE, String.valueOf(page.getSize()));

        if (page.getTotalPages() > page.getNumber() + 1)
            httpServletResponse.addHeader(PAGINATION_HEADER_NEXT_PAGE, String.valueOf(page.getNumber() + 2));
        else
            httpServletResponse.addHeader(PAGINATION_HEADER_NEXT_PAGE, "");

        if (page.getNumber() > 0)
            httpServletResponse.addHeader(PAGINATION_HEADER_PREV_PAGE, String.valueOf(page.getNumber()));
        else
            httpServletResponse.addHeader(PAGINATION_HEADER_PREV_PAGE, "");

        httpServletResponse.addHeader(PAGINATION_HEADER_TOTAL, String.valueOf(page.getTotalElements()));
        httpServletResponse.addHeader(PAGINATION_HEADER_TOTAL_PAGES, String.valueOf(page.getTotalPages()));
    }
}
