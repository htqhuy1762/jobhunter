package vn.hoidanit.jobhunter.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.service.CompanyService;

/**
 * Controller Test cho CompanyController
 *
 * Đây là CONTROLLER TEST - test các API endpoints
 *
 * Annotations:
 * - @SpringBootTest: Load toàn bộ Spring context
 * - @AutoConfigureMockMvc: Tự động config MockMvc để test controller
 * - @MockBean: Tạo mock bean và thay thế bean thật trong context
 * - @WithMockUser: Giả lập user đã authenticate (vì có Security)
 * - @ActiveProfiles: Chọn profile test
 *
 * MockMvc: Tool để gửi HTTP request và verify response
 * ObjectMapper: Chuyển đổi object <-> JSON
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Company Controller Tests")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        testCompany.setDescription("Test Description");
        testCompany.setAddress("123 Test Street");
        testCompany.setLogo("test-logo.png");
        testCompany.setCreatedAt(Instant.now());
        testCompany.setCreatedBy("admin");
    }

    /**
     * Test case 1: Test GET /api/v1/companies/{id} - Thành công
     *
     * MockMvc workflow:
     * 1. perform() - Gửi HTTP request
     * 2. andExpect() - Verify response
     * 3. andDo() - In ra kết quả (optional)
     */
    @Test
    @DisplayName("GET /api/v1/companies/{id} - Nên trả về company khi ID tồn tại")
    @WithMockUser(username = "user@test.com") // Giả lập user đã login
    void testGetCompanyById_WhenIdExists_ShouldReturnCompany() throws Exception {
        // Arrange: Mock service trả về company
        when(companyService.findById(1L)).thenReturn(Optional.of(testCompany));

        // Act & Assert: Gửi GET request và verify response
        mockMvc.perform(get("/api/v1/companies/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Verify HTTP 200
                .andExpect(jsonPath("$.id").value(1L)) // Verify JSON response
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.address").value("123 Test Street"));

        // Verify service method được gọi
        verify(companyService, times(1)).findById(1L);
    }

    /**
     * Test case 2: Test POST /api/v1/companies - Tạo company mới
     */
    @Test
    @DisplayName("POST /api/v1/companies - Nên tạo company mới thành công")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"}) // User có role ADMIN
    void testCreateCompany_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        Company newCompany = new Company();
        newCompany.setName("New Company");
        newCompany.setDescription("New Description");
        newCompany.setAddress("New Address");

        Company savedCompany = new Company();
        savedCompany.setId(2L);
        savedCompany.setName("New Company");
        savedCompany.setDescription("New Description");
        savedCompany.setAddress("New Address");
        savedCompany.setCreatedAt(Instant.now());

        when(companyService.handleCreateCompany(any(Company.class))).thenReturn(savedCompany);

        // Act & Assert: Gửi POST request với JSON body
        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany))) // Convert object -> JSON
                .andExpect(status().isCreated()) // Verify HTTP 201
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("New Company"));

        verify(companyService, times(1)).handleCreateCompany(any(Company.class));
    }

    /**
     * Test case 3: Test POST /api/v1/companies - Validation error
     */
    @Test
    @DisplayName("POST /api/v1/companies - Nên trả về 400 khi name bị thiếu")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void testCreateCompany_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange: Company không có name (vi phạm @NotBlank)
        Company invalidCompany = new Company();
        invalidCompany.setDescription("Description only");

        // Act & Assert: Expect validation error
        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCompany)))
                .andExpect(status().isBadRequest()); // HTTP 400

        // Service không được gọi vì validation failed
        verify(companyService, never()).handleCreateCompany(any());
    }

    /**
     * Test case 4: Test PUT /api/v1/companies - Update company
     */
    @Test
    @DisplayName("PUT /api/v1/companies - Nên update company thành công")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void testUpdateCompany_WithValidData_ShouldReturnUpdated() throws Exception {
        // Arrange
        Company updateData = new Company();
        updateData.setId(1L);
        updateData.setName("Updated Name");
        updateData.setDescription("Updated Description");
        updateData.setAddress("Updated Address");

        Company updatedCompany = new Company();
        updatedCompany.setId(1L);
        updatedCompany.setName("Updated Name");
        updatedCompany.setDescription("Updated Description");
        updatedCompany.setAddress("Updated Address");
        updatedCompany.setUpdatedAt(Instant.now());

        when(companyService.handleUpdateCompany(any(Company.class))).thenReturn(updatedCompany);

        // Act & Assert
        mockMvc.perform(put("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(companyService, times(1)).handleUpdateCompany(any(Company.class));
    }

    /**
     * Test case 5: Test DELETE /api/v1/companies/{id}
     */
    @Test
    @DisplayName("DELETE /api/v1/companies/{id} - Nên xóa company thành công")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void testDeleteCompany_WithValidId_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(companyService).handleDeleteCompany(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/companies/{id}", 1L))
                .andExpect(status().isOk());

        verify(companyService, times(1)).handleDeleteCompany(1L);
    }

    /**
     * Test case 6: Test Security - Unauthorized access
     */
    @Test
    @DisplayName("POST /api/v1/companies - Nên trả về 403 khi không có role ADMIN")
    @WithMockUser(username = "user@test.com", roles = {"USER"}) // User thường, không có role ADMIN
    void testCreateCompany_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        Company newCompany = new Company();
        newCompany.setName("New Company");

        // Act & Assert: Expect 403 Forbidden
        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany)))
                .andExpect(status().isForbidden()); // HTTP 403

        // Service không được gọi vì bị chặn bởi Security
        verify(companyService, never()).handleCreateCompany(any());
    }

    /**
     * Test case 7: Test unauthenticated access
     */
    @Test
    @DisplayName("POST /api/v1/companies - Nên trả về 401/403 khi chưa login")
    // Không có @WithMockUser - simulate chưa login
    void testCreateCompany_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        Company newCompany = new Company();
        newCompany.setName("New Company");

        // Act & Assert: Expect 401 hoặc 403
        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany)))
                .andExpect(status().is4xxClientError()); // 401 hoặc 403

        verify(companyService, never()).handleCreateCompany(any());
    }
}

