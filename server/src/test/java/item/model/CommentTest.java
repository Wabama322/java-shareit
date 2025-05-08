package item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CommentTest {
    Comment comment = Comment.builder()
            .id(1L)
            .text("Cool")
            .item(null)
            .author(null)
            .created(null)
            .build();
    Comment comment2 = Comment.builder()
            .id(1L)
            .text("Cool")
            .item(null)
            .author(null)
            .created(null)
            .build();

    Comment comment3 = Comment.builder()
            .id(1L)
            .text("Cool3")
            .item(null)
            .author(null)
            .created(null)
            .build();

    @Test
    void testCommentHashCode() {
        assertEquals(comment, comment2);
        assertNotEquals(comment, comment3);
    }

}
