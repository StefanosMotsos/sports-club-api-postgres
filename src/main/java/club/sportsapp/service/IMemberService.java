package club.sportsapp.service;

import club.sportsapp.core.exceptions.EntityAlreadyExistsException;
import club.sportsapp.core.exceptions.EntityInvalidArgumentException;
import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.core.exceptions.FileUploadException;
import club.sportsapp.dto.MemberInsertDTO;
import club.sportsapp.dto.MemberReadOnlyDTO;
import club.sportsapp.dto.MemberUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface IMemberService {

    MemberReadOnlyDTO saveMember(MemberInsertDTO memberInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    void saveMembershipIdFile(UUID uuid, MultipartFile memberFile)
            throws FileUploadException, EntityNotFoundException;

    MemberReadOnlyDTO updateMember(MemberUpdateDTO memberUpdateDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException;

    MemberReadOnlyDTO deleteMemberByUUID(UUID uuid) throws EntityNotFoundException;

    MemberReadOnlyDTO getMemberByUUID(UUID uuid) throws EntityNotFoundException;
    public MemberReadOnlyDTO getMemberByUUIDAndDeletedFalse(UUID uuid) throws EntityNotFoundException;

    Page<MemberReadOnlyDTO> getPaginatedMembers(Pageable pageable);
    Page<MemberReadOnlyDTO> getPaginatedMembersDeletedFalse(Pageable pageable);
    //Page<MemberReadOnlyDTO> getPaginatedMembersFiltered(Pageable pageable, MemberFilters filters);

    boolean isMemberExists(String vat);

}
