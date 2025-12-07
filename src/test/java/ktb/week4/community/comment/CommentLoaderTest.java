package ktb.week4.community.comment;

import ktb.week4.community.domain.comment.entity.Comment;
import ktb.week4.community.domain.comment.entity.CommentTestBuilder;
import ktb.week4.community.domain.comment.loader.CommentLoader;
import ktb.week4.community.domain.comment.repository.CommentRepository;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentLoaderTest {

	@Mock
	CommentRepository commentRepository;

	@InjectMocks
	CommentLoader commentLoader;

	@Test
	@DisplayName("존재하지 않는 댓글을 조회하면 COMMENT_NOT_FOUND 예외가 발생한다.")
	void givenNotExistingComment_whenGetCommentById_thenThrowsCommentNotFound() {
		// given
		when(commentRepository.findById(999L)).thenReturn(Optional.empty());

		// when
		GeneralException exc = assertThrows(
				GeneralException.class,
				() -> commentLoader.getCommentById(999L)
		);

		// then
		assertEquals(ErrorCode.COMMENT_NOT_FOUND, exc.getErrorCode());
	}

	@Test
	@DisplayName("댓글 ID로 조회에 성공하면 Comment를 반환한다.")
	void givenExistingComment_whenGetCommentById_thenReturnsComment() {
		// given
		Comment expected = CommentTestBuilder.aComment().build();
		when(commentRepository.findById(1L)).thenReturn(Optional.of(expected));

		// when
		Comment result = commentLoader.getCommentById(1L);

		// then
		assertEquals(expected, result);
	}
}
