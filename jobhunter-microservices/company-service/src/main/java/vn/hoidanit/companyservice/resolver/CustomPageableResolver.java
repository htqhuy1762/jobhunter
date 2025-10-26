package vn.hoidanit.companyservice.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import vn.hoidanit.companyservice.annotation.PageableDefault;

/**
 * Custom resolver for Pageable parameters that handles 1-based page indexing from frontend.
 * Automatically converts 1-based page numbers to 0-based for Spring Data JPA.
 */
public class CustomPageableResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Pageable.class)
                && parameter.hasParameterAnnotation(PageableDefault.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        PageableDefault defaults = parameter.getParameterAnnotation(PageableDefault.class);

        // Get page parameter (1-based from frontend)
        String pageParam = webRequest.getParameter("page");
        int page = pageParam != null ? Integer.parseInt(pageParam) : defaults.page();

        // Convert to 0-based for Spring Data
        int pageIndex = page - 1;
        if (pageIndex < 0) pageIndex = 0;

        // Get size parameter
        String sizeParam = webRequest.getParameter("size");
        int size = sizeParam != null ? Integer.parseInt(sizeParam) : defaults.size();

        // Get sort parameters
        String[] sortParam = webRequest.getParameterValues("sort");
        Sort sort = buildSort(sortParam, defaults);

        return PageRequest.of(pageIndex, size, sort);
    }

    private Sort buildSort(String[] sortParam, PageableDefault defaults) {
        if (sortParam != null && sortParam.length > 0) {
            String sortString = sortParam[0];
            String[] parts = sortString.split(",");

            String property = parts.length > 0 ? parts[0] : defaults.sort();
            String direction = parts.length > 1 ? parts[1] : defaults.direction();

            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            return Sort.by(sortDirection, property);
        }

        // Use defaults
        Sort.Direction defaultDirection = defaults.direction().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(defaultDirection, defaults.sort());
    }
}


