package org.controller;

import org.json.simple.JSONObject;
import org.service.JsonData;
import org.service.RunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Z_HAO 2020/11/20
 */

@RestController
@RequestMapping(value = "/api")
public class ApiController {

    @GetMapping(value = "/get")
    public String get(String data) {
        return dealInfo(data);
    }

    @PostMapping(value = "/post")
    @ResponseBody
    @CrossOrigin
    public String post(@RequestBody JSONObject data) {
        //ResponseEntity rsp = new ResponseEntity();
        return dealInfo(data.toString());
    }

    public String dealInfo(String data) {
        DataController jsonDataController = new JsonDataController();
        RunService service = new RunService(jsonDataController);
        service.startService(data);
        return jsonDataController.getWriter().toString();
    }
}
