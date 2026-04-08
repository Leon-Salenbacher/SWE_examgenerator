package service.pdf.dto;

public record TocEntry(
        int page,
        String chapter,
        int possiblePoints
) {
}
