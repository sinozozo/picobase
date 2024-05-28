package com.picobase.console.web;

import com.picobase.console.web.interceptor.LoadCollection;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/files")
public class FileController {


    @PostMapping("/token")
    public Object fileToken(HttpServletRequest request) {
        return null;
    }

    @LoadCollection
    @GetMapping("/{collectionIdOrName}/{recordId}/{filename}")
    public Object download(
            @PathVariable("recordId") String recordId,
            @PathVariable("filename") String filename,
            @RequestParam("token") String token,
            @RequestParam(value = "thumb", required = false) String thumb,
            @RequestParam(value = "download", required = false) String download) {

        return null;
    }

}
