package io.dnrdl12.remittance.comm.utills;

import io.dnrdl12.remittance.comm.api.PageDto;
import org.springframework.data.domain.*;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : io.dnrdl12.remittance.comm.utills
 * fileName       : PageUtils
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
public final class PageUtils {

    private PageUtils() {}

    /** "prop,DESC" 형태의 문자열 리스트를 Sort로 변환 */
    public static Sort toSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) return Sort.unsorted();
        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sortParams) {
            String[] sp = s.split(",", 2);
            String prop = sp[0].trim();
            Sort.Direction dir = (sp.length > 1 ? Sort.Direction.fromString(sp[1].trim()) : Sort.Direction.ASC);
            orders.add(new Sort.Order(dir, prop));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    public static Pageable toPageable(PageDto.PageReq req, int defaultSize, int maxSize) {
        int page = req.getPage() == null ? 0 : Math.max(0, req.getPage());
        int size = req.getSize() == null ? defaultSize : Math.max(1, Math.min(req.getSize(), maxSize));
        Sort sort = toSort(req.getSort());
        return PageRequest.of(page, size, sort);
    }

    public static <T> PageDto.PageResponse<T> toPageResponse(Page<T> page) {
        PageDto.PageInfo info = PageDto.PageInfo.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
        return PageDto.PageResponse.of(page.getContent(), info);
    }

    public static <T> PageDto.SliceResponse<T> toSliceResponse(Slice<T> slice) {
        return PageDto.SliceResponse.of(
                slice.getContent(),
                slice.isFirst(),
                slice.isLast(),
                slice.getSize(),
                slice.getNumberOfElements(),
                slice.hasNext()
        );
    }
}