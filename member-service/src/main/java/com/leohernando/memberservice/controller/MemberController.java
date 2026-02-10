package com.leohernando.memberservice.controller;

import com.leohernando.memberservice.dto.MemberRequestDTO;
import com.leohernando.memberservice.dto.MemberResponseDTO;
import com.leohernando.memberservice.dto.validators.CreateMemberValidationGroup;
import com.leohernando.memberservice.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/members")
@Tag(name = "Member", description = "API for managing Members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "Get Members")
    public ResponseEntity<List<MemberResponseDTO>> getAllMembers() {
        List<MemberResponseDTO> members = memberService.getMembers();
        return ResponseEntity.ok(members);
    }

    @PostMapping
    @Operation(summary = "Create a new Member")
    public ResponseEntity<MemberResponseDTO> createMember(@Validated({Default.class, CreateMemberValidationGroup.class})
                                                              @RequestBody MemberRequestDTO memberRequestDTO) {
        MemberResponseDTO memberResponseDTO = memberService.createMember(memberRequestDTO);
        return ResponseEntity.ok(memberResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a new Member")
    public ResponseEntity<MemberResponseDTO> updateMember(@PathVariable UUID id,
                                                          @Validated({Default.class}) @RequestBody MemberRequestDTO memberRequestDTO) {
        MemberResponseDTO memberResponseDTO = memberService.updateMember(id,
                memberRequestDTO);

        return ResponseEntity.ok(memberResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Member")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

}
