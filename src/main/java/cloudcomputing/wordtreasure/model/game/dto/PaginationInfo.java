package cloudcomputing.wordtreasure.model.game.dto;

public record PaginationInfo(
        Integer currentPage,
        Integer totalPages,
        Integer pageSize,
        Long totalItems
) {
    public static PaginationInfo of(int currentPage, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PaginationInfo(currentPage, totalPages, pageSize, totalItems);
    }
}
