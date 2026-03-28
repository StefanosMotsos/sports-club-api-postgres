package club.sportsapp.service;

import club.sportsapp.core.exceptions.EntityAlreadyExistsException;
import club.sportsapp.core.exceptions.EntityInvalidArgumentException;
import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.core.exceptions.FileUploadException;
import club.sportsapp.core.filters.MemberFilters;
import club.sportsapp.dto.MemberInsertDTO;
import club.sportsapp.dto.MemberReadOnlyDTO;
import club.sportsapp.dto.MemberUpdateDTO;
import club.sportsapp.mapper.Mapper;
import club.sportsapp.model.*;
import club.sportsapp.model.static_data.Sport;
import club.sportsapp.repository.*;
import club.sportsapp.specification.MemberSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService{

    private final MemberRepository memberRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final RoleRepository roleRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    public MemberReadOnlyDTO saveMember(MemberInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        try {
            if (memberRepository.findByVat(dto.vat()).isPresent() && dto.vat() != null) {
                throw new EntityAlreadyExistsException("Member", "Member with vat " + dto.vat() + " already exists");
            }

            if (personalInfoRepository.findByMembershipId(dto.personalInfoInsertDTO().membershipId()).isPresent()) {
                throw new EntityAlreadyExistsException("MembershipId", "Member with membership " + dto.personalInfoInsertDTO().membershipId() + " already exists");
            }

            if (userRepository.findByUsername(dto.userInsertDTO().username()).isPresent()) {
                throw new EntityAlreadyExistsException("User", "User with username " + dto.userInsertDTO().username() + " already exists");
            }

            Member member = mapper.mapToMemberEntity(dto);

            Sport sport = sportRepository.findById(dto.sportId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Sport", "Sport id= " + dto.sportId() + " invalid"));

            Role role = roleRepository.findById(dto.userInsertDTO().roleId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Role id= " + dto.userInsertDTO().roleId() + " invalid"));

            sport.addMember(member);

            User user = member.getUser();
            role.addUser(user);
            user.setPassword(passwordEncoder.encode(dto.userInsertDTO().password()));

            memberRepository.save(member);
            log.info("Member with vat={} saved successfully", dto.vat());
            return mapper.mapToMemberReadOnlyDTO(member);
        } catch (EntityAlreadyExistsException e) {
            log.error("Saved failed for member with vat={}. Member already exists", dto.vat(), e);
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed for member with vat={}. Sport id is invalid", dto.vat(), e);
            throw e;
        }
    }

    @Override
    @Retryable(
            retryFor = {IOException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void saveMembershipIdFile(UUID uuid, MultipartFile memberFile)
            throws FileUploadException, EntityNotFoundException {

        try {
            String originalFilename = memberFile.getOriginalFilename();
            String savedName = UUID.randomUUID() + getFileExtension(originalFilename);

            String uploadDirectory = uploadDir;
            Path filePath = Paths.get(uploadDirectory + savedName);
            Files.createDirectories(filePath.getParent());
            memberFile.transferTo(filePath);

            Attachment attachment = new Attachment();
            attachment.setFilename(originalFilename);
            attachment.setSavedName(savedName);
            attachment.setFilePath(filePath.toString());

            Tika tika = new Tika();
            String contentType = tika.detect(memberFile.getBytes());
            attachment.setContentType(contentType);
            attachment.setExtension(getFileExtension(originalFilename));

            Member member = memberRepository.findByUuid(uuid).orElseThrow(()
                    -> new EntityNotFoundException("Member", "Member with uuid=" + uuid + " not found"));

            PersonalInfo personalInfo = member.getPersonalInfo();

            if (personalInfo.getMembershipFile() != null) {
                Files.deleteIfExists(Path.of(personalInfo.getMembershipFile().getFilePath()));
                personalInfo.removeMembershipFile();
            }

            personalInfo.addMembershipFile(attachment);
            memberRepository.save(member);
            log.info("Attachment for member with membership={} saved", personalInfo.getMembershipId());

        } catch (EntityNotFoundException e) {
            log.error("Attachment for member with uuid={} not found", uuid, e);
        } catch (IOException | HttpServerErrorException e) {
            log.error("Error saving attachment for member with uuid={}", uuid, e);
            throw new FileUploadException("MembershipId", "Error saving attachment for member with uuid=" + uuid);
        }


    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_MEMBER')")
    public MemberReadOnlyDTO updateMember(MemberUpdateDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException {

        try {
            Member member = memberRepository.findByUuid(dto.uuid()).
                    orElseThrow(() -> new EntityNotFoundException("Member", "Update failed. Member with uuid " + dto.uuid() + " was not found"));

            mapper.updateMemberEntity(member, dto);

            if (!member.getVat().equals(dto.vat())) {
                if (memberRepository.findByVat(dto.vat()).isPresent()) {
                    throw new EntityAlreadyExistsException("Member", "Update failed. Member with vat " + dto.vat() + " already exists");
                }
                member.setVat(dto.vat());
            }

            if (!Objects.equals(member.getSport().getId(), dto.sportId())) {
                Sport sport = sportRepository.findById(dto.sportId())
                        .orElseThrow(() -> new EntityInvalidArgumentException("Sport","Sport id=" + dto.sportId() + " invalid"));
                Sport oldSport = member.getSport();
                if (oldSport != null) {
                    oldSport.removeMember(member);
                }
                sport.addMember(member);
            }

            if (!member.getUser().getUsername().equals(dto.userUpdateDTO().username())) {
                if (userRepository.findByUsername(dto.userUpdateDTO().username()).isPresent()) {
                    throw new EntityAlreadyExistsException("User", "Update failed. Member with username " + dto.userUpdateDTO().username() + " already exists");
                }
                member.getUser().setUsername((dto.userUpdateDTO().username()));
            }

            if (!member.getUser().getPassword().equals(dto.userUpdateDTO().password())) {
                member.getUser().setPassword(passwordEncoder.encode(dto.userUpdateDTO().password()));
            }

            if (!member.getPersonalInfo().getMembershipId().equals(dto.personalInfoUpdateDTO().membershipId())) {
                if (personalInfoRepository.findByMembershipId(dto.personalInfoUpdateDTO().membershipId()).isPresent()) {
                    throw new EntityAlreadyExistsException("PersonalInfo", "Update failed. Member with membership " + dto.personalInfoUpdateDTO().membershipId() + " already exists");
                }
                member.getPersonalInfo().setMembershipId(dto.personalInfoUpdateDTO().membershipId());
            }

            if (!member.getPersonalInfo().getIdentityNumber().equals(dto.personalInfoUpdateDTO().identityNumber())) {
                if (personalInfoRepository.findByMembershipId(dto.personalInfoUpdateDTO().identityNumber()).isPresent()) {
                    throw new EntityAlreadyExistsException("PersonalInfo", "Update failed. Member with identity number " + dto.personalInfoUpdateDTO().identityNumber() + " already exists");
                }
                member.getPersonalInfo().setMembershipId(dto.personalInfoUpdateDTO().identityNumber());
            }

            log.info("Member with uuid={} updated successfully", dto.uuid());
            return mapper.mapToMemberReadOnlyDTO(member);
        } catch (EntityNotFoundException e) {
            log.error("Update failed for member with uuid={}. Member not found", dto.uuid(), e);
            throw e;
        } catch (EntityAlreadyExistsException e) {
            log.error("Update failed for member with uuid={}. Member with vat={} already exists", dto.uuid(), dto.vat(), e);
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Update failed for member with uuid={}. Sport id={} invalid", dto.uuid(), dto.sportId(), e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_MEMBER')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public MemberReadOnlyDTO deleteMemberByUUID(UUID uuid) throws EntityNotFoundException {

        try {
            Member member = memberRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Member", "Member with uuid " + uuid + " was not found"));

            member.softDelete();
            member.getPersonalInfo().softDelete();
            member.getUser().softDelete();

            log.info("Member with uuid={} was deleted successfully", uuid);
            return (mapper.mapToMemberReadOnlyDTO(member));
        } catch (EntityNotFoundException e) {
            log.error("Update failed. Member with uuid={} was not found", uuid);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public MemberReadOnlyDTO getMemberByUUID(UUID uuid) throws EntityNotFoundException {

        try {
            Member member = memberRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Member", "Member with uuid " + uuid + " was not found"));

            log.debug("Get member by uuid={} returned successfully", uuid);
            return mapper.mapToMemberReadOnlyDTO(member);
        } catch (EntityNotFoundException e) {
            log.error("Get member by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public MemberReadOnlyDTO getMemberByUUIDAndDeletedFalse(UUID uuid) throws EntityNotFoundException {
        try {
            Member member = memberRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Member", "Member with uuid " + uuid + " was not found"));

            log.debug("Get non deleted member by uuid={} returned successfully", uuid);
            return mapper.mapToMemberReadOnlyDTO(member);
        } catch (EntityNotFoundException e) {
            log.error("Get non deleted member by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MEMBERS')")
    @Transactional(readOnly = true)
    public Page<MemberReadOnlyDTO> getPaginatedMembersFiltered(Pageable pageable, MemberFilters filters) throws EntityNotFoundException {
        try {
            if (filters.getVat() != null) {
                Member member = memberRepository.findByVat(filters.getVat())
                        .orElseThrow(() -> new EntityNotFoundException("Member", "Member with vat= " + filters.getVat() + " not found"));
                return singleResultPage(member, pageable);
            }

            if (filters.getMembershipId() != null) {
                Member member = memberRepository.findByPersonalInfo_MembershipId(filters.getMembershipId())
                        .orElseThrow(() -> new EntityNotFoundException("Member", "Member with membership= " + filters.getMembershipId() + " not found"));
            }

            var filtered = memberRepository.findAll(MemberSpecification.build(filters), pageable);

            log.debug("Filtered and paginated members were returned successfully with page={} and size{}",
                    pageable.getPageNumber(), pageable.getPageSize());

            return filtered.map(mapper::mapToMemberReadOnlyDTO);
        } catch (EntityNotFoundException e) {
            log.error("Filtered and paginated members were not found");
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MEMBERS')")
    @Transactional(readOnly = true)
    public Page<MemberReadOnlyDTO> getPaginatedMembers(Pageable pageable) {
        Page<Member> memberPage = memberRepository.findAll(pageable);
        log.debug("All paginated members were returned successfully");
        return memberPage.map(mapper::mapToMemberReadOnlyDTO);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MEMBERS')")
    @Transactional(readOnly = true)
    public Page<MemberReadOnlyDTO> getPaginatedMembersDeletedFalse(Pageable pageable) {
        Page<Member> memberPage = memberRepository.findAllByDeletedFalse(pageable);
        log.debug("Paginated members were returned successfully");
        return memberPage.map(mapper::mapToMemberReadOnlyDTO);
    }

    @Override
    public boolean isMemberExists(String vat) {
        return memberRepository.findByVat(vat).isPresent();
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private Page<MemberReadOnlyDTO> singleResultPage(Member member, Pageable pageable) {
        return new PageImpl<>(
                List.of(mapper.mapToMemberReadOnlyDTO(member)),
                pageable,
                1
        );
    }
}
