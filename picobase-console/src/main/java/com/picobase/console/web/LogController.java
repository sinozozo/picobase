package com.picobase.console.web;

import com.picobase.PbUtil;
import com.picobase.console.mapper.LogMapper;
import com.picobase.console.model.LogModel;
import com.picobase.console.model.LogsStatsItem;
import com.picobase.console.web.interceptor.LoadCollection;
import com.picobase.json.PbJsonTemplate;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.SimpleFieldResolver;
import com.picobase.search.SearchFilter;
import com.picobase.util.PbConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private PbJsonTemplate jsonTemplate;

    private LogMapper logMapper;

    public LogController(PbJsonTemplate jsonTemplate, LogMapper mapper) {
        this.jsonTemplate = jsonTemplate;
        this.logMapper = mapper;
    }

    @GetMapping
    @LoadCollection
    public Page<LogModel> list() {
        return PbUtil.rQueryPage(LogModel.class, "");
    }

    @GetMapping("/stats")
    public List<LogsStatsItem> stats(HttpServletRequest request) {
        SimpleFieldResolver resolver = new SimpleFieldResolver(List.of("rowid", "level", "message", "id", "created", "data", "updated", "^data\\.[\\w\\.\\:]*\\w+$"));
        var expr = new SearchFilter(request.getParameter(PbConstants.QueryParam.FILTER)).buildExpr(resolver);

        return logMapper.stats(expr);
    }
}
