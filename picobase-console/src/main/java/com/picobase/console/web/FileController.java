package com.picobase.console.web;

import com.picobase.PbUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/files")
public class FileController {


    @PostMapping("/token")
    public Object fileToken(HttpServletRequest request) {
        return null;
    }

    @GetMapping("/{collectionIdOrName}/{recordId}/{filename}")
    public void download(
            @PathVariable("collectionIdOrName") String collectionIdOrName,
            @PathVariable("recordId") String recordId,
            @PathVariable("filename") String filename) {

        PbUtil.download(collectionIdOrName, recordId, filename);
    }

}
