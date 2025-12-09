package ktb.week4.community.user;

import ktb.week4.community.domain.user.dto.*;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.domain.user.loader.UserLoader;
import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.domain.user.service.UserCommandService;
import ktb.week4.community.domain.user.service.UserQueryService;
import ktb.week4.community.domain.user.validator.UserValidator;
import ktb.week4.community.global.file.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	UserRepository userRepository;
	
	@Mock
	UserValidator userValidator;
	
	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	UserLoader userLoader;
	
	@Mock
	MultipartFile multipartFile;
	
	@Mock
	FileStorageService fileStorageService;
	
	@InjectMocks
	UserCommandService userCommandService;
	
	@InjectMocks
	UserQueryService userQueryService;

	User user;

	@BeforeEach
	void setUp() {
		user = spy(UserTestBuilder.aUser().build());
	}

	@Test
	@DisplayName("정상적인 SignUpRequest를 받으면 회원가입 한 유저 저장")
	void givenValidSignUpRequest_whenCreateUser_thenSavesUser() {
		
		// Arrange
		SignUpRequestDto validRequest = new SignUpRequestDto(
				"valid@test.com",
				"Aa12345!!",
				"테스트 유저",
				null);
		
		when(passwordEncoder.encode(validRequest.password())).thenReturn(validRequest.password());
		User user = spy(UserTestBuilder.aUser()
				.build());
		doReturn(1L).when(user).getId();
		when(userRepository.save(any(User.class))).thenReturn(user);
		
		// Act
		Long userId = userCommandService.createUser(validRequest).user_id();
		
		// Assert/Verify
		assertEquals(user.getId(), userId);
	}

	@Test
	@DisplayName("사용자 ID로 조회 시 UserResponseDto를 반환한다.")
	void givenUserId_whenGetUser_thenReturnsDto() {
		// given
		when(userLoader.getUserById(1L)).thenReturn(user);
		
		// when
		UserResponseDto response = userQueryService.getUser(1L);
		
		// then
		assertThat(response).isNotNull();
		assertEquals(user.getId(), response.userId());
		assertEquals(user.getNickname(), response.nickname());
	}
	
	@Test
	@DisplayName("사용자 삭제 시 상태와 deletedAt이 설정되고 저장된다.")
	void givenUser_whenDeleteUser_thenMarksDeleted() {
		// given
		when(userLoader.getUserById(1L)).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);
		
		// when
		userCommandService.deleteUser(1L);
		
		// then
		assertThat(user.getDeletedAt()).isNotNull();
		verify(userRepository).save(user);
	}
	
	@Test
	@DisplayName("비밀번호 변경 시 인코딩 후 저장한다.")
	void givenPasswordUpdateRequest_whenUpdatePassword_thenEncodesAndSaves() {
		// given
		UpdatePasswordRequestDto request = new UpdatePasswordRequestDto("NewP@ssw0rd");
		when(userLoader.getUserById(1L)).thenReturn(user);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded");
		when(userRepository.save(user)).thenReturn(user);
		
		// when
		userCommandService.updatePassword(1L, request);
		
		// then
		verify(passwordEncoder).encode(request.password());
		verify(userRepository).save(user);
		assertEquals("encoded", user.getPassword());
	}
	
	@Test
	@DisplayName("사용자 정보 변경 시 닉네임/프로필 이미지가 갱신된다.")
	void givenUpdateRequest_whenUpdateUser_thenUpdatesFields() {
		// given
		UpdateUserRequestDto request = new UpdateUserRequestDto("newNick", "newImage.png");
		
		when(userLoader.getUserById(1L)).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);
		
		// when
		UserResponseDto response = userCommandService.updateUser(1L, request, null);
		
		// then
		assertThat(response).isNotNull();
		assertEquals("newNick", user.getNickname());
		assertEquals("newImage.png", user.getProfileImage());
		verify(userRepository).save(user);
	}
	
	@Test
	@DisplayName("프로필 이미지 파일이 들어오면 기존 이미지 삭제 후 새 경로로 변경한다.")
	void givenProfileImageFile_whenUpdateUser_thenDeletesOldAndStoresNew() {
		// given
		String originalProfile = user.getProfileImage();
		UpdateUserRequestDto request = new UpdateUserRequestDto(user.getNickname(), null);
		
		when(userLoader.getUserById(1L)).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);
		when(multipartFile.isEmpty()).thenReturn(false);
		when(fileStorageService.store(multipartFile, "profiles")).thenReturn("/uploads/profiles/new.png");
		
		// when
		userCommandService.updateUser(1L, request, multipartFile);
		
		// then
		verify(fileStorageService).delete(originalProfile);
		verify(fileStorageService).store(multipartFile, "profiles");
		assertEquals("/uploads/profiles/new.png", user.getProfileImage());
	}
	
	@Test
	@DisplayName("프로필 이미지를 빈 문자열로 보내면 기존 이미지 삭제 후 기본 이미지로 변경한다.")
	void givenBlankProfileImage_whenUpdateUser_thenResetsToDefault() {
		// given
		String originalProfile = user.getProfileImage();
		UpdateUserRequestDto request = new UpdateUserRequestDto(user.getNickname(), " ");
		
		when(userLoader.getUserById(1L)).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);
		
		// when
		userCommandService.updateUser(1L, request, null);
		
		// then
		verify(fileStorageService).delete(originalProfile);
		assertEquals("", user.getProfileImage());
	}
}
