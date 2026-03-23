package club.sportsapp.service;

import club.sportsapp.core.exceptions.EntityAlreadyExistsException;
import club.sportsapp.core.exceptions.EntityInvalidArgumentException;
import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.core.exceptions.FileUploadException;
import club.sportsapp.dto.MemberInsertDTO;
import club.sportsapp.dto.MemberReadOnlyDTO;
import club.sportsapp.mapper.Mapper;
import club.sportsapp.model.*;
import club.sportsapp.model.static_data.Sport;
import club.sportsapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public MemberReadOnlyDTO updateMember(MemberInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException {
        return null;
    }

    @Override
    public MemberReadOnlyDTO deleteMemberByUUID(UUID uuid) throws EntityNotFoundException {
        return null;
    }

    @Override
    public MemberReadOnlyDTO getMemberByUUID(UUID uuid) throws EntityNotFoundException {
        return null;
    }

    @Override
    public MemberReadOnlyDTO getMemberByUUIDAndDeletedFalse(UUID uuid) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Page<MemberReadOnlyDTO> getPaginatedMembers(Pageable pageable) {
        return null;
    }

    @Override
    public Page<MemberReadOnlyDTO> getPaginatedMembersDeletedFalse(Pageable pageable) {
        return null;
    }

    @Override
    public boolean isMemberExists(String vat) {
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }
}
