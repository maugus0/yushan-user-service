package com.yushan.user_service.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PageResponseDTOTest {

    @Test
    void testNoArgsConstructor() {
        PageResponseDTO<String> dto = new PageResponseDTO<>();
        assertNotNull(dto);
        assertNotNull(dto.getContent());
        assertTrue(dto.getContent().isEmpty());
    }

    @Test
    void testMainConstructorAndPaginationLogic() {
        List<String> content = List.of("item1", "item2");

        PageResponseDTO<String> firstPage = new PageResponseDTO<>(content, 10, 0, 2);
        assertEquals(content, firstPage.getContent());
        assertEquals(10, firstPage.getTotalElements());
        assertEquals(5, firstPage.getTotalPages());
        assertEquals(0, firstPage.getCurrentPage());
        assertEquals(2, firstPage.getSize());
        assertTrue(firstPage.isFirst());
        assertFalse(firstPage.isLast());
        assertTrue(firstPage.isHasNext());
        assertFalse(firstPage.isHasPrevious());

        PageResponseDTO<String> middlePage = new PageResponseDTO<>(content, 10, 2, 2);
        assertFalse(middlePage.isFirst());
        assertFalse(middlePage.isLast());
        assertTrue(middlePage.isHasNext());
        assertTrue(middlePage.isHasPrevious());

        PageResponseDTO<String> lastPage = new PageResponseDTO<>(content, 10, 4, 2);
        assertFalse(lastPage.isFirst());
        assertTrue(lastPage.isLast());
        assertFalse(lastPage.isHasNext());
        assertTrue(lastPage.isHasPrevious());

        PageResponseDTO<String> singlePage = new PageResponseDTO<>(content, 2, 0, 2);
        assertTrue(singlePage.isFirst());
        assertTrue(singlePage.isLast());
        assertFalse(singlePage.isHasNext());
        assertFalse(singlePage.isHasPrevious());
    }

    @Test
    void testStaticOfFactoryMethod() {
        List<String> content = List.of("a", "b");
        PageResponseDTO<String> dto = PageResponseDTO.of(content, 5, 0, 2);

        assertNotNull(dto);
        assertEquals(content, dto.getContent());
        assertEquals(5, dto.getTotalElements());
        assertEquals(3, dto.getTotalPages());
        assertEquals(0, dto.getCurrentPage());
    }

    @Test
    void testAllArgsConstructor() {
        List<String> content = List.of("data");
        PageResponseDTO<String> dto = new PageResponseDTO<>(content, 10L, 5, 2, 2, false, false, true, true);

        assertEquals(content, dto.getContent());
        assertEquals(10L, dto.getTotalElements());
        assertEquals(5, dto.getTotalPages());
        assertEquals(2, dto.getCurrentPage());
        assertEquals(2, dto.getSize());
        assertFalse(dto.isFirst());
        assertFalse(dto.isLast());
        assertTrue(dto.isHasNext());
        assertTrue(dto.isHasPrevious());
    }

    @Test
    void testContentDefensiveCopyInSetter() {
        List<String> originalList = new ArrayList<>();
        originalList.add("a");

        PageResponseDTO<String> dto = new PageResponseDTO<>();
        dto.setContent(originalList);

        originalList.add("b");

        assertEquals(1, dto.getContent().size());
        assertEquals("a", dto.getContent().get(0));
    }

    @Test
    void testContentDefensiveCopyInGetter() {
        PageResponseDTO<String> dto = new PageResponseDTO<>();
        dto.setContent(List.of("a"));

        List<String> retrievedList = dto.getContent();
        try {
            retrievedList.add("b");
        } catch (UnsupportedOperationException e) {
        }

        assertEquals(1, dto.getContent().size());
    }

    @Test
    void testContentDefensiveCopyInConstructor() {
        List<String> originalList = new ArrayList<>();
        originalList.add("a");

        PageResponseDTO<String> dto = new PageResponseDTO<>(originalList, 1, 0, 1);

        originalList.add("b");

        assertEquals(1, dto.getContent().size());
        assertEquals("a", dto.getContent().get(0));
    }

    @Test
    void testEqualsAndHashCode() {
        PageResponseDTO<String> dto1 = new PageResponseDTO<>(List.of("a"), 10L, 5, 2, 2, false, false, true, true);
        PageResponseDTO<String> dto2 = new PageResponseDTO<>(List.of("b"), 10L, 5, 2, 2, false, false, true, true);
        PageResponseDTO<String> dto3 = new PageResponseDTO<>(List.of("a"), 11L, 5, 2, 2, false, false, true, true);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        PageResponseDTO<String> dto = new PageResponseDTO<>(List.of("a"), 1, 0, 1);
        String dtoAsString = dto.toString();

        assertTrue(dtoAsString.contains("totalElements=1"));
        assertTrue(dtoAsString.contains("totalPages=1"));
        assertTrue(dtoAsString.contains("currentPage=0"));
    }
}
