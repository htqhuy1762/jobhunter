package vn.hoidanit.jobhunter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

/**
 * Unit Test cho CompanyService
 *
 * Đây là UNIT TEST - test từng method riêng lẻ với mock dependencies
 *
 * Annotations:
 * - @ExtendWith(MockitoExtension.class): Tích hợp Mockito với JUnit 5
 * - @Mock: Tạo mock object (giả lập) cho dependencies
 * - @InjectMocks: Tự động inject các mock vào service đang test
 * - @BeforeEach: Chạy trước mỗi test case
 * - @Test: Đánh dấu đây là một test method
 * - @DisplayName: Tên hiển thị dễ đọc cho test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyService Unit Tests")
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company testCompany;

    /**
     * Setup method - chạy trước mỗi test
     * Chuẩn bị dữ liệu test
     */
    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        testCompany.setDescription("Test Description");
        testCompany.setAddress("Test Address");
        testCompany.setLogo("test-logo.png");
        testCompany.setCreatedAt(Instant.now());
    }

    /**
     * Test case 1: Test tìm company theo ID - Trường hợp thành công
     *
     * Pattern AAA (Arrange-Act-Assert):
     * - Arrange: Chuẩn bị dữ liệu và mock behavior
     * - Act: Thực thi method cần test
     * - Assert: Kiểm tra kết quả
     */
    @Test
    @DisplayName("Nên trả về Company khi tìm theo ID hợp lệ")
    void testFindById_WhenIdExists_ShouldReturnCompany() {
        // Arrange: Giả lập repository trả về company
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

        // Act: Gọi method cần test
        Optional<Company> result = companyService.findById(1L);

        // Assert: Kiểm tra kết quả
        assertTrue(result.isPresent(), "Company phải tồn tại");
        assertEquals("Test Company", result.get().getName(), "Tên company phải khớp");
        assertEquals(1L, result.get().getId(), "ID phải khớp");

        // Verify: Kiểm tra method đã được gọi đúng số lần
        verify(companyRepository, times(1)).findById(1L);
    }

    /**
     * Test case 2: Test tìm company theo ID - Trường hợp không tìm thấy
     */
    @Test
    @DisplayName("Nên trả về Optional.empty() khi ID không tồn tại")
    void testFindById_WhenIdNotExists_ShouldReturnEmpty() {
        // Arrange
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Company> result = companyService.findById(999L);

        // Assert
        assertFalse(result.isPresent(), "Không nên tìm thấy company");
        verify(companyRepository, times(1)).findById(999L);
    }

    /**
     * Test case 3: Test tạo company mới - Thành công
     */
    @Test
    @DisplayName("Nên tạo company mới thành công")
    void testHandleCreateCompany_WhenValidData_ShouldCreateSuccessfully() {
        // Arrange
        Company newCompany = new Company();
        newCompany.setName("New Company");
        newCompany.setDescription("New Description");

        Company savedCompany = new Company();
        savedCompany.setId(2L);
        savedCompany.setName("New Company");
        savedCompany.setDescription("New Description");

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // Act
        Company result = companyService.handleCreateCompany(newCompany);

        // Assert
        assertNotNull(result, "Result không được null");
        assertEquals(2L, result.getId(), "ID phải được gán");
        assertEquals("New Company", result.getName(), "Tên phải khớp");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    /**
     * Test case 4: Test update company - Thành công
     */
    @Test
    @DisplayName("Nên update company thành công khi ID tồn tại")
    void testHandleUpdateCompany_WhenIdExists_ShouldUpdateSuccessfully() {
        // Arrange
        Company updateData = new Company();
        updateData.setId(1L);
        updateData.setName("Updated Name");
        updateData.setDescription("Updated Description");
        updateData.setAddress("Updated Address");
        updateData.setLogo("updated-logo.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        // Act
        Company result = companyService.handleUpdateCompany(updateData);

        // Assert
        assertNotNull(result, "Result không được null");
        assertEquals("Updated Name", testCompany.getName(), "Tên phải được cập nhật");
        assertEquals("Updated Description", testCompany.getDescription(), "Description phải được cập nhật");
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).save(testCompany);
    }

    /**
     * Test case 5: Test update company - ID không tồn tại
     */
    @Test
    @DisplayName("Nên trả về null khi update company với ID không tồn tại")
    void testHandleUpdateCompany_WhenIdNotExists_ShouldReturnNull() {
        // Arrange
        Company updateData = new Company();
        updateData.setId(999L);
        updateData.setName("Updated Name");

        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Company result = companyService.handleUpdateCompany(updateData);

        // Assert
        assertNull(result, "Result phải null khi ID không tồn tại");
        verify(companyRepository, times(1)).findById(999L);
        verify(companyRepository, never()).save(any(Company.class));
    }

    /**
     * Test case 6: Test delete company - Thành công
     */
    @Test
    @DisplayName("Nên xóa company và users liên quan thành công")
    void testHandleDeleteCompany_WhenIdExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(userRepository.findByCompany(testCompany)).thenReturn(java.util.Collections.emptyList());

        // Act
        companyService.handleDeleteCompany(1L);

        // Assert
        verify(companyRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByCompany(testCompany);
        verify(userRepository, times(1)).deleteAll(anyList());
        verify(companyRepository, times(1)).deleteById(1L);
    }
}

