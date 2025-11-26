package ktb.week4.community.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	UserRepository userRepository;
	
	String email;
	String password;
	String wrongEmail = "failTest@test.t";
	
	@BeforeEach
	void setUp() {
		email = "test@test.t";
		password = "Aa#12345";
		
		User user = new User(
				"User", passwordEncoder.encode(password), email, ""
		);
		
		userRepository.save(user);
	}
	
	@Test
	@DisplayName("로그인 성공 시 쿠키 반환, 유저 정보 조회에 접속 가능")
	void shouldReturnCookieAndSuccessToUsersIfLoginSuccess() throws Exception {
		
		MvcResult mvcRes = mockMvc.perform(
				post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequestDto(email,  password)
						))
		).andExpect(status().is2xxSuccessful())
				.andExpect(cookie().exists("accessToken"))
				.andExpect(cookie().exists("refreshToken"))
				.andReturn();
		
		Cookie[] cookies = mvcRes.getResponse().getCookies();
		String accessTokenValue = null;
		
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("accessToken")) {
				accessTokenValue = cookie.getValue();
			}
		}
		
		mockMvc.perform(
				get("/users")
						.cookie(new Cookie("accessToken", accessTokenValue))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("로그인 실패 시 쿠키 미반환")
	void shouldNotReturnCookieIfLoginFail() throws Exception {
		
		mockMvc.perform(
						post("/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(
										new LoginRequestDto(wrongEmail,  password)
								))
				).andExpect(status().is4xxClientError())
				.andExpect(cookie().doesNotExist("accessToken"))
				.andExpect(cookie().doesNotExist("refreshToken"))
				.andReturn();
		
		mockMvc.perform(
				get("/users")
		).andExpect(status().is4xxClientError());
	}
}
