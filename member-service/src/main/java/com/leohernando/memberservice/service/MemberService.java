package com.leohernando.memberservice.service;

import com.leohernando.memberservice.dto.MemberRequestDTO;
import com.leohernando.memberservice.dto.MemberResponseDTO;
import com.leohernando.memberservice.exception.EmailAlreadyExistsException;
import com.leohernando.memberservice.exception.MemberNotFoundException;
import com.leohernando.memberservice.grpc.BillingServiceGrpcClient;
import com.leohernando.memberservice.kafka.KafkaProducer;
import com.leohernando.memberservice.mapper.MemberMapper;
import com.leohernando.memberservice.model.Member;
import com.leohernando.memberservice.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public MemberService(MemberRepository memberRepository,
                         BillingServiceGrpcClient billingServiceGrpcClient,
                         KafkaProducer kafkaProducer
    ) {
        this.memberRepository = memberRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<MemberResponseDTO> getMembers() {
        List<Member> members = memberRepository.findAll();

        return members.stream().map(MemberMapper::toDTO).toList();
    }

    public MemberResponseDTO createMember(MemberRequestDTO memberRequestDTO) {
        if (memberRepository.existsByEmail(memberRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A member with this email " + "already exists" + memberRequestDTO.getEmail());
        }
        Member newMember = memberRepository.save(MemberMapper.toModel(memberRequestDTO));

        billingServiceGrpcClient.createBillingAccount(newMember.getId().toString(),
                newMember.getFullName(), newMember.getEmail());

        kafkaProducer.sendEvent(newMember);

        return MemberMapper.toDTO(newMember);
    }

    public MemberResponseDTO updateMember(UUID id,
                                          MemberRequestDTO memberRequestDTO) {

        Member member = memberRepository.findById(id).orElseThrow(
                () -> new MemberNotFoundException("Member not found with ID: " + id));

        if (memberRepository.existsByEmailAndIdNot(memberRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A member with this email " + "already exists"
                            + memberRequestDTO.getEmail());
        }

        member.setFirstName(memberRequestDTO.getFirstName());
        member.setLastName(memberRequestDTO.getLastName());
        member.setEmail(memberRequestDTO.getEmail());
        member.setPhoneNumber(memberRequestDTO.getPhoneNumber());
        member.setAddress(memberRequestDTO.getAddress());
        member.setDateOfBirth(LocalDate.parse(memberRequestDTO.getDateOfBirth()));
        if (memberRequestDTO.getMembershipEndDate() != null) {
            member.setMembershipEndDate(LocalDate.parse(memberRequestDTO.getMembershipEndDate()));
        }
        member.setStatus(memberRequestDTO.getStatus());

        Member updatedMember = memberRepository.save(member);
        return MemberMapper.toDTO(updatedMember);
    }

    public void deleteMember(UUID id) {
        memberRepository.deleteById(id);
    }
}
