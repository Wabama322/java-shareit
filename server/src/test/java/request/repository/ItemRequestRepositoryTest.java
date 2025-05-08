package request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = ShareItServer.class)
public class ItemRequestRepositoryTest {
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ItemRequestRepository itemRequestRepository;
    @Autowired
    UserRepository userRepository;
    User user;
    User user1;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("userName1")
                .email("test@mail.ru")
                .build();
        user1 = User.builder()
                .name("userName2")
                .email("test2@mail.ru")
                .build();
        userRepository.save(user);
        userRepository.save(user1);
        itemRepository.save(Item.builder()
                .name("item1")
                .description("item 1")
                .available(true)
                .owner(user)
                .build());
        itemRepository.save(Item.builder()
                .name("Doll")
                .description("Barbie item")
                .available(true)
                .owner(user)
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .description("Barbie item")
                .requester(user1)
                .created(LocalDateTime.now())
                .build());
    }

    @Test
    void findByNotRequesterIdTest() {
        List<ItemRequest> itemRequests = itemRequestRepository
                .findByNotRequesterId(user.getId(), PageRequest.of(0, 2)).getContent();

        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());
    }

    @Test
    void findAllTest() {
        List<ItemRequest> itemRequests = itemRequestRepository
                .findAll(PageRequest.of(0, 2)).getContent();

        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());
    }

    @Test
    public void findItemRequestsByUserIdTest() {
        List<ItemRequest> itemRequests = itemRequestRepository
                .findItemRequestsByUserId(user1.getId());

        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());
    }
}
