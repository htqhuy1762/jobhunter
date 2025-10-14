package vn.hoidanit.jobhunter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.util.constant.GenderEnum;

/**
 * Unit Test cho UserService
 * Ví dụ test service phức tạp hơn với nhiều dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Company testCompany;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Setup test company
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");

        // Setup test role
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setAge(25);
        testUser.setGender(GenderEnum.MALE);
        testUser.setAddress("123 Test Street");
        testUser.setCompany(testCompany);
        testUser.setRole(testRole);
    }

    /**
     * Test case 1: Test tạo user mới với company và role hợp lệ
     */
    @Test
    @DisplayName("Nên tạo user thành công khi company và role hợp lệ")
    void testHandleCreateUser_WithValidCompanyAndRole_ShouldCreateSuccessfully() {
        // Arrange
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword("raw_password");

        Company userCompany = new Company();
        userCompany.setId(1L);
        newUser.setCompany(userCompany);

        Role userRole = new Role();
        userRole.setId(1L);
        newUser.setRole(userRole);

        // Mock behaviors
        when(companyService.findById(1L)).thenReturn(Optional.of(testCompany));
        when(roleService.fetchById(1L)).thenReturn(testRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.handleCreateUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(testCompany, newUser.getCompany());
        assertEquals(testRole, newUser.getRole());
        verify(companyService, times(1)).findById(1L);
        verify(roleService, times(1)).fetchById(1L);
        verify(userRepository, times(1)).save(newUser);
    }

    /**
     * Test case 2: Test tạo user khi company không tồn tại
     */
    @Test
    @DisplayName("Nên set company = null khi company không tồn tại")
    void testHandleCreateUser_WithInvalidCompany_ShouldSetCompanyNull() {
        // Arrange
        User newUser = new User();
        newUser.setName("New User");

        Company userCompany = new Company();
        userCompany.setId(999L); // Invalid company ID
        newUser.setCompany(userCompany);

        when(companyService.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.handleCreateUser(newUser);

        // Assert
        assertNull(newUser.getCompany(), "Company phải là null khi không tồn tại");
        verify(companyService, times(1)).findById(999L);
        verify(userRepository, times(1)).save(newUser);
    }

    /**
     * Test case 3: Test tạo user khi role không tồn tại
     */
    @Test
    @DisplayName("Nên set role = null khi role không tồn tại")
    void testHandleCreateUser_WithInvalidRole_ShouldSetRoleNull() {
        // Arrange
        User newUser = new User();
        newUser.setName("New User");

        Role userRole = new Role();
        userRole.setId(999L); // Invalid role ID
        newUser.setRole(userRole);

        when(roleService.fetchById(999L)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        userService.handleCreateUser(newUser);

        // Assert
        assertNull(newUser.getRole(), "Role phải là null khi không tồn tại");
        verify(roleService, times(1)).fetchById(999L);
        verify(userRepository, times(1)).save(newUser);
    }

    /**
     * Test case 4: Test get user by ID - Tìm thấy
     */
    @Test
    @DisplayName("Nên trả về user khi ID tồn tại")
    void testHandleGetUserById_WhenIdExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.handleGetUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    /**
     * Test case 5: Test get user by ID - Không tìm thấy
     */
    @Test
    @DisplayName("Nên trả về null khi ID không tồn tại")
    void testHandleGetUserById_WhenIdNotExists_ShouldReturnNull() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        User result = userService.handleGetUserById(999L);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findById(999L);
    }

    /**
     * Test case 6: Test delete user
     */
    @Test
    @DisplayName("Nên xóa user thành công")
    void testHandleDeleteUser_ShouldCallRepositoryDelete() {
        // Arrange
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.handleDeleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    /**
     * Test case 7: Test update user - Thành công
     */
    @Test
    @DisplayName("Nên update user thành công khi ID tồn tại")
    void testHandleUpdateUser_WhenIdExists_ShouldUpdateSuccessfully() {
        // Arrange
        User updateData = new User();
        updateData.setId(1L);
        updateData.setName("Updated Name");
        updateData.setAddress("Updated Address");
        updateData.setAge(30);
        updateData.setGender(GenderEnum.FEMALE);

        Company newCompany = new Company();
        newCompany.setId(2L);
        updateData.setCompany(newCompany);

        Role newRole = new Role();
        newRole.setId(2L);
        updateData.setRole(newRole);

        // Mock existing user
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(companyService.findById(2L)).thenReturn(Optional.of(newCompany));
        when(roleService.fetchById(2L)).thenReturn(newRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.handleUpdateUser(updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", testUser.getName());
        assertEquals("Updated Address", testUser.getAddress());
        assertEquals(30, testUser.getAge());
        assertEquals(GenderEnum.FEMALE, testUser.getGender());
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Test case 8: Test update user - ID không tồn tại
     */
    @Test
    @DisplayName("Nên trả về null khi update user với ID không tồn tại")
    void testHandleUpdateUser_WhenIdNotExists_ShouldReturnNull() {
        // Arrange
        User updateData = new User();
        updateData.setId(999L);
        updateData.setName("Updated Name");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        User result = userService.handleUpdateUser(updateData);

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }
}
