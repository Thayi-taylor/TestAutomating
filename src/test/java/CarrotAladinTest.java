import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CarrotAladinTest {
    static Playwright playwright;
    static Browser browser;
    Page page;

    @BeforeAll
    static void setUpAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setUp() {
        page = browser.newPage();
        page.navigate("https://carrotaladin.shop");
    }

    @AfterEach
    void tearDown() {
        page.close();
    }

    @AfterAll
    static void tearDownAll() {
        browser.close();
        playwright.close();
    }

    //  공통 메서드
    void login(String username, String password) {
        page.navigate("https://carrotaladin.shop/02_loginForm.jsp");
        page.fill("input[placeholder='아이디를 입력하세요']", username);
        page.fill("input[placeholder='비밀번호를 입력하세요']", password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();
    }


    @Nested
    @DisplayName("1. 메인 페이지 테스트")
    class HomePageTests {
        @Test
        @DisplayName("1.1 메인 페이지 로딩 확인")
        void testHomePageLoad() {
            assertEquals("중고서점 : 당근알라딘", page.title(), "페이지 제목이 일치하지 않음");
        }

        @Test
        @DisplayName("1.2 배너 이미지 확인")
        void testBannerImage() {
            assertTrue(page.locator("img[alt='배너이미지']").isVisible(), "배너 이미지가 보이지 않음");
        }

        @Test
        @DisplayName("1.3 검색 기능 확인")
        void testSearchFunctionality() {
            Locator searchBox = page.locator("input[placeholder='검색어를 입력하세요.']");
            searchBox.fill("데미안");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("검색")).click();
            assertTrue(page.url().contains("search"), "검색 결과 페이지로 이동하지 않음");
        }
    }


    @Nested
    @DisplayName("2. 도서 목록 테스트")
    class BookListTests {
        @Test
        @DisplayName("2.1 도서 목록이 표시되는지 확인")
        void testBookListDisplay() {
            assertTrue(page.locator(".card").count() > 0, "도서 목록이 보이지 않음");
        }

        @Test
        @DisplayName("2.2 도서 카드 클릭 테스트")
        void testBookCardClick() {
            page.locator(".card").nth(0).click();
            page.waitForURL("**/05_itemInfo.jsp?bookNumber=*");
            assertTrue(page.url().contains("05_itemInfo.jsp"), "도서 상세 페이지로 이동하지 않음");
        }
    }


    @Nested
    @DisplayName("3. 도서 상세 페이지 테스트")
    class BookDetailTests {
        @BeforeEach
        void navigateToBookDetail() {
            page.locator(".card").nth(0).click();
            page.waitForURL("**/05_itemInfo.jsp?bookNumber=*");
        }

        @Test
        @DisplayName("3.1 도서 이미지, 제목, 가격 확인")
        void testBookDetails() {
            assertTrue(page.locator("img[alt='Book Image']").isVisible(), "도서 이미지가 보이지 않음");
            assertTrue(page.locator("p.text-end").isVisible(), "도서 가격이 보이지 않음");
            assertTrue(page.locator(".card-text").first().isVisible(), "도서 제목이 보이지 않음");
        }

        @Test
        @DisplayName("3.2 좋아요 버튼 가시성 확인")
        void testLikeButtonVisible() {
            login("5555", "5555");
            page.navigate("https://carrotaladin.shop/05_itemInfo.jsp?bookNumber=1");

            Locator likeButton = page.locator("#like-button");
            assertTrue(likeButton.isVisible(), "좋아요 버튼이 보이지 않음");
        }
    }

    @Nested
    @DisplayName("4. 사용자 인증 테스트")
    class UserAuthTests {
        @Test
        @DisplayName("4.1 로그인 및 로그아웃 기능 확인")
        void testLogin() {
            login("5555", "5555");
            assertTrue(page.url().contains("01_main.jsp"), "메인 페이지로 이동하지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("5. 찜목록 및 문의 테스트")
    class CartAndPurchaseTests {
        @Test
        @DisplayName("5.1 도서 찜목록에 담기")
        void testAddToLikes() {
            login("5555", "5555");
            page.navigate("https://carrotaladin.shop/05_itemInfo.jsp?bookNumber=1");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Like")).click();
        }
    }

    @Nested
    @DisplayName("6. 도서 판매 등록 테스트")
    class SellBookTests {

        @Test
        @DisplayName("6.1 도서 판매 폼 표시 확인")
        void testSellBookFormVisible() {
            // 1. 로그인 절차
            login("5555", "5555");

            // 2. 내 책 팔기 페이지로 이동
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("내 책 팔기")).click();

            // 3. 필수 요소 확인
            assertTrue(page.locator("input[name='title']").isVisible(), "제목 입력란이 보이지 않음");
            assertTrue(page.locator("input[name='price']").isVisible(), "가격 입력란이 보이지 않음");
            assertTrue(page.locator("input[name='image']").isVisible(), "이미지 업로드란이 보이지 않음");
            assertTrue(page.locator("select[name='category']").isVisible(), "카테고리 선택란이 보이지 않음");
            assertTrue(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("제출")).isVisible(), "제출 버튼이 보이지 않음");
        }

        @Test
        @DisplayName("6.2 도서 판매 등록 및 리디렉션 확인")
        void testSellBookSubmission() {
            // 1. 로그인 절차
            login("5555", "5555");

            // 2. 내 책 팔기 페이지로 이동
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("내 책 팔기")).click();

            // 3. 폼에 데이터 입력
            page.fill("input[name='title']", "테스트 도서");
            page.fill("input[name='price']", "15000");
            page.fill("input[name='memo']", "이 도서는 테스트용입니다.");
            page.selectOption("select[name='category']", "10");

            // 4. 이미지 업로드 (로컬 이미지 파일 경로 입력)
            // page.setInputFiles("input[name='image']", Paths.get("src/test/resources/test-image.jpg"));

            // 5. Alert 처리 및 폼 제출
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("제출")).click();

            page.onceDialog(dialog -> {
                assertEquals("도서를 등록했습니다!", dialog.message(), "Alert 메시지가 일치하지 않습니다.");
                dialog.accept(); // Alert 창 확인 클릭
            });

            // 6. 리디렉션 확인
            assertTrue(page.url().contains("05_itemInfo.jsp"), "리디렉션이 05_itemInfo.jsp로 이루어지지 않았습니다.");
        }
    }
}
