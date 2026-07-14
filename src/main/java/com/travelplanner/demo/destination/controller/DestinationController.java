package com.travelplanner.demo.destination.controller;

import com.travelplanner.demo.destination.dto.DestinationRequest;
import com.travelplanner.demo.destination.dto.DestinationResponse;
import com.travelplanner.demo.destination.service.DestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Destination", description = "여행지 CRUD API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @Operation(summary = "여행지 수정", description = "기존 여행지를 수정합니다. (JWT 인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = DestinationResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "여행지를 찾을 수 없음")
    })
    @PutMapping("/destinations/{id}")
    public ResponseEntity<?> updateDestination(
            @Parameter(description = "여행지 ID", example = "1", required = true) @PathVariable Integer id,
            @Valid @RequestBody DestinationRequest request) {
        System.out.println(">>>> debug destination controller update destination");
        System.out.println(">>>> debug param : " + request);
        DestinationResponse response = destinationService.update(id, request);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @Operation(summary = "여행지 삭제", description = "ID로 여행지를 삭제합니다. (JWT 인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "여행지를 찾을 수 없음")
    })
    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<?> deleteDestination(
            @Parameter(description = "여행지 ID", example = "1", required = true) @PathVariable Integer id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
