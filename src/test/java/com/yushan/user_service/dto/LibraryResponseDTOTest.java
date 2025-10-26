package com.yushan.user_service.dto;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class LibraryResponseDTOTest {

    @Test
    void testNoArgsConstructor() {
        LibraryResponseDTO dto = new LibraryResponseDTO();
        assertNotNull(dto);
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        Date now = new Date();
        LibraryResponseDTO dto = new LibraryResponseDTO(1, 101, "Title", "Author", "cover.jpg", 50, 5, 10, now, now);

        assertEquals(1, dto.getId());
        assertEquals(101, dto.getNovelId());
        assertEquals("Title", dto.getNovelTitle());
        assertEquals("Author", dto.getNovelAuthor());
        assertEquals("cover.jpg", dto.getNovelCover());
        assertEquals(50, dto.getProgress());
        assertEquals(5, dto.getChapterNumber());
        assertEquals(10, dto.getChapterCnt());
        assertEquals(now.getTime(), dto.getCreateTime().getTime());
        assertEquals(now.getTime(), dto.getUpdateTime().getTime());
    }

    @Test
    void testSetters() {
        LibraryResponseDTO dto = new LibraryResponseDTO();
        Date now = new Date();

        dto.setId(1);
        dto.setNovelId(101);
        dto.setNovelTitle("Title");
        dto.setNovelAuthor("Author");
        dto.setNovelCover("cover.jpg");
        dto.setProgress(50);
        dto.setChapterNumber(5);
        dto.setChapterCnt(10);
        dto.setCreateTime(now);
        dto.setUpdateTime(now);

        assertEquals(1, dto.getId());
        assertEquals(101, dto.getNovelId());
        assertEquals("Title", dto.getNovelTitle());
        assertEquals("Author", dto.getNovelAuthor());
        assertEquals("cover.jpg", dto.getNovelCover());
        assertEquals(50, dto.getProgress());
        assertEquals(5, dto.getChapterNumber());
        assertEquals(10, dto.getChapterCnt());
        assertEquals(now.getTime(), dto.getCreateTime().getTime());
        assertEquals(now.getTime(), dto.getUpdateTime().getTime());
    }

    @Test
    void testDateDefensiveCopyInSetter() {
        LibraryResponseDTO dto = new LibraryResponseDTO();
        Date originalDate = new Date();
        long originalTime = originalDate.getTime();

        dto.setCreateTime(originalDate);
        originalDate.setTime(originalTime + 10000);

        assertNotEquals(originalDate.getTime(), dto.getCreateTime().getTime());
        assertEquals(originalTime, dto.getCreateTime().getTime());
    }

    @Test
    void testDateDefensiveCopyInGetter() {
        Date originalDate = new Date();
        long originalTime = originalDate.getTime();
        LibraryResponseDTO dto = new LibraryResponseDTO();
        dto.setCreateTime(originalDate);

        Date retrievedDate = dto.getCreateTime();
        retrievedDate.setTime(originalTime + 10000);

        assertNotEquals(retrievedDate.getTime(), dto.getCreateTime().getTime());
        assertEquals(originalTime, dto.getCreateTime().getTime());
    }

    @Test
    void testDateDefensiveCopyInConstructor() {
        Date originalDate = new Date();
        long originalTime = originalDate.getTime();

        LibraryResponseDTO dto = new LibraryResponseDTO(1, 101, "T", "A", "C", 1, 1, 1, originalDate, originalDate);
        originalDate.setTime(originalTime + 10000);

        assertEquals(originalTime, dto.getCreateTime().getTime());
    }

    @Test
    void testEqualsAndHashCode() {
        Date now = new Date();
        LibraryResponseDTO dto1 = new LibraryResponseDTO(1, 101, "Title", "Author", "cover.jpg", 50, 5, 10, now, now);
        LibraryResponseDTO dto2 = new LibraryResponseDTO(1, 101, "Title", "Author", "cover.jpg", 50, 5, 10, now, now);
        LibraryResponseDTO dto3 = new LibraryResponseDTO(2, 102, "Title2", "Author2", "cover2.jpg", 60, 6, 11, now, now);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        Date now = new Date();
        LibraryResponseDTO dto = new LibraryResponseDTO(1, 101, "Title", "Author", "cover.jpg", 50, 5, 10, now, now);
        String dtoAsString = dto.toString();
        assertTrue(dtoAsString.contains("title=Title"));
        assertTrue(dtoAsString.contains("authorUsername=Author"));
    }
}
