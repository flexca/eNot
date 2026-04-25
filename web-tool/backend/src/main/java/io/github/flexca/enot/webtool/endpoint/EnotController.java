package io.github.flexca.enot.webtool.endpoint;

import io.github.flexca.enot.core.exception.EnotException;
import io.github.flexca.enot.core.parser.EnotInputFormat;
import io.github.flexca.enot.webtool.model.ExampleParamsRequest;
import io.github.flexca.enot.webtool.model.SerializeRequest;
import io.github.flexca.enot.webtool.service.EnotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/enot/api/v1")
public class EnotController {

    private final EnotService enotService;

    @PostMapping("/serialize")
    public String encode(@RequestBody SerializeRequest serializeRequest) throws EnotException {

        return enotService.serialize(serializeRequest.getTemplate(), serializeRequest.getParams());
    }

    @PostMapping("/example-params")
    public String getExampleParams(@RequestBody ExampleParamsRequest exampleParamsRequest) throws EnotException {

        return enotService.getExampleParams(exampleParamsRequest.getTemplate(), EnotInputFormat.fromName(exampleParamsRequest.getFormat()));
    }
}
