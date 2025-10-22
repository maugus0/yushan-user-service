package com.yushan.user_service.service;

import com.yushan.user_service.client.ContentServiceClient;
import com.yushan.user_service.client.dto.ChapterInfoDTO;
import com.yushan.user_service.client.dto.NovelInfoDTO;
import com.yushan.user_service.dao.LibraryMapper;
import com.yushan.user_service.dao.NovelLibraryMapper;
import com.yushan.user_service.dto.ApiResponse;
import com.yushan.user_service.dto.LibraryResponseDTO;
import com.yushan.user_service.dto.PageResponseDTO;
import com.yushan.user_service.entity.Library;
import com.yushan.user_service.entity.NovelLibrary;
import com.yushan.user_service.enums.NovelStatus;
import com.yushan.user_service.exception.ResourceNotFoundException;
import com.yushan.user_service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private ContentServiceClient contentServiceClient;

    @Mock
    private NovelLibraryMapper novelLibraryMapper;

    @Mock
    private LibraryMapper libraryMapper;

    @InjectMocks
    private LibraryService libraryService;

    private UUID userId;
    private Integer novelId;
    private Integer chapterId;
    private NovelInfoDTO novelInfo;
    private ChapterInfoDTO chapterInfo;
    private Library library;
    private NovelLibrary novelLibrary;

    private <T> ApiResponse<T> createApiResponse(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        novelId = 1;
        chapterId = 10;

        novelInfo = new NovelInfoDTO(novelId, "Test Novel", "Test Author",
                "cover.jpg", 100, NovelStatus.PUBLISHED.name());
        chapterInfo = new ChapterInfoDTO(chapterId, 1, novelId);

        library = new Library();
        library.setId(1);
        library.setUserId(userId);

        novelLibrary = new NovelLibrary();
        novelLibrary.setId(1);
        novelLibrary.setLibraryId(1);
        novelLibrary.setNovelId(novelId);
        novelLibrary.setProgress(chapterId);
    }

    // ======= addNovelToLibrary Tests =======

    @Test
    void addNovelToLibrary_Success() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(chapterInfo)));
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);
        when(libraryMapper.selectByUserId(userId))
                .thenReturn(library);

        // When
        libraryService.addNovelToLibrary(userId, novelId, chapterId);

        // Then
        verify(novelLibraryMapper).insertSelective(any(NovelLibrary.class));
    }

    @Test
    void addNovelToLibrary_NovelNotFound() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(null));

        // When & Then
        assertThatThrownBy(() -> libraryService.addNovelToLibrary(userId, novelId, chapterId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("novel not found: " + novelId);
    }

    @Test
    void addNovelToLibrary_ChapterNotFound() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.emptyList()));

        // When & Then
        assertThatThrownBy(() -> libraryService.addNovelToLibrary(userId, novelId, chapterId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Chapter not found with id: " + chapterId);
    }

    @Test
    void addNovelToLibrary_ChapterNotBelongToNovel() {
        // Given
        ChapterInfoDTO wrongChapter = new ChapterInfoDTO(chapterId, 5, 999);
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(wrongChapter)));

        // When & Then
        assertThatThrownBy(() -> libraryService.addNovelToLibrary(userId, novelId, chapterId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Chapter doesn't belong to novel id: " + novelId);
    }

    @Test
    void addNovelToLibrary_NovelAlreadyExists() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(chapterInfo)));
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);

        // When & Then
        assertThatThrownBy(() -> libraryService.addNovelToLibrary(userId, novelId, chapterId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("novel has existed in library");
    }

    @Test
    void addNovelToLibrary_UserLibraryNotFound() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(chapterInfo)));
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);
        when(libraryMapper.selectByUserId(userId))
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> libraryService.addNovelToLibrary(userId, novelId, chapterId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with ID " + userId + " does not have a library");
    }

    // ======= removeNovelFromLibrary Tests =======

    @Test
    void removeNovelFromLibrary_Success() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);

        // When
        libraryService.removeNovelFromLibrary(userId, novelId);

        // Then
        verify(novelLibraryMapper).deleteByPrimaryKey(novelLibrary.getId());
    }

    @Test
    void removeNovelFromLibrary_NovelNotInLibrary() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> libraryService.removeNovelFromLibrary(userId, novelId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("novel don't exist in library");
    }

    // ======= batchRemoveNovelsFromLibrary Tests =======

    @Test
    void batchRemoveNovelsFromLibrary_Success() {
        // Given
        List<Integer> novelIds = Arrays.asList(1, 2, 3);
        List<NovelLibrary> libraryEntries = Arrays.asList(
                createNovelLibrary(1, 1),
                createNovelLibrary(2, 2),
                createNovelLibrary(3, 3)
        );
        when(novelLibraryMapper.selectByUserIdAndNovelIds(userId, novelIds))
                .thenReturn(libraryEntries);

        // When
        libraryService.batchRemoveNovelsFromLibrary(userId, novelIds);

        // Then
        verify(novelLibraryMapper).deleteByUserIdAndNovelIds(userId, novelIds);
    }

    @Test
    void batchRemoveNovelsFromLibrary_EmptyList() {
        // When
        libraryService.batchRemoveNovelsFromLibrary(userId, Collections.emptyList());

        // Then
        verify(novelLibraryMapper, never()).selectByUserIdAndNovelIds(any(), any());
        verify(novelLibraryMapper, never()).deleteByUserIdAndNovelIds(any(), any());
    }

    @Test
    void batchRemoveNovelsFromLibrary_SomeNovelsNotInLibrary() {
        // Given
        List<Integer> novelIds = Arrays.asList(1, 2, 3);
        List<NovelLibrary> libraryEntries = Arrays.asList(
                createNovelLibrary(1, 1),
                createNovelLibrary(2, 2)
        );
        when(novelLibraryMapper.selectByUserIdAndNovelIds(userId, novelIds))
                .thenReturn(libraryEntries);

        // When & Then
        assertThatThrownBy(() -> libraryService.batchRemoveNovelsFromLibrary(userId, novelIds))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("One or more novels are not in your library");
    }

    // ======= getUserLibrary Tests =======

    @Test
    void getUserLibrary_Success() {
        // Given
        List<Integer> allNovelIds = Arrays.asList(1, 2);
        List<NovelInfoDTO> allNovels = Arrays.asList(
                new NovelInfoDTO(1, "Novel 1", "Author 1", "c1.jpg", 50, "PUBLISHED"),
                new NovelInfoDTO(2, "Novel 2", "Author 2", "c2.jpg", 60, "PUBLISHED")
        );
        List<NovelLibrary> paginatedLibraries = Arrays.asList(
                createNovelLibrary(1, 1),
                createNovelLibrary(2, 2)
        );
        // 修正：从分页结果中动态获取去重后的 chapterId 列表
        List<Integer> distinctChapterIds = paginatedLibraries.stream()
                .map(NovelLibrary::getProgress)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<ChapterInfoDTO> chapters = Collections.singletonList(
                new ChapterInfoDTO(10, 1, 1)
        );

        when(novelLibraryMapper.selectNovelIdsByUserId(userId)).thenReturn(allNovelIds);
        when(contentServiceClient.getNovelsByIds(allNovelIds)).thenReturn(createApiResponse(allNovels));
        when(novelLibraryMapper.countByUserId(eq(userId), anyList())).thenReturn(2L);
        when(novelLibraryMapper.selectByUserIdWithPagination(eq(userId), anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedLibraries);
        // 使用去重后的 ID 列表进行 mock
        when(contentServiceClient.getChaptersByIds(distinctChapterIds)).thenReturn(createApiResponse(chapters));

        // When
        PageResponseDTO<LibraryResponseDTO> result = libraryService.getUserLibrary(userId, 0, 10, "createTime", "desc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getContent().get(0).getNovelTitle()).isEqualTo("Novel 1");
    }

    @Test
    void getUserLibrary_EmptyLibrary() {
        // Given
        when(novelLibraryMapper.selectNovelIdsByUserId(userId))
                .thenReturn(Collections.emptyList());

        // When
        PageResponseDTO<LibraryResponseDTO> result = libraryService.getUserLibrary(userId, 0, 10, "createTime", "desc");

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getUserLibrary_NoPublishedNovels() {
        // Given
        List<Integer> novelIds = Arrays.asList(1, 2);
        List<NovelInfoDTO> novels = Arrays.asList(
                new NovelInfoDTO(1, "Novel 1", "Author 1", "c1.jpg", 50, "DRAFT"),
                new NovelInfoDTO(2, "Novel 2", "Author 2", "c2.jpg", 60, "DRAFT")
        );

        when(novelLibraryMapper.selectNovelIdsByUserId(userId)).thenReturn(novelIds);
        when(contentServiceClient.getNovelsByIds(novelIds))
                .thenReturn(createApiResponse(novels));

        // When
        PageResponseDTO<LibraryResponseDTO> result = libraryService.getUserLibrary(userId, 0, 10, "createTime", "desc");

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ======= updateReadingProgress Tests =======

    @Test
    void updateReadingProgress_Success() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(chapterInfo)));
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);

        // When
        LibraryResponseDTO result = libraryService.updateReadingProgress(userId, novelId, chapterId);

        // Then
        verify(novelLibraryMapper).updateByPrimaryKeySelective(novelLibrary);
        assertThat(result).isNotNull();
        assertThat(result.getNovelId()).isEqualTo(novelId);
        assertThat(result.getProgress()).isEqualTo(chapterId);
    }

    @Test
    void updateReadingProgress_ProgressNull() {
        // When & Then
        assertThatThrownBy(() -> libraryService.updateReadingProgress(userId, novelId, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("progress cannot be null while updating");
    }

    @Test
    void updateReadingProgress_NovelNotInLibrary() {
        // Given
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(chapterInfo)));
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> libraryService.updateReadingProgress(userId, novelId, chapterId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("novel don't exist in library");
    }

    // ======= getNovel Tests =======

    @Test
    void getNovel_Success() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));
        when(contentServiceClient.getChaptersByIds(Collections.singletonList(chapterId)))
                .thenReturn(createApiResponse(Collections.singletonList(chapterInfo)));

        // When
        LibraryResponseDTO result = libraryService.getNovel(userId, novelId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNovelId()).isEqualTo(novelId);
        assertThat(result.getProgress()).isEqualTo(chapterId);
        assertThat(result.getNovelTitle()).isEqualTo("Test Novel");
    }

    @Test
    void getNovel_NoProgressChapter() {
        // Given
        novelLibrary.setProgress(null);
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);
        when(contentServiceClient.getNovelById(novelId))
                .thenReturn(createApiResponse(novelInfo));

        // When
        LibraryResponseDTO result = libraryService.getNovel(userId, novelId);

        // Then
        verify(contentServiceClient, never()).getChaptersByIds(any());
        assertThat(result).isNotNull();
        assertThat(result.getProgress()).isNull();
        assertThat(result.getChapterNumber()).isNull();
    }

    @Test
    void getNovel_NovelNotInLibrary() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> libraryService.getNovel(userId, novelId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("novel don't exist in library");
    }

    // ======= novelFromLibrary Tests =======

    @Test
    void novelFromLibrary_Success() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);

        // When
        NovelLibrary result = libraryService.novelFromLibrary(userId, novelId);

        // Then
        assertThat(result).isEqualTo(novelLibrary);
    }

    @Test
    void novelFromLibrary_NotFound() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);

        // When
        NovelLibrary result = libraryService.novelFromLibrary(userId, novelId);

        // Then
        assertThat(result).isNull();
    }

    // ======= novelInLibrary Tests =======

    @Test
    void novelInLibrary_True() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(novelLibrary);

        // When
        boolean result = libraryService.novelInLibrary(userId, novelId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void novelInLibrary_False() {
        // Given
        when(novelLibraryMapper.selectByUserIdAndNovelId(userId, novelId))
                .thenReturn(null);

        // When
        boolean result = libraryService.novelInLibrary(userId, novelId);

        // Then
        assertThat(result).isFalse();
    }

    // ======= checkNovelsInLibrary Tests =======

    @Test
    void checkNovelsInLibrary_Success() {
        // Given
        List<Integer> novelIds = Arrays.asList(1, 2, 3);
        List<NovelLibrary> libraryEntries = Arrays.asList(
                createNovelLibrary(1, 1),
                createNovelLibrary(2, 2)
        );

        when(novelLibraryMapper.selectByUserIdAndNovelIds(userId, novelIds))
                .thenReturn(libraryEntries);

        // When
        Map<Integer, Boolean> result = libraryService.checkNovelsInLibrary(userId, novelIds);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(1)).isTrue();
        assertThat(result.get(2)).isTrue();
        assertThat(result.get(3)).isFalse();
    }

    @Test
    void checkNovelsInLibrary_EmptyList() {
        // When
        Map<Integer, Boolean> result = libraryService.checkNovelsInLibrary(userId, Collections.emptyList());

        // Then
        assertThat(result).isEmpty();
    }

    // ======= Helper Methods =======

    private NovelLibrary createNovelLibrary(Integer id, Integer novelId) {
        NovelLibrary lib = new NovelLibrary();
        lib.setId(id);
        lib.setNovelId(novelId);
        lib.setLibraryId(1);
        lib.setProgress(10);
        return lib;
    }
}
