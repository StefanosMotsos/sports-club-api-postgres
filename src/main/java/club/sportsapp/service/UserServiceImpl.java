package club.sportsapp.service;

import club.sportsapp.core.exceptions.EntityAlreadyExistsException;
import club.sportsapp.core.exceptions.EntityInvalidArgumentException;
import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.dto.UserInsertDTO;
import club.sportsapp.dto.UserReadOnlyDTO;
import club.sportsapp.mapper.Mapper;
import club.sportsapp.model.Role;
import club.sportsapp.model.User;
import club.sportsapp.repository.RoleRepository;
import club.sportsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public UserReadOnlyDTO saveUser(UserInsertDTO insertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        try {
            if (userRepository.findByUsername(insertDTO.username()).isPresent()) {
                throw new EntityAlreadyExistsException("User", "User with username: " + insertDTO.username() + " already exists");
            }

            User user = mapper.mapToUserEntity(insertDTO);
            user.setPassword(passwordEncoder.encode(insertDTO.password()));

            Role role = roleRepository.findById(insertDTO.roleId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Role with id: " + insertDTO.roleId() + " does not exist"));
            role.addUser(user);

            userRepository.save(user);
            log.info("User with username={} saved successfully", insertDTO.username());
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityAlreadyExistsException e) {
            log.error("Save failed. User with username={} already exists", insertDTO.username());
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed. Role with id={} does not exist", insertDTO.roleId());
            throw e;
        }

    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUuid(UUID uuid) throws EntityNotFoundException {

        try {

            User user = userRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid: " + uuid + " not found"));

            log.info("User with uuid={} found successfully", uuid);
            return mapper.mapToUserReadOnlyDTO(user);

        } catch (EntityNotFoundException e) {
            log.error("Get failed. User with uuid={} not found", uuid);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUuidDeletedFalse(UUID uuid) throws EntityNotFoundException {

        try {

            User user = userRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid: " + uuid + " not found"));

            log.info("Non-deleted User with uuid={} found successfully", uuid);
            return mapper.mapToUserReadOnlyDTO(user);

        } catch (EntityNotFoundException e) {
            log.error("Get failed. Non-deleted User with uuid={} not found", uuid);
            throw e;
        }
    }
}
