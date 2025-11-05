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
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.repository.PermissionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("CREATE_USER");
        testPermission.setApiPath("/api/v1/users");
        testPermission.setMethod("POST");
        testPermission.setModule("USER");
    }


    @Test
    @DisplayName("isPermissionExist - Khi permission tồn tại - Nên trả về true")
    void isPermissionExist_whenExists_shouldReturnTrue() {
        // Arrange: Mock repository trả về true
        when(permissionRepository.existsByModuleAndApiPathAndMethod(
                "USER", "/api/v1/users", "POST"
        )).thenReturn(true);

        // Act: Gọi method cần test
        boolean result = permissionService.isPermissionExist(testPermission);

        // Assert: Kiểm tra kết quả
        assertTrue(result);

        // Verify: Kiểm tra repository được gọi đúng 1 lần với đúng params
        verify(permissionRepository, times(1))
                .existsByModuleAndApiPathAndMethod("USER", "/api/v1/users", "POST");
    }


    @Test
    @DisplayName("isPermissionExist - Khi permission không tồn tại - Nên trả về false")
    void isPermissionExist_whenNotExists_shouldReturnFalse() {
        // Arrange
        when(permissionRepository.existsByModuleAndApiPathAndMethod(
                anyString(), anyString(), anyString()
        )).thenReturn(false);

        // Act
        boolean result = permissionService.isPermissionExist(testPermission);

        // Assert
        assertFalse(result);
        verify(permissionRepository, times(1))
                .existsByModuleAndApiPathAndMethod(anyString(), anyString(), anyString());
    }


    @Test
    @DisplayName("fetchById - Khi ID hợp lệ - Nên trả về Permission")
    void fetchById_whenValidId_shouldReturnPermission() {
        // Arrange: Mock repository trả về Optional chứa permission
        when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(testPermission));

        // Act
        Permission result = permissionService.fetchById(1L);

        // Assert: Kiểm tra nhiều thuộc tính
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CREATE_USER", result.getName());
        assertEquals("/api/v1/users", result.getApiPath());
        assertEquals("POST", result.getMethod());
        assertEquals("USER", result.getModule());

        verify(permissionRepository, times(1)).findById(1L);
    }


    @Test
    @DisplayName("fetchById - Khi ID không tồn tại - Nên trả về null")
    void fetchById_whenInvalidId_shouldReturnNull() {
        // TODO: Viết code ở đây
        // Gợi ý: Tham khảo test fetchById_whenValidId_shouldReturnPermission ở trên
        long permissionId = 999L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        Permission result = permissionService.fetchById(permissionId);

        assertNull(result);
        verify(permissionRepository, times(1)).findById(permissionId);
    }


    @Test
    @DisplayName("create - Khi input hợp lệ - Nên tạo và trả về Permission")
    void create_whenValidInput_shouldReturnSavedPermission() {
        // TODO: Viết code ở đây
        when(permissionRepository.save(any(Permission.class)))
                .thenReturn(testPermission);

        Permission result = permissionService.create(testPermission);

        assertNotNull(result);
        assertEquals("CREATE_USER", result.getName());
        assertEquals("/api/v1/users", result.getApiPath());
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }


    @Test
    @DisplayName("update - Khi permission tồn tại - Nên update thành công")
    void update_whenPermissionExists_shouldReturnUpdatedPermission() {
        // TODO: Viết code ở đây
        // Gợi ý: Tham khảo RoleServiceTest.update_whenRoleExists_shouldReturnUpdatedRole()
        Permission existingPermission = new Permission();
        existingPermission.setId(1L);
        existingPermission.setName("CREATE_USER");
        existingPermission.setApiPath("/api/v1/users");
        existingPermission.setMethod("POST");
        existingPermission.setModule("USER");

        Permission updatedPermission = new Permission();
        updatedPermission.setId(1L);
        updatedPermission.setName("UPDATE_USER");
        updatedPermission.setApiPath("/api/v1/users/{id}");
        updatedPermission.setMethod("PUT");
        updatedPermission.setModule("USER");

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(existingPermission));
        when(permissionRepository.save(any(Permission.class)))
                .thenReturn(existingPermission);

        Permission result = permissionService.update(updatedPermission);

        assertNotNull(result);
        assertEquals("UPDATE_USER", result.getName());
        assertEquals("/api/v1/users/{id}", result.getApiPath());
        assertEquals("PUT", result.getMethod());
        assertEquals("USER", result.getModule());
        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }


    @Test
    @DisplayName("update - Khi permission không tồn tại - Nên trả về null")
    void update_whenPermissionNotExists_shouldReturnNull() {
        // TODO: Viết code ở đây
        Permission nonExistentPermission = new Permission();
        nonExistentPermission.setId(999L);
        nonExistentPermission.setName("NON_EXISTENT");
        nonExistentPermission.setApiPath("/api/v1/nonexistent");
        nonExistentPermission.setMethod("GET");
        nonExistentPermission.setModule("NONE");
        when(permissionRepository.findById(999L))
                .thenReturn(Optional.empty());

        Permission result = permissionService.update(nonExistentPermission);

        assertNull(result);
        verify(permissionRepository, times(1)).findById(999L);
        verify(permissionRepository, never()).save(any(Permission.class));
    }


    @Test
    @DisplayName("delete - Khi xóa permission - Nên gọi repository.deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        // TODO: Viết code ở đây
        // Gợi ý: Tham khảo RoleServiceTest.delete_whenCalled_shouldCallRepositoryDeleteById()
        long permissionId = 1L;
        doNothing().when(permissionRepository).deleteById(permissionId);

        permissionService.delete(permissionId);

        verify(permissionRepository, times(1)).deleteById(permissionId);
    }


     @Test
     @DisplayName("getPermissions - Nên trả về ResultPaginationDTO")
     void getPermissions_shouldReturnResultPaginationDTO() {
         // Arrange
         Pageable pageable = PageRequest.of(0, 10);
         Page<Permission> permissionPage = new PageImpl<>(List.of(testPermission), pageable, 1);

         when(permissionRepository.findAll((Specification<Permission>) isNull(), eq(pageable)))
                 .thenReturn(permissionPage);

         // Act
         ResultPaginationDTO result = permissionService.getPermissions(null, pageable);

         // Assert
         assertNotNull(result);
         assertNotNull(result.getMeta());
         assertNotNull(result.getResult());
         assertEquals(1, result.getMeta().getPage());
         assertEquals(10, result.getMeta().getPageSize());
         assertEquals(1, result.getMeta().getTotal());

         verify(permissionRepository, times(1))
                 .findAll((Specification<Permission>) isNull(), eq(pageable));
     }
}

