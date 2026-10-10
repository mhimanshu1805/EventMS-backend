package com.ems.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ems.dto.QueryRespondRequest;
import com.ems.dto.QueryResponse;
import com.ems.service.QueryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/queries")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public List<QueryResponse> listMine() {
        return queryService.listMine();
    }

    @PatchMapping("/{id}/respond")
    @PreAuthorize("hasRole('ORGANIZER')")
    public QueryResponse respond(@PathVariable Long id, @Valid @RequestBody QueryRespondRequest request) {
        return queryService.respond(id, request.getResponse());
    }
}
