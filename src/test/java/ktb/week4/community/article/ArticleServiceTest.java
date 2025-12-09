package ktb.week4.community.article;

import ktb.week4.community.domain.article.dto.ArticleResponseDto;
import ktb.week4.community.domain.article.dto.CreateArticleRequestDto;
import ktb.week4.community.domain.article.dto.UpdateArticleRequestDto;
import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.article.loader.ArticleLoader;
import ktb.week4.community.domain.article.policy.ArticlePolicy;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.domain.article.service.ArticleCommandService;
import ktb.week4.community.domain.article.service.ArticleQueryService;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.domain.user.loader.UserLoader;
import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import ktb.week4.community.global.file.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {
	
	@Mock
	private ArticleRepository articleRepository;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserLoader userLoader;
	
	@Mock
	private ArticleLoader articleLoader;
	
	@Mock
	private ArticlePolicy articlePolicy;
	
	@Mock
	private FileStorageService fileStorageService;
	
	@Mock
	private MultipartFile multipartFile;
	
	@InjectMocks
	private ArticleCommandService articleCommandService;
	
	@InjectMocks
	private ArticleQueryService  articleQueryService;
	
	User author;
	User user;
	Article article;
	
	@BeforeEach
	public void setUp() {
		author = spy(UserTestBuilder.aUser()
					.withEmail("validAuthor@test.com")
					.withNickname("author")
					.build());
		user = spy(UserTestBuilder.aUser()
				.withEmail("validUser@test.com")
				.withNickname("user")
				.build());
		article = spy(ArticleTestBuilder.anArticle()
				.withUser(author)
				.build());
	}
	
	
	@Test
	@DisplayName("게시글 조회 시 조회 수가 1 증가한다.")
	void givenArticle_whenViewed_thenViewCountIncreases() {
		
		// given
		when(articleLoader.getArticleById(1L)).thenReturn(article);
		
		// when
		int viewCount = article.getViewCount();
		ArticleResponseDto res = articleQueryService.getArticle(1L);
		
		// then
		assertThat(res).isNotNull();
		assertThat(res.title()).isEqualTo(article.getTitle());
		assertThat(res.content()).isEqualTo(article.getContent());
		assertThat(res.viewCount()).isEqualTo(viewCount + 1);
	}
		
	@Test
	@DisplayName("게시글 작성 시 ArticleResponseDto를 반환한다.")
	void givenValidRequest_whenCreateArticle_thenSucceeds() {
		
		// given
		CreateArticleRequestDto request = new CreateArticleRequestDto(
				"게시글 제목입니다.", "게시글 내용입니다.", null
		);
		Article savedArticle = spy(new Article(
				request.title(),
				request.content(),
				request.articleImage(),
				author
		));
		doReturn(1L).when(savedArticle).getId();
		when(userLoader.getUserById(author.getId())).thenReturn(author);
		when(fileStorageService.store(null, "articles")).thenReturn(null);
		when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
		
		// when
		ArticleResponseDto response = articleCommandService.createArticle(author.getId(), request, null);
		
		// then
		assertEquals(savedArticle.getId(), response.articleId());
		assertEquals(request.title(), response.title());
		assertEquals(request.content(), response.content());
		verify(articleRepository).save(any(Article.class));
	}
	
	@Test
	@DisplayName("게시글 작성 시 저장된 파일 경로가 있으면 해당 경로로 이미지가 설정된다.")
	void givenImageUpload_whenCreateArticle_thenUsesStoredImagePath() {
		
		// given
		String storedPath = "/uploads/articles/stored.png";
		CreateArticleRequestDto request = new CreateArticleRequestDto(
				"제목", "내용", null
		);
		
		when(userLoader.getUserById(author.getId())).thenReturn(author);
		when(fileStorageService.store(multipartFile, "articles")).thenReturn(storedPath);
		when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		// when
		ArticleResponseDto response = articleCommandService.createArticle(author.getId(), request, multipartFile);
		
		// then
		assertEquals(storedPath, response.articleImage());
		verify(fileStorageService).store(multipartFile, "articles");
	}
	
	@Test
	@DisplayName("게시글 작성자 본인의 해당 글을 수정할 수 있다.")
	void givenAuthor_whenModifyArticle_thenSucceeds() {
		
		//given
		when(userLoader.getUserById(author.getId())).thenReturn(author);
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		when(articleRepository.save(any(Article.class))).thenReturn(article);
		UpdateArticleRequestDto validRequest = new UpdateArticleRequestDto(
				"게시글 제목입니다.", "게시글 내용입니다.", null
		);
		
		// when
		ArticleResponseDto response = articleCommandService.updateArticle(
				author.getId(), article.getId(), validRequest, null
		);
		
		// then
		assertEquals(response.articleId(), article.getId());
		assertEquals(response.title(), article.getTitle());
		assertEquals(response.content(), article.getContent());
	}
	
	@Test
	@DisplayName("게시글 수정 시 새 이미지 파일이 오면 기존 이미지를 삭제하고 새 경로로 교체한다.")
	void givenNewImage_whenUpdateArticle_thenDeletesOldAndSetsNewImage() {
		
		// given
		String oldImage = article.getArticleImage();
		String storedPath = "/uploads/articles/new.png";
		UpdateArticleRequestDto request = new UpdateArticleRequestDto(null, null, null);
		
		when(userLoader.getUserById(author.getId())).thenReturn(author);
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		when(articleRepository.save(article)).thenReturn(article);
		when(multipartFile.isEmpty()).thenReturn(false);
		when(fileStorageService.store(multipartFile, "articles")).thenReturn(storedPath);
		
		// when
		articleCommandService.updateArticle(author.getId(), article.getId(), request, multipartFile);
		
		// then
		verify(fileStorageService).delete(oldImage);
		verify(fileStorageService).store(multipartFile, "articles");
		assertEquals(storedPath, article.getArticleImage());
	}
	
	@Test
	@DisplayName("게시글 수정 시 이미지 값을 빈 문자열로 보내면 기존 이미지를 삭제하고 null로 설정한다.")
	void givenBlankImage_whenUpdateArticle_thenDeletesAndClearsImage() {
		
		// given
		String oldImage = article.getArticleImage();
		UpdateArticleRequestDto request = new UpdateArticleRequestDto(null, null, " ");
		
		when(userLoader.getUserById(author.getId())).thenReturn(author);
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		when(articleRepository.save(article)).thenReturn(article);
		
		// when
		articleCommandService.updateArticle(author.getId(), article.getId(), request, null);
		
		// then
		verify(fileStorageService).delete(oldImage);
		assertThat(article.getArticleImage()).isNull();
	}
	
	@Test
	@DisplayName("게시글 수정 시 이미지 경로 값이 오면 해당 값으로 변경한다.")
	void givenImagePath_whenUpdateArticle_thenSetsProvidedPath() {
		
		// given
		UpdateArticleRequestDto request = new UpdateArticleRequestDto(null, null, "/uploads/articles/custom.png");
		
		when(userLoader.getUserById(author.getId())).thenReturn(author);
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		when(articleRepository.save(article)).thenReturn(article);
		
		// when
		articleCommandService.updateArticle(author.getId(), article.getId(), request, null);
		
		// then
		assertEquals("/uploads/articles/custom.png", article.getArticleImage());
		verify(fileStorageService, never()).delete(anyString());
		verify(fileStorageService, never()).store(any(), anyString());
	}
	
	@Test
	@DisplayName("게시글 작성자 본인이 아닌 경우 해당 글을 수정할 수 없다.")
	void givenAuthor_whenModifyArticle_thenThrowForbidden() {
		
		//given
		when(userLoader.getUserById(user.getId())).thenReturn(user);
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		UpdateArticleRequestDto validRequest = new UpdateArticleRequestDto(
				"게시글 제목입니다.", "게시글 내용입니다.", null
		);
		doThrow(new GeneralException(ErrorCode.FORBIDDEN_REQUEST))
				.when(articlePolicy)
				.checkWrittenBy(any(Article.class), any());
		
		// when
		GeneralException exc = assertThrows(GeneralException.class,
				() -> articleCommandService.updateArticle(user.getId(), article.getId(), validRequest, null));
				
		// then
		assertEquals(ErrorCode.FORBIDDEN_REQUEST, exc.getErrorCode());
	}
	
	@Test
	@DisplayName("게시글 작성자 본인인 경우 해당 글을 삭제할 수 있다.")
	void givenAuthor_whenDeleteArticle_thenSucceeds() {
		
		//given
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		
		// when
		articleCommandService.deleteArticle(author.getId(), article.getId());
		
		// then
		assertThat(article.getDeletedAt()).isBefore(LocalDateTime.now());
	}
	
	@Test
	@DisplayName("게시글 작성자 본인이 아닌 경우 해당 글을 수정할 수 없다.")
	void givenAuthor_whenDeleteArticle_thenThrowForbidden() {
		
		// given
		when(articleLoader.getArticleById(article.getId())).thenReturn(article);
		doThrow(new GeneralException(ErrorCode.FORBIDDEN_REQUEST))
				.when(articlePolicy)
				.checkWrittenBy(any(Article.class), any());
		
		// when
		GeneralException exc = assertThrows(GeneralException.class,
				() -> articleCommandService.deleteArticle(user.getId(), article.getId()));
		
		// then
		assertEquals(ErrorCode.FORBIDDEN_REQUEST, exc.getErrorCode());
		assertThat(article.getDeletedAt()).isNull();
	}
}
