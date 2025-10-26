package com.yushan.user_service.controller;

import com.yushan.user_service.dto.*;
import com.yushan.user_service.exception.UnauthorizedException;
import com.yushan.user_service.exception.ValidationException;
import com.yushan.user_service.security.CustomUserDetailsService;
import com.yushan.user_service.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/library")
@Validated
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    /**
     * add novel to library
     * @param request
     * @param authentication
     * @return
     */
    @PostMapping("/{novelId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<String> addNovelToLibrary(@PathVariable Integer novelId,
                                                 @RequestBody(required = false) @Valid LibraryRequestDTO request,
                                                 Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);

        Integer progress = (request != null) ? request.getProgress() : null;

        libraryService.addNovelToLibrary(userId, novelId, progress);
        return ApiResponse.success("Add novel to library successfully");
    }

    /**
     * remove novel from library
     * @param novelId
     * @param authentication
     * @return
     */
    @DeleteMapping("/{novelId}")
    public ApiResponse<String> removeNovelFromLibrary(@PathVariable Integer novelId,
                                                      Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        libraryService.removeNovelFromLibrary(userId, novelId);
        return ApiResponse.success("Remove novel from library successfully");
    }

    /**
     * batch remove novels from library
     * @param request
     * @param authentication
     * @return
     */
    @DeleteMapping("/batch")
    public ApiResponse<String> batchRemoveNovelsFromLibrary(@RequestBody @Valid BatchRequestDTO request,
                                                            Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        libraryService.batchRemoveNovelsFromLibrary(userId, request.getIds());
        return ApiResponse.success("batch remove successfully");
    }

    /**
     * get user library
     * @param page
     * @param size
     * @param sort
     * @param order
     * @param authentication
     * @return
     */
    @GetMapping
    public ApiResponse<PageResponseDTO<LibraryResponseDTO>> getUserLibrary(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
            @RequestParam(value = "order", defaultValue = "desc") String order,
            Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        PageResponseDTO<LibraryResponseDTO> response = libraryService.getUserLibrary(userId, page, size, sort, order);
        return ApiResponse.success("Novels retrieved successfully", response);
    }


    /**
     * check if novel in library
     * @param novelId
     * @param authentication
     * @return boolean
     */
    @GetMapping("/check/{novelId}")
    public ApiResponse<Boolean> checkNovelInLibrary(@PathVariable Integer novelId,
                                                    Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        boolean inLibrary = libraryService.novelInLibrary(userId, novelId);
        return ApiResponse.success(inLibrary);
    }


    /**
     * get novel info(progress) in library
     * @param novelId
     * @param authentication
     * @return libraryResponseDTO or http 404
     */
    @GetMapping("/{novelId}")
    public ApiResponse<LibraryResponseDTO> getLibraryNovel(@PathVariable Integer novelId,
                                                        Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        LibraryResponseDTO responseDTO = libraryService.getNovel(userId, novelId);
        return ApiResponse.success(responseDTO);
    }

    /**
     * update progress
     * @param novelId
     * @param request
     * @param authentication
     * @return
     */
    @PatchMapping("/{novelId}/progress")
    public ApiResponse<LibraryResponseDTO> updateReadingProgress(@PathVariable Integer novelId,
                                                                 @RequestBody @Valid LibraryRequestDTO request,
                                                                 Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        LibraryResponseDTO libraryResponseDTO = libraryService.updateReadingProgress(userId, novelId, request.getProgress());
        return ApiResponse.success(libraryResponseDTO);
    }

    /**
     * get current user id from authentication
     */
    protected UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserDetails) {
            String id = ((CustomUserDetailsService.CustomUserDetails) principal).getUserId();
            if (id != null) {
                return UUID.fromString(id);
            } else {
                throw new ValidationException("User ID not found");
            }
        }
        throw new UnauthorizedException("Invalid authentication");
    }
}
