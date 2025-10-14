package vn.hoidanit.jobhunter.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

/**
 * Integration Test cho Repository layer
 *
 * Đây là INTEGRATION TEST - test với database thật (H2 in-memory)
 *
 * Annotations:
 * - @DataJpaTest: Tự động config Spring Data JPA test
 *   + Tạo in-memory database (H2)
 *   + Tự động rollback sau mỗi test
 *   + Chỉ load các @Repository beans
 * - @ActiveProfiles: Chọn profile để test
 * - @Autowired: Inject beans từ Spring context
 *
 * TestEntityManager: Tool để manipulate entities trong test
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Company Repository Integration Tests")
class CompanyRepositoryIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Company testCompany;

    /**
     * Setup - Tạo dữ liệu test trước mỗi test case
     */
    @BeforeEach
    void setUp() {
        // Xóa hết dữ liệu cũ
        companyRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo company mới
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setDescription("Test Description");
        testCompany.setAddress("123 Test Street");
        testCompany.setLogo("test-logo.png");
        testCompany.setCreatedAt(Instant.now());
        testCompany.setCreatedBy("tester");
    }

    /**
     * Test case 1: Test lưu company vào database
     */
    @Test
    @DisplayName("Nên lưu company vào database thành công")
    void testSaveCompany_ShouldPersistToDatabase() {
        // Act: Lưu company
        Company savedCompany = companyRepository.save(testCompany);

        // Assert: Kiểm tra đã lưu thành công
        assertNotNull(savedCompany, "Saved company không được null");
        assertTrue(savedCompany.getId() > 0, "ID phải > 0");

        // Flush để đảm bảo data được ghi xuống DB
        entityManager.flush();

        // Tìm lại từ DB để verify
        Optional<Company> found = companyRepository.findById(savedCompany.getId());
        assertTrue(found.isPresent(), "Company phải tồn tại trong DB");
        assertEquals("Test Company", found.get().getName());
    }

    /**
     * Test case 2: Test tìm company theo ID
     */
    @Test
    @DisplayName("Nên tìm thấy company khi ID tồn tại")
    void testFindById_WhenIdExists_ShouldReturnCompany() {
        // Arrange: Lưu company trước
        Company saved = companyRepository.save(testCompany);
        entityManager.flush();

        // Act: Tìm company
        Optional<Company> result = companyRepository.findById(saved.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Company", result.get().getName());
        assertEquals("123 Test Street", result.get().getAddress());
    }

    /**
     * Test case 3: Test update company
     */
    @Test
    @DisplayName("Nên update company thành công")
    void testUpdateCompany_ShouldUpdateSuccessfully() {
        // Arrange: Lưu company trước
        Company saved = companyRepository.save(testCompany);
        Long companyId = saved.getId();
        entityManager.flush();

        // Act: Update company
        saved.setName("Updated Company Name");
        saved.setAddress("New Address");
        saved.setUpdatedAt(Instant.now());
        companyRepository.save(saved);
        entityManager.flush();

        // Assert: Verify update
        Optional<Company> found = companyRepository.findById(companyId);
        assertTrue(found.isPresent());
        assertEquals("Updated Company Name", found.get().getName());
        assertEquals("New Address", found.get().getAddress());
    }

    /**
     * Test case 4: Test xóa company
     */
    @Test
    @DisplayName("Nên xóa company thành công")
    void testDeleteCompany_ShouldRemoveFromDatabase() {
        // Arrange: Lưu company trước
        Company saved = companyRepository.save(testCompany);
        Long companyId = saved.getId();
        entityManager.flush();

        // Verify company tồn tại
        assertTrue(companyRepository.findById(companyId).isPresent());

        // Act: Xóa company
        companyRepository.deleteById(companyId);
        entityManager.flush();

        // Assert: Verify đã xóa
        assertFalse(companyRepository.findById(companyId).isPresent());
    }

    /**
     * Test case 5: Test tìm tất cả companies
     */
    @Test
    @DisplayName("Nên tìm được tất cả companies")
    void testFindAll_ShouldReturnAllCompanies() {
        // Arrange: Tạo nhiều companies
        Company company1 = new Company();
        company1.setName("Company 1");
        company1.setCreatedAt(Instant.now());

        Company company2 = new Company();
        company2.setName("Company 2");
        company2.setCreatedAt(Instant.now());

        Company company3 = new Company();
        company3.setName("Company 3");
        company3.setCreatedAt(Instant.now());

        companyRepository.save(company1);
        companyRepository.save(company2);
        companyRepository.save(company3);
        entityManager.flush();

        // Act: Tìm tất cả
        List<Company> allCompanies = companyRepository.findAll();

        // Assert
        assertEquals(3, allCompanies.size(), "Phải có 3 companies");
    }

    /**
     * Test case 6: Test count companies
     */
    @Test
    @DisplayName("Nên đếm đúng số lượng companies")
    void testCount_ShouldReturnCorrectCount() {
        // Arrange
        companyRepository.save(testCompany);

        Company another = new Company();
        another.setName("Another Company");
        another.setCreatedAt(Instant.now());
        companyRepository.save(another);

        entityManager.flush();

        // Act & Assert
        assertEquals(2, companyRepository.count());
    }

    /**
     * Test case 7: Test exists by ID
     */
    @Test
    @DisplayName("Nên kiểm tra tồn tại company đúng")
    void testExistsById_ShouldReturnCorrectValue() {
        // Arrange
        Company saved = companyRepository.save(testCompany);
        entityManager.flush();

        // Act & Assert
        assertTrue(companyRepository.existsById(saved.getId()));
        assertFalse(companyRepository.existsById(999L));
    }
}
