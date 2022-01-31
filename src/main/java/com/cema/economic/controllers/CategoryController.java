package com.cema.economic.controllers;

import com.cema.economic.domain.Category;
import com.cema.economic.entities.CemaCategory;
import com.cema.economic.exceptions.NotFoundException;
import com.cema.economic.mapping.Mapping;
import com.cema.economic.repositories.CategoryRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@Api(produces = "application/json", value = "Allows interaction with the category database. V1")
@Validated
@Slf4j
public class CategoryController {

    private static final String BASE_URL = "/category/";

    private final CategoryRepository categoryRepository;
    private final Mapping<CemaCategory, Category> categoryMapping;

    public CategoryController(CategoryRepository categoryRepository, Mapping<CemaCategory, Category> categoryMapping) {
        this.categoryRepository = categoryRepository;
        this.categoryMapping = categoryMapping;
    }

    @ApiOperation(value = "Retrieve category", response = Category.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found category"),
            @ApiResponse(code = 404, message = "Category not found")
    })
    @GetMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Category> lookUpCategoryByName(
            @ApiParam(
                    value = "The cuig of the category you are looking for.",
                    example = "123")
            @PathVariable("name") String name) {

        log.info("Request for category with name {}", name);

        CemaCategory cemaCategory = categoryRepository.findCemaCategoryByNameIgnoreCase(name);
        if (cemaCategory == null) {
            throw new NotFoundException(String.format("Category with name %s doesn't exits", name));
        }


        Category category = categoryMapping.mapEntityToDomain(cemaCategory);

        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve categories", response = Category.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all categories", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Category>> listCategories(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of categories to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        Pageable paging = PageRequest.of(page, size);

        Page<CemaCategory> cemaCategoryPage = categoryRepository.findAll(paging);

        List<CemaCategory> cemaCategories = cemaCategoryPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaCategoryPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaCategoryPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaCategoryPage.getNumber()));

        List<Category> categories = cemaCategories.stream().map(categoryMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(categories);
    }
}
