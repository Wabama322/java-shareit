package item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = ShareItServer.class)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("userName1")
                .email("test@mail.ru")
                .build();
        userRepository.save(user);
        itemRepository.save(Item.builder()
                .name("item1")
                .description("item 1")
                .available(true)
                .owner(user)
                .build());
        itemRepository.save(Item.builder()
                .name("Doll")
                .description("item 2")
                .available(true)
                .owner(user)
                .build());
    }

    @Test
    void findAllByOwnerOrderByIdTest() {
        List<Item> itemList = itemRepository
                .findAllByOwnerIdOrderById(user.getId(), PageRequest.of(0, 2)).getContent();

        assertNotNull(itemList);
        assertEquals(2, itemList.size());
    }

    @Test
    void searchItemsByTextTest() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByNameContainingIgnoreCaseAndAvailableTrue("Doll", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Doll", result.getContent().get(0).getName());
    }

    @Test
    void searchItemsByTextTest_Combined() {
        Pageable pageable = PageRequest.of(0, 10);

        itemRepository.save(Item.builder()
                .name("Hammer")
                .description("Professional drilling hammer")
                .available(true)
                .owner(user)
                .build());

        Page<Item> byName = itemRepository.findByNameContainingIgnoreCaseAndAvailableTrue("Hammer", pageable);
        Page<Item> byDesc = itemRepository.findByDescriptionContainingIgnoreCaseAndAvailableTrue("drilling", pageable);

        assertEquals(1, byName.getContent().size(), "Должна найтись 1 вещь по названию");
        assertEquals(1, byDesc.getContent().size(), "Должна найтись 1 вещь по описанию");
    }

    @Test
    public void getAllItemsWithBlankTextShouldReturnEmptyListTest() {
        String text = "text";
        Pageable page = PageRequest.of(0, 10);

        Page<Item> actualResult = itemRepository.findByDescriptionContainingIgnoreCaseAndAvailableTrue(text, page);

        assertEquals(List.of(), actualResult.getContent());
    }

    @Test
    public void findByRequestIdInTest() {
        List<Item> actualResult = itemRepository.findByRequestIdIn(List.of(user.getId()));

        assertNotNull(actualResult);
        assertEquals(0, actualResult.size());
    }

    @Test
    public void findByRequestIdTest() {
        itemRepository.save(Item.builder()
                .name("Banana")
                .description("banana")
                .available(true)
                .owner(user)
                .build());
        List<Item> actualResult = itemRepository.findByRequestId(user.getId());

        assertNotNull(actualResult);
        assertEquals(0, actualResult.size());
    }
}