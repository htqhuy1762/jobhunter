package vn.hoidanit.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.authservice.domain.Permission;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.repository.PermissionRepository;
import vn.hoidanit.authservice.repository.RoleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private Permission testPermission;

    /**
     * Setup method chạy trước mỗi test case
     * Khởi tạo dữ liệu test chung
     */
    @BeforeEach
    void setUp() {
        // Tạo test data
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("CREATE_USER");
        testPermission.setApiPath("/api/v1/users");
        testPermission.setMethod("POST");
        testPermission.setModule("USER");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");
        testRole.setDescription("Administrator role");
        testRole.setActive(true);
        testRole.setPermissions(Arrays.asList(testPermission));
    }

    /**
     * TEST 1: Test method existByName
     * Kịch bản: Role tồn tại
     * Kỳ vọng: Trả về true
     */
    @Test
    @DisplayName("existByName - Khi role tồn tại - Nên trả về true")
    void existByName_whenRoleExists_shouldReturnTrue() {
        // Arrange: Chuẩn bị dữ liệu
        String roleName = "ADMIN";
        when(roleRepository.existsByName(roleName)).thenReturn(true);

        // Act: Thực hiện hành động
        boolean result = roleService.existByName(roleName);

        // Assert: Kiểm tra kết quả
        assertTrue(result);
        verify(roleRepository, times(1)).existsByName(roleName);
    }

    /**
     * TEST 2: Test method existByName
     * Kịch bản: Role không tồn tại
     * Kỳ vọng: Trả về false
     */
    @Test
    @DisplayName("existByName - Khi role không tồn tại - Nên trả về false")
    void existByName_whenRoleNotExists_shouldReturnFalse() {
        // Arrange
        String roleName = "NON_EXIST";
        when(roleRepository.existsByName(roleName)).thenReturn(false);

        // Act
        boolean result = roleService.existByName(roleName);

        // Assert
        assertFalse(result);
        verify(roleRepository, times(1)).existsByName(roleName);
    }

    /**
     * TEST 3: Test method fetchById
     * Kịch bản: Tìm role với ID hợp lệ
     * Kỳ vọng: Trả về Role object
     */
    @Test
    @DisplayName("fetchById - Khi ID hợp lệ - Nên trả về Role")
    void fetchById_whenValidId_shouldReturnRole() {
        // Arrange
        long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

        // Act
        Role result = roleService.fetchById(roleId);

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        assertEquals("Administrator role", result.getDescription());
        verify(roleRepository, times(1)).findById(roleId);
    }

    /**
     * TEST 4: Test method fetchById
     * Kịch bản: Tìm role với ID không tồn tại
     * Kỳ vọng: Trả về null
     */
    @Test
    @DisplayName("fetchById - Khi ID không tồn tại - Nên trả về null")
    void fetchById_whenInvalidId_shouldReturnNull() {
        // Arrange
        long roleId = 999L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act
        Role result = roleService.fetchById(roleId);

        // Assert
        assertNull(result);
        verify(roleRepository, times(1)).findById(roleId);
    }

    /**
     * TEST 5: Test method create
     * Kịch bản: Tạo role với permissions hợp lệ
     * Kỳ vọng: Trả về role đã được save
     */
    @Test
    @DisplayName("create - Khi role có permissions hợp lệ - Nên tạo thành công")
    void create_whenRoleWithValidPermissions_shouldReturnSavedRole() {
        // Arrange
        List<Long> permissionIds = Arrays.asList(1L);
        List<Permission> permissions = Arrays.asList(testPermission);

        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        Role result = roleService.create(testRole);

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        assertNotNull(result.getPermissions());
        assertEquals(1, result.getPermissions().size());

        // Verify các method được gọi đúng
        verify(permissionRepository, times(1)).findAllById(permissionIds);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    /**
     * TEST 6: Test method create
     * Kịch bản: Tạo role không có permissions
     * Kỳ vọng: Trả về role đã được save (không set permissions)
     */
    @Test
    @DisplayName("create - Khi role không có permissions - Nên tạo thành công")
    void create_whenRoleWithoutPermissions_shouldReturnSavedRole() {
        // Arrange
        Role roleWithoutPermissions = new Role();
        roleWithoutPermissions.setName("USER");
        roleWithoutPermissions.setDescription("User role");
        roleWithoutPermissions.setActive(true);
        roleWithoutPermissions.setPermissions(null);

        when(roleRepository.save(any(Role.class))).thenReturn(roleWithoutPermissions);

        // Act
        Role result = roleService.create(roleWithoutPermissions);

        // Assert
        assertNotNull(result);
        assertEquals("USER", result.getName());

        // Verify permission repository không được gọi
        verify(permissionRepository, never()).findAllById(anyList());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    /**
     * TEST 7: Test method update
     * Kịch bản: Update role tồn tại với dữ liệu mới
     * Kỳ vọng: Trả về role đã được update
     */
    @Test
    @DisplayName("update - Khi role tồn tại - Nên update thành công")
    void update_whenRoleExists_shouldReturnUpdatedRole() {
        // Arrange
        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setName("OLD_NAME");
        existingRole.setDescription("Old description");
        existingRole.setActive(false);

        Role updatedRole = new Role();
        updatedRole.setId(1L);
        updatedRole.setName("NEW_NAME");
        updatedRole.setDescription("New description");
        updatedRole.setActive(true);
        updatedRole.setPermissions(Arrays.asList(testPermission));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(permissionRepository.findAllById(anyList())).thenReturn(Arrays.asList(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        // Act
        Role result = roleService.update(updatedRole);

        // Assert
        assertNotNull(result);
        assertEquals("NEW_NAME", result.getName());
        assertEquals("New description", result.getDescription());
        assertTrue(result.isActive());

        verify(roleRepository, times(1)).findById(1L);
        verify(permissionRepository, times(1)).findAllById(anyList());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    /**
     * TEST 8: Test method update
     * Kịch bản: Update role không tồn tại
     * Kỳ vọng: Trả về null
     */
    @Test
    @DisplayName("update - Khi role không tồn tại - Nên trả về null")
    void update_whenRoleNotExists_shouldReturnNull() {
        // Arrange
        Role updatedRole = new Role();
        updatedRole.setId(999L);
        updatedRole.setName("NON_EXIST");

        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Role result = roleService.update(updatedRole);

        // Assert
        assertNull(result);
        verify(roleRepository, times(1)).findById(999L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * TEST 9: Test method delete
     * Kịch bản: Xóa role theo ID
     * Kỳ vọng: Method deleteById được gọi 1 lần
     */
    @Test
    @DisplayName("delete - Khi xóa role - Nên gọi repository.deleteById")
    void delete_whenCalled_shouldCallRepositoryDeleteById() {
        // Arrange
        long roleId = 1L;
        doNothing().when(roleRepository).deleteById(roleId);

        // Act
        roleService.delete(roleId);

        // Assert
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    /**
     * TEST 10: Test method getRoles
     * Kịch bản: Lấy danh sách roles với pagination
     * Kỳ vọng: Trả về ResultPaginationDTO với đúng meta và data
     */
    @Test
    @DisplayName("getRoles - Khi lấy danh sách - Nên trả về ResultPaginationDTO")
    void getRoles_whenCalled_shouldReturnResultPaginationDTO() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Role> roles = Arrays.asList(testRole);
        Page<Role> page = new PageImpl<>(roles, pageable, 1);

        when(roleRepository.findAll((Specification<Role>) isNull(), eq(pageable))).thenReturn(page);

        // Act
        ResultPaginationDTO result = roleService.getRoles(null, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertNotNull(result.getResult());

        // Kiểm tra meta data
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(1, result.getMeta().getPages());
        assertEquals(1, result.getMeta().getTotal());

        // Kiểm tra result data
        List<?> resultList = (List<?>) result.getResult();
        assertEquals(1, resultList.size());

        verify(roleRepository, times(1)).findAll((Specification<Role>) isNull(), eq(pageable));
    }

    /**
     * TEST 11: Test method getRoles với empty result
     * Kịch bản: Lấy danh sách roles nhưng không có data
     * Kỳ vọng: Trả về ResultPaginationDTO với list rỗng
     */
    @Test
    @DisplayName("getRoles - Khi không có data - Nên trả về list rỗng")
    void getRoles_whenNoData_shouldReturnEmptyList() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(roleRepository.findAll((Specification<Role>) isNull(), eq(pageable))).thenReturn(emptyPage);

        // Act
        ResultPaginationDTO result = roleService.getRoles(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getMeta().getTotal());
        List<?> resultList = (List<?>) result.getResult();
        assertEquals(0, resultList.size());

        verify(roleRepository, times(1)).findAll((Specification<Role>) isNull(), eq(pageable));
    }
}

