package io.github.dmytrozelinskyy.currencyconverter.controller;

import io.github.dmytrozelinskyy.currencyconverter.dto.ConversionHistoryDto;
import io.github.dmytrozelinskyy.currencyconverter.service.ConversionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryController {
    private final ConversionHistoryService historyService;

    public HistoryController(ConversionHistoryService service) {
        this.historyService = service;
    }

    @Operation(summary = "List recent conversions, newest first")
    @GetMapping("/api/history")
    public PagedModel<ConversionHistoryDto> history(
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(historyService.recent(pageable));
    }
}
