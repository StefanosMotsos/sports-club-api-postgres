package club.sportsapp.api;

import club.sportsapp.authentication.JwtService;
import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.dto.MemberInsertDTO;
import club.sportsapp.dto.MemberReadOnlyDTO;
import club.sportsapp.dto.PersonalInfoInsertDTO;
import club.sportsapp.dto.UserInsertDTO;
import club.sportsapp.service.IMemberService;
import club.sportsapp.service.MemberServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MemberRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean
    private MemberServiceImpl memberService;

    @Test
    void getMemberByUuid_shouldReturnOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        MemberReadOnlyDTO dto = new MemberReadOnlyDTO(
                uuid.toString(), "Nikos", "Papadopoulos", "123456789",
                "FOOTBALL", "BASIC", "ACTIVE", "AB123456", "MEM-2024-001", "nikospap"
        );

        when(memberService.getMemberByUUIDAndDeletedFalse(uuid)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/members/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Nikos"));
    }

    @Test
    void getMembersByUuid_shouldReturnNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(memberService.getMemberByUUIDAndDeletedFalse(uuid))
                .thenThrow(new EntityNotFoundException("Member", "Member not found"));

        mockMvc.perform(get("/api/v1/members/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveMember_shouldReturnCreated() throws Exception {
        UserInsertDTO userInsertDTO = new UserInsertDTO(
                "stefuser", "Cod1ngF@", 3L);
        PersonalInfoInsertDTO personalInfoInsertDTO = PersonalInfoInsertDTO.builder()
                .membershipId("22222222222").identityNumber("DB123457")
                .placeOfBirth("Athens").branchOfRegistration("Kypseli")
                .build();

        MemberInsertDTO memberInsertDTO = new MemberInsertDTO(
                "Stefanos", "Motsos", "222222222", 1L, 1L,
                userInsertDTO, personalInfoInsertDTO
        );

        MemberReadOnlyDTO dto = new MemberReadOnlyDTO(
                "uuid-123",
                "Stefanos",
                "Motsos",
                "222222222",
                "FOOTBALL",
                "BASIC",
                "ACTIVE",
                "AB123456",
                "MEM-2024-001",
                "stef"
        );

        when(memberService.isMemberExists(any())).thenReturn(false);
        when(memberService.saveMember(any(MemberInsertDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberInsertDTO))) // ✅ fixed
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname").value("Stefanos"))
                .andExpect(jsonPath("$.uuid").value("uuid-123"));
    }

}
