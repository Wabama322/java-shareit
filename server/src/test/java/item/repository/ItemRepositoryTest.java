package item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = ShareItServer.class)
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    User user;

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
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        List<Item> itemList =
                itemRepository.searchAvailableItemsByNameOrDescription("it", pageable).getContent();

        assertNotNull(itemList);
        assertEquals(2, itemList.size());
    }

    @Test
    public void getAllItemsWithBlankTextShouldReturnEmptyListTest() {
        String text = "text";
        Pageable page = PageRequest.of(0, 10);

        Page<Item> actualResult = itemRepository.searchAvailableItemsByNameOrDescription(text, page);
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
